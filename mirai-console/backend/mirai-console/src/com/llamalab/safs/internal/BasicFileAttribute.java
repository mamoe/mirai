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

import com.llamalab.safs.attribute.BasicFileAttributes;
import com.llamalab.safs.attribute.FileAttribute;

public enum BasicFileAttribute {
  fileKey {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.fileKey();
    }
  },
  isDirectory {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.isDirectory();
    }
  },
  isRegularFile {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.isRegularFile();
    }
  },
  isSymbolicLink {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.isSymbolicLink();
    }
  },
  isOther {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.isOther();
    }
  },
  size {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.size();
    }
  },
  creationTime {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.creationTime();
    }
  },
  lastModifiedTime {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.lastModifiedTime();
    }
  },
  lastAccessTime {
    @Override
    public Object valueOf (BasicFileAttributes attrs) {
      return attrs.lastAccessTime();
    }
  };

  public static final String VIEW_NAME = "basic";

  public abstract Object valueOf (BasicFileAttributes attrs);

  public FileAttribute newFileAttribute (Object value) {
    return new BasicFileAttributeValue(this, value);
  }

  public static Iterable<BasicFileAttribute> parse (String attributes) {
    return new AttributeParser<BasicFileAttribute>(BasicFileAttribute.class, attributes, VIEW_NAME, true);
  }

}
