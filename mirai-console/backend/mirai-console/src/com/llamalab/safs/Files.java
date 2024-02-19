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

import com.llamalab.safs.attribute.BasicFileAttributes;
import com.llamalab.safs.attribute.FileAttribute;
import com.llamalab.safs.attribute.FileAttributeView;
import com.llamalab.safs.attribute.FileTime;
import com.llamalab.safs.internal.BasicFileAttribute;
import com.llamalab.safs.internal.DefaultFileSystem;
import com.llamalab.safs.internal.SearchSet;
import com.llamalab.safs.internal.Utils;
import com.llamalab.safs.channels.SeekableByteChannel;
import com.llamalab.safs.spi.FileSystemProvider;
import com.llamalab.safs.spi.FileTypeDetector;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

public final class Files {

  //private static final OpenOption[] COPY_REPLACE_EXISTING = { StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE };
  //private static final OpenOption[] COPY_KEEP_EXISTING = { StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW };
  private static final LinkOption[] LINK_NOFOLLOW_LINKS = { LinkOption.NOFOLLOW_LINKS };
  private static final Set<FileVisitOption> VISIT_EMPTY = EnumSet.noneOf(FileVisitOption.class);

  private static final String COPIED_ATTRIBUTES = BasicFileAttribute.lastModifiedTime + "," + BasicFileAttribute.lastAccessTime + "," + BasicFileAttribute.creationTime;

  private static final SecureRandom TEMP_RAND = new SecureRandom();

  private Files () {}

  public static FileStore getFileStore (Path path) throws IOException {
    return provider(path).getFileStore(path);
  }

  public static void delete (Path path) throws IOException {
    provider(path).delete(path);
  }

  private static void deleteQuietly (Path path) {
    try {
      delete(path);
    }
    catch (Throwable t) {
      // ignore
    }
  }
  public static boolean deleteIfExists (Path path) throws IOException {
    return provider(path).deleteIfExists(path);
  }

  public static Path createDirectory (Path dir, FileAttribute<?>... attrs) throws IOException {
    provider(dir).createDirectory(dir, attrs);
    return dir;
  }

  public static Path createDirectories (Path dir, FileAttribute<?>... attrs) throws IOException {
    final FileSystemProvider provider = provider(dir);
    try {
      provider.createDirectory(dir, attrs);
      return dir;
    }
    catch (FileAlreadyExistsException e) {
      if (Files.isDirectory(dir))
        return dir;
      throw e;
    }
    catch (Exception e) {
      // ignore
    }
    final Path absolute = dir.toAbsolutePath();
    Path parent = absolute.getParent();
    for (;; parent = parent.getParent()) {
      if (parent == null)
        throw new FileSystemException(dir.toString(), null, "No root directory");
      try {
        final BasicFileAttributes basic = provider.readAttributes(parent, BasicFileAttributes.class);
        if (!basic.isDirectory())
          throw new FileAlreadyExistsException(dir.toString());
        break;
      }
      catch (NoSuchFileException e) {
        // continue
      }
    }
    Path child = parent;
    for (final Path name : parent.relativize(absolute))
      provider.createDirectory(child = child.resolve(name));
    return dir;
  }

