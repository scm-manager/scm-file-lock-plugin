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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import jakarta.inject.Provider;
import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class RepositoryLinkEnricherTest {

  private final Repository repository = new Repository("id-1", "git", "hitchhiker", "HeartOfGold");

  @Mock
  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private RepositoryConfigStore configStore;

  @Mock
  private HalAppender appender;

  @InjectMocks
  private RepositoryLinkEnricher enricher;

  @BeforeEach
  void init() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("scm/api"));
    when(scmPathInfoStoreProvider.get()).thenReturn(scmPathInfoStore);
  }

  @Test
  void shouldNotEnrichLink() {
    when(serviceFactory.create(repository)).thenReturn(service);
    when(service.isSupported(Command.FILE_LOCK)).thenReturn(false);
    enricher.enrich(HalEnricherContext.of(repository), appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldNotEnrichConfigLinkIfNotPermitted() {
    when(configStore.getConfig(repository)).thenReturn(new RepositoryConfig());
    when(serviceFactory.create(repository)).thenReturn(service);
    when(service.isSupported(Command.FILE_LOCK)).thenReturn(true);

    enricher.enrich(HalEnricherContext.of(repository), appender);

    verify(appender).appendLink("fileLocks", "scm/api/v2/file-lock/hitchhiker/HeartOfGold");
    verify(appender, never()).appendLink(eq("fileLockConfig"), anyString());
  }

  @Test
  @SubjectAware(permissions = {"repository:push:id-1", "repository:configureFileLock:id-1"})
  void shouldEnrichConfigLinkIfPermitted() {
    when(configStore.getConfig(repository)).thenReturn(new RepositoryConfig());
    when(serviceFactory.create(repository)).thenReturn(service);
    when(service.isSupported(Command.FILE_LOCK)).thenReturn(true);

    enricher.enrich(HalEnricherContext.of(repository), appender);

    verify(appender).appendLink("fileLocks", "scm/api/v2/file-lock/hitchhiker/HeartOfGold");
    verify(appender).appendLink("fileLockConfig", "scm/api/v2/file-lock/hitchhiker/HeartOfGold/config");
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldNotEnrichLinkIfConfigDisabled() {
    RepositoryConfig repositoryConfig = new RepositoryConfig();
    repositoryConfig.setEnabled(false);
    when(serviceFactory.create(repository)).thenReturn(service);
    when(service.isSupported(Command.FILE_LOCK)).thenReturn(true);
    when(configStore.getConfig(repository)).thenReturn(repositoryConfig);

    enricher.enrich(HalEnricherContext.of(repository), appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }
}
