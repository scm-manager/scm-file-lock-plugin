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

import { binder } from "@scm-manager/ui-extensions";
import { FileLockButton, FileLockIcon } from "./FileLockAction";
import { FileLockDownloadButton, FileLockDownloadIcon, FileLockLargeDownloadButton } from "./FileLockDownloadAction";
import FileLockUploadModal from "./FileLockUploadModal";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import RepoConfig from "./config/RepoConfig";

binder.bind("repos.sources.tree.row.right", FileLockIcon, { priority: 1000 });
binder.bind("repos.sources.content.actionbar", FileLockButton, { priority: 1000 });
binder.bind("repos.sources.content.actionbar.download", FileLockDownloadButton, { priority: 1000 });
binder.bind("repos.sources.actionbar.download", FileLockDownloadIcon, { priority: 1000 });
binder.bind("editorPlugin.file.upload.validation", FileLockUploadModal);
binder.bind("repos.sources.content.downloadButton", FileLockLargeDownloadButton);
cfgBinder.bindRepositorySetting(
  "/filelock-config",
  "scm-file-lock-plugin.navLink.config",
  "fileLockConfig",
  RepoConfig
);
