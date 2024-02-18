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

import com.llamalab.safs.LinkOption;
import com.llamalab.safs.Path;
import com.llamalab.safs.ProviderMismatchException;
import com.llamalab.safs.WatchEvent;
import com.llamalab.safs.WatchKey;
import com.llamalab.safs.WatchService;
import com.llamalab.safs.internal.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class UnixPath implements Path {

  private final AbstractUnixFileSystem fs;
  private final String path;
  private volatile short[] nameOffsets; // lazy

  protected UnixPath (AbstractUnixFileSystem fs, String path) {
    this.fs = fs;
    this.path = path;
  }

  protected UnixPath (UnixPath other) {
    this(other.fs, other.path);
    this.nameOffsets = other.nameOffsets;
  }

  @Override
  public final AbstractUnixFileSystem getFileSystem () {
    return fs;
  }

  @Override
  public final String toString () {
    return path;
  }

  @Override
  public final int hashCode () {
    int hc = 17;
    hc = 37*hc + fs.hashCode();
    hc = 37*hc + path.hashCode();
    return hc;
  }

  @Override
  public boolean equals (Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof UnixPath))
      return false;
    final UnixPath other = (UnixPath)obj;
    return fs.equals(other.fs)
        && path.equals(other.path);
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public int compareTo (Path other) {
    return path.compareTo(other.toString());
  }

  @Override
  public final boolean isAbsolute () {
    return path.startsWith("/");
  }

  public final boolean isHidden () {
    return path.startsWith(".");
  }

  public final boolean isEmpty () {
    return path.isEmpty();
  }

  public final boolean isRoot () {
    return path.equals("/");
  }

  /**
   * @return true if . or ..
   */
  public final boolean isDots () {
    return ".".equals(path) || "..".equals(path);
  }

  private static final short[] ZERO_NAME_OFFSETS = new short[] { 0 };
  private static final short[] EMPTY_NAME_OFFSETS = new short[0];

  private short[] getNameOffsets () {
    if (nameOffsets != null)
      return nameOffsets;
    final String path = this.path;
    final int length = path.length();
    if (length == 0)
      return nameOffsets = ZERO_NAME_OFFSETS;
    int count = 0;
    int index = 0;
    while (index < length) {
      if ('/' != path.charAt(index)) {
        if (index++ > 0xFFFF)
          throw new ArrayStoreException();
        ++count;
        while (index < length && '/' != path.charAt(index))
          ++index;
      }
      else
        ++index;
    }
    if (count == 0)
      return nameOffsets = EMPTY_NAME_OFFSETS;
    final short[] offsets = new short[count];
    index = 0;
    count = 0;
    while (index < length) {
      if ('/' != path.charAt(index)) {
        offsets[count++] = (short)index++;
        while (index < length && '/' != path.charAt(index))
          ++index;
      }
      else
        ++index;
    }
    return nameOffsets = offsets;
  }

  @Override
  public Path getRoot () {
    return isAbsolute() ? fs.getRootDirectory() : null;
  }

  @Override
  public Path getParent () {
    final short[] offsets = getNameOffsets();
    final int count = offsets.length;
    if (count == 0)
      return null;
    final int end = (offsets[count - 1] & 0xFFFF) - 1;
    if (end <= 0)
      return getRoot();
    return fs.getPathSanitized(path.substring(0, end));
  }

  /*
  public final Path getAncestor (int endIndex) {
    final short[] offsets = getNameOffsets();
    final int count = offsets.length;
    if (endIndex < 0 || count < endIndex)
      throw new IllegalArgumentException();
    if (count == 0)
      return null;
    if (endIndex == count)
      return this;
    final int end = (offsets[endIndex] & 0xFFFF) - 1;
    if (end <= 0)
      return getRoot();
    return fs.getPathSanitized(path.substring(0, end));
  }
  */

  @Override
  public Path getFileName () {
    final short[] offsets = getNameOffsets();
    final int count = offsets.length;
    if (count == 0)
      return null;
    if (count == 1 && path.length() != 0 && '/' != path.charAt(0))
      return this;
    return fs.getPathSanitized(path.substring(offsets[count - 1] & 0xFFFF));
  }

  public final int getNameCount () {
    return getNameOffsets().length;
  }

  public Path getName (int index) {
    return subpath(index, index + 1);
  }

  public Path subpath (int beginIndex, int endIndex) {
    final short[] offsets = getNameOffsets();
    final int count = offsets.length;
    if (beginIndex < 0 || count < endIndex || endIndex <= beginIndex)
      throw new IllegalArgumentException();
    if (endIndex < count)
      return fs.getPathSanitized(path.substring(offsets[beginIndex] & 0xFFFF, (offsets[endIndex] & 0xFFFF) - 1));
    else
      return fs.getPathSanitized(path.substring(offsets[beginIndex] & 0xFFFF));
  }

  @Override
  public final boolean startsWith (Path other) {
    return fs.equals(other.getFileSystem())
        && startsWithSanitized(((UnixPath)other).path);
  }

  @Override
  public final boolean startsWith (String other) {
    return startsWithSanitized(sanitize(other, Utils.EMPTY_STRING_ARRAY));
  }

  private boolean startsWithSanitized (String other) {
    if (!path.startsWith(other))
      return false;
    final int end = other.length();
    return end == path.length() || '/' == path.charAt(end) || "/".equals(other);
  }

  public final boolean endsWith (Path other) {
    return fs.equals(other.getFileSystem())
        && endsWithSanitized(((UnixPath)other).path);
  }

  public final boolean endsWith (String other) {
    return endsWithSanitized(sanitize(other, Utils.EMPTY_STRING_ARRAY));
  }

  private boolean endsWithSanitized (String other) {
    if (!path.endsWith(other))
      return false;
    final int start = path.length() - other.length();
    return start == 0 || '/' == path.charAt(start - 1);
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public Iterator<Path> iterator () {
    final short[] offsets = getNameOffsets();
    final int count = offsets.length;
    if (count == 0)
      return Utils.emptyIterator();
    return new NameIterator<Path>(path, offsets, count) {
      @Override
      protected Path next (String path, int start, int end) {
        return fs.getPathSanitized(path.substring(start, end));
      }
    };
  }

  public final Iterator<CharSequence> charSequenceIterator () {
    final short[] offsets = getNameOffsets();
    final int count = offsets.length;
    if (count == 0)
      return Utils.emptyIterator();
    return new NameIterator<CharSequence>(path, offsets, count) {
      @Override
      protected CharSequence next (String path, int start, int end) {
        return path.subSequence(start, end);
      }
    };
  }

  public final Iterator<String> stringIterator () {
    final short[] offsets = getNameOffsets();
    final int count = offsets.length;
    if (count == 0)
      return Utils.emptyIterator();
    return new NameIterator<String>(path, offsets, count) {
      @Override
      protected String next (String path, int start, int end) {
        return path.substring(start, end);
      }
    };
  }

  // FIXME: horrid
  @Override
  public Path normalize () {
    CharSequence[] names = Utils.EMPTY_CHAR_SEQUENCE_ARRAY;
    int end = 0;
    for (Iterator<CharSequence> i = charSequenceIterator(); i.hasNext();) {
      final CharSequence n = i.next();
      if ("..".contentEquals(n) && end > 0)
        --end;
      else if (!".".contentEquals(n)){
        if (names.length == end)
          names = Arrays.copyOf(names, Math.max(end*2, 8));
        names[end++] = n;
      }
    }
    if (end == 0)
      return isAbsolute() ? fs.getRootDirectory() : fs.getEmptyDirectory();
    return fs.getPath(join(names, 0, end, isAbsolute() ? "/" : ""));
  }

  // FIXME: horrid
  @Override
  public Path relativize (Path other) {
    checkPath(other);
    if (isAbsolute() != other.isAbsolute())
      throw new IllegalArgumentException("Absolute vs relative");
    //if (equals(that))
    //  return fs.getEmptyDirectory();
    final Iterator<CharSequence> bi = charSequenceIterator();
    final Iterator<CharSequence> ci = ((UnixPath)other).charSequenceIterator();
    final RelativizeHelper h = new RelativizeHelper();
    CharSequence bn, cn = null;
    out: while (bi.hasNext() && ci.hasNext()) {
      while (".".contentEquals(bn = bi.next()))
        if (!bi.hasNext()) break out;
      while (".".contentEquals(cn = ci.next()))
        if (!ci.hasNext()) break out;
      if (!Utils.contentEquals(bn, cn)) {
        h.base(bn);
        break;
      }
      h.add(bn);
      ++h.start;
      cn = null;
    }
    while (bi.hasNext())
      if (!".".contentEquals(bn = bi.next())) h.base(bn);
    if (cn != null && !".".contentEquals(cn))
      h.child(cn);
    while (ci.hasNext())
      if (!".".contentEquals(cn = ci.next())) h.child(cn);
    return fs.getPathSanitized(join(h.names, h.start, h.end, ""));
  }

  private static final class RelativizeHelper {
    public CharSequence[] names = new CharSequence[8];
    public int start = 0, end = 0;
    public void base (CharSequence value) {
      if ("..".contentEquals(value) && start > 0)
        --start;
      else
        add("..");
    }
    public void child (CharSequence value) {
      if ("..".contentEquals(value) && start < end && !"..".contentEquals(names[end - 1]))
        --end;
      else
        add(value);
    }
    public void add (CharSequence value) {
      if (names.length == end)
        names = Arrays.copyOf(names, end*2);
      names[end++] = value;
    }
  } // class RelativizeHelper

  @Override
  public Path resolve (Path other) {
    checkPath(other);
    if (other.isAbsolute())
      return other;
    if (((UnixPath)other).isEmpty())
      return this;
    return fs.getPath(path, ((UnixPath)other).path);
  }

  @Override
  public Path resolve (String other) {
    if (other.startsWith("/"))
      return fs.getPath(other);
    if (other.isEmpty())
      return this;
    return fs.getPath(path, other);
  }

  public Path resolveSibling (Path other) {
    checkPath(other);
    final Path parent = getParent();
    return (parent != null) ? parent.resolve(other) : other;
  }

  public Path resolveSibling (String other) {
    final Path parent = getParent();
    return (parent != null) ? parent.resolve(other) : fs.getPath(other);
  }

  @Override
  public Path toAbsolutePath () {
    return isAbsolute() ? this : fs.getCurrentDirectory().resolve(this);
  }

  @Override
  public Path toRealPath (LinkOption... options) throws IOException {
    return fs.toRealPath(this, options);
  }

  @Override
  public File toFile () {
    return new File(path);
  }

  @Override
  public URI toUri () {
    try {
      return new URI(fs.provider().getScheme(), toAbsolutePath().toString(), null);
    }
    catch (Throwable t) {
      throw new IllegalStateException(t); // shouldn't happen
    }
  }

  private static final WatchEvent.Modifier[] EMPTY_MODIFIER_ARRAY = new WatchEvent.Modifier[0];

  @Override
  public final WatchKey register (WatchService service, WatchEvent.Kind<?>... kinds) throws IOException {
    return register(service, kinds, EMPTY_MODIFIER_ARRAY);
  }

  @Override
  public WatchKey register (WatchService service, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
    throw new UnsupportedOperationException();
  }

  private static void checkPath (Path path) {
    if (!(path instanceof UnixPath))
      throw (path == null) ? new NullPointerException() : new ProviderMismatchException();
  }

  /*
  private static String sanitize (String path) {
    return sanitize(path, Utils.EMPTY_STRING_ARRAY);
  }
  */

  /**
   * Removes double and tail slash
   */
  static String sanitize (String first, String[] more) {
    CharSequence parent = "";
    for (int i = -1, count = more.length;;) {
      parent = sanitizeChunk(parent, first);
      if (++i == count)
        break;
      first = more[i];
    }
    return parent.toString();
  }

  private static CharSequence sanitizeChunk (CharSequence parent, String path) {
    int end = path.length();
    if (end == 0)
      return parent; // empty
    while (end > 0 && '/' == path.charAt(end - 1)) --end;
    if (end == 0)
      return (parent.length() == 0) ? "/" : parent; // root
    // At this point it's neither empty nor root.
    int start = 0;
    while (start < end && '/' == path.charAt(start)) ++start;
    final int parentLength = parent.length();
    int index = path.indexOf("//", start + 1);
    if (index == -1 || index >= end) {
      // no //
      if (parentLength == 0) {
        if (start > 0)
          --start; // keep initial /
        return path.substring(start, end);
      }
      if (parentLength == 1 && '/' == parent.charAt(0)) {
        // root parent
        if (start > 0)
          return path.substring(start - 1, end);
        else
          return new StringBuilder("/").append(path, start, end);
      }
      final StringBuilder sb = (parent instanceof StringBuilder) ? (StringBuilder)parent : new StringBuilder(parent);
      return sb.append('/').append(path, start, end);
    }
    // found //
    final StringBuilder sb = (parent instanceof StringBuilder) ? (StringBuilder)parent : new StringBuilder(parent);
    if (parentLength == 0) {
      if (start > 0)
        --start; // keep initial /
    }
    else if (parentLength != 1 || '/' != parent.charAt(0)) {
      // not root parent
      sb.append('/');
    }
    while (++index < end) {
      sb.append(path, start, index);
      while (index < end && '/' == path.charAt(index)) ++index;
      start = index;
    }
    return sb.append(path, start, index);
  }

  private static String join (CharSequence[] cs, int start, int end, String prefix) {
    final StringBuilder sb = new StringBuilder();
    while (start < end) {
      sb.append(prefix).append(cs[start++]);
      prefix = "/";
    }
    return sb.toString();
  }

  private static abstract class NameIterator<T> implements Iterator<T> {

    private final String path;
    private final short[] offsets;
    private final int end;
    private int index;

    public NameIterator (String path, short[] offsets, int count) {
      this.path = path;
      this.offsets = offsets;
      this.end = count - 1;
    }

    @Override
    public final boolean hasNext () {
      return index <= end;
    }

    @Override
    public final T next () {
      final int i = index;
      if (i > end)
        throw new NoSuchElementException();
      ++index;
      return next(
          path,
          offsets[i] & 0xFFFF,
          (i < end) ? (offsets[i + 1] & 0xFFFF) - 1 : path.length());
    }

    @Override
    public final void remove () {
      throw new UnsupportedOperationException();
    }

    protected abstract T next (String path, int start, int end);

  } // class NameIterator

}
