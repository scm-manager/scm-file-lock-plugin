package com.cloudogu.filelock;

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.LockCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class FileEnricherTest {

  private final Repository repository = new Repository("id-1", "git", "hitchhiker", "HeartOfGold");

  @Mock
  private HalAppender appender;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private LockCommandBuilder lockCommandBuilder;
  @Mock
  private FileLockMapper mapper;

  @InjectMocks
  private FileEnricher enricher;

  @Test
  void shouldNotEnrichWithoutPermission() {
    FileLock fileLock = new FileLock("trillian", Instant.ofEpochMilli(10000));
    FileObject fileObject = mock(FileObject.class);
    String filepath = "myfile";

    when(lockCommandBuilder.status(filepath)).thenReturn(Optional.of(fileLock));

    enricher.enrich(HalEnricherContext.of(repository, fileObject), appender);

    verify(appender, never()).appendEmbedded(anyString(), any(FileLockDto.class));
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldEnrichWithPushPermission() {
    FileLock fileLock = new FileLock("trillian", Instant.ofEpochMilli(10000));
    FileLockDto dto = new FileLockDto("trillian", Instant.ofEpochMilli(10000));
    FileObject fileObject = mock(FileObject.class);
    String filepath = "myfile";

    when(fileObject.getPath()).thenReturn(filepath);
    when(serviceFactory.create(repository)).thenReturn(service);
    when(service.getLockCommand()).thenReturn(lockCommandBuilder);
    when(lockCommandBuilder.status(filepath)).thenReturn(Optional.of(fileLock));
    when(mapper.map(fileLock)).thenReturn(dto);

    enricher.enrich(HalEnricherContext.of(repository, fileObject), appender);

    verify(appender).appendEmbedded("fileLock", dto);
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldNotEnrichIfNotLocked() {
    FileObject fileObject = mock(FileObject.class);
    String filepath = "myfile";

    when(fileObject.getPath()).thenReturn(filepath);
    when(serviceFactory.create(repository)).thenReturn(service);
    when(service.getLockCommand()).thenReturn(lockCommandBuilder);
    when(lockCommandBuilder.status(filepath)).thenReturn(Optional.empty());

    enricher.enrich(HalEnricherContext.of(repository, fileObject), appender);

    verify(appender, never()).appendEmbedded(anyString(), any(FileLockDto.class));
  }
}
