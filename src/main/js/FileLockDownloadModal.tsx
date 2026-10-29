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

import React, { FC, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Modal } from "@scm-manager/ui-components";
import { Button } from "@scm-manager/ui-core";
import { File, Link, Repository } from "@scm-manager/ui-types";
import { useFileLock } from "./useFileLock";

type Props = {
  repository: Repository;
  file: File;
  close: () => void;
};

const FileLockDownloadModal: FC<Props> = ({ repository, file, close }) => {
  const [t] = useTranslation("plugins");
  const { lock } = useFileLock(repository, file);
  const initialFocusRef = useRef<HTMLButtonElement>(null);

  const downloadFile = () => {
    const href = (file._links.self as Link).href;
    const link = document.createElement("a");
    link.href = href;
    link.download = href.substring(href.lastIndexOf("/") + 1);
    link.click();
  };

  const lockAndDownload = () => {
    if (lock) {
      lock();
    }
    downloadFile();
    close();
  };

  const downloadOnly = () => {
    downloadFile();
    close();
  };

  return (
    <Modal
      title={t("scm-file-lock-plugin.downloadModal.title")}
      active={true}
      closeFunction={close}
      body={t("scm-file-lock-plugin.downloadModal.description")}
      initialFocusRef={initialFocusRef}
      footer={
        <>
          <Button
            className="is-warning"
            onClick={lockAndDownload}
            ref={lock ? initialFocusRef : undefined}
            disabled={!lock}
          >
            {t("scm-file-lock-plugin.downloadModal.lockDownloadButton")}
          </Button>
          <Button onClick={downloadOnly} ref={!lock ? initialFocusRef : undefined}>
            {t("scm-file-lock-plugin.downloadModal.downloadButton")}
          </Button>
        </>
      }
    />
  );
};

export default FileLockDownloadModal;
