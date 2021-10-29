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
import React, { FC, useState } from "react";
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

  return (
    <Modal
      title={t("scm-file-lock-plugin.downloadModal.title")}
      active={true}
      closeFunction={onClose}
      body={t("scm-file-lock-plugin.downloadModal.description")}
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
          <DarkHoverIcon name="download" color="info" onClick={() => setShowModal(true)} tabIndex={0} />
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
    if (file._embedded?.fileLock?.writeAccess) {
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
              tabIndex={0}
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
