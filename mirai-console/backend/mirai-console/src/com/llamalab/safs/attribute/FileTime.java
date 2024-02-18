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

package com.llamalab.safs.attribute;

import com.llamalab.safs.internal.Utils;

import java.util.concurrent.TimeUnit;

public final class FileTime implements Comparable<FileTime> {

  private static final FileTime ZERO = new FileTime(0);

  private final long value;

  private FileTime (long value) {
    this.value = value;
  }

  /**
   * YYYY-MM-DDThh:mm:ss[.s+]Z
   */
  @Override
  public String toString () {
    return Utils.formatRfc3339(value);
  }

  @Override
  public int hashCode () {
    return (int)(value ^ (value >>> 32));
  }

  @Override
  public boolean equals (Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FileTime))
      return false;
    return value == ((FileTime)obj).value;
  }

  @Override
  public int compareTo (FileTime other) {
    final long lhs = this.value;
    final long rhs = other.value;
    //noinspection UseCompareMethod
    return (lhs < rhs) ? -1 : (lhs == rhs) ? 0 : 1;
  }

  public long to (TimeUnit unit) {
    return unit.convert(value, TimeUnit.MILLISECONDS);
  }

  public long toMillis () {
    return to(TimeUnit.MILLISECONDS);
  }

  public static FileTime from (long value, TimeUnit unit) {
    if (unit == null)
      throw new NullPointerException("unit");
    return fromMillis(TimeUnit.MILLISECONDS.convert(value, unit));
  }

  public static FileTime fromMillis (long value) {
    return (value != 0) ? new FileTime(value) : ZERO;
  }
}
