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

import java.util.Iterator;

public class PathDescender<T extends SegmentEntry> implements Iterator<PathDescender.Event> {

  public enum Event {
    DIRECTORY,
    FILE,
    MISSING_DIRECTORY,
    MISSING_FILE
  }

  private final Iterator<String> segments;
  private SegmentEntry parent;
  private String segment;
  private int index;

  public PathDescender (T root, Iterator<String> segments) {
    this.parent = root;
    this.segments = segments;
  }

  @Override
  public boolean hasNext () {
    return index >= 0 && segments.hasNext();
  }

  @Override
  public Event next () {
    if (segment != null)
      parent = parent.children[index];
    if ((index = parent.binarySearch(segment = segments.next())) >= 0)
      return segments.hasNext() ? Event.DIRECTORY : Event.FILE;
    else
      return segments.hasNext() ? Event.MISSING_DIRECTORY : Event.MISSING_FILE;
  }

  @Override
  public void remove () {
    parent.remove(index);
    index = -1;
  }

  public void set (T entry) {
    entry.segment = segment;
    index = parent.put(index, entry);
  }

  public String segment () {
    return segment;
  }

  @SuppressWarnings("unchecked")
  public T parent () {
    return (T)parent;
  }

  @SuppressWarnings("unchecked")
  public T entry () {
    return (T)parent.children[index];
  }

}
