package com.hypirion.bench.arraylist;

import java.util.ArrayList;
import java.util.Random;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Level;

@State(Scope.Benchmark)
public class Get {

    @Param({"0", "1", "2", "3", "4", "5"})
    public int bits;
    int size;

    Random r;

    ArrayList al;

    @Setup(Level.Trial)
    public void setup() {
        size = 1 << (5*bits);
        r = new Random(1);
        al = new ArrayList();
        for (int i = 0; i < size; i++) {
            al.add(new Object());
        }
    }
    /*
    @Benchmark
    public void benchUpdate() {
        al.set(r.nextInt(size), new Object());
    }
    */
}
