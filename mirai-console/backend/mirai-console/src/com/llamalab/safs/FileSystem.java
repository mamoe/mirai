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

import com.llamalab.safs.attribute.UserPrincipalLookupService;
import com.llamalab.safs.spi.FileSystemProvider;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

public abstract class FileSystem implements Closeable {
  public abstract FileSystemProvider provider ();
  public abstract boolean isOpen ();
  public abstract boolean isReadOnly ();
  public abstract Set<String> supportedFileAttributeViews();
  public abstract String getSeparator ();
  public abstract Path getPath (String first, String... more);
  public abstract PathMatcher getPathMatcher (String syntaxAndPattern);
  public abstract Iterable<FileStore> getFileStores ();
  public abstract Iterable<Path> getRootDirectories ();
  public abstract UserPrincipalLookupService getUserPrincipalLookupService ();
  public abstract WatchService newWatchService () throws IOException;
}
