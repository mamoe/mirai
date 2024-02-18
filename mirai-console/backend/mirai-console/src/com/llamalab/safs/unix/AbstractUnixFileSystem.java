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

import com.llamalab.safs.FileStore;
import com.llamalab.safs.FileSystem;
import com.llamalab.safs.LinkOption;
import com.llamalab.safs.Path;
import com.llamalab.safs.PathMatcher;
import com.llamalab.safs.WatchService;
import com.llamalab.safs.attribute.UserPrincipalLookupService;
import com.llamalab.safs.internal.BasicFileAttribute;
import com.llamalab.safs.internal.Utils;
import com.llamalab.safs.spi.FileSystemProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class AbstractUnixFileSystem extends FileSystem {

  protected final FileSystemProvider provider;
  private volatile Path emptyDirectory;
  private volatile Path rootDirectory;

  public AbstractUnixFileSystem (FileSystemProvider provider) {
    this.provider = provider;
  }

  @Override
  public final FileSystemProvider provider () {
    return provider;
  }

  @Override
  public final Path getPath (String first, String... more) {
    return getPathSanitized(UnixPath.sanitize(first, more));
  }

  public final Path getPath (String first) {
    return getPathSanitized(UnixPath.sanitize(first, Utils.EMPTY_STRING_ARRAY));
  }

  protected Path getPathSanitized (String path) {
    return new UnixPath(this, path);
  }

  @Override
  public PathMatcher getPathMatcher (String syntaxAndPattern) {
    final Pattern pattern;
    if (syntaxAndPattern.startsWith("regex:"))
      pattern = Pattern.compile(syntaxAndPattern.substring(6));
    else if (syntaxAndPattern.startsWith("glob:"))
      pattern = Pattern.compile(Utils.globToRegex(syntaxAndPattern, 5, syntaxAndPattern.length()));
    else
      throw new UnsupportedOperationException(syntaxAndPattern);
    return new PathMatcher() {
      @Override
      public boolean matches (Path path) {
        return pattern.matcher(path.toString()).matches();
      }
    };
  }

  protected abstract Path toRealPath (Path path, LinkOption... options) throws IOException;

  @Override
  public final String getSeparator() {
    return "/";
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return Collections.singleton(BasicFileAttribute.VIEW_NAME);
  }

  @Override
  public Iterable<FileStore> getFileStores () {
    return Collections.emptySet();
  }

  @Override
  public final Iterable<Path> getRootDirectories () {
    return Collections.singleton(getRootDirectory());
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService () {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchService newWatchService () throws IOException {
    throw new UnsupportedOperationException();
  }

  public abstract Path getCurrentDirectory ();

  public final Path getEmptyDirectory () {
    if (emptyDirectory == null)
      emptyDirectory = getPathSanitized("");
    return emptyDirectory;
  }

  public final Path getRootDirectory () {
    if (rootDirectory == null)
      rootDirectory = getPathSanitized("/");
    return rootDirectory;
  }

}
