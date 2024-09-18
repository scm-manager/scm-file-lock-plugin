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

import {
  Checkbox,
  ErrorNotification,
  Level,
  Loading,
  SubmitButton,
  Subtitle,
  Notification
} from "@scm-manager/ui-components";
import React, { FC, useState } from "react";
import { FileLockConfig, useFileLockConfig, useUpdateFileLockConfig } from "./useFileLockConfig";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository;
  link: string;
};

type EditorProps = {
  data: FileLockConfig;
};

const RepoConfigEditor: FC<EditorProps> = ({ data }) => {
  const [t] = useTranslation("plugins");

  const { update, error: updateError, isLoading: updateLoading } = useUpdateFileLockConfig();
  const [config, setConfig] = useState<FileLockConfig>(data);
  const [showNotification, setShowNotification] = useState(false);

  return (
    <>
      <Subtitle subtitle={t("scm-file-lock-plugin.config.subtitle")} />
      {showNotification ? (
        <Notification type="info" onClose={() => setShowNotification(false)}>
          {t("scm-file-lock-plugin.config.notification")}
        </Notification>
      ) : null}
      <Checkbox
        label={t("scm-file-lock-plugin.config.enabled.label")}
        helpText={t(t("scm-file-lock-plugin.config.enabled.helpText"))}
        name="enabled"
        checked={config.enabled}
        disabled={updateLoading}
        onChange={value => setConfig({ ...config, enabled: value })}
      />
      <ErrorNotification error={updateError} />
      <Level
        right={
          <SubmitButton
            label={t("scm-file-lock-plugin.config.submitButton")}
            disabled={data === config}
            action={() => {
              update(config);
              setShowNotification(true);
            }}
            loading={updateLoading}
          />
        }
      />
    </>
  );
};

const RepoConfig: FC<Props> = ({ link, repository }) => {
  const { data, error, isLoading } = useFileLockConfig(repository, link);

  if (isLoading) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (data) {
    return <RepoConfigEditor data={data} />;
  }

  return null;
};

export default RepoConfig;
