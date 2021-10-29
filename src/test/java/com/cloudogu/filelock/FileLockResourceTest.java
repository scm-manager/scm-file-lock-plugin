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
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.LockCommandBuilder;
import sonia.scm.repository.api.LockCommandResult;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnlockCommandResult;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
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
  private LockCommandBuilder lockCommandBuilder;
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
    LockCommandBuilder.InnerLockCommandBuilder innerLockCommandBuilder = mock(LockCommandBuilder.InnerLockCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(lockCommandBuilder.lock()).thenReturn(innerLockCommandBuilder);
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
    LockCommandBuilder.InnerUnlockCommandBuilder innerUnlockCommandBuilder = mock(LockCommandBuilder.InnerUnlockCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(lockCommandBuilder.unlock()).thenReturn(innerUnlockCommandBuilder);
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
