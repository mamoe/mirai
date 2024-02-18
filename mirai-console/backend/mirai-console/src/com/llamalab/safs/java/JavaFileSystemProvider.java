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

import com.llamalab.safs.AccessDeniedException;
import com.llamalab.safs.AtomicMoveNotSupportedException;
import com.llamalab.safs.CopyOption;
import com.llamalab.safs.DirectoryNotEmptyException;
import com.llamalab.safs.DirectoryStream;
import com.llamalab.safs.FileAlreadyExistsException;
import com.llamalab.safs.FileStore;
import com.llamalab.safs.FileSystemException;
import com.llamalab.safs.LinkOption;
import com.llamalab.safs.NoSuchFileException;
import com.llamalab.safs.NotDirectoryException;
import com.llamalab.safs.NotLinkException;
import com.llamalab.safs.OpenOption;
import com.llamalab.safs.Path;
import com.llamalab.safs.StandardCopyOption;
import com.llamalab.safs.StandardOpenOption;
import com.llamalab.safs.attribute.BasicFileAttributes;
import com.llamalab.safs.attribute.FileAttribute;
import com.llamalab.safs.attribute.FileTime;
import com.llamalab.safs.channels.SeekableByteChannel;
import com.llamalab.safs.internal.AbstractDirectoryStream;
import com.llamalab.safs.internal.BasicFileAttributeValue;
import com.llamalab.safs.internal.CompleteBasicFileAttributes;
import com.llamalab.safs.internal.FileType;
import com.llamalab.safs.internal.SearchSet;
import com.llamalab.safs.internal.Utils;
import com.llamalab.safs.spi.FileSystemProvider;
import com.llamalab.safs.unix.AbstractUnixFileSystemProvider;
import com.llamalab.safs.unix.UnixPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Set;

/**
 * Only support {@link UnixPath}.
 */
public abstract class JavaFileSystemProvider extends AbstractUnixFileSystemProvider {

  public JavaFileSystemProvider () {}
  public JavaFileSystemProvider (FileSystemProvider provider) {}

  @Override
  public String getScheme () {
    return "file";
  }

  @Override
  public FileStore getFileStore (Path path) throws IOException {
    // TODO: possible?
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSameFile (Path path1, Path path2) throws IOException {
    if (path1.equals(path2))
      return true;
    return getPathType().isInstance(path1) && getPathType().isInstance(path2)
        && path1.getFileSystem().equals(path2.getFileSystem())
        && isSameFile(path1.toFile(), path2.toFile());
  }

  @Override
  public boolean isHidden (Path path) throws IOException {
    checkPath(path);
    return ((UnixPath)path).isHidden();
  }

  @Override
  public void createDirectory (Path dir, FileAttribute<?>... attrs) throws IOException {
    checkPath(dir);
    createDirectory(dir.toFile(), attrs);
  }

  @Override
  public void delete (Path path) throws IOException {
    checkPath(path);
    delete(path.toFile(), false);
  }

  @Override
  public void copy (Path source, Path target, CopyOption... options) throws IOException {
    checkPath(source);
    checkPath(target);
    transfer(source, target, false, new SearchSet<CopyOption>(options));
  }

  @Override
  public void move (Path source, Path target, CopyOption... options) throws IOException {
    checkPath(source);
    checkPath(target);
    transfer(source, target, true, new SearchSet<CopyOption>(options));
  }

  // TODO: symbolic links
  private void transfer (Path source, Path target, boolean move, Set<CopyOption> options) throws IOException {
    final File sourceFile = source.toFile();
    final BasicFileAttributes sourceAttrs = readBasicFileAttributes(sourceFile);
    final File targetFile = target.toFile();
    if (sourceFile.getCanonicalPath().equals(targetFile.getCanonicalPath()))
      return;
    // atomic
    if (move && options.contains(StandardCopyOption.ATOMIC_MOVE)) {
      if (sourceFile.renameTo(targetFile))
        return;
      throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "Rename failed");
    }
    // delete target
    if (options.contains(StandardCopyOption.REPLACE_EXISTING))
      delete(targetFile, true); // throws DirectoryNotEmptyException
    else if (targetFile.exists())
      throw new FileAlreadyExistsException(target.toString());
    // rename
    if (move && sourceFile.renameTo(targetFile))
      return;
    // transfer
    if (sourceAttrs.isDirectory()) {
      if (move && isNonEmptyDirectory(sourceFile))
        throw new DirectoryNotEmptyException(source.toString());
      createDirectory(target);
    }
    else
      copyFile(sourceFile, targetFile);
    try {
      if (options.contains(StandardCopyOption.COPY_ATTRIBUTES))
        setLastModifiedTime(targetFile, sourceAttrs.lastModifiedTime());
      if (move)
        delete(sourceFile, false);
    }
    catch (IOException e) {
      //noinspection ResultOfMethodCallIgnored
      targetFile.delete();
      throw e;
    }
    catch (RuntimeException  e) {
      //noinspection ResultOfMethodCallIgnored
      targetFile.delete();
      throw e;
    }
  }

