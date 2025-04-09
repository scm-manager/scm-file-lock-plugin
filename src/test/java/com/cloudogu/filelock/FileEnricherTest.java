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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.FileLockCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.inject.Provider;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class FileEnricherTest {

  private final Repository repository = new Repository("id-1", "git", "hitchhiker", "HeartOfGold");

  @Mock
  Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  @Mock
  private HalAppender appender;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryConfigStore configStore;
  @Mock
  private RepositoryService service;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private FileLockCommandBuilder lockCommandBuilder;
  @Mock
  private FileLockMapper mapper;

  @InjectMocks
  private FileEnricher enricher;

  @BeforeEach
  void init() {
    when(repositoryManager.get(repository.getNamespaceAndName())).thenReturn(repository);
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("scm/api/"));
    lenient().when(scmPathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);
  }

  @Nested
  @SubjectAware(permissions = {"repository:push:id-1", "repository:configureFileLock:id-1"})
  class WithConfigEnabled {
    @BeforeEach
    void mockConfig() {
      when(configStore.getConfig(repository)).thenReturn(new RepositoryConfig());
    }

    @Test
    void shouldNotEnrichIdLockCommandNotSupported() {
      FileObject fileObject = mock(FileObject.class);

      when(serviceFactory.create(repository)).thenReturn(service);
      when(service.isSupported(Command.FILE_LOCK)).thenReturn(false);

      enricher.enrich(HalEnricherContext.of(repository.getNamespaceAndName(), fileObject), appender);

      verify(appender, never()).appendEmbedded(anyString(), any(FileLockDto.class));
      verify(appender, never()).appendLink(anyString(), anyString());
    }

    @Nested
    class WithLockCommandSupport {

      @BeforeEach
      void mockLockCommand() {
        when(serviceFactory.create(repository)).thenReturn(service);
        when(service.getLockCommand()).thenReturn(lockCommandBuilder);
        when(service.isSupported(Command.FILE_LOCK)).thenReturn(true);
      }

      @Test
      void shouldEnrichWithPushPermission() {
        FileLock fileLock = new FileLock("src/test.md", "", "trillian", Instant.ofEpochMilli(10000));
        FileLockDto dto = new FileLockDto("trillian", Instant.ofEpochMilli(10000), "myfile", false);
        FileObject fileObject = mock(FileObject.class);
        String filepath = "myfile";

        when(fileObject.getPath()).thenReturn(filepath);
        when(lockCommandBuilder.status(filepath)).thenReturn(Optional.of(fileLock));
        when(mapper.map(repository, fileLock)).thenReturn(dto);

        enricher.enrich(HalEnricherContext.of(repository.getNamespaceAndName(), fileObject), appender);

        verify(appender).appendEmbedded("fileLock", dto);
        verify(appender).appendLink("unlock", "scm/api/v2/file-lock/hitchhiker/HeartOfGold/lock/myfile");
      }

      @Test
      void shouldOnlyEnrichLockLink() {
        FileObject fileObject = mock(FileObject.class);
        String filepath = "src/myfile";

        when(fileObject.getPath()).thenReturn(filepath);
        when(lockCommandBuilder.status(filepath)).thenReturn(Optional.empty());

        enricher.enrich(HalEnricherContext.of(repository.getNamespaceAndName(), fileObject), appender);

        verify(appender, never()).appendEmbedded(anyString(), any(FileLockDto.class));
        verify(appender).appendLink("lock", "scm/api/v2/file-lock/hitchhiker/HeartOfGold/lock/src%2Fmyfile");
      }
    }
  }

  @Test
  void shouldNotEnrichWithoutPermission() {
    FileObject fileObject = mock(FileObject.class);
    enricher.enrich(HalEnricherContext.of(repository.getNamespaceAndName(), fileObject), appender);

    verify(appender, never()).appendEmbedded(anyString(), any(FileLockDto.class));
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldNotAppendLinksIfConfigDisabled() {
    RepositoryConfig repositoryConfig = new RepositoryConfig();
    repositoryConfig.setEnabled(false);
    when(configStore.getConfig(repository)).thenReturn(repositoryConfig);
    FileObject fileObject = mock(FileObject.class);

    enricher.enrich(HalEnricherContext.of(repository.getNamespaceAndName(), fileObject), appender);

    verify(appender, never()).appendEmbedded(anyString(), any(FileLockDto.class));
    verify(appender, never()).appendLink(anyString(), anyString());
  }
}
