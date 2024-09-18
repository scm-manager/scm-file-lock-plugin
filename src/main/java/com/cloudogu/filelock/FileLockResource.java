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

import com.cloudogu.jaxrstie.GenerateLinkBuilder;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.stream.Collectors;

@Path("v2/file-lock")
@OpenAPIDefinition(tags = {
  @Tag(name = "File Lock", description = "File Lock plugin related endpoints")
})
@GenerateLinkBuilder(className = "RestApiLinks")
public class FileLockResource {

  public static final String MEDIA_TYPE = VndMediaType.PREFIX + "file-lock" + VndMediaType.SUFFIX;

  private final RepositoryServiceFactory serviceFactory;
  private final FileLockMapper mapper;

  @Inject
  public FileLockResource(RepositoryServiceFactory serviceFactory, FileLockMapper mapper) {
    this.serviceFactory = serviceFactory;
    this.mapper = mapper;
  }

  @POST
  @Path("{namespace}/{name}/lock/{path}")
  @Operation(
    summary = "Add file lock",
    description = "Locks a single file to prevent write access.",
    tags = "File Lock",
    operationId = "file_lock_lock"
  )
  @ApiResponse(responseCode = "204", description = "no content")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the push privilege on this repository")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void lockFile(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("path") String path) {
    try (RepositoryService service = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      RepositoryPermissions.push(service.getRepository()).check();
      service.getLockCommand().lock(path).execute();
    }
  }

  @DELETE
  @Path("{namespace}/{name}/lock/{path}")
  @Operation(
    summary = "Removes file lock",
    description = "Removes lock from a single locked file.",
    tags = "File Lock",
    operationId = "file_lock_unlock"
  )
  @ApiResponse(responseCode = "204", description = "delete successful or nothing to do")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the push privilege on this repository")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void unlockFile(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("path") String path) {
    try (RepositoryService service = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      RepositoryPermissions.push(service.getRepository()).check();
      service.getLockCommand().unlock(path).force(true).execute();
    }
  }

  @GET
  @Path("{namespace}/{name}")
  @Produces(MEDIA_TYPE)
  @Operation(
    summary = "Get all locked files",
    description = "Get all locked files for repository.",
    tags = "File Lock",
    operationId = "file_lock_get_all"
  )
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the push privilege on this repository")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public HalRepresentation getAll(@Context UriInfo uriInfo, @PathParam("namespace") String namespace, @PathParam("name") String name) {
    try (RepositoryService service = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = service.getRepository();
      RepositoryPermissions.push(repository).check();
      return new HalRepresentation(
        createLinks(uriInfo, repository),
        createEmbedded(service, repository));
    }
  }

  private Links createLinks(UriInfo uriInfo, Repository repository) {
    String selfLink = new RestApiLinks(uriInfo).fileLock().getAll(repository.getNamespace(), repository.getName()).asString();
    return Links.linkingTo().self(selfLink).build();
  }

  private Embedded createEmbedded(RepositoryService service, Repository repository) {
    return Embedded.embeddedBuilder().with(
      "fileLocks",
      service.getLockCommand().getAll().stream()
        .map((FileLock fileLock) -> mapper.map(repository, fileLock))
        .collect(Collectors.toList()
      )
    ).build();
  }
}

