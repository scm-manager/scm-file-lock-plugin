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

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  private final RepositoryServiceFactory serviceFactory;
  private final RepositoryConfigStore configStore;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, RepositoryServiceFactory serviceFactory, RepositoryConfigStore configStore) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.serviceFactory = serviceFactory;
    this.configStore = configStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    appendLinks(appender, repository);
  }

  private void appendLinks(HalAppender appender, Repository repository) {
    RestApiLinks restApiLinks = new RestApiLinks(scmPathInfoStoreProvider.get().get().getApiRestUri());

    try (RepositoryService service = serviceFactory.create(repository)) {
      if (service.isSupported(Command.FILE_LOCK)) {
        if (shouldAppendLinks(repository)) {
          appender.appendLink(
            "fileLocks",
            restApiLinks.fileLock().getAll(repository.getNamespace(), repository.getName()).asString()
          );
        }
        if (PermissionCheck.mayConfigure(repository)) {
          appender.appendLink(
            "fileLockConfig",
            restApiLinks.fileLockConfig().getRepositoryConfig(repository.getNamespace(), repository.getName()).asString()
          );
        }
      }
    }
  }

  private boolean shouldAppendLinks(Repository repository) {
    return RepositoryPermissions.push(repository).isPermitted()
      && configStore.getConfig(repository).isEnabled();
  }
}
