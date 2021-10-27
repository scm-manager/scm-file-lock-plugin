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
import { apiClient } from "@scm-manager/ui-components";
import { File, HalRepresentation, Link, Repository } from "@scm-manager/ui-types";
import { useMutation, useQueryClient, useQuery } from "react-query";
import { FileLock } from "./FileLockAction";

export const useFileLock = (repository: Repository, file: File) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error, Promise<Response>>(
    (promise: Promise<Response>) => promise,
    {
      onSuccess: () => {
        return queryClient.invalidateQueries(["repository", repository.namespace, repository.name]);
      }
    }
  );
  return {
    lock: file._links.lock ? () => mutate(apiClient.post((file._links.lock as Link).href)) : undefined,
    unlock: file._links.unlock ? () => mutate(apiClient.delete((file._links.unlock as Link).href)) : undefined,
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
