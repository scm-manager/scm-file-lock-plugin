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

import { apiClient } from "@scm-manager/ui-components";
import { File, HalRepresentation, Link, Repository } from "@scm-manager/ui-types";
import { useMutation, useQueryClient, useQuery } from "react-query";
import { FileLock } from "./FileLockAction";

export const useFileLock = (repository: Repository, file: File) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error, boolean>(
    (lock: boolean) => {
      if (lock) {
        return apiClient.post((file._links.lock as Link).href);
      } else {
        return apiClient.delete((file._links.unlock as Link).href);
      }
    },
    {
      onSuccess: () => {
        return queryClient.invalidateQueries(["repository", repository.namespace, repository.name]);
      }
    }
  );
  return {
    lock: file._links.lock ? () => mutate(true) : undefined,
    unlock: file._links.unlock ? () => mutate(false) : undefined,
    isLoading,
    error
  };
};

export const useUnlockFiles = (repository: Repository) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error, FileLock[]>(
    ["repository", repository.namespace, repository.name, "file-locks"],
    (locks: FileLock[]) => {
      const promises = [];
      for (let lock of locks) {
        promises.push(apiClient.delete((lock._links.unlock as Link).href));
      }
      return Promise.all(promises);
    },
    {
      onSuccess: () => {
        return queryClient.invalidateQueries(["repository", repository.namespace, repository.name]);
      }
    }
  );
  return {
    unlockFiles: (locks: FileLock[]) => mutate(locks),
    isLoading,
    error
  };
};

export const useFileLocks = (repository: Repository) => {
  const { error, isLoading, data } = useQuery<HalRepresentation, Error>(
    ["repository", repository.namespace, repository.name, "file-locks"],
    () => apiClient.get((repository._links.fileLocks as Link).href).then(res => res.json())
  );

  return {
    error,
    isLoading,
    data
  };
};
