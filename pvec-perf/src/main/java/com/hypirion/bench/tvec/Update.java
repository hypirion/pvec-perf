package com.hypirion.bench.tvec;

import java.util.Random;
import com.hypirion.pvec.TVec;

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

    Random r;
    TVec t;

    @Setup(Level.Trial)
    public void setup() {
        r = new Random(1);
        size = (1 << (5*bits)) + 32;
        t = new TVec();
        for (int i = 0; i < size; i++) {
            t = t.push(new Object());
        }
    }

    @Benchmark
    public TVec benchUpdate() {
        t = t.set(r.nextInt(size - 32), new Object());
        return t;
    }
}
