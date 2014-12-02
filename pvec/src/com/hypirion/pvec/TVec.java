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

/* This implementation is essentially copypaste from PVec, but there are a
   couple of "manual" changes which is done:

   - Cloning and node creating is replaced with ensureEditable() and newNode().
   - Whenever a new TVec from a persistent vector, the tail is expanded.
   - Whenever a TVec is converted back to a persistent vector, its tail is
     compressed and the TVec cannot be used anymore.
   - Instead of returning a new vector like PVec, it always returns itself.
*/

import java.util.Iterator;

public final class TVec implements Iterable {
    private int size;
    private int shift;
    private Object[] tail;
    private Object[] root;
    private volatile Object id;

    public TVec() {
        id = new Object();
        size = 0;
        shift = 0;
        tail = newNode(id);
        root = null;
    }

    TVec(int size, int shift, Object[] root, Object[] tail) {
        id = new Object();
        this.size = size;
        this.shift = shift;
        this.tail = expandNode(tail, id);
        this.root = root;
    }

    private static Object[] expandNode(Object[] node, Object id) {
        Object[] expanded = new Object[33];
        System.arraycopy(node, 0, expanded, 0, node.length);
        expanded[32] = id;
        return expanded;
    }

    public TVec set(int i, Object val) {
        rangeCheck(i);
        if (i >= tailOffset()) {
            tail[i & 31] = val;
            return this;
        }
        else {
            root = ensureEditable(root, id);
            Object[] node = root;
            for (int level = shift; level > 0; level -= 5) {
                int subidx = (i >>> level) & 31;
                Object[] child = (Object[]) node[subidx];
                child = ensureEditable(child, id);
                node[subidx] = child;
                node = child;
            }
            node[i & 31] = val;
            return this;
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

    public TVec push(Object val) {
        int ts = tailSize();
        if (ts != 32) {
            tail[ts] = val;
            size++;
            return this;
        }
        else { // have to insert tail into root.
            Object[] newTail = newNode(id);
            newTail[0] = val;
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
                newRoot[0] = root;
                newRoot[1] = newPath(shift, tail, id);
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
            Object[] child = (Object[]) node[subidx];
            // You could replace this null check with
            // ((tailOffset() - 1) ^ tailOffset() >> level) != 0
            // but we'll still have to assign node[subidx].
            // The null check should therefore be a bit faster.
            if (child == null) {
                node[subidx] = newPath(level - 5, tail, id);
                return newRoot;
            }
            child = ensureEditable(child, id);
            node[subidx] = child;
            node = child;
        }
        node[(i >>> 5) & 31] = tail;
        return newRoot;
    }

    private static Object[] newPath(int levels, Object[] tail, Object id) {
        Object[] topNode = tail;
        for (int level = levels; level > 0; level -= 5) {
            Object[] newTop = newNode(id);
            newTop[0] = topNode;
            topNode = newTop;
        }
        return topNode;
    }

    public TVec pop() {
        if (size == 0) {
            throw new IllegalStateException("Vector is already empty");
        }
        if (size == 1) {
            size = 0;
            return this;
        }
        if (((size-1) & 31) > 0) {
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
                Object[] newRoot = (Object[]) root[0];

                // find new tail
                Object[] node = (Object[]) root[1];
                for (int level = shift; level > 0; level -= 5) {
                    node = (Object[]) node[0];
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
                    Object[] child = (Object[]) node[subidx];
                    if (hasDiverged) {
                        node = child;
                    } else if ((diverges >>> level) != 0) {
                        hasDiverged = true;
                        node[subidx] = null;
                        node = child;
                    } else {
                        child = ensureEditable(child, id);
                        node[subidx] = child;
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

    public TVec map(Fun f) {
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

    public PVec asPersistent() {
        id = null;
        return new PVec(size, shift, root, compressedTail());
    }

    private Object[] compressedTail() {
        int ts = tailSize();
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
        if (node.length == 33 && node[32] == id) {
            return node;
        }
        else {
            Object[] editable = new Object[33];
            // this arraycopy assumes nodes cannot be more than 33 elts long
            System.arraycopy(node, 0, editable, 0, node.length);
            editable[32] = id;
            return editable;
        }
    }

    // Use iteration with CARE: The iterator is fully usable even if the
    // transient is updated.
    public Iterator iterator() {
        return new VecIter(size, shift, root, tail);
    }
}
