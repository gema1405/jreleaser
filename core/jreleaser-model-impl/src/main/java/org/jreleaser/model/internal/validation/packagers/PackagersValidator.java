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
package org.jreleaser.model.internal.validation.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.packagers.Packagers;
import org.jreleaser.model.internal.packagers.RepositoryPackager;
import org.jreleaser.model.internal.packagers.RepositoryTap;
import org.jreleaser.model.internal.packagers.SdkmanPackager;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class PackagersValidator extends Validator {
    public static void validatePackagers(JReleaserContext context, Mode mode, Errors errors) {
        if (!mode.validateConfig()) {
            return;
        }
        context.getLogger().debug("packagers");

        JReleaserModel model = context.getModel();
        Packagers packagers = model.getPackagers();
        Project project = model.getProject();
        Releaser gitService = model.getRelease().getReleaser();

        packagers.getAppImage().resolveEnabled(project);
        packagers.getAppImage().getRepository().resolveEnabled(project);
        validatePackager(context,
            packagers.getAppImage(),
            packagers.getAppImage().getRepository(),
            errors);
        if (packagers.getAppImage().getScreenshots().isEmpty()) {
            packagers.getAppImage().setScreenshots(project.getScreenshots());
        }
        if (packagers.getAppImage().getIcons().isEmpty()) {
            packagers.getAppImage().setIcons(project.getIcons());
        }
        if (isBlank(packagers.getAppImage().getDeveloperName())) {
            packagers.getAppImage().setDeveloperName(String.join(", ", project.getAuthors()));
        }

        packagers.getAsdf().resolveEnabled(project);
        packagers.getAsdf().getRepository().resolveEnabled(project);
        validatePackager(context,
            packagers.getAsdf(),
            packagers.getAsdf().getRepository(),
            errors);

        packagers.getBrew().resolveEnabled(project);
        packagers.getBrew().getTap().resolveEnabled(project);
        validatePackager(context,
            packagers.getBrew(),
            packagers.getBrew().getTap(),
            errors);

        packagers.getChocolatey().resolveEnabled(project);
        packagers.getChocolatey().getBucket().resolveEnabled(project);
        validatePackager(context,
            packagers.getChocolatey(),
            packagers.getChocolatey().getBucket(),
            errors);

        packagers.getDocker().resolveEnabled(project);
        packagers.getDocker().getPackagerRepository().resolveEnabled(project);
        validatePackager(context,
            packagers.getDocker(),
            packagers.getDocker().getPackagerRepository(),
            errors);

        if (!packagers.getDocker().getSpecs().isEmpty()) {
            errors.configuration(RB.$("validation_packagers_docker_specs"));
        }

        packagers.getFlatpak().resolveEnabled(project);
        packagers.getFlatpak().getRepository().resolveEnabled(project);
        validatePackager(context,
            packagers.getFlatpak(),
            packagers.getFlatpak().getRepository(),
            errors);
        if (packagers.getFlatpak().getScreenshots().isEmpty()) {
            packagers.getFlatpak().setScreenshots(project.getScreenshots());
        }
        if (packagers.getFlatpak().getIcons().isEmpty()) {
            packagers.getFlatpak().setIcons(project.getIcons());
        }
        if (isBlank(packagers.getFlatpak().getDeveloperName())) {
            packagers.getFlatpak().setDeveloperName(String.join(", ", project.getAuthors()));
        }

        packagers.getGofish().resolveEnabled(project);
        packagers.getGofish().getRepository().resolveEnabled(project);
        validatePackager(context,
            packagers.getGofish(),
            packagers.getGofish().getRepository(),
            errors);

        if (isBlank(packagers.getGofish().getRepository().getName())) {
            packagers.getGofish().getRepository().setName(gitService.getOwner() + "-fish-food");
        }
        packagers.getGofish().getRepository().setTapName(gitService.getOwner() + "-fish-food");

        packagers.getJbang().resolveEnabled(project);
        packagers.getJbang().getCatalog().resolveEnabled(project);
        validatePackager(context,
            packagers.getJbang(),
            packagers.getJbang().getCatalog(),
            errors);

        packagers.getMacports().resolveEnabled(project);
        packagers.getMacports().getRepository().resolveEnabled(project);
        validatePackager(context,
            packagers.getMacports(),
            packagers.getMacports().getRepository(),
            errors);
        if (packagers.getMacports().getMaintainers().isEmpty()) {
            packagers.getMacports().getMaintainers().addAll(project.getMaintainers());
        }

        packagers.getScoop().resolveEnabled(project);
        packagers.getScoop().getBucket().resolveEnabled(project);
        validatePackager(context,
            packagers.getScoop(),
            packagers.getScoop().getBucket(),
            errors);

        if (isBlank(packagers.getScoop().getBucket().getName())) {
            packagers.getScoop().getBucket().setName("scoop-" + gitService.getOwner());
        }
        packagers.getScoop().getBucket().setTapName("scoop-" + gitService.getOwner());

        packagers.getSnap().resolveEnabled(project);
        packagers.getSnap().getSnap().resolveEnabled(project);
        validatePackager(context,
            packagers.getSnap(),
            packagers.getSnap().getSnap(),
            errors);

        packagers.getSpec().resolveEnabled(project);
        packagers.getSpec().getRepository().resolveEnabled(project);
        validatePackager(context,
            packagers.getSpec(),
            packagers.getSpec().getRepository(),
            errors);

        if (isBlank(packagers.getSpec().getRepository().getName())) {
            packagers.getSpec().getRepository().setName(gitService.getOwner() + "-spec");
        }
        packagers.getSpec().getRepository().setTapName(gitService.getOwner() + "-spec");

        validateSdkman(context, packagers.getSdkman(), errors);
    }

    private static void validateSdkman(JReleaserContext context, SdkmanPackager packager, Errors errors) {
        packager.resolveEnabled(context.getModel().getProject());
        validateTimeout(packager);
    }

    private static void validatePackager(JReleaserContext context,
                                         RepositoryPackager packager,
                                         RepositoryTap tap,
                                         Errors errors) {
        BaseReleaser service = context.getModel().getRelease().getReleaser();
        validateCommitAuthor(packager, service);
        validateOwner(tap, service);

        tap.setUsername(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_USERNAME",
                "<empty>",
                tap.getUsername(),
                service.getUsername()));

        tap.setToken(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_TOKEN",
                "<empty>",
                tap.getToken(),
                service.getToken()));

        if (isBlank(tap.getTagName())) {
            tap.setTagName(service.getTagName());
        }

        if (isBlank(tap.getBranch())) {
            tap.setBranch("HEAD");
        }
    }
}