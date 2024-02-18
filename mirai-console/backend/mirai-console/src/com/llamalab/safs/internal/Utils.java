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

package com.llamalab.safs.internal;

import com.llamalab.safs.CopyOption;
import com.llamalab.safs.DirectoryStream;
import com.llamalab.safs.FileVisitOption;
import com.llamalab.safs.FileVisitResult;
import com.llamalab.safs.FileVisitor;
import com.llamalab.safs.Files;
import com.llamalab.safs.LinkOption;
import com.llamalab.safs.OpenOption;
import com.llamalab.safs.Path;
import com.llamalab.safs.SimpleFileVisitor;
import com.llamalab.safs.attribute.BasicFileAttributes;
import com.llamalab.safs.attribute.FileTime;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@SuppressWarnings("unused")
public final class Utils {

  private static final int BUFFER_SIZE = 8192;

  public static final Charset UTF_8 = Charset.forName("utf-8");
  public static final Charset US_ASCII = Charset.forName("US-ASCII");

  public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
  public static final FileTime ZERO_TIME = FileTime.fromMillis(0);

  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  public static final CharSequence[] EMPTY_CHAR_SEQUENCE_ARRAY = new CharSequence[0];
  public static final CopyOption[] EMPTY_COPY_OPTION_ARRAY = new CopyOption[0];
  public static final LinkOption[] EMPTY_LINK_OPTION_ARRAY = new LinkOption[0];
  public static final FileVisitOption[] EMPTY_FILE_VISIT_OPTION_ARRAY = new FileVisitOption[0];
  public static final OpenOption[] EMPTY_OPEN_OPTION_ARRAY = new OpenOption[0];

  public static final DirectoryStream.Filter<Path> ACCEPT_ALL_FILTER = new DirectoryStream.Filter<Path> () {
    @Override
    public boolean accept (Path entry) throws IOException {
      return true;
    }
  };

