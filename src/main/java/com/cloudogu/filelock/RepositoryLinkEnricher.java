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

import javax.inject.Inject;
import javax.inject.Provider;

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
