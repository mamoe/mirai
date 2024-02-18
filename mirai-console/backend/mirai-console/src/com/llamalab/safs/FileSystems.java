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

import com.llamalab.safs.java.DefaultJavaFileSystemProvider;
import com.llamalab.safs.spi.FileSystemProvider;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public final class FileSystems {

  private FileSystems () {}

  public static FileSystem getDefault () {
    return DefaultFileSystemHolder.fileSystem;
  }

  public static FileSystem getFileSystem (URI uri) {
    for (final FileSystemProvider provider : FileSystemProvider.installedProviders()) {
      if (provider.getScheme().equalsIgnoreCase(uri.getScheme()))
        return provider.getFileSystem(uri);
    }
    throw new ProviderNotFoundException(uri.getScheme());
  }

  public static FileSystem newFileSystem (URI uri, Map<String,?> env) throws IOException {
    for (final FileSystemProvider provider : FileSystemProvider.installedProviders()) {
      if (provider.getScheme().equalsIgnoreCase(uri.getScheme()))
        return provider.newFileSystem(uri, env);
    }
    throw new ProviderNotFoundException(uri.getScheme());
  }


  private static final class DefaultFileSystemHolder {

    static final FileSystem fileSystem = loadDefaultProvider().getFileSystem(URI.create("file:///"));

    private static FileSystemProvider loadDefaultProvider () {
      FileSystemProvider provider = new DefaultJavaFileSystemProvider();
      final String value = System.getProperty("com.llamalab.safs.spi.DefaultFileSystemProvider");
      if (value != null) {
        try {
          for (final String className : value.split(",")) {
            provider = (FileSystemProvider)Class.forName(className)
                .getDeclaredConstructor(FileSystemProvider.class)
                .newInstance(provider);
          }
        }
        catch (Throwable t) {
          throw new Error(t);
        }
      }
      return provider;
    }

  } // class DefaultFileSystemHolder

}
