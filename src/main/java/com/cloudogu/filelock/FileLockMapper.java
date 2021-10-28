/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cloudogu.filelock;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.FileLock;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class FileLockMapper {

  @Inject
  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  @Inject
  private UserDisplayManager userDisplayManager;

  @VisibleForTesting
  public void setScmPathInfoStoreProvider(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  @VisibleForTesting
  public void setUserDisplayManager(UserDisplayManager userDisplayManager) {
    this.userDisplayManager = userDisplayManager;
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  @Mapping(target = "username", source = "userId", qualifiedByName = "mapUser")
  public abstract FileLockDto map(@Context Repository repository, FileLock fileLock);

  @Named("mapUser")
  String mapUser(String userId) {
    return userDisplayManager.get(userId).orElseGet(() -> DisplayUser.from(new User(userId, userId, null))).getDisplayName();
  }

  @ObjectFactory
  FileLockDto createDto(@Context Repository repository, FileLock fileLock) {
    Links.Builder linksBuilder = linkingTo();

    RestApiLinks restApiLinks = new RestApiLinks(scmPathInfoStoreProvider.get().get().getApiRestUri());
    linksBuilder.single(
      link(
        "unlock",
        restApiLinks.fileLock().unlockFile(repository.getNamespace(), repository.getName(), fileLock.getPath()).asString()
      )
    );
    return new FileLockDto(linksBuilder.build());
  }
}
