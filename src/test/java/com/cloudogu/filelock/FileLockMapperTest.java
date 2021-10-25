package com.cloudogu.filelock;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.api.FileLock;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FileLockMapperTest {

  FileLockMapper mapper = new FileLockMapperImpl();

  @Test
  void mapToDto() {
    FileLockDto dto = mapper.map(new FileLock("trillian", Instant.ofEpochMilli(10000)));

    assertThat(dto.getTimestamp()).isEqualTo(Instant.ofEpochMilli(10000));
    assertThat(dto.getUserId()).isEqualTo("trillian");
  }
}
