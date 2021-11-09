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

type Props = {
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

const RepoConfig: FC<Props> = ({ link }) => {
  const { data, error, isLoading } = useFileLockConfig(link);

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
