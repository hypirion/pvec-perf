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

package com.hypirion.pvec;

import java.util.Iterator;

class VecIter implements Iterator {
    private final int size;
    private final Object[] tail;
    private final Object[][] stack;
    private Object[] leaf;
    private int index;
    private int jump;

    VecIter(int size, int shift, Object[] root, Object[] tail) {
        index = 0;
        this.size = size;
        this.tail = tail;
        jump = 32;
        // top is at the end, and rank 2 nodes are at the front
        stack = new Object[shift/5][];
        if (size <= 32) {
            leaf = tail;
        }
        else if (size <= 64) {
            leaf = root;
        }
        else {
            stack[stack.length-1] = root;
            for (int i = stack.length-2; i >= 0; i--) {
                stack[i] = (Object[]) stack[i+1][0];
            }
            leaf = (Object[])stack[0][0];
        }
    }

    public boolean hasNext() {
        return index < size;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Object next() {
        if (index == jump) {
            if (index >= ((size - 1) & (~31))) {
                leaf = tail;
            } else {
                jump += 32;
                int diff = index ^ (index - 1);
                // there is at least one jump, so skip first check
                int level = 10;
                int stackUpdates = 0;
                // count number of nodes we have to rewind back up
                while ((diff >>> level) != 0) {
                    stackUpdates++;
                    level += 5;
                }
                level -= 5;
                // rewrite stack if need be
                while (stackUpdates > 0) {
                    stack[stackUpdates - 1] = (Object[])
                        stack[stackUpdates][(index >>> level) & 31];
                    stackUpdates--;
                    level -= 5;
                }
                leaf = (Object[]) stack[0][(index >>> 5) & 31];
            }
        }
        return leaf[index++ & 31];
    }
}
