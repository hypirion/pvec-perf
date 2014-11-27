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

// WROOOOOOOOOOOOOOOM

import java.lang.reflect.Field;
import sun.misc.Unsafe;

public final class TVecUnsafe {
    private static final Unsafe unsafe;
    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Object arrGet(Object[] arr, int i) {
        return unsafe.getObject(arr, (long)
                                (Unsafe.ARRAY_OBJECT_BASE_OFFSET +
                                 Unsafe.ARRAY_OBJECT_INDEX_SCALE * i));
    }

    private static void arrSet(Object[] arr, int i, Object o) {
        unsafe.putObject(arr, (long)
                         (Unsafe.ARRAY_OBJECT_BASE_OFFSET +
                          Unsafe.ARRAY_OBJECT_INDEX_SCALE * i),
                         o);
    }

    private int size;
    private int shift;
    private Object[] tail;
    private Object[] root;
    private volatile Object id;

    public TVecUnsafe() {
        id = new Object();
        size = 0;
        shift = 0;
        tail = newNode(id);
        root = null;
    }

    TVecUnsafe(int size, int shift, Object[] root, Object[] tail) {
        id = new Object();
        this.size = size;
        this.shift = shift;
        this.tail = expandNode(tail, id);
        this.root = root;
    }

    private static Object[] expandNode(Object[] node, Object id) {
        Object[] expanded = new Object[33];
        System.arraycopy(node, 0, expanded, 0, node.length);
        arrSet(expanded, 32, id);
        return expanded;
    }

    public TVecUnsafe set(int i, Object val) {
        rangeCheck(i);
        if (i >= tailOffset()) {
            arrSet(tail, i & 31, val);
            return this;
        }
        else {
            root = ensureEditable(root, id);
            Object[] node = root;
            for (int level = shift; level > 0; level -= 5) {
                int subidx = (i >>> level) & 31;
                Object[] child = (Object[]) arrGet(node, subidx);
                child = ensureEditable(child, id);
                arrSet(node, subidx, child);
                node = child;
            }
            arrSet(node, i & 31, val);
            return this;
        }
    }

    public Object get(int i) {
        rangeCheck(i);
        if (i >= tailOffset()) {
            return arrGet(tail, i & 31);
        }
        else {
            Object[] node = root;
            for (int level = shift; level > 0; level -= 5) {
                node = (Object[]) arrGet(node, (i >>> level) & 31);
            }
            return arrGet(node, i & 31);
        }
    }

    public TVecUnsafe push(Object val) {
        int ts = tailSize();
        if (ts != 32) {
            arrSet(tail, ts, val);
            size++;
            return this;
        }
        else { // have to insert tail into root.
            Object[] newTail = newNode(id);
            arrSet(newTail, 0, val);
            // Special case: If old size == 32, then tail is new root
            if (size == 32) {
                size++;
                root = tail;
                tail = newTail;
                return this;
            }
            // check if the root is completely filled. Must also increment
            // shift if that's the case.
            if ((size >>> 5) > (1 << shift)) {
                Object[] newRoot = newNode(id);
                arrSet(newRoot, 0, root);
                arrSet(newRoot, 1, newPath(shift, tail, id));
                shift += 5;
                size++;
                root = newRoot;
                tail = newTail;
                return this;
            }
            else { // still space in root
                root = pushLeaf(shift, size-1, root, tail, id);
                tail = newTail;
                size++;
                return this;
            }
        }
    }

    private static Object[] pushLeaf(int shift, int i, Object[] root,
                                     Object[] tail, Object id) {
        Object[] newRoot = ensureEditable(root, id);
        Object[] node = newRoot;
        for (int level = shift; level > 5; level -= 5) {
            int subidx = (i >>> level) & 31;
            Object[] child = (Object[]) arrGet(node, subidx);
            // You could replace this null check with
            // ((tailOffset() - 1) ^ tailOffset() >> level) != 0
            // but we'll still have to assign node[subidx].
            // The null check should therefore be a bit faster.
            if (child == null) {
                arrSet(node, subidx, newPath(level - 5, tail, id));
                return newRoot;
            }
            child = ensureEditable(child, id);
            arrSet(node, subidx, child);
            node = child;
        }
        arrSet(node, (i >>> 5) & 31, tail);
        return newRoot;
    }

    private static Object[] newPath(int levels, Object[] tail, Object id) {
        Object[] topNode = tail;
        for (int level = levels; level > 0; level -= 5) {
            Object[] newTop = newNode(id);
            arrSet(newTop, 0, topNode);
            topNode = newTop;
        }
        return topNode;
    }

    public TVecUnsafe pop() {
        if (size == 0) {
            throw new IllegalStateException("Vector is already empty");
        }
        if (size == 1) {
            size = 0;
            tail[0] = null;
            return this;
        }
        int ts_1 = (size-1) & 31;
        if (ts_1 > 0) {
            arrSet(tail, ts_1, null);
            size--;
            return this;
        }
        else { // has to find new tail
            int newTrieSize = size - 33;
            // special case: if new size is 32, then new root turns is null, old
            // root the tail
            if (newTrieSize == 0) {
                shift = 0;
                size = 32;
                tail = root;
                root = null;
                return this;
            }
            // check if we can reduce the trie's height
            if (newTrieSize == 1 << shift) { // can lower the height
                shift -= 5;
                Object[] newRoot = (Object[]) arrGet(root, 0);

                // find new tail
                Object[] node = (Object[]) arrGet(root, 1);
                for (int level = shift; level > 0; level -= 5) {
                    node = (Object[]) arrGet(node, 0);
                }
                size--;
                root = newRoot;
                tail = node;
                return this;
            } else { // height is same
                // diverges contain information on when the path diverges.
                int diverges = newTrieSize ^ (newTrieSize - 1);
                boolean hasDiverged = false;
                Object[] newRoot = ensureEditable(root, id);
                Object[] node = newRoot;
                for (int level = shift; level > 0; level -= 5) {
                    int subidx = (newTrieSize >>> level) & 31;
                    Object[] child = (Object[]) arrGet(node, subidx);
                    if (hasDiverged) {
                        node = child;
                    } else if ((diverges >>> level) != 0) {
                        hasDiverged = true;
                        arrSet(node, subidx, null);
                        node = child;
                    } else {
                        child = ensureEditable(child, id);
                        arrSet(node, subidx, child);
                        node = child;
                    }
                }
                root = newRoot;
                tail = node;
                size--;
                return this;
            }
        }
    }

    public int size() {
        return size;
    }

    public TVecUnsafe map(Fun f) {
        return null;
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index:"+index+", Size:"+size);
    }

    private void editCheck() {
        if (id == null)
            throw new IllegalStateException("Transient has been converted to persistent.");
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

    public PVecUnsafe asPersistent() {
        id = null;
        return new PVecUnsafe(size, shift, root, compressedTail());
    }

    private Object[] compressedTail() {
        int ts = tailSize();
        ts = ts == 32 ? 33 : ts;
        Object[] compressed = new Object[ts];
        System.arraycopy(tail, 0, compressed, 0, ts);
        return compressed;
    }

    private static Object[] newNode(Object id) {
        Object[] node = new Object[33];
        node[32] = id;
        return node;
    }

    private static Object[] ensureEditable(Object[] node, Object id) {
        if (arrGet(node, 32) == id) {
            return node;
        }
        else {
            Object[] editable = new Object[33];
            // this arraycopy assumes nodes cannot be more than 33 elts long
            System.arraycopy(node, 0, editable, 0, 32);
            arrSet(editable, 32, id);
            return editable;
        }
    }
}
