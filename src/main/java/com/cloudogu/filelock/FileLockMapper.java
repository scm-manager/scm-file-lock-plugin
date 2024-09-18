/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.filelock;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.FileLock;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import jakarta.inject.Inject;
import org.mapstruct.Named;
import jakarta.inject.Provider;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class FileLockMapper {

  @Inject
  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  @Inject
  private UserDisplayManager userDisplayManager;

  @VisibleForTesting
  void setScmPathInfoStoreProvider(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  @VisibleForTesting
 void setUserDisplayManager(UserDisplayManager userDisplayManager) {
    this.userDisplayManager = userDisplayManager;
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  @Mapping(target = "username", source = "userId", qualifiedByName = "mapUser")
  public abstract FileLockDto map(@Context Repository repository, FileLock fileLock);

  @Named("mapUser")
  String mapUser(String userId) {
    return userDisplayManager.get(userId).orElseGet(() -> DisplayUser.from(new User(userId, userId, null))).getDisplayName();
  }

  @AfterMapping
  void mapWriteAccess(@MappingTarget FileLockDto dto, FileLock fileLock) {
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    dto.setOwned(fileLock.getUserId().equals(username));
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
