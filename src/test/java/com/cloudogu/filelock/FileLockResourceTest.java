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
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.FileLockCommandBuilder;
import sonia.scm.repository.api.LockCommandResult;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnlockCommandResult;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class FileLockResourceTest {

  private final Repository repository = new Repository("id-1", "git", "hitchhiker", "HeartOfGold");

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private FileLockCommandBuilder lockCommandBuilder;
  @InjectMocks
  private FileLockResource resource;

  private RestDispatcher dispatcher;
  private final MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void initResource() {
    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);

    when(serviceFactory.create(repository.getNamespaceAndName())).thenReturn(service);
    when(service.getRepository()).thenReturn(repository);
    lenient().when(service.getLockCommand()).thenReturn(lockCommandBuilder);
  }

  @Test
  void shouldNotLockFileWithoutPushPermission() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.post(
      String.format(
        "/v2/file-lock/%s/%s/lock/%s",
        repository.getNamespace(),
        repository.getName(),
        "src%2FmyFile"
      )
    );

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldLockFile() throws URISyntaxException {
    FileLockCommandBuilder.InnerLockCommandBuilder innerLockCommandBuilder = mock(FileLockCommandBuilder.InnerLockCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(lockCommandBuilder.lock(any())).thenReturn(innerLockCommandBuilder);
    when(innerLockCommandBuilder.execute()).thenReturn(new LockCommandResult(true));
    MockHttpRequest request = MockHttpRequest.post(
      String.format(
        "/v2/file-lock/%s/%s/lock/%s",
        repository.getNamespace(),
        repository.getName(),
        "src%2FmyFile"
      )
    );

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
  }

  @Test
  void shouldNotUnlockFileWithoutPushPermission() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete(
      String.format(
        "/v2/file-lock/%s/%s/lock/%s",
        repository.getNamespace(),
        repository.getName(),
        "src%2FmyFile"
      )
    );

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldUnlockFile() throws URISyntaxException {
    FileLockCommandBuilder.InnerUnlockCommandBuilder innerUnlockCommandBuilder = mock(FileLockCommandBuilder.InnerUnlockCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(lockCommandBuilder.unlock(any())).thenReturn(innerUnlockCommandBuilder);
    when(innerUnlockCommandBuilder.execute()).thenReturn(new UnlockCommandResult(true));
    MockHttpRequest request = MockHttpRequest.delete(
      String.format(
        "/v2/file-lock/%s/%s/lock/%s",
        repository.getNamespace(),
        repository.getName(),
        "src%2FmyFile"
      )
    );

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
  }
}
