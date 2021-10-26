import { apiClient } from "@scm-manager/ui-components";
import { File, Link, Repository } from "@scm-manager/ui-types";
import { useMutation, useQueryClient } from "react-query";

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
