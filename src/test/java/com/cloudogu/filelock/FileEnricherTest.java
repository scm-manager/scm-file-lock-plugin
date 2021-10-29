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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
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
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.LockCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Provider;
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
  private RepositoryService service;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private LockCommandBuilder lockCommandBuilder;
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

  @Test
  void shouldNotEnrichWithoutPermission() {
    FileLock fileLock = new FileLock("src/test.md", "" ,"trillian", Instant.ofEpochMilli(10000));
    FileObject fileObject = mock(FileObject.class);
    String filepath = "myfile";

    when(lockCommandBuilder.status(filepath)).thenReturn(Optional.of(fileLock));

    enricher.enrich(HalEnricherContext.of(repository.getNamespaceAndName(), fileObject), appender);

    verify(appender, never()).appendEmbedded(anyString(), any(FileLockDto.class));
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldEnrichWithPushPermission() {
    FileLock fileLock = new FileLock("src/test.md", "" ,"trillian", Instant.ofEpochMilli(10000));
    FileLockDto dto = new FileLockDto("trillian", Instant.ofEpochMilli(10000), "myfile", false);
    FileObject fileObject = mock(FileObject.class);
    String filepath = "myfile";

    when(fileObject.getPath()).thenReturn(filepath);
    when(serviceFactory.create(repository)).thenReturn(service);
    when(service.getLockCommand()).thenReturn(lockCommandBuilder);
    when(lockCommandBuilder.status(filepath)).thenReturn(Optional.of(fileLock));
    when(mapper.map(repository, fileLock)).thenReturn(dto);

    enricher.enrich(HalEnricherContext.of(repository.getNamespaceAndName(), fileObject), appender);

    verify(appender).appendEmbedded("fileLock", dto);
    verify(appender).appendLink("unlock", "scm/api/v2/file-lock/hitchhiker/HeartOfGold/unlock/myfile");
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldOnlyEnrichLockLink() {
    FileObject fileObject = mock(FileObject.class);
    String filepath = "src/myfile";

    when(fileObject.getPath()).thenReturn(filepath);
    when(serviceFactory.create(repository)).thenReturn(service);
    when(service.getLockCommand()).thenReturn(lockCommandBuilder);
    when(lockCommandBuilder.status(filepath)).thenReturn(Optional.empty());

    enricher.enrich(HalEnricherContext.of(repository.getNamespaceAndName(), fileObject), appender);

    verify(appender, never()).appendEmbedded(anyString(), any(FileLockDto.class));
    verify(appender).appendLink("lock", "scm/api/v2/file-lock/hitchhiker/HeartOfGold/lock/src%2Fmyfile");
  }
}
