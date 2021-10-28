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

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.stream.Collectors;

@Path("v2/file-lock")
@OpenAPIDefinition(tags = {
  @Tag(name = "File Lock", description = "File Lock plugin related endpoints")
})
@GenerateLinkBuilder(className = "RestApiLinks")
public class FileLockResource {

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
      service.getLockCommand().lock().setFile(path).execute();
    }
  }

  @DELETE
  @Path("{namespace}/{name}/unlock/{path}")
  @Operation(
    summary = "Removes file lock",
    description = "Unlocks a single locked file.",
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
      service.getLockCommand().unlock().setFile(path).force(true).execute();
    }
  }

  @GET
  @Path("{namespace}/{name}")
  @Produces("application/json")
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

