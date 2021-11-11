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
