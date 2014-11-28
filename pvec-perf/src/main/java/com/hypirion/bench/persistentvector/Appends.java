package com.hypirion.bench.persistentvector;

import java.util.Random;
import clojure.lang.PersistentVector;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Level;

@State(Scope.Benchmark)
public class Appends {
 
    @Param({"0", "1", "2", "3", "4", "5"})
    public int bits;
    int size;

    @Setup(Level.Trial)
    public void setup() {
        size = (1 << (5*bits)) + 32;
    }

    @Benchmark
    public PersistentVector benchAppends() {
        PersistentVector p = PersistentVector.EMPTY;
        for (int i = 0; i < size; i++) {
            p = p.cons(null);
        }
        return p;
    }
}
