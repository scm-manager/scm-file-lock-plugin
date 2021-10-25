package com.cloudogu.filelock;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileLockDto extends HalRepresentation {

  private String userId;
  private Instant timestamp;

  public FileLockDto(Links links) {
    super(links);
  }

  public FileLockDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
