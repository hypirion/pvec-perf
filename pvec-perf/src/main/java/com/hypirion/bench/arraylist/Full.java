package com.hypirion.bench.arraylist;

import java.util.Random;
import java.util.ArrayList;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Level;

@State(Scope.Benchmark)
public class Full {

    // consider single-shot due to slowness
    @Param({"0", "1", "2", "3", "4", "5"})
    public int bits;
    int size;

    @Setup(Level.Trial)
    public void setup() {
        size = (1 << (5*bits)) + 32;
    }

    @Benchmark
    public long benchFull() {
        ArrayList al = new ArrayList();
        for (int i = 0; i < size; i++) {
            al.add(i);
        }
        long sum = 0;
        for (int i = 0; i < size; i++) {
            sum += (Integer) al.get(i);
        }
        for (int i = 0; i < size; i++) {
            al.remove(al.size()-1);
        }
        return sum + al.size();
    }
}