  @Override
  public InputStream newInputStream (Path path, OpenOption... options) throws IOException {
    checkPath(path);
    return newInputStream(path.toFile(), (options.length == 0) ? DEFAULT_NEW_INPUT_STREAM_OPTIONS : new SearchSet<OpenOption>(options));
  }

  // TODO: LinkOption.NOFOLLOW_LINKS
  private InputStream newInputStream (File file, Set<? extends OpenOption> options) throws IOException {
    if (options.contains(StandardOpenOption.WRITE))
      throw new IllegalArgumentException();
    try {
      return new FileInputStream(file);
    }
    catch (IOException e) {
      throw toProperException(e, file.toString(), null);
    }
  }

  @Override
  public OutputStream newOutputStream (Path path, OpenOption... options) throws IOException {
    checkPath(path);
    return newOutputStream(path.toFile(), (options.length == 0) ? DEFAULT_NEW_OUTPUT_STREAM_OPTIONS : new SearchSet<OpenOption>(options));
  }

  // TODO: LinkOption.NOFOLLOW_LINKS
  private OutputStream newOutputStream (File file, Set<? extends OpenOption> options) throws IOException {
    if (!options.contains(StandardOpenOption.WRITE))
      throw new IllegalArgumentException();
    try {
      checkCreateOptions(file, options);
      return new FileOutputStream(file, options.contains(StandardOpenOption.APPEND));
    }
    catch (IOException e) {
      throw toProperException(e, file.toString(), null);
    }
  }

  @Override
  public SeekableByteChannel newByteChannel (Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
    checkPath(path);
    return newByteChannel(path.toFile(), options, attrs);
  }

  // TODO: LinkOption.NOFOLLOW_LINKS
  private SeekableByteChannel newByteChannel (File file, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
    try {
      if (!options.contains(StandardOpenOption.WRITE))
        return new SeekableByteChannelWrapper(new RandomAccessFile(file, "r").getChannel(), false);
      checkCreateOptions(file, options);
      final RandomAccessFile raf = new RandomAccessFile(file, toModeString(options));
      if (options.contains(StandardOpenOption.TRUNCATE_EXISTING)) {
        try {
          raf.setLength(0);
        }
        catch (IOException e) {
          Utils.closeQuietly(raf);
          throw e;
        }
      }
      return new SeekableByteChannelWrapper(raf.getChannel(), options.contains(StandardOpenOption.APPEND));
    }
    catch (IOException e) {
      throw toProperException(e, file.toString(), null);
    }
  }

  private static String toModeString (Set<? extends OpenOption> options) {
    if (options.contains(StandardOpenOption.SYNC))
      return "rws";
    if (options.contains(StandardOpenOption.DSYNC))
      return "rwd";
    return "rw";
  }

  private static void checkCreateOptions (File file, Set<? extends OpenOption> options) throws IOException {
    if (options.contains(StandardOpenOption.CREATE_NEW)) {
      if (file.exists())
        throw new FileAlreadyExistsException(file.toString());
    }
    else if (!options.contains(StandardOpenOption.CREATE)) {
      if (!file.exists())
        throw new NoSuchFileException(file.toString());
    }
  }

  /*
  private RandomAccessFile newRandomAccessFile (File file, Set<? extends OpenOption> options) throws IOException {
    try {
      if (!options.contains(StandardOpenOption.WRITE))
        return new RandomAccessFile(file, toModeString(options));
      final RandomAccessFile raf = new RandomAccessFile(file, toModeString(options));
      if (options.contains(StandardOpenOption.TRUNCATE_EXISTING)) {
        try {
          raf.setLength(0);
        }
        catch (IOException e) {
          Utils.closeQuietly(raf);
          throw e;
        }
      }
      return raf;
    }
    catch (IOException e) {
      throw toProperException(e, file.toString(), null);
    }
  }
  */

  /*
  @Override
  public void createSymbolicLink (Path link, Path target) throws IOException {
    checkPaths(link, target);
    createSymbolicLink(link.toFile(), target.toFile());
  }

  protected void createSymbolicLink (File link, File target) throws IOException {
    final Process process = Runtime.getRuntime().exec(new String[] { "ln", "-s", target.toString(), link.toString() });
    try {
      if (0 != process.waitFor()) {
        if (target.exists())
          throw new FileAlreadyExistsException(target.toString());
        else
          throw new IOException("ln command failure");
      }
    }
    catch (InterruptedException e) {
      throw (IOException)new InterruptedIOException().initCause(e);
    }
    finally {
      process.destroy();
    }
  }
  */

  @Override
  public Path readSymbolicLink (Path link) throws IOException {
    final Path parent = link.getParent();
    if (parent != null)
      link = parent.toRealPath().resolve(link.getFileName());
    final Path real = link.toRealPath();
    if (real.equals(link.toAbsolutePath()))
      throw new NotLinkException(link.toString());
    return real;
  }

  /**
   * May be overridden with a faster implementation.
   */
  protected boolean isSymbolicLink (Path path) {
    return isSymbolicLink(path.toFile());
  }

