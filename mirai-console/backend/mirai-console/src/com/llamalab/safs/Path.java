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

import java.io.File;
import java.io.IOException;
import java.net.URI;

public interface Path extends Comparable<Path>, Iterable<Path>, Watchable {
  public FileSystem getFileSystem ();

  public boolean isAbsolute ();
  public Path getRoot ();
  public Path getParent ();
  public Path getFileName ();
  public int getNameCount ();
  public Path getName (int index);
  public Path subpath (int beginIndex, int endIndex);
  public boolean startsWith (Path other);
  public boolean startsWith (String other);
  public boolean endsWith (Path other);
  public boolean endsWith (String other);

  public Path normalize ();
  public Path relativize (Path other);
  public Path resolve (Path other);
  public Path resolve (String other);
  public Path resolveSibling (Path other);
  public Path resolveSibling (String other);

  public Path toAbsolutePath ();
  public Path toRealPath (LinkOption... options) throws IOException;
  public File toFile ();
  public URI toUri ();
}
