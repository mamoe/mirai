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

package com.llamalab.safs.spi;

import com.llamalab.safs.CopyOption;
import com.llamalab.safs.DirectoryStream;
import com.llamalab.safs.FileStore;
import com.llamalab.safs.FileSystem;
import com.llamalab.safs.FileSystems;
import com.llamalab.safs.LinkOption;
import com.llamalab.safs.NoSuchFileException;
import com.llamalab.safs.OpenOption;
import com.llamalab.safs.Path;
import com.llamalab.safs.attribute.BasicFileAttributes;
import com.llamalab.safs.attribute.FileAttribute;
import com.llamalab.safs.attribute.FileAttributeView;
import com.llamalab.safs.channels.SeekableByteChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

public abstract class FileSystemProvider {

  protected FileSystemProvider () {}

  public static List<FileSystemProvider> installedProviders () {
    return InstalledFileSystemProvidersHolder.providers;
  }

  public abstract String getScheme ();
  public abstract Path getPath (URI uri);
  public abstract FileSystem getFileSystem (URI uri);
  public abstract FileSystem newFileSystem (Path path, Map<String,?> env) throws IOException;
  public abstract FileSystem newFileSystem (URI uri, Map<String,?> env) throws IOException;
  public abstract FileStore getFileStore (Path path) throws IOException;
  public abstract boolean isSameFile (Path path1, Path path2) throws IOException;
  public abstract boolean isHidden (Path path) throws IOException;
  public abstract void createDirectory (Path dir, FileAttribute<?>... attrs) throws IOException;
  public abstract void delete (Path path) throws IOException;
  public boolean deleteIfExists (Path path) throws IOException {
    try {
      delete(path);
      return true;
    }
    catch (NoSuchFileException e) {
      return false;
    }
  }
  public abstract void copy (Path source, Path target, CopyOption... options) throws IOException;
  public abstract void move (Path source,Path target, CopyOption... options) throws IOException;
  public abstract InputStream newInputStream (Path path, OpenOption... options) throws IOException;
  public abstract OutputStream newOutputStream (Path path, OpenOption... options) throws IOException;
  public abstract SeekableByteChannel newByteChannel (Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException;
  /*
  public void createSymbolicLink (Path link, Path target) throws IOException {
    throw new UnsupportedOperationException();
  }
  */
  public Path readSymbolicLink (Path link) throws IOException {
    throw new UnsupportedOperationException();
  }
  public abstract <A extends BasicFileAttributes> A readAttributes (Path path, Class<A> type, LinkOption... options) throws IOException;
  public abstract Map<String,Object> readAttributes (Path path, String attributes, LinkOption... options) throws IOException;
  public abstract void setAttribute (Path path, String attribute, Object value, LinkOption... options) throws IOException;
  public abstract <V extends FileAttributeView> V getFileAttributeView (Path path, Class<V> type, LinkOption... options);

  public abstract DirectoryStream<Path> newDirectoryStream (Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException;

  private static final class InstalledFileSystemProvidersHolder {

    static final List<FileSystemProvider> providers = loadInstalledProviders();

    private static List<FileSystemProvider> loadInstalledProviders () {
      final List<FileSystemProvider> providers = new ArrayList<FileSystemProvider>();
      providers.add(FileSystems.getDefault().provider());
      for (final FileSystemProvider provider : ServiceLoader.load(FileSystemProvider.class, FileSystemProvider.class.getClassLoader())) {
        if (!"file".equalsIgnoreCase(provider.getScheme()))
          providers.add(provider);
      }
      return Collections.unmodifiableList(providers);
    }

  } // class InstalledFileSystemProvidersHolder
}
