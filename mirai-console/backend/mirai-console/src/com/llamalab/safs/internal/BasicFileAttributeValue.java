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

import com.llamalab.safs.attribute.FileAttribute;

public final class BasicFileAttributeValue implements FileAttribute<Object> {

  private final BasicFileAttribute type;
  private final Object value;

  public BasicFileAttributeValue (BasicFileAttribute type, Object value) {
    if (type == null || value == null)
      throw new NullPointerException();
    this.type = type;
    this.value = value;
  }

  @Override
  public String name () {
    return BasicFileAttribute.VIEW_NAME + ":" + type;
  }

  @Override
  public Object value () {
    return value;
  }

  public BasicFileAttribute type () {
    return type;
  }

  @Override
  public int hashCode () {
    return type.hashCode();
  }

  @Override
  public boolean equals (Object other) {
    return other instanceof BasicFileAttributeValue
        && type == ((BasicFileAttributeValue)other).type;
  }
}
