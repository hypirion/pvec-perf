/*
 * Copyright (c) 2014 Jean Niklas L'orange. All rights reserved.
 *
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file LICENSE at the root of this distribution.
 *
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 *
 * You must not remove this notice, or any other, from this software.
 */

// Notice: This is not a scientific benchmark (see JMH), but should be able to
// give an estimation on the order of magnitude.
package com.hypirion.pvec.tests;

import com.hypirion.pvec.PVec;
import com.hypirion.pvec.PVecUnsafe;
import com.hypirion.pvec.TVecUnsafe;
import com.hypirion.pvec.TVec;
import java.util.ArrayList;
import clojure.lang.PersistentVector;
import clojure.lang.ITransientVector;

public class Test {
    static final int WARMUP = 3;
    static final int RUNS = 100;

    public static void main(String[] args) {
        double[] times = new double[7];
        for (int i = 0; i < WARMUP; i++) {
            pushThenPop(times);
        }
        times = new double[7];
        for (int i = 0; i < RUNS; i++) {
            pushThenPop(times);
        }
        System.out.println("ArrayList: " + times[0]/RUNS);
        System.out.println("PVec: " + times[1]/RUNS);
        System.out.println("PVecUnsafe: " + times[5]/RUNS);
        System.out.println("TVec: " + times[2]/RUNS);
        System.out.println("TVecUnsafe: " + times[6]/RUNS);
        System.out.println("PersistentVector: " + times[3]/RUNS);
        System.out.println("TransientVector: " + times[4]/RUNS);
    }

    public static void pushThenPop(double[] times) {
        long start_time, end_time;
        double difference;

        /*====================================================================*/
        /*                     Clojure's PersistentVector                     */
        /*====================================================================*/

        start_time = System.nanoTime();
        PersistentVector cljP = PersistentVector.EMPTY;
        for (int i = 0; i < 10000000; i++) {
            cljP = cljP.cons(i);
        }
        for (int i = 0; i < 10000000; i++) {
            Integer ret = (Integer) cljP.nth(i);
            if (ret != i) {
                System.err.printf("%d shoulda given %d, but was %d\n",
                                  i, i, ret);
                System.exit(1);
            }
        }
        for (int i = 0; i < 10000000; i++) {
            cljP = cljP.pop();
        }
        end_time = System.nanoTime();
        difference = (end_time - start_time)/1e6;
        times[3] += difference;
        cljP = null;

        /*====================================================================*/
        /*                                PVec                                */
        /*====================================================================*/

        start_time = System.nanoTime();
        PVec p = new PVec();
        for (int i = 0; i < 10000000; i++) {
            p = p.push(i);
        }
        for (int i = 0; i < 10000000; i++) {
            Integer ret = (Integer) p.get(i);
            if (ret != i) {
                System.err.printf("%d shoulda given %d, but was %d\n",
                                  i, i, ret);
                System.exit(1);
            }
        }
        for (int i = 0; i < 10000000; i++) {
            p = p.pop();
        }
        end_time = System.nanoTime();
        difference = (end_time - start_time)/1e6;
        times[1] += difference;
        p = null;

        /*====================================================================*/
        /*                             PVecUnsafe                             */
        /*====================================================================*/

        start_time = System.nanoTime();
        PVecUnsafe pu = new PVecUnsafe();
        for (int i = 0; i < 10000000; i++) {
            pu = pu.push(i);
        }
        for (int i = 0; i < 10000000; i++) {
            Integer ret = (Integer) pu.get(i);
            if (ret != i) {
                System.err.printf("%d shoulda given %d, but was %d\n",
                                  i, i, ret);
                System.exit(1);
            }
        }
        for (int i = 0; i < 10000000; i++) {
            pu = pu.pop();
        }
        end_time = System.nanoTime();
        difference = (end_time - start_time)/1e6;
        times[5] += difference;
        pu = null;

        /*====================================================================*/
        /*                     Clojure's TransientVector                      */
        /*====================================================================*/

        start_time = System.nanoTime();
        ITransientVector cljT = PersistentVector.EMPTY.asTransient();
        for (int i = 0; i < 10000000; i++) {
            cljT = (ITransientVector) cljT.conj(i);
        }
        for (int i = 0; i < 10000000; i++) {
            Integer ret = (Integer) cljT.nth(i);
            if (ret != i) {
                System.err.printf("%d shoulda given %d, but was %d\n",
                                  i, i, ret);
                System.exit(1);
            }
        }
        for (int i = 0; i < 10000000; i++) {
            cljT = cljT.pop();
        }
        end_time = System.nanoTime();
        difference = (end_time - start_time)/1e6;
        times[4] += difference;
        cljT = null;

        /*====================================================================*/
        /*                                TVec                                */
        /*====================================================================*/

        start_time = System.nanoTime();
        TVec t = new TVec();
        for (int i = 0; i < 10000000; i++) {
            t = t.push(i);
        }
        for (int i = 0; i < 10000000; i++) {
            Integer ret = (Integer) t.get(i);
            if (ret != i) {
                System.err.printf("%d shoulda given %d, but was %d\n",
                                  i, i, ret);
                System.exit(1);
            }
        }
        for (int i = 0; i < 10000000; i++) {
            t = t.pop();
        }
        end_time = System.nanoTime();
        difference = (end_time - start_time)/1e6;
        times[2] += difference;
        t = null;

        /*====================================================================*/
        /*                             TVecUnsafe                             */
        /*====================================================================*/

        start_time = System.nanoTime();
        TVecUnsafe tu = new TVecUnsafe();
        for (int i = 0; i < 10000000; i++) {
            tu = tu.push(i);
        }
        for (int i = 0; i < 10000000; i++) {
            Integer ret = (Integer) tu.get(i);
            if (ret != i) {
                System.err.printf("%d shoulda given %d, but was %d\n",
                                  i, i, ret);
                System.exit(1);
            }
        }
        for (int i = 0; i < 10000000; i++) {
            tu = tu.pop();
        }
        end_time = System.nanoTime();
        difference = (end_time - start_time)/1e6;
        times[6] += difference;
        tu = null;

        /*====================================================================*/
        /*                            Array List                              */
        /*====================================================================*/

        start_time = System.nanoTime();
        ArrayList<Integer> al = new ArrayList<Integer>();
        for (int i = 0; i < 10000000; i++) {
            al.add(i);
        }
        for (int i = 0; i < 10000000; i++) {
            Integer ret = al.get(i);
            if (ret != i) {
                System.err.printf("%d shoulda given %d, but was %d\n",
                                  i, i, ret);
                System.exit(1);
            }
        }
        for (int i = 0; i < 10000000; i++) {
            al.remove(al.size() - 1);
        }
        end_time = System.nanoTime();
        difference = (end_time - start_time)/1e6;
        times[0] += difference;
        al = null;
    }
}
