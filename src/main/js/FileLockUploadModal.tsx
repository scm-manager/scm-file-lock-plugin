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
import React, { FC, useEffect, useState } from "react";
import { File, Repository } from "@scm-manager/ui-types";
import { Button, ButtonGroup, ErrorNotification, Modal } from "@scm-manager/ui-components";
import { useFileLocks, useUnlockFiles } from "./useFileLock";
import { FileLock } from "./FileLockAction";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
  files: File[];
  isValid: (valid: boolean) => void;
};

const FileLockUploadModal: FC<Props> = ({ repository, files, isValid }) => {
  const { data, error } = useFileLocks(repository);
  const { unlockFiles, error: unlockError } = useUnlockFiles(repository);
  const [showModal, setShowModal] = useState(false);
  const [t] = useTranslation("plugins");

  useEffect(() => {
    if (data) {
      const valid = validate();
      isValid(valid);
      setShowModal(!valid);
    }
  }, [repository, files, data]);

  const validate = () => {
    let valid = true;
    for (let file of files) {
      if ((data?._embedded?.fileLocks as FileLock[]).some(lockedFile => lockedFile.path === file.path)) {
        valid = false;
      }
    }
    return valid;
  };

  const unlockConflictingFiles = () => {
    const conflictingLocks: FileLock[] = [];
    for (let file of files) {
      for (let lock of data?._embedded?.fileLocks as FileLock[]) {
        if (lock.path === file.path) {
          conflictingLocks.push(lock);
        }
      }
    }
    unlockFiles(conflictingLocks);
  };

  if (error || unlockError) {
    return <ErrorNotification error={error || unlockError} />;
  }

  if (data) {
    return (
      <Modal
        closeFunction={() => setShowModal(false)}
        active={showModal}
        headColor="warning"
        title={t("scm-file-lock-plugin.uploadLockModal.title")}
        body={t("scm-file-lock-plugin.uploadLockModal.description")}
        footer={
          <ButtonGroup>
            <Button
              color="warning"
              label={t("scm-file-lock-plugin.uploadLockModal.unlockButton")}
              action={() => unlockConflictingFiles()}
            />
          </ButtonGroup>
        }
      />
    );
  }

  return null;
};

export default FileLockUploadModal;
