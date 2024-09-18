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

import org.apache.shiro.authz.AuthorizationException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ShiroExtension.class)
class RepositoryConfigStoreTest {

  private static final Repository REPOSITORY = RepositoryTestData.create42Puzzle();
  private final RepositoryConfigStore store = new RepositoryConfigStore(new InMemoryConfigurationStoreFactory());

  static {
    REPOSITORY.setId("id-1");
  }

  @Nested
  @SubjectAware(value = "trillian", permissions = "repository")
  class WithPermission {

    @Test
    void shouldGetDefaultConfig() {
      RepositoryConfig config = store.getConfig(REPOSITORY);

      assertThat(config.isEnabled()).isTrue();
    }

    @Test
    void shouldSetConfig() {
      RepositoryConfig repositoryConfig = new RepositoryConfig();
      repositoryConfig.setEnabled(false);

      store.updateConfig(REPOSITORY, repositoryConfig);

      assertThat(store.getConfig(REPOSITORY).isEnabled()).isFalse();
    }
  }

  @Test
  void shouldNotUpdateConfigWithoutPermission() {
    RepositoryConfig repositoryConfig = new RepositoryConfig();
    assertThrows(AuthorizationException.class, () -> store.updateConfig(REPOSITORY, repositoryConfig));
  }
}
