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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Button, Icon, Tooltip } from "@scm-manager/ui-core";
import { File, Link, Repository } from "@scm-manager/ui-types";
import { useFileLock } from "./useFileLock";
import FileLockDownloadModal from "./FileLockDownloadModal";
import { FileLock } from "./FileLockAction";

type Props = {
  repository: Repository;
  file: File;
  type: "ICON" | "LARGE_BUTTON";
};

const FileLockDownloadAction: FC<Props> = ({ repository, file, type }) => {
  const [t] = useTranslation("plugins");
  const { lock, unlock } = useFileLock(repository, file);
  const [showModal, setShowModal] = useState(false);

  const downloadFile = (filePath: string) => {
    let link = document.createElement("a");
    link.href = filePath;
    link.download = filePath.slice(filePath.lastIndexOf("/") + 1);
    link.click();
  };

  const downloadTooltip = t("scm-file-lock-plugin.downloadLock.label");
  if (lock) {
    return (
      <>
        <Tooltip message={downloadTooltip}>
          <Button
            className={`is-info ${type === "ICON" && "is-inverted p-0 m-0"}`}
            onClick={() => setShowModal(true)}
            aria-label={downloadTooltip}
          >
            <Icon>download</Icon> {type === "LARGE_BUTTON" ? <span>{downloadTooltip}</span> : null}
          </Button>
        </Tooltip>
        {showModal && <FileLockDownloadModal repository={repository} file={file} close={() => setShowModal(false)} />}
      </>
    );
  }

  if (unlock) {
    const fileLock = file._embedded?.fileLock as FileLock;
    const ownedColor = fileLock.owned ? "info" : "warning";
    const ownedTooltip = fileLock.owned ? downloadTooltip : t("scm-file-lock-plugin.downloadLock.tooltip");

    return (
      <Tooltip message={ownedTooltip}>
        <Button
          className={`is-${ownedColor} ${type === "ICON" && "is-inverted p-0 m-0"}`}
          onClick={() => downloadFile((file._links.self as Link).href)}
          aria-label={downloadTooltip}
        >
          <Icon>download</Icon> {type === "LARGE_BUTTON" ? <span>{downloadTooltip}</span> : null}
        </Button>
      </Tooltip>
    );
  }

  return (
    <Tooltip message={downloadTooltip}>
      <Button
        className={`is-info ${type === "ICON" && "is-inverted p-0 m-0"}`}
        onClick={() => downloadFile((file._links.self as Link).href)}
        aria-label={downloadTooltip}
      >
        <Icon>download</Icon> {type === "LARGE_BUTTON" ? <span>{downloadTooltip}</span> : null}
      </Button>
    </Tooltip>
  );
};

export default FileLockDownloadAction;
