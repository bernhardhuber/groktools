/*
 * Copyright 2021 berni3.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.huberb.groktools;

import io.krakens.grok.api.Grok;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class GrokItExampleLogFilesTest {

    final String serverlogPatterndefinitions = "/groktoolspatterns/server_log";

    final String activmq_log_file = "src/main/resources/examples/activemq.log";
    final String flume_log_file = "src/main/resources/examples/flume.log";
    final String logstash_plain_log_file = "src/main/resources/examples/logstash-plain.log";
    final String server_log_file = "src/main/resources/examples/server.log";

    @Test
    public void testActiveMqActiveMqlog() throws IOException {
        final File logFile = new File(activmq_log_file);

        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{ACTIVEMQ_ACTIVEMQLOG}")
                .patternDefinitionsFromClasspath(serverlogPatterndefinitions)
                .namedOnly(true);

        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty());
        int i = 0;
        for (String line : lines) {
            i += 1;
            final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
            assertNotNull(grokMatchResult);
            final String m = String.format("lineno: %d, grokMatchResult: %s", i, grokMatchResult);
            assertAll(
                    () -> assertFalse(grokMatchResult.m.isEmpty(), m),
                    () -> assertTrue(grokMatchResult.m.size() >= 5, m),
                    () -> assertTrue(!grokMatchResult.m.get("timestampIso8601").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("level").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("category").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("thread").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("message").toString().isEmpty(), m)
            );
        }

    }

    @Test
    public void testElkstackLogstashlog() throws IOException {
        final File logFile = new File(logstash_plain_log_file);

        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{ELKSTACK_LOGSTASHLOG}")
                .patternDefinitionsFromClasspath(serverlogPatterndefinitions)
                .namedOnly(false);

        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty());
        int i = 0;
        for (String line : lines) {
            i += 1;
            final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
            assertNotNull(grokMatchResult);
            if (grokMatchResult.start == 0 && grokMatchResult.end == 0) {
                continue;
            }
            final String m = String.format("lineno: %d, grokMatchResult: %s", i, grokMatchResult);
            assertAll(
                    () -> assertFalse(grokMatchResult.m.isEmpty(), m),
                    () -> assertTrue(grokMatchResult.m.size() >= 4, m),
                    () -> assertTrue(!grokMatchResult.m.get("timestampIso8601").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("level").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("category").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("message").toString().isEmpty(), m)
            );
        }
    }

    @Test
    public void testFlumeFlumelog() throws IOException {
        final File logFile = new File(flume_log_file);

        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{FLUME_FLUMELOG}")
                .patternDefinitionsFromClasspath(serverlogPatterndefinitions)
                .namedOnly(false);

        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty());
        int i = 0;
        for (String line : lines) {
            i += 1;
            final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
            assertNotNull(grokMatchResult);
            if (grokMatchResult.start == 0 && grokMatchResult.end == 0) {
                continue;
            }
            final String m = String.format("lineno: %d, grokMatchResult: %s", i, grokMatchResult);
            assertAll(
                    () -> assertFalse(grokMatchResult.m.isEmpty(), m),
                    () -> assertTrue(grokMatchResult.m.size() >= 5, m),
                    () -> assertTrue(!grokMatchResult.m.get("timestamp").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("level").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("thread").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("category").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("message").toString().isEmpty(), m)
            );
        }
    }

    @Test
    public void testWildflyServerlog() throws IOException {
        final File logFile = new File(server_log_file);

        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{WILDFLY_SERVERLOG}")
                .patternDefinitionsFromClasspath(serverlogPatterndefinitions)
                .namedOnly(true);
        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
        assertFalse(lines.isEmpty());
        int i = 0;
        for (String line : lines) {
            i += 1;
            final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
            assertNotNull(grokMatchResult);
            if (grokMatchResult.start == 0 && grokMatchResult.end == 0) {
                continue;
            }
            final String m = String.format("lineno: %d, grokMatchResult: %s", i, grokMatchResult);

            assertAll(
                    () -> assertFalse(grokMatchResult.m.isEmpty(), m),
                    () -> assertTrue(grokMatchResult.m.size() >= 5, m),
                    () -> assertTrue(!grokMatchResult.m.get("timestampIso8601").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("level").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("category").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("thread").toString().isEmpty(), m),
                    () -> assertTrue(!grokMatchResult.m.get("message").toString().isEmpty(), m)
            );
        }
    }
}
