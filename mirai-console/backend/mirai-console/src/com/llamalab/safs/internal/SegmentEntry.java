/*
 * Copyright (C) 2019 Henrik Lindqvist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.llamalab.safs.internal;

import com.llamalab.safs.unix.UnixPath;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SegmentEntry<T extends SegmentEntry<T>> implements Iterable<T> {

  private static final SegmentEntry[] EMPTY = new SegmentEntry[0];

  String segment;
  SegmentEntry[] children = EMPTY;
  int size;

  @SuppressWarnings("NullableProblems")
  @Override
  public Iterator<T> iterator () {
    return new Iterator<T>() {

      private int index;

      @Override
      public boolean hasNext () {
        return index < size;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T next () {
        if (index >= size)
          throw new NoSuchElementException();
        return (T)children[index++];
      }

      @Override
      public void remove () {
        if (index == 0)
          throw new IllegalStateException();
        SegmentEntry.this.remove(--index);
      }
    };
  }

  public final boolean isEmpty () {
    return size == 0;
  }

  public final void clear () {
    size = 0;
    Arrays.fill(children, null);
  }

  @SuppressWarnings({ "unchecked", "SuspiciousSystemArraycopy" })
  public T[] toArray (T[] a) {
    if (size > a.length)
      a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
    System.arraycopy(children, 0, a, 0, size);
    return a;
  }

  public T getDescendant (UnixPath path) {
    return getDescendant(path.stringIterator());
  }

  @SuppressWarnings("unchecked")
  public T getDescendant (Iterator<String> segments) {
    SegmentEntry parent = this;
    while (segments.hasNext()) {
      final int index = parent.binarySearch(segments.next());
      if (index < 0)
        return null;
      parent = parent.children[index];
    }
    return (T)parent;
  }

  public final PathDescender<T> descentor (UnixPath path) {
    return descentor(path.stringIterator());
  }

  @SuppressWarnings("unchecked")
  public final PathDescender<T> descentor (Iterator<String> segments) {
    return new PathDescender<T>((T)this, segments);
  }

  final int binarySearch (String segment) {
    final SegmentEntry[] c = children;
    int low = 0;
    int high = size - 1;
    while (low <= high) {
      final int mid = (low + high) >>> 1;
      final int cmp = c[mid].segment.compareTo(segment);
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1); // key not found.
  }

  final int put (int index, SegmentEntry child) {
    if (index < 0) {
      index = ~index;
      final SegmentEntry[] oc = children;
      final int s = size++;
      if (s == oc.length) {
        final SegmentEntry[] nc = new SegmentEntry[1 << (32 - Integer.numberOfLeadingZeros(s))];
        System.arraycopy(oc, 0, nc, 0, index);
        System.arraycopy(oc, index, nc, index + 1, s - index);
        children = nc;
        nc[index] = child;
      }
      else {
        System.arraycopy(oc, index, oc, index + 1, s - index);
        oc[index] = child;
      }
    }
    else
      children[index] = child;
    return index;
  }

  final void remove (int index) {
    if (index < 0 || size <= index)
      throw new IndexOutOfBoundsException();
    System.arraycopy(children, index + 1, children, index, --size - index);
  }

}
