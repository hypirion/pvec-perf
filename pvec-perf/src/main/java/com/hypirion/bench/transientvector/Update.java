package com.hypirion.bench.transientvector;

import java.util.Random;
import clojure.lang.PersistentVector;
import clojure.lang.ITransientVector;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Level;

@State(Scope.Benchmark)
public class Update {
 
    @Param({"0", "1", "2", "3", "4", "5"})
    public int bits;
    int size;

    ITransientVector t;
    Random r;

    @Setup(Level.Trial)
    public void setup() {
        r = new Random(1);
        size = (1 << (5*bits)) + 32;
        t = PersistentVector.EMPTY.asTransient();
        for (int i = 0; i < size; i++) {
            t = (ITransientVector) t.conj(null);
        }
    }

    @Benchmark
    public Object benchUpdate() {
        t = (ITransientVector) t.assocN(r.nextInt(size - 32), new Object());
        return t;
    }
}
