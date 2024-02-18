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

package com.llamalab.safs;

import java.io.IOException;

@SuppressWarnings("serial")
public class FileSystemException extends IOException {

  private final String file;
  private final String otherFile;

  public FileSystemException (String file) {
    this(file, null, null);
  }

  public FileSystemException (String file, String otherFile, String reason) {
    super(reason);
    this.file = file;
    this.otherFile = otherFile;
  }

  public String getFile () {
    return file;
  }

  public String getOtherFile () {
    return otherFile;
  }

  public String getReason () {
    return super.getMessage();
  }

  @Override
  public String getMessage() {
    if (file == null && otherFile == null)
      return getReason();
    final StringBuilder sb = new StringBuilder();
    if (file != null)
      sb.append(file);
    if (otherFile != null)
      sb.append(" -> ").append(otherFile);
    if (getReason() != null)
      sb.append(": ").append(getReason());
    return sb.toString();
  }
}
