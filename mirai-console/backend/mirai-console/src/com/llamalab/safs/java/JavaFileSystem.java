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

package com.llamalab.safs.java;

import com.llamalab.safs.LinkOption;
import com.llamalab.safs.NoSuchFileException;
import com.llamalab.safs.Path;
import com.llamalab.safs.internal.DefaultFileSystem;
import com.llamalab.safs.spi.FileSystemProvider;
import com.llamalab.safs.unix.AbstractUnixFileSystem;

import java.io.File;
import java.io.IOException;

/**
 * Only support {@link com.llamalab.safs.unix.UnixPath}.
 */
public class JavaFileSystem extends AbstractUnixFileSystem implements DefaultFileSystem {

  protected volatile Path cacheDirectory;
  protected volatile Path currentDirectory;

  public JavaFileSystem (FileSystemProvider provider) {
    super(provider);
  }

  @Override
  public final void close () throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public final boolean isOpen () {
    return true;
  }

  @Override
  public boolean isReadOnly () {
    return false;
  }

  @Override
  public Path getCacheDirectory () {
    if (cacheDirectory == null)
      cacheDirectory = getPathSanitized(System.getProperty("java.io.tmpdir"));
    return cacheDirectory;
  }

  public final Path getCurrentDirectory () {
    if (currentDirectory == null)
      currentDirectory = getPathSanitized(System.getProperty("user.dir"));
    return currentDirectory;
  }

  @Override
  protected Path toRealPath (Path path, LinkOption... options) throws IOException {
    final File file = path.toFile();
    if (!file.exists())
      throw new NoSuchFileException(path.toString());
    for (final LinkOption option : options) {
      if (LinkOption.NOFOLLOW_LINKS == option)
        return path.toAbsolutePath().normalize();
    }
    return getPathSanitized(file.getCanonicalPath());
  }
}
