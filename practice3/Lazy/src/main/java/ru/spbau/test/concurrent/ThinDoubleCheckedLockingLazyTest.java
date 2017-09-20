package ru.spbau.test.concurrent;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.IntResult4;
import ru.spbau.app.Lazy;
import ru.spbau.app.LazyFactory;
import ru.spbau.test.CountingSupplier;

@JCStressTest
@Outcome(id = "1, 1, 1, 1", expect = Expect.ACCEPTABLE, desc = "OK")
@Outcome(expect =  Expect.FORBIDDEN, desc = "Everything else is forbidden")
@State
public class ThinDoubleCheckedLockingLazyTest {

    public CountingSupplier supplier = new CountingSupplier();
    public Lazy<Integer> lazy = LazyFactory.createThinDoubleCheckedLockingLazy(supplier);

    @Actor
    public void actor1(IntResult4 r) {
        r.r1 = lazy.get();
        r.r2 = supplier.invocationsCount;
    }

    @Actor
    public void actor2(IntResult4 r) {
        r.r3 = lazy.get();
        r.r4 = supplier.invocationsCount;
    }

}
