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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.project.Project;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractRepositoryTap<S extends AbstractRepositoryTap<S>> extends AbstractModelObject<S> implements RepositoryTap {
    protected Active active;
    @JsonIgnore
    protected boolean enabled;
    @JsonIgnore
    protected String basename;
    @JsonIgnore
    protected String tapName;
    protected String owner;
    protected String name;
    protected String tagName;
    protected String branch;
    protected String username;
    protected String token;
    protected String commitMessage;

    public AbstractRepositoryTap(String basename, String tapName) {
        this.basename = basename;
        this.tapName = tapName;
    }

    @Override
    public String getBasename() {
        return basename;
    }

    public void setTapName(String tapName) {
        this.tapName = tapName;
    }

    @Override
    public void merge(S source) {
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        this.owner = merge(this.owner, source.owner);
        this.name = merge(this.name, source.name);
        this.tagName = merge(this.tagName, source.tagName);
        this.branch = merge(this.branch, source.branch);
        this.username = merge(this.username, source.username);
        this.token = merge(this.token, source.token);
        this.commitMessage = merge(this.commitMessage, source.commitMessage);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.RELEASE;
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    @Override
    public String getCanonicalRepoName() {
        return owner + "/" + getResolvedName();
    }

    public String getResolvedName() {
        if (isNotBlank(name)) {
            return name;
        }
        return tapName;
    }

    public String getResolvedCommitMessage(Map<String, Object> props) {
        return resolveTemplate(commitMessage, props);
    }

    public String getResolvedTagName(Map<String, Object> props) {
        return resolveTemplate(tagName, props);
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.put("owner", owner);
        map.put("name", getResolvedName());
        map.put("tagName", tagName);
        map.put("branch", branch);
        map.put("username", username);
        map.put("token", isNotBlank(token) ? HIDE : UNSET);
        map.put("commitMessage", commitMessage);
        return map;
    }
}
