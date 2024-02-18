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

import com.llamalab.safs.StandardWatchEventKinds;
import com.llamalab.safs.WatchEvent;
import com.llamalab.safs.WatchKey;
import com.llamalab.safs.Watchable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWatchKey implements WatchKey {

  private enum State {
    READY,
    SIGNALLED,
  }
  private final Object eventLock = new Object();
  private final AbstractWatchService service;
  private final Watchable watchable;
  private final int overflowLimit;
  private List<WatchEvent<?>> events = new ArrayList<WatchEvent<?>>();
  private State state = State.READY;

  public AbstractWatchKey (AbstractWatchService service, Watchable watchable, int overflowLimit) {
    this.service = service;
    this.watchable = watchable;
    this.overflowLimit = overflowLimit;
  }

  public final AbstractWatchService service () {
    return service;
  }

  @Override
  public final Watchable watchable () {
    return watchable;
  }

  @Override
  public boolean isValid () {
    return service.isOpen();
  }

  @Override
  public final boolean reset () {
    synchronized (eventLock) {
      if (!isValid())
        return false;
      if (State.SIGNALLED == state) {
        if (events.isEmpty())
          state = State.READY;
        else
          service.offer(this);
      }
      return true;
    }
  }

  @Override
  public final List<WatchEvent<?>> pollEvents () {
    synchronized (eventLock) {
      final List<WatchEvent<?>> result = events;
      events = new ArrayList<WatchEvent<?>>();
      return result;
    }
  }

  protected final <T> void signalEvent (WatchEvent.Kind<T> kind, T context) {
    synchronized (eventLock) {
      final int size = events.size();
      if (size > 0) {
        final Event<?> event = (Event<?>)events.get(size - 1);
        if (   StandardWatchEventKinds.OVERFLOW == event.kind
            || (event.kind == kind && Utils.equals(event.context, context))) {
          ++event.count;
          return;
        }
      }
      if (size < overflowLimit)
        events.add(new Event<T>(kind, context));
      else
        events.add(new Event<Object>(StandardWatchEventKinds.OVERFLOW, null));
      if (State.READY == state) {
        state = State.SIGNALLED;
        service.offer(this);
      }
    }
  }

  private static final class Event<T> implements WatchEvent<T> {

    private final WatchEvent.Kind<T> kind;
    private final T context;
    private int count = 1;

    public Event (WatchEvent.Kind<T> kind, T context) {
      this.kind = kind;
      this.context = context;
    }

    @Override
    public Kind<T> kind () {
      return kind;
    }

    @Override
    public T context () {
      return context;
    }

    @Override
    public int count () {
      return count;
    }

    @Override
    public String toString () {
      return super.toString()+"[kind="+kind+", context="+context+", count="+count+"]";
    }

  } // class Event
}
