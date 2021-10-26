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
import sonia.scm.plugin.Extension;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;

@Extension
@Enrich(FileObject.class)
public class FileEnricher implements HalEnricher {

  private final RepositoryServiceFactory serviceFactory;
  private final FileLockMapper mapper;
  private final RepositoryManager repositoryManager;

  @Inject
  public FileEnricher(RepositoryServiceFactory serviceFactory, FileLockMapper mapper, RepositoryManager repositoryManager) {
    this.serviceFactory = serviceFactory;
    this.mapper = mapper;
    this.repositoryManager = repositoryManager;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = repositoryManager.get(context.oneRequireByType(NamespaceAndName.class));
    FileObject fileObject = context.oneRequireByType(FileObject.class);

    if (RepositoryPermissions.push(repository).isPermitted()) {
      try (RepositoryService service = serviceFactory.create(repository)) {
        service.getLockCommand()
          .status(fileObject.getPath())
          .ifPresent(fileLock -> appender.appendEmbedded("fileLock", mapper.map(fileLock)));
      }
    }
  }
}
