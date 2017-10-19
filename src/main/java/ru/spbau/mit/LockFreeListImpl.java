package ru.spbau.mit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeListImpl<T extends Comparable<T>> implements LockFreeList<T> {
    @NotNull
    private final Node<T> head = new Node<>();
    @NotNull
    private final Node<T> tail = new Node<>();

    {
        head.setNext(tail);
    }

    @Override
    public boolean isEmpty() {
        for (Node<T> nextNode = head.getNext(); nextNode != tail; nextNode = head.getNext()) {
            if (!head.isMarked()) {
                return false;
            }

            head.next.compareAndSet(nextNode, nextNode.getNext(), true, true);
        }

        return true;
    }

    @Override
    public void append(@NotNull final T key) {
        final Node<T> newNode = new Node<>(key);
        newNode.setNext(tail);

        while (true) {
            final SearchResult<T> result = search(null);  // returns (lastNode, tail)
            final Node<T> lastNode = result.leftNode;
            if (lastNode.next.compareAndSet(tail, newNode, false, false)) {
                return;
            }
        }
    }

    @Override
    public boolean remove(@NotNull final T key) {
        Node<T> leftNode, rightNode, rightNodeNext;

        while (true) {
            final SearchResult<T> result = search(key);
            leftNode = result.leftNode;
            rightNode = result.rightNode;
            if (rightNode.key == null || rightNode.key.compareTo(key) != 0) {
                return false;
            }

            rightNodeNext = rightNode.getNext();
            if (rightNode.next.attemptMark(rightNodeNext, true)) {
                break;
            }
        }

        if (!leftNode.next.compareAndSet(rightNode, rightNodeNext, false, false)) {
            search(rightNode.key);
        }

        return true;
    }

    @Override
    public boolean contains(@NotNull final T key) {
        final SearchResult<T> result = search(key);
        final Node<T> leftNode = result.leftNode;
        final Node<T> rightNode = result.rightNode;
        return leftNode != tail && rightNode.key != null && rightNode.key.compareTo(key) == 0;
    }

    @NotNull
    private SearchResult<T> search(@Nullable final T key) {
        Node<T> leftNode = head, leftNodeNext = null, rightNode;

        while (true) {
            Node<T> node = head;

            /* 1: Find leftNode and rightNode */
            do {
                if (!node.isMarked()) {
                    leftNode = node;
                    leftNodeNext = leftNode.getNext();
                }

                node = node.getNext();
            } while (node.isMarked() || node.key != null && (key == null || node.key.compareTo(key) != 0));
            rightNode = node;

            /* 2: Check nodes are adjacent */
            if (leftNodeNext == rightNode) {
                if (rightNode != tail && rightNode.isMarked()) {
                    continue;
                } else {
                    return new SearchResult<>(leftNode, rightNode);
                }
            }

            /* 3: Remove one or more marked nodes */
            final boolean isMarked = leftNode.isMarked();
            if (leftNode.next.compareAndSet(leftNodeNext, rightNode, isMarked, isMarked)) {
                if (rightNode == tail || !rightNode.isMarked()) {
                    return new SearchResult<>(leftNode, rightNode);
                }
            }
        }
    }

    private static class Node<T extends Comparable<T>> {
        @Nullable
        private final T key;
        @NotNull
        private final AtomicMarkableReference<Node<T>> next =
                new AtomicMarkableReference<>(null, false);

        private Node() {
            this.key = null;
        }

        Node(@NotNull final T key) {
            this.key = key;
        }

        @NotNull
        Node<T> getNext() {
            return next.getReference();
        }

        void setNext(@NotNull final Node<T> node) {
            next.set(node, false);
        }

        boolean isMarked() {
            return next.isMarked();
        }
    }

    private static class SearchResult<T extends Comparable<T>> {
        @NotNull
        final Node<T> leftNode;
        @NotNull
        final Node<T> rightNode;

        private SearchResult(@NotNull final Node<T> leftNode, @NotNull final Node<T> rightNode) {
            this.leftNode = leftNode;
            this.rightNode = rightNode;
        }
    }
}
