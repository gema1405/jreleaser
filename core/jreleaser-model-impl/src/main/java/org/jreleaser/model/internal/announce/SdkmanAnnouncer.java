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
package org.jreleaser.model.internal.announce;

import org.jreleaser.model.Active;
import org.jreleaser.model.Sdkman;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.SdkmanAnnouncer.TYPE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SdkmanAnnouncer extends AbstractAnnouncer<SdkmanAnnouncer, org.jreleaser.model.api.announce.SdkmanAnnouncer> {
    private String consumerKey;
    private String consumerToken;
    private String candidate;
    private String releaseNotesUrl;
    private String downloadUrl;
    private Sdkman.Command command;

    private final org.jreleaser.model.api.announce.SdkmanAnnouncer immutable = new org.jreleaser.model.api.announce.SdkmanAnnouncer() {
        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.SdkmanAnnouncer.TYPE;
        }

        @Override
        public String getConsumerKey() {
            return consumerKey;
        }

        @Override
        public String getConsumerToken() {
            return consumerToken;
        }

        @Override
        public String getCandidate() {
            return candidate;
        }

        @Override
        public String getReleaseNotesUrl() {
            return releaseNotesUrl;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public Sdkman.Command getCommand() {
            return command;
        }

        @Override
        public boolean isMajor() {
            return SdkmanAnnouncer.this.isMajor();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return SdkmanAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return SdkmanAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SdkmanAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SdkmanAnnouncer.this.getPrefix();
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

    public SdkmanAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.SdkmanAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SdkmanAnnouncer source) {
        super.merge(source);
        this.consumerKey = merge(this.consumerKey, source.consumerKey);
        this.consumerToken = merge(this.consumerToken, source.consumerToken);
        this.candidate = merge(this.candidate, source.candidate);
        this.releaseNotesUrl = merge(this.releaseNotesUrl, source.releaseNotesUrl);
        this.downloadUrl = merge(this.downloadUrl, source.downloadUrl);
        this.command = merge(this.command, source.command);
    }

    @Override
    public boolean isSnapshotSupported() {
        return false;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerToken() {
        return consumerToken;
    }

    public void setConsumerToken(String consumerToken) {
        this.consumerToken = consumerToken;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Sdkman.Command getCommand() {
        return command;
    }

    public void setCommand(Sdkman.Command command) {
        this.command = command;
    }

    public void setCommand(String str) {
        setCommand(Sdkman.Command.of(str));
    }

    public boolean isCommandSet() {
        return command != null;
    }

    public boolean isMajor() {
        return command == Sdkman.Command.MAJOR;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("consumerKey", isNotBlank(consumerKey) ? HIDE : UNSET);
        props.put("consumerToken", isNotBlank(consumerToken) ? HIDE : UNSET);
        props.put("candidate", candidate);
        props.put("releaseNotesUrl", releaseNotesUrl);
        props.put("downloadUrl", downloadUrl);
        props.put("command", command);
    }
}