  private boolean isSymbolicLink (File file) {
    try {
      final File parent = file.getParentFile();
      if (parent != null)
        file = new File(parent.getCanonicalPath(), file.getName());
      return !file.getCanonicalPath().equals(file.getAbsolutePath());
    }
    catch (IOException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <A extends BasicFileAttributes> A readAttributes (Path path, Class<A> type, LinkOption... options) throws IOException {
    checkPath(path);
    if (BasicFileAttributes.class != type)
      throw new UnsupportedOperationException("Unsupported type: "+type);
    return (A)readBasicFileAttributes(path.toFile(), options);
  }

  protected BasicFileAttributes readBasicFileAttributes (File file, LinkOption... options) throws IOException {
    for (final LinkOption option : options) {
      if (LinkOption.NOFOLLOW_LINKS == option) {
        if (!isSymbolicLink(file))
          break;
        return new CompleteBasicFileAttributes(null, FileType.SYMBOLIC_LINK, 0, Utils.ZERO_TIME, Utils.ZERO_TIME, Utils.ZERO_TIME);
      }
    }
    if (!file.exists())
      throw new NoSuchFileException(file.toString());
    final FileType fileType;
    if (file.isDirectory())
      fileType = FileType.DIRECTORY;
    else if (file.isFile())
      fileType = FileType.REGULAR_FILE;
    else
      fileType = FileType.OTHER;
    return new CompleteBasicFileAttributes(
        null,
        fileType,
        file.length(),
        Utils.ZERO_TIME,
        FileTime.fromMillis(file.lastModified()),
        Utils.ZERO_TIME);
  }

  @Override
  protected void setAttributes (Path path, Set<? extends FileAttribute<?>> attrs, LinkOption... options) throws IOException {
    checkPath(path);
    for (final FileAttribute<?> attr : attrs) {
      if (attr instanceof BasicFileAttributeValue) {
        switch (((BasicFileAttributeValue)attr).type()) {
          case lastModifiedTime:
            setLastModifiedTime(path.toFile(), (FileTime)attr.value());
            continue;
        }
      }
      throw new UnsupportedOperationException("Attribute: "+attr.name());
    }
  }

  protected void setLastModifiedTime (File file, FileTime value) throws IOException {
    if (!file.setLastModified(value.toMillis()))
      throw new FileSystemException(file.toString(), null, "Failed to set last modified time");
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream (final Path dir, final DirectoryStream.Filter<? super Path> filter) throws IOException {
    checkPath(dir);
    if (filter == null)
      throw new NullPointerException("filter");
    try {
      final File dirFile = dir.toFile();
      final String[] files = dirFile.list();
      if (files == null) {
        if (dirFile.exists() && !dirFile.canRead())
          throw new AccessDeniedException(dir.toString());
        throw new NotDirectoryException(dir.toString());
      }
      return new AbstractDirectoryStream<Path>() {
        private int index;
        @Override
        protected Path advance () throws IOException {
          while (index < files.length) {
            final Path entry = dir.resolve(files[index++]);
            if (filter.accept(entry))
              return entry;
          }
          return null;
        }
      };
    }
    catch (IOException e) {
      throw toProperException(e, dir.toString(), null);
    }
  }

  protected final void createDirectory (File dir, FileAttribute<?>... attrs) throws IOException {
    if (!dir.mkdir()) {
      if (dir.exists()) {
        if (!dir.canWrite())
          throw new AccessDeniedException(dir.toString());
        throw new FileAlreadyExistsException(dir.toString());
      }
      throw new FileSystemException(dir.toString(), null, "Failed to create directory");
    }
  }

  protected final void delete (File file, boolean ifExists) throws IOException {
    if (!file.delete()) {
      if (isNonEmptyDirectory(file))
        throw new DirectoryNotEmptyException(file.toString());
      if (file.exists()) {
        if (!file.canWrite())
          throw new AccessDeniedException(file.toString());
        throw new FileSystemException(file.toString(), null, "Failed to delete file");
      }
      else if (!ifExists)
        throw new NoSuchFileException(file.toString());
    }
  }

  protected final void copyFile (File source, File target) throws IOException {
    final FileChannel in;
    try {
      in = new FileInputStream(source).getChannel();
    }
    catch (IOException e) {
      throw toProperException(e, source.toString(), null);
    }
    try {
      final FileChannel out;
      try {
        out = new FileOutputStream(target).getChannel();
      }
      catch (IOException e) {
        throw toProperException(e, target.toString(), null);
      }
      try {
        Utils.transfer(in, out);
      }
      catch (IOException e) {
        throw toProperException(e, source.toString(), target.toString());
      }
      finally {
        out.close();
      }
    }
    finally {
      in.close();
    }
  }

  protected final boolean isSameFile (File file1, File file2) throws IOException {
    return file1.getCanonicalPath().equals(file2.getCanonicalPath());
  }

  protected final boolean isNonEmptyDirectory (File dir) {
    final String[] names = dir.list(new FilenameFilter() {
      private boolean found;
      @Override
      public boolean accept (File dir, String filename) {
        if (found)
          return false;
        return found = true;
      }
    });
    return names != null && names.length != 0;
  }

}
