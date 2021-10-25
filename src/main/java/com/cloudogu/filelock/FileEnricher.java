package com.cloudogu.filelock;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;

@Extension
@Enrich(FileObject.class)
public class FileEnricher implements HalEnricher {

  private final RepositoryServiceFactory serviceFactory;
  private final FileLockMapper mapper;

  @Inject
  public FileEnricher(RepositoryServiceFactory serviceFactory, FileLockMapper mapper) {
    this.serviceFactory = serviceFactory;
    this.mapper = mapper;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
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
