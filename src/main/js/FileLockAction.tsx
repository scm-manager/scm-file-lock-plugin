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
import React, { FC } from "react";
import { File, HalRepresentation, Repository } from "@scm-manager/ui-types";
import { Button, Tooltip, useDateFormatter } from "@scm-manager/ui-components";
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
  writeAccess: boolean;
};

const FileLockAction: FC<Props> = ({ repository, file, type }) => {
  const [t] = useTranslation("plugins");
  const { isLoading, lock, unlock } = useFileLock(repository, file);
  const fileLock: FileLock = file._embedded?.fileLock;
  const formatter = useDateFormatter({ date: fileLock?.timestamp });

  if (!fileLock && lock) {
    return (
      <Tooltip
        message={t("scm-file-lock-plugin.unlockIcon.tooltip")}
        location="top"
        className={type === "BUTTON" ? "pr-2" : ""}
      >
        {type === "ICON" ? (
          <DarkHoverIcon name="lock-open" color="success" onClick={lock} tabIndex={0} />
        ) : (
          <Button icon="lock-open" loading={isLoading} action={lock} />
        )}
      </Tooltip>
    );
  } else if (unlock) {
    return (
      <Tooltip
        message={t("scm-file-lock-plugin.lockIcon.tooltip", {
          userId: fileLock.username,
          timestamp: formatter?.formatDistance()
        })}
        location="top"
        className={type === "BUTTON" ? "pr-2" : ""}
      >
        {type === "ICON" ? (
          <DarkHoverIcon name="lock" color="warning" onClick={unlock} />
        ) : (
          <Button icon="lock" color="warning" loading={isLoading} action={unlock} />
        )}
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
