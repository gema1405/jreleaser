/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.model.internal.upload;

import org.jreleaser.model.Active;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.api.upload.ScpUploader.TYPE;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class ScpUploader extends AbstractSshUploader<org.jreleaser.model.api.upload.ScpUploader, ScpUploader> {
    private final org.jreleaser.model.api.upload.ScpUploader immutable = new org.jreleaser.model.api.upload.ScpUploader() {
        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public Integer getPort() {
            return port;
        }

        @Override
        public String getKnownHostsFile() {
            return knownHostsFile;
        }

        @Override
        public String getPublicKey() {
            return publicKey;
        }

        @Override
        public String getPrivateKey() {
            return privateKey;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public String getFingerprint() {
            return fingerprint;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return ScpUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return ScpUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return ScpUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return ScpUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return ScpUploader.this.isChecksums();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return ScpUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ScpUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ScpUploader.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }

        @Override
        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        @Override
        public Integer getReadTimeout() {
            return readTimeout;
        }
    };

    public ScpUploader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.upload.ScpUploader asImmutable() {
        return immutable;
    }
}
