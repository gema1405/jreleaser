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
package org.jreleaser.model.internal.assemble;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class Assemble extends AbstractModelObject<Assemble> implements Domain, Activatable {
    private final Map<String, ArchiveAssembler> archive = new LinkedHashMap<>();
    private final Map<String, JavaArchiveAssembler> javaArchive = new LinkedHashMap<>();
    private final Map<String, JlinkAssembler> jlink = new LinkedHashMap<>();
    private final Map<String, JpackageAssembler> jpackage = new LinkedHashMap<>();
    private final Map<String, NativeImageAssembler> nativeImage = new LinkedHashMap<>();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    private final org.jreleaser.model.api.assemble.Assemble immutable = new org.jreleaser.model.api.assemble.Assemble() {
        private Map<String, ? extends org.jreleaser.model.api.assemble.ArchiveAssembler> archive;
        private Map<String, ? extends org.jreleaser.model.api.assemble.JavaArchiveAssembler> javaArchive;
        private Map<String, ? extends org.jreleaser.model.api.assemble.JlinkAssembler> jlink;
        private Map<String, ? extends org.jreleaser.model.api.assemble.JpackageAssembler> jpackage;
        private Map<String, ? extends org.jreleaser.model.api.assemble.NativeImageAssembler> nativeImage;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.assemble.ArchiveAssembler> getArchive() {
            if (null == archive) {
                archive = Assemble.this.archive.values().stream()
                    .map(ArchiveAssembler::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.assemble.Assembler::getName, identity()));
            }
            return archive;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.assemble.JavaArchiveAssembler> getJavaArchive() {
            if (null == javaArchive) {
                javaArchive = Assemble.this.javaArchive.values().stream()
                    .map(JavaArchiveAssembler::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.assemble.Assembler::getName, identity()));
            }
            return javaArchive;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.assemble.JlinkAssembler> getJlink() {
            if (null == jlink) {
                jlink = Assemble.this.jlink.values().stream()
                    .map(JlinkAssembler::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.assemble.Assembler::getName, identity()));
            }
            return jlink;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.assemble.JpackageAssembler> getJpackage() {
            if (null == jpackage) {
                jpackage = Assemble.this.jpackage.values().stream()
                    .map(JpackageAssembler::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.assemble.Assembler::getName, identity()));
            }
            return jpackage;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.assemble.NativeImageAssembler> getNativeImage() {
            if (null == nativeImage) {
                nativeImage = Assemble.this.nativeImage.values().stream()
                    .map(NativeImageAssembler::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.assemble.Assembler::getName, identity()));
            }
            return nativeImage;
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return Assemble.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Assemble.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.assemble.Assemble asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Assemble source) {
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        setArchive(mergeModel(this.archive, source.archive));
        setJavaArchive(mergeModel(this.javaArchive, source.javaArchive));
        setJlink(mergeModel(this.jlink, source.jlink));
        setJpackage(mergeModel(this.jpackage, source.jpackage));
        setNativeImage(mergeModel(this.nativeImage, source.nativeImage));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Deprecated
    public void setEnabled(Boolean enabled) {
        nag("assemble.enabled is deprecated since 1.1.0 and will be removed in 2.0.0");
        if (null != enabled) {
            this.active = enabled ? Active.ALWAYS : Active.NEVER;
        }
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            setActive(Env.resolveOrDefault("assemble.active", "", "ALWAYS"));
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

    public List<ArchiveAssembler> getActiveArchives() {
        return archive.values().stream()
            .filter(ArchiveAssembler::isEnabled)
            .collect(toList());
    }

    public Map<String, ArchiveAssembler> getArchive() {
        return archive;
    }

    public void setArchive(Map<String, ArchiveAssembler> archive) {
        this.archive.clear();
        this.archive.putAll(archive);
    }

    public void addArchive(ArchiveAssembler archive) {
        this.archive.put(archive.getName(), archive);
    }

    public List<JavaArchiveAssembler> getActiveJavaArchives() {
        return javaArchive.values().stream()
            .filter(JavaArchiveAssembler::isEnabled)
            .collect(toList());
    }

    public Map<String, JavaArchiveAssembler> getJavaArchive() {
        return javaArchive;
    }

    public void setJavaArchive(Map<String, JavaArchiveAssembler> javaArchive) {
        this.javaArchive.clear();
        this.javaArchive.putAll(javaArchive);
    }

    public void addJavaArchive(JavaArchiveAssembler javaArchive) {
        this.javaArchive.put(javaArchive.getName(), javaArchive);
    }

    public List<JlinkAssembler> getActiveJlinks() {
        return jlink.values().stream()
            .filter(JlinkAssembler::isEnabled)
            .collect(toList());
    }

    public Map<String, JlinkAssembler> getJlink() {
        return jlink;
    }

    public void setJlink(Map<String, JlinkAssembler> jlink) {
        this.jlink.clear();
        this.jlink.putAll(jlink);
    }

    public void addJlink(JlinkAssembler jlink) {
        this.jlink.put(jlink.getName(), jlink);
    }

    public JlinkAssembler findJlink(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("Jlink name must not be blank");
        }

        if (jlink.containsKey(name)) {
            return jlink.get(name);
        }

        throw new JReleaserException("Jlink '" + name + "' not found");
    }

    public List<JpackageAssembler> getActiveJpackages() {
        return jpackage.values().stream()
            .filter(JpackageAssembler::isEnabled)
            .collect(toList());
    }

    public Map<String, JpackageAssembler> getJpackage() {
        return jpackage;
    }

    public void setJpackage(Map<String, JpackageAssembler> jpackage) {
        this.jpackage.clear();
        this.jpackage.putAll(jpackage);
    }

    public void addJpackage(JpackageAssembler jpackage) {
        this.jpackage.put(jpackage.getName(), jpackage);
    }

    public List<NativeImageAssembler> getActiveNativeImages() {
        return nativeImage.values().stream()
            .filter(NativeImageAssembler::isEnabled)
            .collect(toList());
    }

    public Map<String, NativeImageAssembler> getNativeImage() {
        return nativeImage;
    }

    public void setNativeImage(Map<String, NativeImageAssembler> nativeImage) {
        this.nativeImage.clear();
        this.nativeImage.putAll(nativeImage);
    }

    public void addNativeImage(NativeImageAssembler nativeImage) {
        this.nativeImage.put(nativeImage.getName(), nativeImage);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);

        List<Map<String, Object>> archive = this.archive.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!archive.isEmpty()) map.put("archive", archive);

        List<Map<String, Object>> javaArchive = this.javaArchive.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!javaArchive.isEmpty()) map.put("javaArchive", javaArchive);

        List<Map<String, Object>> jlink = this.jlink.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!jlink.isEmpty()) map.put("jlink", jlink);

        List<Map<String, Object>> jpackage = this.jpackage.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!jpackage.isEmpty()) map.put("jpackage", jpackage);

        List<Map<String, Object>> nativeImage = this.nativeImage.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!nativeImage.isEmpty()) map.put("nativeImage", nativeImage);

        return map;
    }

    public <A extends Assembler> Map<String, A> findAssemblersByType(String assemblerName) {
        switch (assemblerName) {
            case org.jreleaser.model.api.assemble.ArchiveAssembler.TYPE:
                return (Map<String, A>) archive;
            case org.jreleaser.model.api.assemble.JavaArchiveAssembler.TYPE:
                return (Map<String, A>) javaArchive;
            case org.jreleaser.model.api.assemble.JlinkAssembler.TYPE:
                return (Map<String, A>) jlink;
            case org.jreleaser.model.api.assemble.JpackageAssembler.TYPE:
                return (Map<String, A>) jpackage;
            case org.jreleaser.model.api.assemble.NativeImageAssembler.TYPE:
                return (Map<String, A>) nativeImage;
        }

        return Collections.emptyMap();
    }

    public <A extends Assembler> Collection<A> findAllAssemblers() {
        List<A> assemblers = new ArrayList<>();
        assemblers.addAll((List<A>) getActiveArchives());
        assemblers.addAll((List<A>) getActiveJavaArchives());
        assemblers.addAll((List<A>) getActiveJlinks());
        assemblers.addAll((List<A>) getActiveJpackages());
        assemblers.addAll((List<A>) getActiveNativeImages());
        return assemblers;
    }
}
