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
public class AppendTail {

    @Param({"4", "8", "12", "16", "20", "24", "28"})
    public int offset;
    int size;

    PVecUnsafe p;

    @Setup(Level.Trial)
    public void setup() {
        size = 32 + offset;
        p = new PVecUnsafe();
        for (int i = 0; i < size; i++) {
            p = p.push(new Object());
        }
    }

    @Benchmark
    public PVecUnsafe benchAppendTail() {
        return p.push(null);
    }
}
