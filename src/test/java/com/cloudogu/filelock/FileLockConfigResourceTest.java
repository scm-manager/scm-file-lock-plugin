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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.apache.shiro.authz.AuthorizationException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class FileLockConfigResourceTest {

  private final Repository repository = new Repository("id-1", "git", "hitchhiker", "HeartOfGold");

  @Mock
  private RepositoryConfigStore configStore;
  @Mock
  private RepositoryManager manager;

  private RestDispatcher dispatcher;
  private final JsonMockHttpResponse response = new JsonMockHttpResponse();

  @BeforeEach
  void initResource() {
    FileLockConfigResource resource = new FileLockConfigResource(configStore, manager);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  @SubjectAware(permissions = "repository:configureFileLock:id-1")
  void shouldGetConfigWithLinks() throws URISyntaxException {
    when(configStore.getConfig(repository)).thenReturn(new RepositoryConfig());
    when(manager.get(repository.getNamespaceAndName())).thenReturn(repository);

    MockHttpRequest request = MockHttpRequest.get(String.format("/v2/file-lock/%s/config", repository.getNamespaceAndName()));
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    JsonNode mainNode = response.getContentAsJson();
    assertThat(mainNode.path("enabled").asBoolean()).isTrue();
    assertThat(mainNode.path("_links").path("self").path("href").textValue()).isEqualTo("/v2/file-lock/hitchhiker/HeartOfGold/config");
    assertThat(mainNode.path("_links").path("update").path("href").textValue()).isEqualTo("/v2/file-lock/hitchhiker/HeartOfGold/config");
  }

  @Test
  void shouldUpdateConfig() throws URISyntaxException {
    when(manager.get(repository.getNamespaceAndName())).thenReturn(repository);

    byte[] contentJson = ("{\"enabled\" : \"false\"}").getBytes();

    MockHttpRequest request = MockHttpRequest.put(String.format("/v2/file-lock/%s/config", repository.getNamespaceAndName()))
      .contentType(FileLockConfigResource.MEDIA_TYPE)
      .content(contentJson);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
    verify(configStore).updateConfig(eq(repository), argThat(config -> {
      assertThat(config.isEnabled()).isFalse();
      return true;
    }));
  }
}
