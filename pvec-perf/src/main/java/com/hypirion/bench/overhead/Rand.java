package com.hypirion.bench.overhead;

import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Level;

@State(Scope.Benchmark)
public class Rand {

    public int bits;
    int size;

    Random r;

    @Setup(Level.Trial)
    public void setup() {
        bits = 5;
        size = 1 << (5*bits);
        r = new Random(1);
    }

    @Benchmark
    public Object benchRandObj() {
        r.nextInt(size);
        return new Object();
    }

    @Benchmark
    public int benchRand() {
        return r.nextInt(size);
    }
}
