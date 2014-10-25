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

    public PVec update(int index, Object e) {
        return null;
    }

    public Object get(int index) {
        return null;
    }

    public PVec pop() {
        return null;
    }

    public PVec push(Object e) {
        return null;        
    }

    public int size() {
        return size;
    }

    public PVec map(Fun f) {
        return null;
    }
}
