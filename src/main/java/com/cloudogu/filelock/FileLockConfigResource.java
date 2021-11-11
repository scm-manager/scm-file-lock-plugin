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

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

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
