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

import com.llamalab.safs.attribute.FileTime;

public class CompleteBasicFileAttributes extends PartialBasicFileAttributes {

  private final Object fileKey;
  private final FileTime creationTime;
  private final FileTime lastModifiedTime;
  private final FileTime lastAccessTime;

  public CompleteBasicFileAttributes (Object fileKey, FileType type, long size, FileTime creationTime, FileTime lastModifiedTime, FileTime lastAccessTime) {
    super(type, size);
    this.fileKey = fileKey;
    this.creationTime = creationTime;
    this.lastModifiedTime = lastModifiedTime;
    this.lastAccessTime = lastAccessTime;
  }

  @Override
  public final Object fileKey () {
    return fileKey;
  }

  @Override
  public final FileTime creationTime () {
    return creationTime;
  }

  @Override
  public final FileTime lastModifiedTime () {
    return lastModifiedTime;
  }

  @Override
  public final FileTime lastAccessTime () {
    return lastAccessTime;
  }
}
