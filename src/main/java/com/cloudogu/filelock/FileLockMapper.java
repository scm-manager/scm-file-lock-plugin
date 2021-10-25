package com.cloudogu.filelock;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sonia.scm.repository.api.FileLock;

@Mapper
public abstract class FileLockMapper  {

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract FileLockDto map(FileLock fileLock);
}
