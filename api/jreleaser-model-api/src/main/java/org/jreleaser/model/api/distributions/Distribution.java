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
package org.jreleaser.model.api.distributions;

import org.jreleaser.model.Stereotype;
import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Artifact;
import org.jreleaser.model.api.common.Executable;
import org.jreleaser.model.api.common.ExtraProperties;
import org.jreleaser.model.api.common.Java;
import org.jreleaser.model.api.packagers.Packagers;
import org.jreleaser.model.api.platform.Platform;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Distribution extends Packagers, ExtraProperties, Activatable {
    EnumSet<org.jreleaser.model.Distribution.DistributionType> JAVA_DISTRIBUTION_TYPES = EnumSet.of(
        org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY,
        org.jreleaser.model.Distribution.DistributionType.JLINK,
        org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR);

    Platform getPlatform();

    org.jreleaser.model.Distribution.DistributionType getType();

    Stereotype getStereotype();

    String getName();

    Executable getExecutable();

    Set<? extends Artifact> getArtifacts();

    List<String> getTags();

    Java getJava();
}