  public static Path createFile (Path path, FileAttribute<?>... attrs) throws IOException {
    try {
      newByteChannel(path, EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW), attrs).close();
    }
    catch (UnsupportedOperationException e) {
      if (attrs.length != 0)
        throw e;
      newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW).close();
    }
    return path;
  }

  private static Path makeTempPath (Path dir, String prefix, String suffix) {
    long n = TEMP_RAND.nextLong();
    if (n == Long.MIN_VALUE)
      n = 0;
    else if (n < 0)
      n *= -1;
    final Path name = dir.getFileSystem().getPath(prefix + Long.toString(n) + suffix);
    if (name.getParent() != null)
      throw new IllegalArgumentException("Invalid prefix or suffix");
    return dir.resolve(name);
  }

  public static Path createTempFile (Path dir, String prefix, String suffix, FileAttribute<?>... attrs) throws IOException {
    if (prefix == null)
      prefix = "";
    if (suffix == null)
      suffix = ".tmp";
    for (;;) {
      try {
        return createFile(makeTempPath(dir, prefix, suffix), attrs);
      }
      catch (FileAlreadyExistsException e) {
        // retry
      }
    }
  }

  public static Path createTempFile (String prefix, String suffix, FileAttribute<?>... attrs) throws IOException {
    final FileSystem fs = FileSystems.getDefault();
    if (!(fs instanceof DefaultFileSystem))
      throw new UnsupportedOperationException();
    return createTempFile(((DefaultFileSystem)fs).getCacheDirectory(), prefix, suffix, attrs);
  }

  public static Path createTempDirectory (Path dir, String prefix, FileAttribute<?>... attrs) throws IOException {
    if (prefix == null)
      prefix = "";
    for (;;) {
      try {
        return Files.createDirectory(makeTempPath(dir, prefix, ""), attrs);
      }
      catch (FileAlreadyExistsException e) {
        // retry
      }
    }
  }

  public static Path createTempDirectory (String prefix, FileAttribute<?>... attrs) throws IOException {
    final FileSystem fs = FileSystems.getDefault();
    if (!(fs instanceof DefaultFileSystem))
      throw new UnsupportedOperationException();
    return createTempDirectory(((DefaultFileSystem)fs).getCacheDirectory(), prefix, attrs);

  }

  public static byte[] readAllBytes (Path path) throws IOException {
    final InputStream in = newInputStream(path);
    try {
      final long size = size(path);
      if (size > Integer.MAX_VALUE)
        throw new OutOfMemoryError("Array size exceeded");
      return Utils.readAllBytes(in, (int)size);
    }
    finally {
      in.close();
    }
  }

  public static Path write (Path path, byte[] bytes, OpenOption... options) throws IOException {
    final OutputStream out = newOutputStream(path, options);
    try {
      out.write(bytes);
    }
    finally {
      out.close();
    }
    return path;
  }

  /**
   * Does not delete target upon failure.
   */
  public static long copy (InputStream in, Path target, CopyOption... options) throws IOException {
    if (in == null)
      throw new NullPointerException("in");
    for (final CopyOption option : options) {
      if (StandardCopyOption.REPLACE_EXISTING == option) {
        deleteIfExists(target);
        break;
      }
    }
    final OutputStream out = newOutputStream(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
    try {
      return Utils.transfer(in, out);
    }
    finally {
      out.close();
    }
  }

  public static long copy (Path source, OutputStream out) throws IOException {
    final InputStream in = newInputStream(source);
    try {
      return Utils.transfer(in, out);
    }
    finally {
      out.close();
    }
  }

  public static Path copy (Path source, Path target, CopyOption... options) throws IOException {
    return transfer(source, target, false, options);
  }

  public static Path move (Path source, Path target, CopyOption... options) throws IOException {
    return transfer(source, target, true, options);
  }

  private static Path transfer (Path source, Path target, boolean move, CopyOption[] options) throws IOException {
    final FileSystemProvider provider = provider(source);
    if (provider == provider(target)) {
      if (move)
        provider.move(source, target, options);
      else
        provider.copy(source, target, options);
      return target;
    }
    boolean replaceExisting = false;
    boolean copyAttributes = false;
    LinkOption[] linkOptions = Utils.EMPTY_LINK_OPTION_ARRAY;
    for (final CopyOption option : options) {
      if (StandardCopyOption.REPLACE_EXISTING == option)
        replaceExisting = true;
      else if (StandardCopyOption.COPY_ATTRIBUTES == option)
        copyAttributes = true;
      else if (LinkOption.NOFOLLOW_LINKS == option)
        linkOptions = LINK_NOFOLLOW_LINKS;
      else if (StandardCopyOption.ATOMIC_MOVE == option && move)
        throw new AtomicMoveNotSupportedException(source.toString(), target.toString(), "Different providers");
    }
    if (replaceExisting)
      deleteIfExists(target);
    if (isDirectory(source))
      createDirectory(target);
    else {
      final InputStream in = newInputStream(source);
      try {
        final OutputStream out = newOutputStream(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        try {
          Utils.transfer(in, out);
        }
        finally {
          out.close();
        }
      }
      finally {
        in.close();
      }
    }
    try {
      if (copyAttributes) {
        for (final Map.Entry<String, Object> attr : readAttributes(source, COPIED_ATTRIBUTES, linkOptions).entrySet()) {
          try {
            setAttribute(target, attr.getKey(), attr.getValue());
          }
          catch (UnsupportedOperationException e) {
            // suppressed
          }
        }
      }
      if (move)
        delete(source);
    }
    catch (IOException e) {
      deleteQuietly(target);
      throw e;
    }
    catch (RuntimeException e) {
      deleteQuietly(target);
      throw e;
    }
    return target;
  }

  public static SeekableByteChannel newByteChannel(Path path, OpenOption... options) throws IOException {
    return provider(path).newByteChannel(path, new SearchSet<OpenOption>(options));
  }

  public static SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
    return provider(path).newByteChannel(path, options, attrs);
  }

  public static InputStream newInputStream (Path path, OpenOption...options) throws IOException {
    return provider(path).newInputStream(path);
  }

  public static OutputStream newOutputStream (Path path, OpenOption...options) throws IOException {
    return provider(path).newOutputStream(path, options);
  }

  public static BufferedReader newBufferedReader (Path path) throws IOException {
    return newBufferedReader(path, Utils.UTF_8);
  }

  public static BufferedReader newBufferedReader (Path path, Charset charset) throws IOException {
    return new BufferedReader(new InputStreamReader(newInputStream(path), charset));
  }

  public static BufferedWriter newBufferedWriter (Path path, OpenOption... options) throws IOException {
    return newBufferedWriter(path, Utils.UTF_8, options);
  }

  public static BufferedWriter newBufferedWriter (Path path, Charset charset, OpenOption... options) throws IOException {
    return new BufferedWriter(new OutputStreamWriter(newOutputStream(path, options), charset));
  }

  public static boolean exists (Path path, LinkOption... options) {
    try {
      readAttributes(path, BasicFileAttributes.class, options);
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }

  public static boolean notExists (Path path, LinkOption... options) {
    try {
      readAttributes(path, BasicFileAttributes.class, options);
      return false;
    }
    catch (NoSuchFileException e) {
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }

  public static boolean isSameFile (Path path1, Path path2) throws IOException {
    final FileSystem fs = path1.getFileSystem();
    return fs.equals(path2.getFileSystem())
        && fs.provider().isSameFile(path1, path2);
  }

  public static boolean isHidden (Path path) throws IOException {
    return provider(path).isHidden(path);
  }

  public static boolean isDirectory (Path path, LinkOption... options) {
    try {
      return readAttributes(path, BasicFileAttributes.class, options).isDirectory();
    }
    catch (IOException e) {
      return false;
    }
  }

  public static boolean isRegularFile (Path path, LinkOption... options) {
    try {
      return readAttributes(path, BasicFileAttributes.class, options).isRegularFile();
    }
    catch (IOException e) {
      return false;
    }
  }

  public static boolean isSymbolicLink (Path path) {
    try {
      readSymbolicLink(path);
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }

  public static long size (Path path) throws IOException {
    return readAttributes(path, BasicFileAttributes.class).size();
  }

  public static FileTime getLastModifiedTime (Path path, LinkOption... options) throws IOException {
    return readAttributes(path, BasicFileAttributes.class, options).lastModifiedTime();
  }

  public static Path setLastModifiedTime (Path path, FileTime time) throws IOException {
    return setAttribute(path, BasicFileAttribute.lastModifiedTime.toString(), time);
  }

  public static Path readSymbolicLink (Path link) throws IOException {
    return provider(link).readSymbolicLink(link);
  }

  public static <A extends BasicFileAttributes> A readAttributes (Path path, Class<A> type, LinkOption... options) throws IOException {
    return provider(path).readAttributes(path, type, options);
  }

  public static Map<String,Object> readAttributes (Path path, String attributes, LinkOption... options) throws IOException {
    return provider(path).readAttributes(path, attributes, options);
  }

  public static Path setAttribute (Path path, String attribute, Object value, LinkOption... options) throws IOException {
    provider(path).setAttribute(path, attribute, value, options);
    return path;
  }

  public static <V extends FileAttributeView> V getFileAttributeView (Path path, Class<V> type, LinkOption... options) {
    return provider(path).getFileAttributeView(path, type, options);
  }

  public static String probeContentType (Path path) throws IOException {
    for (final FileTypeDetector detector : InstalledFileTypeDetectorsHolder.detectors) {
      final String type = detector.probeContentType(path);
      if (type != null)
        return type;
    }
    return null;
  }

  public static DirectoryStream<Path> newDirectoryStream (Path dir) throws IOException{
    return newDirectoryStream(dir, Utils.ACCEPT_ALL_FILTER);
  }

  public static DirectoryStream<Path> newDirectoryStream (Path dir, String glob) throws IOException {
    if ("*".equals(glob))
      return newDirectoryStream(dir);
    final PathMatcher matcher = dir.getFileSystem().getPathMatcher("glob:"+glob);
    //noinspection RedundantThrows
    return newDirectoryStream(dir, new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept (Path entry) throws IOException {
        return matcher.matches(entry.getFileName());
      }
    });
  }

  public static DirectoryStream<Path> newDirectoryStream (Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException{
    return provider(dir).newDirectoryStream(dir, filter);
  }

  public static Path walkFileTree (Path start, FileVisitor<? super Path> visitor) throws IOException {
    return walkFileTree(start, VISIT_EMPTY, Integer.MAX_VALUE, visitor);
  }

  @SuppressWarnings("ConstantConditions")
  public static Path walkFileTree (Path start, Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor) throws IOException {
    final boolean followLinks = options.contains(FileVisitOption.FOLLOW_LINKS);
    final LinkOption[] linkOptions = followLinks ? Utils.EMPTY_LINK_OPTION_ARRAY : LINK_NOFOLLOW_LINKS;
    WalkDirectory dir = null;
    try {
      Path path = start;
      FileVisitResult result;
      int depth = 0;
      walk: do {

        // iterate
        while (dir != null) {
          IOException cause = null;
          try {
            if (dir.iterator.hasNext()) {
              path = dir.iterator.next();
              break;
            }
          }
          catch (DirectoryIteratorException e) {
            cause = e.getCause();
          }
          if (FileVisitResult.TERMINATE == visitor.postVisitDirectory(dir.path, cause) || --depth <= 0)
            break walk;
          Utils.closeQuietly(dir.stream);
          dir = dir.parent;
        }

        // descend
        DirectoryStream<Path> stream = null;
        BasicFileAttributes attrs = null;
        try {
          attrs = readAttributes(path, BasicFileAttributes.class, linkOptions);
          if (attrs.isDirectory() && depth < maxDepth) {
            if (followLinks) {
              // check for recursion
              for (WalkDirectory ancestor = dir; ancestor != null; ancestor = ancestor.parent) {
                if (ancestor.isSameFile(path, attrs.fileKey()))
                  throw new FileSystemLoopException(path.toString());
              }
            }
            stream = Files.newDirectoryStream(path);
          }
        }
        catch (NotDirectoryException e) {
          // visitFile below
        }
        catch (IOException e) {
          result = visitor.visitFileFailed(path, e);
          if (FileVisitResult.SKIP_SIBLINGS == result && dir != null)
            dir.iterator = Utils.emptyIterator();
          continue;
        }
        if (stream != null) {
          result = visitor.preVisitDirectory(path, attrs);
          if (FileVisitResult.CONTINUE == result) {
            dir = new WalkDirectory(dir, path, attrs.fileKey(), stream);
            ++depth;
          }
          else
            Utils.closeQuietly(stream);
        }
        else {
          result = visitor.visitFile(path, attrs);
          if (FileVisitResult.SKIP_SIBLINGS == result && dir != null)
            dir.iterator = Utils.emptyIterator();
        }
      } while (FileVisitResult.TERMINATE != result && depth > 0);
    }
    finally {
      for (; dir != null; dir = dir.parent)
        Utils.closeQuietly(dir.stream);
    }
    return start;
  }


    public static File[] walk(@Nullable Path path) {
        return path.toFile().listFiles();
    }


    private static final class WalkDirectory {

    public final WalkDirectory parent;
    public final Path path;
    public final Object key;
    public final DirectoryStream<Path> stream;
    public Iterator<Path> iterator;

    public WalkDirectory (WalkDirectory parent, Path path, Object key, DirectoryStream<Path> stream) {
      this.parent = parent;
      this.path = path;
      this.key = key;
      this.stream = stream;
      this.iterator = stream.iterator();
    }

    public boolean isSameFile (Path path, Object key) {
      if (key != null && this.key != null)
        return key.equals(this.key);
      try {
        return Files.isSameFile(path, this.path);
      }
      catch (IOException e) {
        return false;
      }
    }

  } // class WalkDirectory


  private static FileSystemProvider provider (Path path) {
    return path.getFileSystem().provider();
  }


  private static final class InstalledFileTypeDetectorsHolder {

    private static final List<FileTypeDetector> detectors = loadInstalledProviders();

    private static List<FileTypeDetector> loadInstalledProviders () {
      final List<FileTypeDetector> detectors = new ArrayList<FileTypeDetector>();
      for (final FileTypeDetector detector : ServiceLoader.load(FileTypeDetector.class, FileTypeDetector.class.getClassLoader()))
        detectors.add(detector);
      //noinspection RedundantThrows
      detectors.add(new FileTypeDetector() {
        private final boolean apacheHarmony = isApacheHarmony();
        @Override
        public String probeContentType (Path path) throws IOException {
          // TODO: Use URLConnection.guessContentTypeFromStream()
          String filename = path.toString();
          // BUG: https://code.google.com/p/android/issues/detail?id=162883
          // https://android.googlesource.com/platform/libcore/+/marshmallow-release/luni/src/main/java/java/net/DefaultFileNameMap.java
          if (apacheHarmony)
            filename = filename.replace('#', '_');
          return URLConnection.guessContentTypeFromName(filename);
        }
      });
      return detectors;
    }

    private static boolean isApacheHarmony () {
      try {
        Class.forName("libcore.net.MimeUtils", false, InstalledFileTypeDetectorsHolder.class.getClassLoader());
        return true;
      }
      catch (ClassNotFoundException e) {
        return false;
      }
    }

  } // class InstalledFileTypeDetectorsHolder

}
