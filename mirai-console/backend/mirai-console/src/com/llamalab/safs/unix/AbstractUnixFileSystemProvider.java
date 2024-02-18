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

package com.llamalab.safs.unix;

import com.llamalab.safs.Path;
import com.llamalab.safs.internal.AbstractFileSystemProvider;
import com.llamalab.safs.internal.Utils;

import java.net.URI;

public abstract class AbstractUnixFileSystemProvider extends AbstractFileSystemProvider {

  @Override
  protected Class<? extends UnixPath> getPathType () {
    return UnixPath.class;
  }

  @Override
  public Path getPath (URI uri) {
    if (!uri.isAbsolute() || uri.isOpaque() || uri.getAuthority() != null || uri.getFragment() != null || uri.getQuery() != null)
      throw new IllegalArgumentException();
    return new UnixPath((AbstractUnixFileSystem)getFileSystem(uri), UnixPath.sanitize(uri.getPath(), Utils.EMPTY_STRING_ARRAY));
  }

}
