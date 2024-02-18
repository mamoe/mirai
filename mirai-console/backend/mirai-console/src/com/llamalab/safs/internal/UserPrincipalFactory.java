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

import com.llamalab.safs.attribute.GroupPrincipal;
import com.llamalab.safs.attribute.UserPrincipal;
import com.llamalab.safs.attribute.UserPrincipalLookupService;

import java.io.IOException;

public class UserPrincipalFactory extends UserPrincipalLookupService {

  @Override
  public UserPrincipal lookupPrincipalByName (String name) throws IOException {
    return new User(name);
  }

  @Override
  public GroupPrincipal lookupPrincipalByGroupName (String group) throws IOException {
    return new Group(group);
  }

  protected static class User implements UserPrincipal {

    private final String name;

    public User (String name) {
      if (name == null)
        throw new NullPointerException();
      this.name = name;
    }

    @Override
    public String getName () {
      return name;
    }

    @Override
    public String toString () {
      return super.toString()+"[name="+name+"]";
    }

    @Override
    public boolean equals (Object other) {
      return other instanceof User && name.equals(((User)other).name);
    }

    @Override
    public int hashCode () {
      return name.hashCode();
    }

  }

  protected static class Group extends User implements GroupPrincipal {

    public Group (String name) {
      super(name);
    }
  }
}