  public static final FileVisitor<Path> DELETE_FILE_VISITOR = new SimpleFileVisitor<Path>() {

    @Override
    public FileVisitResult postVisitDirectory (Path dir, IOException e) throws IOException {
      if (e != null)
        throw e;
      Files.delete(dir);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {
      Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed (Path file, IOException e) throws IOException {
      return FileVisitResult.CONTINUE;
    }

  };

  private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object> () {
    @Override
    public boolean hasNext () {
      return false;
    }
    @Override
    public Object next () {
      return null;
    }
    @Override
    public void remove () {
      throw new UnsupportedOperationException();
    }
  };

  private Utils () {}

  public static boolean equals (Object a, Object b) {
    return a != null ? a.equals(b) : b == null;
  }

  public static boolean contentEquals (CharSequence cs1, CharSequence cs2) {
    int l = cs1.length();
    if (l != cs2.length())
      return false;
    for (int i = 0; --l >= 0; ++i) {
      if (cs1.charAt(i) != cs2.charAt(i))
        return false;
    }
    return true;
  }

  public static boolean contentEqualsIgnoreCase (CharSequence cs1, CharSequence cs2) {
    int l = cs1.length();
    if (l != cs2.length())
      return false;
    for (int i = 0; --l >= 0; ++i) {
      char c1 = cs1.charAt(i);
      char c2 = cs2.charAt(i);
      if (c1 != c2) {
        c1 = Character.toUpperCase(c1);
        c2 = Character.toUpperCase(c2);
        if (c1 != c2) {
          c1 = Character.toLowerCase(c1);
          c2 = Character.toLowerCase(c2);
          if (c1 != c2)
            return false;
        }
      }
    }
    return true;
  }

  /**
   * Available in Java 1.8
   * @see java.lang.String#join(CharSequence, Iterable)
   */
  public static String join (CharSequence delimiter, Iterable<? extends CharSequence> elements) {
    final StringBuilder sb = new StringBuilder();
    CharSequence d = "";
    for (final CharSequence element : elements) {
      sb.append(d).append(element);
      d = delimiter;
    }
    return sb.toString();
  }

  /**
   * Available in Java 1.7 (Android 4.4)
   * @see java.util.Collections#emptyIterator()
   */
  @SuppressWarnings("unchecked")
  public static <E> Iterator<E> emptyIterator () {
    return (Iterator<E>)EMPTY_ITERATOR;
  }

  public static <E> Iterator<E> singletonIterator (final E element) {
    return new Iterator<E>() {
      private boolean started;
      @Override
      public boolean hasNext () {
        return !started;
      }
      @Override
      public E next () {
        if (started)
          throw new NoSuchElementException();
        started = true;
        return element;
      }
      @Override
      public void remove () {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static <T> DirectoryStream<T> singletonDirectoryStream (final T entry) {
    return new AbstractDirectoryStream<T>() {
      private boolean started;
      @Override
      protected T advance () throws IOException {
        if (started)
          return null;
        started = true;
        return entry;
      }
    };
  }


  public static <T> List<T> listOf (Iterator<T> i) {
    final List<T> list = new ArrayList<T>();
    while (i.hasNext())
      list.add(i.next());
    return list;
  }

  public static <T> List<T> listOf (Iterable<T> i) {
    return listOf(i.iterator());
  }


  public static void closeQuietly (Closeable c) {
    try {
      c.close();
    }
    catch (Throwable t) {
      // ignore
    }
  }

  /*
  public static void unmap (MappedByteBuffer buf) {
    try {
      final Object cleaner = buf.getClass().getMethod("cleaner").invoke(buf);
      cleaner.getClass().getMethod("clean").invoke(cleaner);
    }
    catch (Throwable t) {
      // ignore
    }
  }
  */


  public static long transfer (ReadableByteChannel in, WritableByteChannel out) throws IOException {
    if (in instanceof FileChannel)
      return transfer((FileChannel)in, out);
    else
      return transfer(in, out, ByteBuffer.allocate(BUFFER_SIZE));
  }

  public static long transfer (ReadableByteChannel in, WritableByteChannel out, ByteBuffer buf) throws IOException {
    long written = 0;
    while (in.read(buf) != -1) {
      buf.flip();
      while (buf.hasRemaining())
        written += out.write(buf);
      buf.clear();
    }
    return written;
  }

  public static long transfer (FileChannel in, WritableByteChannel out) throws IOException {
    long remaining = in.size();
    long written = 0;
    long b;
    while (remaining > 0) {
      b = in.transferTo(written, remaining, out);
      remaining -= b;
      written   += b;
    }
    return written;
  }

  public static long transfer (InputStream in, OutputStream out) throws IOException {
    return transfer(in, out, new byte[BUFFER_SIZE]);
  }

  public static long transfer (InputStream in, OutputStream out, byte[] buf) throws IOException {
    long written = 0;
    int b;
    while ((b = in.read(buf)) != -1) {
      out.write(buf, 0, b);
      written += b;
    }
    return written;
  }

  public static byte[] readAllBytes (InputStream in, int capacity) throws IOException {
    byte[] data = new byte[capacity];
    int size = 0;
    for (int b;;) {
      while ((b = in.read(data, size, capacity - size)) > 0)
        size += b;
      if (b < 0 || (b = in.read()) < 0)
        break;
      if (capacity == Integer.MAX_VALUE)
        throw new OutOfMemoryError("Array size exceeded");
      capacity = (int)Math.min(Math.max(capacity * 2L, BUFFER_SIZE), Integer.MAX_VALUE);
      data = Arrays.copyOf(data, capacity);
      data[size++] = (byte)b;
    }
    return (size == data.length) ? data : Arrays.copyOf(data, size);
  }


  // http://stackoverflow.com/a/522281/445360
  private static final Pattern RFC3339 = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d+))?(?:Z|([+-]\\d{2}:\\d{2}))");
  public static long parseRfc3339 (CharSequence text) {
    final Matcher m = RFC3339.matcher(text);
    if (!m.matches())
      throw new IllegalArgumentException();
    String g = m.group(8);
    final GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone((g != null) ? g : "UTC"), Locale.US);
    //noinspection MagicConstant
    gc.set(
        Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)) - 1, Integer.parseInt(m.group(3)),
        Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)), Integer.parseInt(m.group(6)));
    g = m.group(7);
    gc.set(Calendar.MILLISECOND, (g != null) ? Integer.parseInt(g)%1000 : 0);
    return gc.getTimeInMillis();
  }

  public static String formatRfc3339 (long millis) {
    final GregorianCalendar gc = new GregorianCalendar(Utils.UTC, Locale.US);
    gc.setTimeInMillis(millis);
    //noinspection MagicConstant
    return String.format(Locale.US, (gc.get(Calendar.MILLISECOND) != 0) ? "%1$tFT%1$tT.%1$tLZ" : "%1$tFT%1$tTZ", gc);
  }

  /*
  public static String formatRfc3339 (long time) {
    final GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
    gc.setTimeInMillis(time);
    return String.format(Locale.US, "%04d-%02d-%02dT%02d:%02d:%02d.%dZ",
        gc.get(Calendar.YEAR), gc.get(Calendar.MONTH) + 1, gc.get(Calendar.DAY_OF_MONTH),
        gc.get(Calendar.HOUR_OF_DAY), gc.get(Calendar.MINUTE), gc.get(Calendar.SECOND), gc.get(Calendar.MILLISECOND));
  }
  */

  public static FileTime parseFileTime (CharSequence text) {
    return FileTime.fromMillis(parseRfc3339(text));
  }

  public static String getFileExtension (Path path) {
    final Path fileName = path.getFileName();
    if (fileName != null) {
      final String name = fileName.toString();
      final int i = name.lastIndexOf('.');
      if (i > 0 && i < name.length() - 1)
        return name.substring(i + 1).toLowerCase(Locale.US);
    }
    return null;
  }


  /** Must be sorted! */
  private static final char[] REGEX_ESCAPEES = { '$', '(', ')', '*', '+', '.', '?', '[', '\\', ']', '^', '{', '|' };

  /**
   * Only works with unix path separator (/).
   */
  public static String globToRegex (String glob, int s, int e) {
    final StringBuilder regex = new StringBuilder("^");
    int g = -1;
    char c;
    while (s < e) {
      switch (c = glob.charAt(s++)) {

        case '?':
          regex.append("[^/]");
          break;

        case '*':
          if (s < e && '*' == glob.charAt(s)) {
            regex.append(".*");
            ++s;
          }
          else
            regex.append("[^/]*");
          break;

        case '[':
          regex.append("[[");
          c = glob.charAt(checkGlobEnd(glob, s++, e, "Invalid class"));
          if ('^' == c) {
            regex.append("\\^");
            c = glob.charAt(checkGlobEnd(glob, s++, e, "Invalid class"));
          }
          else {
            if ('!' == c) {
              regex.append('^');
              c = glob.charAt(checkGlobEnd(glob, s++, e, "Invalid class"));
            }
            if ('-' == c) {
              regex.append('-');
              c = glob.charAt(checkGlobEnd(glob, s++, e, "Invalid class"));
            }
          }
          int l = Integer.MAX_VALUE;
          while (']' != c) {
            if ('/' == c)
              throw new PatternSyntaxException("Invalid class character: /", glob, s - 1);
            regex.append(c);
            if ('-' == c) {
              c = glob.charAt(checkGlobEnd(glob, s++, e, "Invalid range"));
              if (c < l)
                throw new PatternSyntaxException("Invalid range", glob, s - 3);
              l = Integer.MAX_VALUE;
              continue;
            }
            else
              l = c;
            c = glob.charAt(checkGlobEnd(glob, s++, e, "Invalid range"));
          }
          regex.append("]&&[^/]]");
          break;

        case '{':
          if (g != -1)
            throw new PatternSyntaxException("Nested group", glob, s - 1);
          regex.append("(?:");
          g = s;
          break;
        case ',':
          regex.append((g != -1) ? '|' : ',');
          break;
        case '}':
          regex.append((g != -1) ? ')' : '}');
          g = -1;
          break;

        case '\\':
          c = glob.charAt(checkGlobEnd(glob, s++, e, "Invalid escape"));
          // fall thu
        default:
          if (Arrays.binarySearch(REGEX_ESCAPEES, c) >= 0)
            regex.append("\\");
          regex.append(c);
      }
    }
    if (g != -1)
      throw new PatternSyntaxException("Invalid group", glob, g - 1);
    return regex.append('$').toString();
  }

  private static int checkGlobEnd (String glob, int s, int e, String message) {
    if (s == e)
      throw new PatternSyntaxException(message, glob, s - 1);
    return s;
  }

}
