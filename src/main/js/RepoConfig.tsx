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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Subtitle, ConfigurationForm, Form, useDocumentTitleForRepository } from "@scm-manager/ui-core";
import { HalRepresentation, Repository } from "@scm-manager/ui-types";

type FileLockConfig = HalRepresentation & {
  enabled: boolean;
};

type Props = {
  link: string;
  repository: Repository;
};

const RepoConfig: FC<Props> = ({ link, repository }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitleForRepository(repository, t("scm-file-lock-plugin.config.subtitle"));

  return (
    <>
      <Subtitle>{t("scm-file-lock-plugin.config.subtitle")}</Subtitle>
      <ConfigurationForm<FileLockConfig> link={link} translationPath={["plugins", "scm-file-lock-plugin.config"]}>
        <Form.Checkbox name="enabled" />
      </ConfigurationForm>
    </>
  );
};

export default RepoConfig;
