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

import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import jakarta.inject.Inject;

public class RepositoryConfigStore {

  private static final String STORE_NAME = "lock-config";

  private final ConfigurationStoreFactory configurationStoreFactory;

  @Inject
  public RepositoryConfigStore(ConfigurationStoreFactory configurationStoreFactory) {
    this.configurationStoreFactory = configurationStoreFactory;
  }

  public RepositoryConfig getConfig(Repository repository) {
    return createStore(repository).getOptional().orElse(new RepositoryConfig());
  }

  public void updateConfig(Repository repository, RepositoryConfig config) {
    PermissionCheck.checkConfigure(repository);
    createStore(repository).set(config);
  }

  private ConfigurationStore<RepositoryConfig> createStore(Repository repository) {
    return configurationStoreFactory.withType(RepositoryConfig.class).withName(STORE_NAME).forRepository(repository).build();
  }
}
