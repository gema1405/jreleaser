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
package org.jreleaser.model.api.assemble;

import org.jreleaser.model.Archive;
import org.jreleaser.model.api.common.Artifact;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.Executable;
import org.jreleaser.model.api.common.Glob;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public interface JavaArchiveAssembler extends Assembler {
    String TYPE = "java-archive";

    String getArchiveName();

    Set<Archive.Format> getFormats();

    String getTemplateDirectory();

    Artifact getMainJar();

    Java getJava();

    Executable getExecutable();

    List<? extends Glob> getJars();

    List<? extends Glob> getFiles();

    interface Java extends Domain {
        String getMainClass();

        String getMainModule();

        List<String> getOptions();
    }
}
