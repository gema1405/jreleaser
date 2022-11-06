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
package org.jreleaser.sdk.mastodon;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class MastodonSdkTest {

    private static final String API_V_1_STATUSES = "/api/v1/statuses";

    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options().dynamicPort());

    @Test
    public void testUpdateStatus() throws MastodonException {
        // given:
        stubFor(post(urlEqualTo(API_V_1_STATUSES))
                    .willReturn(okJson("{\"status\": 202, \"message\":\"success\"}")));
        MastodonSdk command = MastodonSdk.builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
                                         .accessToken("ACCESS_TOKEN")
                                         .host(api.baseUrl() + "/")
                                         .build();

        // when:
        command.status(Collections.singletonList("success"));

        // then:
        RequestPatternBuilder builder = postRequestedFor(urlEqualTo(API_V_1_STATUSES));
        verify(builder.withRequestBody(containing("\"status\" : \"success\"")));
    }

    @Test
    public void testUpdateStatuses() throws MastodonException {
        // given:
        stubFor(post(urlEqualTo(API_V_1_STATUSES))
                    .willReturn(okJson("{\"id\": \"1234\", \"status\": 202, \"message\":\"success\"}")));
        MastodonSdk command = MastodonSdk.builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
                                         .accessToken("ACCESS_TOKEN")
                                         .host(api.baseUrl() + "/")
                                         .build();

        // when:
        command.status(Arrays.asList("success1", "success2"));

        // then:
        RequestPatternBuilder builder = postRequestedFor(urlEqualTo(API_V_1_STATUSES));
        verify(builder
                   .withRequestBody(containing("success2"))
                   .withRequestBody(containing("\"in_reply_to_id\" : \"1234\""))
        );
        verify(postRequestedFor(urlEqualTo(API_V_1_STATUSES))
                   .withRequestBody(containing("success1"))
                   .withRequestBody(notMatching("in_reply_to_id"))
        );
    }
}
