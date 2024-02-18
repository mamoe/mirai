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

import com.llamalab.safs.ClosedWatchServiceException;
import com.llamalab.safs.WatchEvent;
import com.llamalab.safs.WatchKey;
import com.llamalab.safs.WatchService;
import com.llamalab.safs.Watchable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractWatchService implements WatchService {

  private final WatchKey CLOSE_KEY = new WatchKey() {

    @Override
    public Watchable watchable () {
      return null;
    }

    @Override
    public boolean isValid () {
      return true;
    }

    @Override
    public boolean reset () {
      return true;
    }

    @Override
    public void cancel () {
    }

    @Override
    public List<WatchEvent<?>> pollEvents () {
      return null;
    }
  };
  private final LinkedBlockingDeque<WatchKey> pendingKeys = new LinkedBlockingDeque<WatchKey>();
  private final AtomicBoolean closed = new AtomicBoolean();

  final void offer (WatchKey key) {
    pendingKeys.offer(key);
  }

  @Override
  public final WatchKey poll () {
    checkOpen();
    return checkKey(pendingKeys.poll());
  }

  @Override
  public final WatchKey poll (long timeout, TimeUnit unit) throws InterruptedException {
    checkOpen();
    return checkKey(pendingKeys.poll(timeout, unit));
  }

  @Override
  public final WatchKey take () throws InterruptedException {
    checkOpen();
    return checkKey(pendingKeys.take());
  }

  @Override
  public final void close () throws IOException {
    if (closed.compareAndSet(false, true)) {
      try {
        implCloseService();
      }
      finally {
        pendingKeys.clear();
        pendingKeys.offer(CLOSE_KEY);
      }
    }
  }

  protected abstract void implCloseService () throws IOException;

  public final boolean isOpen () {
    return !closed.get();
  }

  private void checkOpen () {
    if (closed.get())
      throw new ClosedWatchServiceException();
  }

  private WatchKey checkKey (WatchKey key) {
    if (CLOSE_KEY == key)
      pendingKeys.offer(key);
    checkOpen();
    return key;
  }
}
