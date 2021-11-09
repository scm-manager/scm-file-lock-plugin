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
