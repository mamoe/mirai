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

import com.llamalab.safs.channels.SeekableByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

final class SeekableByteChannelWrapper implements SeekableByteChannel {

  private final FileChannel fc;
  private final boolean append;

  public SeekableByteChannelWrapper (FileChannel fc, boolean append) {
    this.fc = fc;
    this.append = append;
  }

  @Override
  public void close () throws IOException {
    fc.close();
  }

  @Override
  public boolean isOpen () {
    return fc.isOpen();
  }

  @Override
  public int read (ByteBuffer dst) throws IOException {
    return fc.read(dst);
  }

  @Override
  public int write (ByteBuffer src) throws IOException {
    if (append)
      fc.position(fc.size());
    return fc.write(src);
  }

  @Override
  public long position () throws IOException {
    return fc.position();
  }

  @Override
  public SeekableByteChannel position (long newPosition) throws IOException {
    fc.position(newPosition);
    return this;
  }

  @Override
  public long size () throws IOException {
    return fc.size();
  }

  @Override
  public SeekableByteChannel truncate (long size) throws IOException {
    fc.truncate(size);
    return this;
  }

}
