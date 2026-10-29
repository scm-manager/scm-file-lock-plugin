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

import React from "react";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import FileLockAction from "./FileLockAction";
import FileLockDownloadAction from "./FileLockDownloadAction";
import FileLockUploadModal from "./FileLockUploadModal";
import RepoConfig from "./RepoConfig";
import FileLockDownloadModalAction from "./FileLockDownloadModalAction";

binder.bind("repos.sources.tree.row.right", (props: any) => <FileLockAction type="ICON" {...props} />, {
  priority: 1000,
});
binder.bind("repos.sources.content.actionbar", (props: any) => <FileLockAction type="BUTTON" {...props} />, {
  priority: 1000,
});

binder.bind<extensionPoints.FileViewActionBarOverflowMenu>(
  "repos.sources.content.actionbar.menu",
  {
    category: "Editor",
    label: "scm-file-lock-plugin.downloadLock.label",
    icon: "download",
    modalElement: FileLockDownloadModalAction,
  },
  {
    predicate: (props) => props.file && ("lock" in props.file._links || "unlock" in props.file._links),
    priority: 1000,
  },
);
binder.bind("repos.sources.actionbar.download", (props: any) => <FileLockDownloadAction type="ICON" {...props} />, {
  priority: 1000,
});
binder.bind("repos.sources.content.downloadButton", (props: any) => (
  <FileLockDownloadAction type="LARGE_BUTTON" {...props} />
));

binder.bind("editorPlugin.file.upload.validation", FileLockUploadModal);

cfgBinder.bindRepositorySetting(
  "/filelock-config",
  "scm-file-lock-plugin.navLink.config",
  "fileLockConfig",
  RepoConfig,
);
