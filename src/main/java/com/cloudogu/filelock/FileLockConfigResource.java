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

import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Path("v2/file-lock")
public class FileLockConfigResource {

  public static final String MEDIA_TYPE = VndMediaType.PREFIX + "file-lock-config" + VndMediaType.SUFFIX;

  private final RepositoryConfigStore configStore;
  private final RepositoryManager repositoryManager;

  @Inject
  public FileLockConfigResource(RepositoryConfigStore configStore, RepositoryManager repositoryManager) {
    this.configStore = configStore;
    this.repositoryManager = repositoryManager;
  }

  @GET
  @Path("{namespace}/{name}/config")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Repository file lock configuration",
    description = "Returns the repository-specific file lock configuration.",
    tags = "File Lock Configuration",
    operationId = "file_lock_get_repo_config"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MEDIA_TYPE,
      schema = @Schema(implementation = RepositoryConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configureFileLock\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public RepositoryConfigDto getRepositoryConfig(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw notFound(entity(new NamespaceAndName(namespace, name)));
    }
    PermissionCheck.checkConfigure(repository);
    Links links = createLinks(uriInfo, repository);
    return RepositoryConfigDto.from(configStore.getConfig(repository), links);
  }

  private Links createLinks(UriInfo uriInfo, Repository repository) {
    RestApiLinks restApiLinks = new RestApiLinks(uriInfo);
    Links.Builder links = Links.linkingTo();
    links.self(restApiLinks.fileLockConfig().getRepositoryConfig(repository.getNamespace(), repository.getName()).asString());
    if (PermissionCheck.mayConfigure(repository)) {
      links.single(Link.link("update", restApiLinks.fileLockConfig().setRepositoryConfig(repository.getNamespace(), repository.getName()).asString()));
    }
    return links.build();
  }

  @PUT
  @Path("{namespace}/{name}/config")
  @Consumes(MEDIA_TYPE)
  @Operation(
    summary = "Update Repository file lock configuration",
    description = "Modifies the repository-specific file lock configuration.",
    tags = "File Lock Configuration",
    operationId = "file_lock_put_repo_config"
  )
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configureFileLock\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void setRepositoryConfig(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid RepositoryConfigDto configDto) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if (repository == null) {
      throw notFound(entity(new NamespaceAndName(namespace, name)));
    }
    configStore.updateConfig(repository, configDto.toEntity());
  }
}
