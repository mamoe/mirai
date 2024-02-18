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

import com.llamalab.safs.LinkOption;
import com.llamalab.safs.NoSuchFileException;
import com.llamalab.safs.OpenOption;
import com.llamalab.safs.Path;
import com.llamalab.safs.ProviderMismatchException;
import com.llamalab.safs.StandardOpenOption;
import com.llamalab.safs.attribute.BasicFileAttributeView;
import com.llamalab.safs.attribute.BasicFileAttributes;
import com.llamalab.safs.attribute.FileAttribute;
import com.llamalab.safs.attribute.FileAttributeView;
import com.llamalab.safs.attribute.FileTime;
import com.llamalab.safs.spi.FileSystemProvider;
import com.llamalab.safs.unix.UnixPath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Only support {@link UnixPath}.
 */
public abstract class AbstractFileSystemProvider extends FileSystemProvider {

  protected static final Set<? extends OpenOption> DEFAULT_NEW_INPUT_STREAM_OPTIONS = EnumSet.of(StandardOpenOption.READ);
  protected static final Set<? extends OpenOption> DEFAULT_NEW_OUTPUT_STREAM_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

  protected abstract Class<? extends Path> getPathType ();

  protected void checkPath (Path path) {
    if (!getPathType().isInstance(path))
      throw (path == null) ? new NullPointerException() : new ProviderMismatchException();
    if (path.getFileSystem().provider() != this)
      throw new ProviderMismatchException();
  }

  protected void checkUri (URI uri) {
    if (!getScheme().equalsIgnoreCase(uri.getScheme()))
      throw new ProviderMismatchException();
  }

  protected IOException toProperException (IOException ioe, String file, String otherFile) {
    return (ioe instanceof FileNotFoundException) ? new NoSuchFileException(file) : ioe;
  }

  @Override
  public Map<String, Object> readAttributes (Path path, String attributes, LinkOption... options) throws IOException {
    final Map<String, Object> map = new HashMap<String, Object>();
    BasicFileAttributes basic = null;
    for (final BasicFileAttribute attribute : BasicFileAttribute.parse(attributes)) {
      if (basic == null)
        basic = readAttributes(path, BasicFileAttributes.class, options); // read once
      map.put(attribute.toString(), attribute.valueOf(basic));
    }
    return map;
  }

  @Override
  public void setAttribute (Path path, String attribute, Object value, LinkOption... options) throws IOException {
    String viewName = BasicFileAttribute.VIEW_NAME;
    final int colon = attribute.indexOf(':');
    if (colon != -1) {
      viewName = attribute.substring(0, colon);
      attribute = attribute.substring(colon + 1);
    }
    final FileAttribute<?> attr = newFileAttribute(viewName, attribute, value);
    setAttributes(path, Collections.singleton(attr), options);
  }

  protected FileAttribute<?> newFileAttribute (String viewName, String attribute, Object value) {
    if (BasicFileAttribute.VIEW_NAME.equals(viewName))
      return BasicFileAttribute.valueOf(attribute).newFileAttribute(value);
    throw new UnsupportedOperationException("Attribute: "+viewName+":"+attribute);
  }

  protected abstract void setAttributes (Path path, Set<? extends FileAttribute<?>> attrs, LinkOption... options) throws IOException;

  @SuppressWarnings("unchecked")
  public <V extends FileAttributeView> V getFileAttributeView (final Path path, Class<V> type, final LinkOption... options) {
    checkPath(path);
    if (BasicFileAttributeView.class == type)
      return (V)new BasicFileAttributeViewImpl(path, options);
    return null;
  }


  protected class BasicFileAttributeViewImpl implements BasicFileAttributeView {

    protected final Path path;
    protected final LinkOption[] options;

    public BasicFileAttributeViewImpl (Path path, LinkOption[] options) {
      this.path = path;
      this.options = options;
    }

    @Override
    public String name () {
      return BasicFileAttribute.VIEW_NAME;
    }

    @Override
    public BasicFileAttributes readAttributes () throws IOException {
      return AbstractFileSystemProvider.this.readAttributes(path, BasicFileAttributes.class, options);
    }

    @Override
    public void setTimes (FileTime lastModifiedTime, FileTime lastAccessTime, FileTime creationTime) throws IOException {
      final Set<FileAttribute<?>> attrs = new HashSet<FileAttribute<?>>();
      if (lastModifiedTime != null)
        attrs.add(BasicFileAttribute.lastModifiedTime.newFileAttribute(lastModifiedTime));
      if (lastAccessTime != null)
        attrs.add(BasicFileAttribute.lastAccessTime.newFileAttribute(lastAccessTime));
      if (creationTime != null)
        attrs.add(BasicFileAttribute.creationTime.newFileAttribute(creationTime));
      AbstractFileSystemProvider.this.setAttributes(path, attrs, options);
    }

  } // class BasicFileAttributeViewImpl

}
