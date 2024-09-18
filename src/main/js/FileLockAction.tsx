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
import { File, HalRepresentation, Repository } from "@scm-manager/ui-types";
import { Button, ButtonGroup, Modal, Tooltip, useDateFormatter } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useFileLock } from "./useFileLock";
import { DarkHoverIcon } from "./FileLockDownloadAction";

type Props = {
  repository: Repository;
  file: File;
  type: "BUTTON" | "ICON";
};

export type FileLock = HalRepresentation & {
  username: string;
  timestamp: Date;
  path: string;
  owned: boolean;
  filename?: string;
};

type ModalProps = {
  fileLock: FileLock;
  unlock: () => void;
  setShowModal: (show: boolean) => void;
};

const UnlockModal: FC<ModalProps> = ({ fileLock, setShowModal, unlock }) => {
  const [t] = useTranslation("plugins");
  const initialFocusRef = useRef<HTMLButtonElement>(null);

  return (
    <Modal
      active={true}
      closeFunction={() => setShowModal(false)}
      headColor="warning"
      title={t("scm-file-lock-plugin.unlockModal.title", { username: fileLock.username })}
      body={t("scm-file-lock-plugin.unlockModal.description", { username: fileLock.username })}
      initialFocusRef={initialFocusRef}
      footer={
        <ButtonGroup>
          <Button
            color="warning"
            label={t("scm-file-lock-plugin.unlockModal.unlockButton")}
            action={() => {
              unlock();
              setShowModal(false);
            }}
            ref={initialFocusRef}
          />
          <Button label={t("scm-file-lock-plugin.unlockModal.cancelButton")} action={() => setShowModal(false)} />
        </ButtonGroup>
      }
    />
  );
};

const FileLockAction: FC<Props> = ({ repository, file, type }) => {
  const [t] = useTranslation("plugins");
  const { isLoading, lock, unlock } = useFileLock(repository, file);
  const fileLock: FileLock = file._embedded?.fileLock;
  const formatter = useDateFormatter({ date: fileLock?.timestamp });
  const [showUnlockModal, setShowUnlockModal] = useState(false);

  const resolveLockColor = () => {
    return fileLock.owned ? "success" : "warning";
  };

  const resolveUnlockAction = () => {
    if (unlock) {
      if (fileLock.owned) {
        unlock();
      } else {
        setShowUnlockModal(true);
      }
    }
  };

  if (!fileLock && lock) {
    return (
      <Tooltip
        message={t("scm-file-lock-plugin.unlockIcon.tooltip")}
        location="top"
        className={type === "BUTTON" ? "pr-2" : ""}
      >
        {type === "ICON" ? (
          <DarkHoverIcon name="lock-open" color="success" onClick={lock} tabIndex={0} onEnter={lock} />
        ) : (
          <Button icon="lock-open" loading={isLoading} action={lock} />
        )}
      </Tooltip>
    );
  } else if (unlock) {
    return (
      <Tooltip
        message={
          fileLock.owned
            ? t("scm-file-lock-plugin.lockIcon.tooltip.owned", { timestamp: formatter?.formatDistance() })
            : t("scm-file-lock-plugin.lockIcon.tooltip.default", {
                userId: fileLock.username,
                timestamp: formatter?.formatDistance()
              })
        }
        location="top"
        className={type === "BUTTON" ? "pr-2" : ""}
      >
        {type === "ICON" ? (
          <DarkHoverIcon
            name="lock"
            color={resolveLockColor()}
            onClick={resolveUnlockAction}
            tabIndex={0}
            onEnter={resolveUnlockAction}
          />
        ) : (
          <Button icon="lock" color={resolveLockColor()} loading={isLoading} action={resolveUnlockAction} />
        )}
        {showUnlockModal ? <UnlockModal fileLock={fileLock} unlock={unlock} setShowModal={setShowUnlockModal} /> : null}
      </Tooltip>
    );
  }
  return null;
};

export const FileLockButton: FC<Props> = ({ repository, file }) => {
  return <FileLockAction repository={repository} file={file} type="BUTTON" />;
};

export const FileLockIcon: FC<Props> = ({ repository, file }) => {
  return <FileLockAction repository={repository} file={file} type="ICON" />;
};
