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

import com.llamalab.safs.DirectoryIteratorException;
import com.llamalab.safs.DirectoryStream;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class AbstractDirectoryStream<T> implements DirectoryStream<T>, Iterator<T> {

  private T next;
  private boolean started;
  private boolean closed;

  /**
   * Make sure to call super.close();
   */
  @Override
  public final void close () throws IOException {
    if (!closed) {
      closed = true;
      implCloseStream();
    }
  }

  protected void implCloseStream () throws IOException {}

  @SuppressWarnings("NullableProblems")
  @Override
  public final Iterator<T> iterator () {
    if (started)
      throw new IllegalStateException();
    started = true;
    return this;
  }

  @Override
  public final boolean hasNext () {
    if (next != null)
      return true;
    if (closed)
      return false;
    try {
      return (next = advance()) != null;
    }
    catch (IOException e) {
      throw new DirectoryIteratorException(e);
    }
  }

  @Override
  public final T next () {
    if (closed || !hasNext())
      throw new NoSuchElementException();
    final T result = next;
    next = null;
    return result;
  }

  @Override
  public final void remove () {
    throw new UnsupportedOperationException();
  }

  protected abstract T advance () throws IOException;
}