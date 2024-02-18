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

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Values must MUST implement Comparable.
 */
public class SearchSet<T> extends AbstractSet<T> implements Comparator<Object> {

  private final Object[] elements;

  @SafeVarargs
  public SearchSet (T... elements) {
    this.elements = elements;
    Arrays.sort(elements, this);
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public Iterator<T> iterator () {
    return new Iterator<T>() {
      private int index;

      @Override
      public boolean hasNext () {
        return index < elements.length;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T next () {
        if (index >= elements.length)
          throw new NoSuchElementException();
        return (T)elements[index++];
      }

      @Override
      public void remove () {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public int size () {
    return elements.length;
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public boolean addAll (Collection<? extends T> collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear () {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains (Object object) {
    return Arrays.binarySearch(elements, object, this) >= 0;
  }

  @Override
  public boolean remove (Object object) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public boolean retainAll (Collection<?> collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll (Collection<?> collection) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public int compare (Object lhs, Object rhs) {
    if (lhs == rhs)
      return 0;
    if (lhs == null)
      return 1;
    if (rhs == null)
      return -1;
    final Class lc = lhs.getClass();
    final Class rc = rhs.getClass();
    return (lc != rc) ? lc.getName().compareTo(rc.getName()) : ((Comparable)lhs).compareTo(rhs);
  }

}
