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

import com.google.inject.util.Providers;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.FileLock;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ShiroExtension.class)
class FileLockMapperTest {

  private final Repository repository = RepositoryTestData.create42Puzzle();
  FileLockMapper mapper = new FileLockMapperImpl();

  @BeforeEach
  void init() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("scm/api"));
    mapper.setScmPathInfoStoreProvider(Providers.of(scmPathInfoStore));
  }

  @Test
  @SubjectAware(value = "trillian")
  void mapToDto() {
    FileObject fileObject = new FileObject();
    fileObject.setPath("src/test.md");
    FileLockDto dto = mapper.map(repository, new FileLock("src/test.md", "", "trillian", Instant.ofEpochMilli(10000)));

    assertThat(dto.getTimestamp()).isEqualTo(Instant.ofEpochMilli(10000));
    assertThat(dto.getUserId()).isEqualTo("trillian");
    assertThat(dto.getPath()).isEqualTo(fileObject.getPath());
    assertThat(dto.getLinks().getLinkBy("unlock").get().getHref()).isEqualTo("scm/api/v2/file-lock/hitchhiker/42Puzzle/unlock/src%2Ftest.md");
  }
}
