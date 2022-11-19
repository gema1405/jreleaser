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
package org.jreleaser.model.internal.validation.assemble;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Archive;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public abstract class JavaArchiveAssemblerValidator extends Validator {
    public static void validateJavaArchive(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, JavaArchiveAssembler> archive = context.getModel().getAssemble().getJavaArchive();
        if (!archive.isEmpty()) context.getLogger().debug("assemble.java-archive");

        for (Map.Entry<String, JavaArchiveAssembler> e : archive.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateAssembly()) {
                validateJavaArchive(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateJavaArchive(JReleaserContext context, Mode mode, JavaArchiveAssembler archive, Errors errors) {
        context.getLogger().debug("assemble.java-archive.{}", archive.getName());

        if (!archive.isActiveSet()) {
            archive.setActive(Active.NEVER);
        }
        if (!archive.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isBlank(archive.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "java-archive.name"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            archive.disable();
            return;
        }

        if (null == archive.getStereotype()) {
            archive.setStereotype(context.getModel().getProject().getStereotype());
        }

        if (isBlank(archive.getArchiveName())) {
            archive.setArchiveName("{{distributionName}}-{{projectVersion}}");
        }

        if (isBlank(archive.getExecutable().getName())) {
            archive.getExecutable().setName(archive.getName());
        }

        if (isBlank(archive.getExecutable().getWindowsExtension())) {
            archive.getExecutable().setWindowsExtension("bat");
        }

        if (archive.getFormats().isEmpty()) {
            archive.addFormat(Archive.Format.ZIP);
        }

        if (archive.getJars().isEmpty() && isBlank(archive.getMainJar().getPath())) {
            errors.configuration(RB.$("validation_java_archive_empty_jars", archive.getName()));
        } else {
            validateGlobs(context,
                archive.getJars(),
                "java-archive." + archive.getName() + ".jars",
                errors);
        }

        validateGlobs(context,
            archive.getFiles(),
            "java-archive." + archive.getName() + ".files",
            errors);

        int i = 0;
        for (FileSet fileSet : archive.getFileSets()) {
            validateFileSet(context, mode, archive, fileSet, i++, errors);
        }

        String defaultTemplateDirectory = "src/jreleaser/assemblers/" + archive.getName() + "/" + archive.getType();
        if (isNotBlank(archive.getTemplateDirectory()) &&
            !defaultTemplateDirectory.equals(archive.getTemplateDirectory().trim()) &&
            !Files.exists(context.getBasedir().resolve(archive.getTemplateDirectory().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                archive.getType() + "." + archive.getName() + ".template", archive.getTemplateDirectory()));
        }
        if (isBlank(archive.getTemplateDirectory())) {
            archive.setTemplateDirectory(defaultTemplateDirectory);
        }

        context.getLogger().debug("assemble.java-archive.{}.java", archive.getName());

        Project project = context.getModel().getProject();
        boolean mainJarIsSet = isNotBlank(archive.getMainJar().getPath());

        if (!mainJarIsSet) {
            if (isBlank(archive.getJava().getMainModule())) {
                archive.getJava().setMainModule(project.getJava().getMainModule());
            }
            if (isBlank(archive.getJava().getMainClass())) {
                archive.getJava().setMainClass(project.getJava().getMainClass());
            }
        }

        boolean mainClassIsSet = isNotBlank(archive.getJava().getMainClass());

        if (!mainJarIsSet && !mainClassIsSet) {
            errors.configuration(RB.$("validation_java_archive_main_jar_or_class_missing", archive.getName(), archive.getName()));
        }
    }
}
