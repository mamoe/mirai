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

import com.llamalab.safs.attribute.BasicFileAttributes;

public abstract class PartialBasicFileAttributes implements BasicFileAttributes {

  private final FileType type;
  private final long size;

  protected PartialBasicFileAttributes (FileType type, long size) {
    this.type = type;
    this.size = size;
  }

  @Override
  public final boolean isDirectory () {
    return FileType.DIRECTORY == type;
  }

  @Override
  public final boolean isRegularFile () {
    return FileType.REGULAR_FILE == type;
  }

  @Override
  public final boolean isSymbolicLink () {
    return FileType.SYMBOLIC_LINK == type;
  }

  @Override
  public final boolean isOther () {
    return FileType.OTHER == type;
  }

  @Override
  public final long size () {
    return size;
  }
}
