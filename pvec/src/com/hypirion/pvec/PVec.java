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

public final class PVec {
    private final int size;
    private final int shift;
    private final Object[] tail;
    private final Object[] root;

    private static final Object[] EMPTY_TAIL = new Object[0];

    public PVec() {
        size = 0;
        shift = 0;
        tail = EMPTY_TAIL;
        root = null;
    }

    PVec(int size, int shift, Object[] root, Object[] tail) {
        this.size = size;
        this.shift = shift;
        this.tail = tail;
        this.root = root;
    }

    public PVec set(int i, Object val) {
        rangeCheck(i);
        if (i >= tailOffset()) {
            Object[] newTail = tail.clone();
            newTail[i & 31] = val;
            return new PVec(size, shift, root, newTail);
        }
        else {
            Object[] newRoot = root.clone();
            Object[] node = newRoot;
            for (int level = shift; level > 0; level -= 5) {
                int subidx = (i >>> level) & 31;
                Object[] child = (Object[]) node[subidx];
                child = child.clone();
                node[subidx] = child;
                node = child;
            }
            node[i & 31] = val;
            return new PVec(size, shift, newRoot, tail);
        }
    }

    public Object get(int i) {
        rangeCheck(i);
        if (i >= tailOffset()) {
            return tail[i & 31];
        }
        else {
            Object[] node = root;
            for (int level = shift; level > 0; level -= 5) {
                node = (Object[]) node[(i >>> level) & 31];
            }
            return node[i & 31];
        }
    }

    public PVec push(Object val) {
        int ts = tailSize();
        if (ts != 32) {
            Object[] newTail = new Object[ts+1];
            System.arraycopy(tail, 0, newTail, 0, tail.length);
            newTail[ts] = val;
            return new PVec(size+1, shift, root, newTail);
        }
        else { // have to insert tail into root.
            Object[] newTail = new Object[]{val};
            // Special case: If old size == 32, then tail is new root
            if (size == 32) {
                return new PVec(size+1, 0, tail, newTail);
            }
            // check if the root is completely filled. Must also increment
            // shift if that's the case.
            Object[] newRoot;
            int newShift = shift;
            if ((size >>> 5) > (1 << shift)) {
                newShift += 5;
                newRoot = new Object[32];
                newRoot[0] = root;
                newRoot[1] = newPath(shift, tail);
                return new PVec(size+1, newShift, newRoot, newTail);
            }
            else { // still space in root
                newRoot = pushLeaf(shift, size-1, root, tail);
                return new PVec(size+1, shift, newRoot, newTail);
            }
        }
    }

    private static Object[] pushLeaf(int shift, int i, Object[] root, Object[] tail) {
        Object[] newRoot = root.clone();
        Object[] node = newRoot;
        for (int level = shift; level > 5; level -= 5) {
            int subidx = (i >>> level) & 31;
            Object[] child = (Object[]) node[subidx];
            // You could replace this null check with
            // ((tailOffset() - 1) ^ tailOffset() >> level) != 0
            // but we'll still have to assign node[subidx].
            // The null check should therefore be a bit faster.
            if (child == null) {
                node[subidx] = newPath(level - 5, tail);
                return newRoot;
            }
            child = child.clone();
            node[subidx] = child;
            node = child;
        }
        node[(i >>> 5) & 31] = tail;
        return newRoot;
    }

    private static Object[] newPath(int levels, Object[] tail) {
        Object[] topNode = tail;
        for (int level = levels; level > 0; level -= 5) {
            Object[] newTop = new Object[32];
            newTop[0] = topNode;
            topNode = newTop;
        }
        return topNode;
    }

    public PVec pop() {
        if (size == 0) {
            throw new IllegalStateException("Vector is already empty");
        }
        if (size == 1) {
            return new PVec();
        }
        if (((size-1) & 31) > 0) {
            // This one is curious: having int ts_1 = ((size-1) & 31); and using
            // it is slower than using tail.length - 1 and newTail.length!
            Object[] newTail = new Object[tail.length - 1];
            System.arraycopy(tail, 0, newTail, 0, newTail.length);
            return new PVec(size-1, shift, root, newTail);
        }
        final int newTrieSize = size - 33;
        // special case: if new size is 32, then new root turns is null, old
        // root the tail
        if (newTrieSize == 0) {
            return new PVec(32, 0, null, root);
        }
        // check if we can reduce the trie's height
        if (newTrieSize == 1 << shift) { // can lower the height
            return lowerTrie();
        }
        return popTrie();
    }

    private PVec lowerTrie() {
        int lowerShift = shift - 5;
        Object[] newRoot = (Object[]) root[0];

        // find new tail
        Object[] node = (Object[]) root[1];
        for (int level = lowerShift; level > 0; level -= 5) {
            node = (Object[]) node[0];
        }
        return new PVec(size-1, lowerShift, newRoot, node);
    }

    private PVec popTrie() {
        final int newTrieSize = size - 33;
        // diverges contain information on when the path diverges.
        int diverges = newTrieSize ^ (newTrieSize - 1);
        boolean hasDiverged = false;
        Object[] newRoot = root.clone();
        Object[] node = newRoot;
        for (int level = shift; level > 0; level -= 5) {
            int subidx = (newTrieSize >>> level) & 31;
            Object[] child = (Object[]) node[subidx];
            if (hasDiverged) {
                node = child;
            } else if ((diverges >>> level) != 0) {
                hasDiverged = true;
                node[subidx] = null;
                node = child;
            } else {
                child = child.clone();
                node[subidx] = child;
                node = child;
            }
        }
        return new PVec(size-1, shift, newRoot, node);
    }

    public int size() {
        return size;
    }

    public PVec map(Fun f) {
        return null;
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index:"+index+", Size:"+size);
    }

    private int tailOffset() {
        return (size - 1) & (~31);
    }

    private int tailSize() {
        if (size == 0)
            return 0;
        else
            return ((size-1) & 31)+1;
    }

    public TVec asTransient() {
        return new TVec(size, shift, root, tail);
    }
}
