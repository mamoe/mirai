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

import com.llamalab.safs.internal.WatchEventKind;

public final class StandardWatchEventKinds {

  public static final WatchEvent.Kind<Object> OVERFLOW   = new WatchEventKind<Object>("OVERFLOW", Object.class);
  public static final WatchEvent.Kind<Path> ENTRY_CREATE = new WatchEventKind<Path>("ENTRY_CREATE", Path.class);
  public static final WatchEvent.Kind<Path> ENTRY_DELETE = new WatchEventKind<Path>("ENTRY_DELETE", Path.class);
  public static final WatchEvent.Kind<Path> ENTRY_MODIFY = new WatchEventKind<Path>("ENTRY_MODIFY", Path.class);

  private StandardWatchEventKinds() {}

}
