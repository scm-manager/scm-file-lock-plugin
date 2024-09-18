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

import com.google.inject.util.Providers;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.FileLock;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({ShiroExtension.class, MockitoExtension.class})
class FileLockMapperTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  FileLockMapper mapper = new FileLockMapperImpl();

  @Mock
  private UserDisplayManager userDisplayManager;

  @BeforeEach
  void init() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("scm/api"));
    mapper.setScmPathInfoStoreProvider(Providers.of(scmPathInfoStore));
    mapper.setUserDisplayManager(userDisplayManager);
  }

  @Test
  @SubjectAware(value = "trillian")
  void mapToDto() {
    when(userDisplayManager.get("trillian")).thenReturn(Optional.of(DisplayUser.from(new User("trillian", "Tricia McMillan", null))));

    FileObject fileObject = new FileObject();
    fileObject.setPath("src/test.md");
    FileLockDto dto = mapper.map(repository, new FileLock("src/test.md", "", "trillian", Instant.ofEpochMilli(10000)));

    assertThat(dto.getTimestamp()).isEqualTo(Instant.ofEpochMilli(10000));
    assertThat(dto.getUsername()).isEqualTo("Tricia McMillan");
    assertThat(dto.getPath()).isEqualTo(fileObject.getPath());
    assertThat(dto.getLinks().getLinkBy("unlock").get().getHref()).isEqualTo("scm/api/v2/file-lock/hitchhiker/42Puzzle/lock/src%2Ftest.md");
  }
}
