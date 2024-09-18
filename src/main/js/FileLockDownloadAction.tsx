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

import React, { FC, useRef, useState } from "react";
import { File, Link, Repository } from "@scm-manager/ui-types";
import { Button, ButtonGroup, Icon, Modal, Tooltip } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useFileLock } from "./useFileLock";
import styled from "styled-components";

type Props = {
  repository: Repository;
  file: File;
  type: "ICON" | "BUTTON" | "LARGE_BUTTON";
};

type ModalProps = {
  lock: () => void;
  onClose: () => void;
  downloadFile: () => void;
};

export const DarkHoverIcon = styled(Icon)`
  &:hover {
    color: #363636 !important;
  }
`;

const FileLockDownloadModal: FC<ModalProps> = ({ onClose, lock, downloadFile }) => {
  const [t] = useTranslation("plugins");
  const initialFocusRef = useRef<HTMLButtonElement>(null);

  return (
    <Modal
      title={t("scm-file-lock-plugin.downloadModal.title")}
      active={true}
      closeFunction={onClose}
      body={t("scm-file-lock-plugin.downloadModal.description")}
      initialFocusRef={initialFocusRef}
      footer={
        <ButtonGroup>
          <Button
            label={t("scm-file-lock-plugin.downloadModal.lockDownloadButton")}
            color="warning"
            action={() => {
              lock();
              downloadFile();
              onClose();
            }}
            ref={initialFocusRef}
          />
          <Button
            label={t("scm-file-lock-plugin.downloadModal.downloadButton")}
            action={() => {
              downloadFile();
              onClose();
            }}
          />
        </ButtonGroup>
      }
    />
  );
};

const FileLockDownloadAction: FC<Props> = ({ repository, file, type }) => {
  const [t] = useTranslation("plugins");
  const { lock, unlock } = useFileLock(repository, file);
  const [showModal, setShowModal] = useState(false);

  const downloadFile = (filePath: string) => {
    let link = document.createElement("a");
    link.href = filePath;
    link.download = filePath.substr(filePath.lastIndexOf("/") + 1);
    link.click();
  };

  if (lock) {
    return (
      <>
        {type === "BUTTON" ? <Button icon="download" action={() => setShowModal(true)} /> : null}
        {type === "LARGE_BUTTON" ? (
          <Button
            icon="download"
            color="info"
            label={t("scm-file-lock-plugin.downloadLock.label")}
            action={() => setShowModal(true)}
          />
        ) : null}
        {type === "ICON" ? (
          <DarkHoverIcon
            name="download"
            color="info"
            onClick={() => setShowModal(true)}
            onEnter={() => setShowModal(true)}
            tabIndex={0}
            className="is-clickable"
          />
        ) : null}
        {showModal && (
          <FileLockDownloadModal
            onClose={() => setShowModal(false)}
            lock={lock}
            downloadFile={() => downloadFile((file._links.self as Link).href)}
          />
        )}
      </>
    );
  }

  if (unlock) {
    if (file._embedded?.fileLock?.owned) {
      return (
        <>
          {type === "BUTTON" ? (
            <Button icon="download" action={() => downloadFile((file._links.self as Link).href)} />
          ) : null}
          {type === "LARGE_BUTTON" ? (
            <Button
              icon="download"
              color="info"
              label={t("scm-file-lock-plugin.downloadLock.label")}
              action={() => downloadFile((file._links.self as Link).href)}
            />
          ) : null}
          {type === "ICON" ? (
            <DarkHoverIcon
              name="download"
              color="info"
              onClick={() => downloadFile((file._links.self as Link).href)}
              onEnter={() => downloadFile((file._links.self as Link).href)}
              tabIndex={0}
              className="is-clickable"
            />
          ) : null}
        </>
      );
    } else {
      return (
        <Tooltip message={t("scm-file-lock-plugin.downloadLock.tooltip")} location="top">
          {type === "BUTTON" ? (
            <Button icon="download" color="warning" action={() => downloadFile((file._links.self as Link).href)} />
          ) : null}
          {type === "LARGE_BUTTON" ? (
            <Button
              icon="download"
              color="warning"
              label={t("scm-file-lock-plugin.downloadLock.label")}
              action={() => downloadFile((file._links.self as Link).href)}
            />
          ) : null}
          {type === "ICON" ? (
            <DarkHoverIcon
              name="download"
              color="warning"
              onClick={() => downloadFile((file._links.self as Link).href)}
              onEnter={() => downloadFile((file._links.self as Link).href)}
              tabIndex={0}
            />
          ) : null}
        </Tooltip>
      );
    }
  }

  return (
    <>
      {type === "LARGE_BUTTON" ? (
        <Button
          icon="download"
          color="info"
          label={t("scm-file-lock-plugin.downloadLock.label")}
          action={() => downloadFile((file._links.self as Link).href)}
        />
      ) : null}
      {type === "BUTTON" ? (
        <Button icon="download" action={() => downloadFile((file._links.self as Link).href)} />
      ) : null}
      {type === "ICON" ? (
        <DarkHoverIcon
          name="download"
          color="info"
          onClick={() => downloadFile((file._links.self as Link).href)}
          onEnter={() => downloadFile((file._links.self as Link).href)}
          tabIndex={0}
        />
      ) : null}
    </>
  );
};

type FileLockComponentProps = {
  repository: Repository;
  file: File;
};

export const FileLockDownloadButton: FC<FileLockComponentProps> = ({ repository, file }) => {
  return <FileLockDownloadAction repository={repository} file={file} type="BUTTON" />;
};

export const FileLockDownloadIcon: FC<FileLockComponentProps> = ({ repository, file }) => {
  return <FileLockDownloadAction repository={repository} file={file} type="ICON" />;
};

export const FileLockLargeDownloadButton: FC<FileLockComponentProps> = ({ repository, file }) => {
  return <FileLockDownloadAction repository={repository} file={file} type="LARGE_BUTTON" />;
};
