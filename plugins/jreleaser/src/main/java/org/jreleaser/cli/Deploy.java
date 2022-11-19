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
package org.jreleaser.cli;

import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;
import picocli.CommandLine;

import static org.jreleaser.model.api.JReleaserContext.Mode.DEPLOY;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@CommandLine.Command(name = "deploy")
public class Deploy extends AbstractModelCommand {
    @CommandLine.Option(names = {"--dry-run"})
    Boolean dryrun;

    @CommandLine.ArgGroup
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, order = 1,
            headingKey = "include.filter.header")
        Include include;

        @CommandLine.ArgGroup(exclusive = false, order = 2,
            headingKey = "exclude.filter.header")
        Exclude exclude;

        String[] includedDeployerTypes() {
            return include != null ? include.includedDeployerTypes : null;
        }

        String[] includedDeployerNames() {
            return include != null ? include.includedDeployerNames : null;
        }

        String[] excludedDeployerTypes() {
            return exclude != null ? exclude.excludedDeployerTypes : null;
        }

        String[] excludedDeployerNames() {
            return exclude != null ? exclude.excludedDeployerNames : null;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"-y", "--deployer"},
            paramLabel = "<deployer>")
        String[] includedDeployerTypes;

        @CommandLine.Option(names = {"-yn", "--deployer-name"},
            paramLabel = "<name>")
        String[] includedDeployerNames;
    }

    static class Exclude {
        @CommandLine.Option(names = {"-xy", "--exclude-deployer"},
            paramLabel = "<deployer>")
        String[] excludedDeployerTypes;

        @CommandLine.Option(names = {"-xyn", "--exclude-deployer-name"},
            paramLabel = "<name>")
        String[] excludedDeployerNames;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        if (null != composite) {
            context.setIncludedDeployerTypes(collectEntries(composite.includedDeployerTypes(), true));
            context.setIncludedDeployerNames(collectEntries(composite.includedDeployerNames()));
            context.setExcludedDeployerTypes(collectEntries(composite.excludedDeployerTypes(), true));
            context.setExcludedDeployerNames(collectEntries(composite.excludedDeployerNames()));
        }
        Workflows.deploy(context).execute();
    }

    @Override
    protected Boolean dryrun() {
        return dryrun;
    }

    @Override
    protected Mode getMode() {
        return DEPLOY;
    }
}
