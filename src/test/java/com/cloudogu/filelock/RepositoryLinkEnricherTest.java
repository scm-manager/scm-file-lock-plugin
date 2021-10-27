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
import sonia.scm.repository.RepositoryTestData;

import javax.inject.Provider;

import java.net.URI;

import static org.mockito.ArgumentMatchers.anyString;
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
    enricher.enrich(HalEnricherContext.of(repository), appender);

    verify(appender, never()).appendLink(anyString(), anyString());
  }

  @Test
  @SubjectAware(permissions = "repository:push:id-1")
  void shouldEnrichLink() {
    enricher.enrich(HalEnricherContext.of(repository), appender);

    verify(appender).appendLink("fileLocks", "scm/api/v2/file-lock/hitchhiker/HeartOfGold");
  }
}
