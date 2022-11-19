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
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.CommitAuthor;
import org.jreleaser.model.internal.common.CommitAuthorAware;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.model.api.packagers.DockerPackager.SKIP_DOCKER;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class DockerPackager extends AbstractDockerConfiguration<DockerPackager> implements RepositoryPackager<org.jreleaser.model.api.packagers.DockerPackager>, CommitAuthorAware {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
    }

    private final Map<String, DockerSpec> specs = new LinkedHashMap<>();
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final DockerRepository repository = new DockerRepository();

    private Boolean continueOnError;
    private String downloadUrl;

    private final org.jreleaser.model.api.packagers.DockerPackager immutable = new org.jreleaser.model.api.packagers.DockerPackager() {
        private Set<? extends org.jreleaser.model.api.packagers.DockerPackager.Registry> registries;
        private Map<String, ? extends org.jreleaser.model.api.packagers.DockerSpec> specs;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.packagers.DockerSpec> getSpecs() {
            if (null == specs) {
                specs = DockerPackager.this.specs.values().stream()
                    .map(DockerSpec::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.packagers.DockerSpec::getName, identity()));
            }
            return specs;
        }

        @Override
        public DockerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return commitAuthor.asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return templateDirectory;
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(skipTemplates);
        }

        @Override
        public String getBaseImage() {
            return baseImage;
        }

        @Override
        public Map<String, String> getLabels() {
            return unmodifiableMap(labels);
        }

        @Override
        public Set<String> getImageNames() {
            return unmodifiableSet(imageNames);
        }

        @Override
        public List<String> getBuildArgs() {
            return unmodifiableList(buildArgs);
        }

        @Override
        public List<String> getPreCommands() {
            return unmodifiableList(preCommands);
        }

        @Override
        public List<String> getPostCommands() {
            return unmodifiableList(postCommands);
        }

        @Override
        public Set<? extends org.jreleaser.model.api.packagers.DockerPackager.Registry> getRegistries() {
            if (null == registries) {
                registries = DockerPackager.this.registries.stream()
                    .map(DockerConfiguration.Registry::asImmutable)
                    .collect(toSet());
            }
            return registries;
        }

        @Override
        public boolean isUseLocalArtifact() {
            return DockerPackager.this.isUseLocalArtifact();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return DockerPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
            return DockerPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
            return DockerPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return DockerPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return DockerPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return DockerPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return DockerPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(DockerPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return DockerPackager.this.getPrefix();
        }

        @Override
        public Buildx getBuildx() {
            return DockerPackager.this.getBuildx().asImmutable();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    @JsonIgnore
    private boolean failed;

    @Override
    public org.jreleaser.model.api.packagers.DockerPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(DockerPackager source) {
        super.merge(source);
        this.continueOnError = merge(this.continueOnError, source.continueOnError);
        this.downloadUrl = merge(this.downloadUrl, source.downloadUrl);
        this.failed = source.failed;
        setSpecs(mergeModel(this.specs, source.specs));
        setCommitAuthor(source.commitAuthor);
        setRepository(source.repository);
    }

    @Override
    public void fail() {
        this.failed = true;
    }

    @Override
    public boolean isFailed() {
        return failed;
    }

    @Override
    public boolean isContinueOnError() {
        return continueOnError != null && continueOnError;
    }

    @Override
    public void setContinueOnError(Boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    @Override
    public boolean isContinueOnErrorSet() {
        return continueOnError != null;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isUnix(platform);
    }

    @Override
    public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return SUPPORTED.containsKey(distributionType);
    }

    @Override
    public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return unmodifiableSet(SUPPORTED.getOrDefault(distributionType, emptySet()));
    }

    @Override
    public Set<Stereotype> getSupportedStereotypes() {
        return EnumSet.allOf(Stereotype.class);
    }

    @Override
    public List<Artifact> resolveCandidateArtifacts(JReleaserContext context, Distribution distribution) {
        List<String> fileExtensions = new ArrayList<>(getSupportedFileExtensions(distribution.getType()));
        fileExtensions.sort(naturalOrder());

        return distribution.getArtifacts().stream()
            .filter(Artifact::isActive)
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getResolvedPath(context, distribution).toString().endsWith(ext)))
            .filter(artifact -> supportsPlatform(artifact.getPlatform()))
            .filter(this::isNotSkipped)
            .sorted(Artifact.comparatorByPlatform().thenComparingInt(artifact -> {
                String ext = FileType.getExtension(artifact.getResolvedPath(context, distribution));
                return fileExtensions.indexOf(ext);
            }))
            .collect(toList());
    }

    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_DOCKER));
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.merge(commitAuthor);
    }

    public List<DockerSpec> getActiveSpecs() {
        return specs.values().stream()
            .filter(DockerSpec::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, DockerSpec> getSpecs() {
        return specs;
    }

    public void setSpecs(Map<String, DockerSpec> specs) {
        this.specs.clear();
        this.specs.putAll(specs);
    }

    public void addSpecs(Map<String, DockerSpec> specs) {
        this.specs.putAll(specs);
    }

    public void addSpec(DockerSpec spec) {
        this.specs.put(spec.getName(), spec);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getType(), super.asMap(full));
        return map;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("commitAuthor", commitAuthor.asMap(full));
        props.put("repository", repository.asMap(full));
        props.put("downloadUrl", downloadUrl);
        props.put("continueOnError", isContinueOnError());
        List<Map<String, Object>> specs = this.specs.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!specs.isEmpty()) props.put("specs", specs);
    }

    public void setRepository(DockerRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getPackagerRepository();
    }

    public DockerRepository getPackagerRepository() {
        return repository;
    }

    public static final class DockerRepository extends AbstractRepositoryTap<DockerRepository> implements Domain {
        private Boolean versionedSubfolders;

        private final org.jreleaser.model.api.packagers.DockerPackager.DockerRepository immutable = new org.jreleaser.model.api.packagers.DockerPackager.DockerRepository() {
            @Override
            public boolean isVersionedSubfolders() {
                return DockerRepository.this.isVersionedSubfolders();
            }

            @Override
            public String getBasename() {
                return basename;
            }

            @Override
            public String getCanonicalRepoName() {
                return DockerRepository.this.getCanonicalRepoName();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getTagName() {
                return tagName;
            }

            @Override
            public String getBranch() {
                return branch;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getToken() {
                return token;
            }

            @Override
            public String getCommitMessage() {
                return commitMessage;
            }

            @Override
            public Active getActive() {
                return active;
            }

            @Override
            public boolean isEnabled() {
                return DockerRepository.this.isEnabled();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(DockerRepository.this.asMap(full));
            }

            @Override
            public String getOwner() {
                return owner;
            }
        };

        public DockerRepository() {
            super("docker", "docker");
        }

        public org.jreleaser.model.api.packagers.DockerPackager.DockerRepository asImmutable() {
            return immutable;
        }

        @Override
        public void merge(DockerRepository source) {
            super.merge(source);
            this.versionedSubfolders = this.merge(this.versionedSubfolders, source.versionedSubfolders);
        }

        public boolean isVersionedSubfolders() {
            return versionedSubfolders != null && versionedSubfolders;
        }

        public void setVersionedSubfolders(Boolean versionedSubfolders) {
            this.versionedSubfolders = versionedSubfolders;
        }

        public boolean isVersionedSubfoldersSet() {
            return versionedSubfolders != null;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = super.asMap(full);
            map.put("versionedSubfolders", isVersionedSubfolders());
            return map;
        }
    }
}
