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

import React, { FC, useEffect, useRef, useState } from "react";
import { File, Repository } from "@scm-manager/ui-types";
import {
  Button,
  ButtonGroup,
  Column,
  DateFromNow,
  ErrorNotification,
  Modal,
  Table,
  TextColumn
} from "@scm-manager/ui-components";
import { useFileLocks, useUnlockFiles } from "./useFileLock";
import { FileLock } from "./FileLockAction";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
  files: File[];
  shouldValidate: boolean;
  path: string;
};

const FileLockUploadModal: FC<Props> = ({ repository, files, path, shouldValidate }) => {
  const { data, error } = useFileLocks(repository);
  const { unlockFiles, error: unlockError } = useUnlockFiles(repository);
  const [showModal, setShowModal] = useState(false);
  const [t] = useTranslation("plugins");
  const initialFocusRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (data && shouldValidate) {
      const valid = validate();
      setShowModal(!valid);
    }
  }, [files, data, shouldValidate]);

  const resolveFilePath = (file: File) => {
    if (path) {
      return `${path}/${file.path}`;
    }
    return file.path;
  };

  const validate = () => {
    let valid = true;
    for (let file of files) {
      if (
        (data?._embedded?.fileLocks as FileLock[]).some(
          lockedFile => lockedFile.path === resolveFilePath(file) && !lockedFile.owned
        )
      ) {
        valid = false;
      }
    }
    return valid;
  };

  const getConflictingLocks = () => {
    const conflictingLocks: FileLock[] = [];
    for (let file of files) {
      if (data?._embedded?.fileLocks) {
        for (let lock of data._embedded.fileLocks as FileLock[]) {
          if (lock.path === resolveFilePath(file)) {
            conflictingLocks.push({ ...lock, filename: file.name });
          }
        }
      }
    }
    return conflictingLocks;
  };

  const unlockConflictingFiles = () => {
    unlockFiles(getConflictingLocks());
  };

  if (data) {
    return (
      <Modal
        closeFunction={() => setShowModal(false)}
        active={showModal}
        headColor="warning"
        title={t("scm-file-lock-plugin.uploadLockModal.title")}
        body={
          <>
            {t("scm-file-lock-plugin.uploadLockModal.description")}
            <br />
            <Table data={getConflictingLocks()}>
              <TextColumn header={t("scm-file-lock-plugin.uploadLockModal.column.file")} dataKey="filename" />
              <TextColumn header={t("scm-file-lock-plugin.uploadLockModal.column.user")} dataKey="username" />
              <Column header={t("scm-file-lock-plugin.uploadLockModal.column.date")}>
                {row => <DateFromNow date={row.timestamp} />}
              </Column>
            </Table>
            <ErrorNotification error={error || unlockError} />
          </>
        }
        footer={
          <ButtonGroup>
            <Button
              color="warning"
              label={t("scm-file-lock-plugin.uploadLockModal.unlockButton")}
              action={unlockConflictingFiles}
              ref={initialFocusRef}
            />
            <Button label={t("scm-file-lock-plugin.uploadLockModal.cancelButton")} action={() => setShowModal(false)} />
          </ButtonGroup>
        }
        initialFocusRef={initialFocusRef}
      />
    );
  }

  return null;
};

export default FileLockUploadModal;
