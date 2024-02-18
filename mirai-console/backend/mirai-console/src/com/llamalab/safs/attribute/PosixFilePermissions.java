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

package com.llamalab.safs.attribute;

import com.llamalab.safs.internal.SearchSet;

import java.util.EnumSet;
import java.util.Set;

public final class PosixFilePermissions {

  private PosixFilePermissions () {}

  public static String toString (Set<PosixFilePermission> perms) {
    final StringBuilder sb = new StringBuilder(9);
    append(sb,
        perms.contains(PosixFilePermission.OWNER_READ),
        perms.contains(PosixFilePermission.OWNER_WRITE),
        perms.contains(PosixFilePermission.OWNER_EXECUTE));
    append(sb,
        perms.contains(PosixFilePermission.GROUP_READ),
        perms.contains(PosixFilePermission.GROUP_WRITE),
        perms.contains(PosixFilePermission.GROUP_EXECUTE));
    append(sb,
        perms.contains(PosixFilePermission.OTHERS_READ),
        perms.contains(PosixFilePermission.OTHERS_WRITE),
        perms.contains(PosixFilePermission.OTHERS_EXECUTE));
    return sb.toString();
  }

  private static void append (StringBuilder sb, boolean r, boolean w, boolean x) {
    sb.append(r ? 'r' : '-').append(w ? 'w' : '-').append(x ? 'x' : '-');
  }

  public static Set<PosixFilePermission> fromString (String mode) {
    if (mode.length() != 9)
      throw new IllegalArgumentException();
    final Set<PosixFilePermission> set = EnumSet.noneOf(PosixFilePermission.class);
    add(set, mode, 0, 'r', PosixFilePermission.OWNER_READ);
    add(set, mode, 1, 'w', PosixFilePermission.OWNER_WRITE);
    add(set, mode, 2, 'x', PosixFilePermission.OWNER_EXECUTE);

    add(set, mode, 3, 'r', PosixFilePermission.GROUP_READ);
    add(set, mode, 4, 'w', PosixFilePermission.GROUP_WRITE);
    add(set, mode, 5, 'x', PosixFilePermission.GROUP_EXECUTE);

    add(set, mode, 6, 'r', PosixFilePermission.OTHERS_READ);
    add(set, mode, 7, 'w', PosixFilePermission.OTHERS_WRITE);
    add(set, mode, 8, 'x', PosixFilePermission.OTHERS_EXECUTE);
    return set;
  }

  private static void add (Set<PosixFilePermission> set, String mode, int index, char flag, PosixFilePermission perm) {
    final char c = mode.charAt(index);
    if (flag == c)
      set.add(perm);
    else if ('-' != c)
      throw new IllegalArgumentException();
  }

  public static FileAttribute<Set<PosixFilePermission>> asFileAttribute (Set<PosixFilePermission> perms) {
    //noinspection ToArrayCallWithZeroLengthArrayArgument
    final Set<PosixFilePermission> value = new SearchSet<PosixFilePermission>(perms.toArray(new PosixFilePermission[perms.size()]));
    return new FileAttribute<Set<PosixFilePermission>>() {

      @Override
      public String name() {
        return "posix:permissions";
      }

      @Override
      public Set<PosixFilePermission> value () {
        return value;
      }
    };
  }
}
