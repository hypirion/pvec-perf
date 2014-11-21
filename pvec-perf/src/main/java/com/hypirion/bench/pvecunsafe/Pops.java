package com.hypirion.bench.pvecunsafe;

import java.util.Random;
import com.hypirion.pvec.PVecUnsafe;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Level;

@State(Scope.Benchmark)
public class Pops {

    // consider single-shot due to slowness here
    @Param({"0", "1", "2", "3", "4", "5"})
    public int bits;
    int size;

    PVecUnsafe p;

    @Setup(Level.Trial)
    public void setup() {
        size = (1 << (5*bits)) + 32;
        p = new PVecUnsafe();
        for (int i = 0; i < size; i++) {
            p = p.push(new Object());
        }
    }

    @Benchmark
    public PVecUnsafe benchPops() {
        PVecUnsafe popped = p;
        for (int i = 0; i < size; i++) {
            popped = popped.pop();
        }
        return popped;
    }
}
