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

import { useMutation, useQueryClient, useQuery } from "react-query";
import { apiClient } from "@scm-manager/ui-components";
import { HalRepresentation, Link, Repository } from "@scm-manager/ui-types";

export type FileLockConfig = HalRepresentation & {
  enabled: boolean;
};

export const useFileLockConfig = (repository: Repository, link: string) => {
  const { error, isLoading, data } = useQuery<FileLockConfig, Error>(
    ["file-lock-config", repository.namespace, repository.name],
    () => apiClient.get(link).then(res => res.json())
  );

  return {
    error,
    isLoading,
    data
  };
};

export const useUpdateFileLockConfig = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error, FileLockConfig>(
    (config: FileLockConfig) => {
      return apiClient.put(
        (config._links.update as Link).href,
        config,
        "application/vnd.scmm-file-lock-config+json;v=2"
      );
    },
    {
      onSuccess: () => {
        return queryClient.invalidateQueries(["file-lock-config"]);
      }
    }
  );
  return {
    update: (config: FileLockConfig) => {
      mutate(config);
    },
    isLoading,
    error
  };
};
