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

import java.util.EnumSet;
import java.util.Iterator;

final class AttributeParser<E extends Enum<E>> implements Iterable<E> {

  private final Class<E> enumType;
  private final String attributes;
  private final String viewName;
  private final boolean isDefault;

  public AttributeParser (Class<E> enumType, String attributes, String viewName, boolean isDefault) {
    this.enumType = enumType;
    this.attributes = attributes;
    this.viewName = viewName;
    this.isDefault = isDefault;
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public Iterator<E> iterator () {
    final int length = attributes.length();
    int start = 0, index = 0;
    boolean prefixed = false;
    loop: while (index < length) {
      switch (attributes.charAt(index)) {
        case ':':
          if (prefixed)
            throw new IllegalArgumentException();
          if (index != viewName.length() || !attributes.startsWith(viewName))
            return Utils.emptyIterator();
          start = ++index;
          prefixed = true;
          break;
        case '*':
          if (index != start || ++index != length)
            throw new IllegalArgumentException();
          if (!prefixed && !isDefault)
            return Utils.emptyIterator();
          return EnumSet.allOf(enumType).iterator();
        case ',':
          break loop;
        default:
          ++index;
      }
    }
    if (!prefixed && !isDefault)
      return Utils.emptyIterator();
    return new CommaSeparatedIterator(start, index);
  }

  private final class CommaSeparatedIterator implements Iterator<E> {

    private int start;
    private int index;

    public CommaSeparatedIterator (int start, int index) {
      this.start = start;
      this.index = index;
    }

    @Override
    public boolean hasNext () {
      return start < index;
    }

    @Override
    public E next () {
      final E element = Enum.valueOf(enumType, attributes.substring(start, index));
      index = attributes.indexOf(',', start = index + 1);
      if (index == -1)
        index = attributes.length();
      return element;
    }

    @Override
    public void remove () {
      throw new UnsupportedOperationException();
    }

  } // class CommaSeparatedIterator
}
