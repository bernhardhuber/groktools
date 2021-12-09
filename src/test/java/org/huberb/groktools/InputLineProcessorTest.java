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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.huberb.groktools.GrokMain.InputLineProcessor;
import org.huberb.groktools.GrokMain.MatchingLineMode;
import org.huberb.groktools.OutputGrokResultConverters.IOutputGrokResultConverter;
import org.huberb.groktools.OutputGrokResultConverters.OutputGrokResultAsIs;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.unix4j.Unix4j;
import org.unix4j.io.StringInput;

/**
 *
 * @author berni3
 */
public class InputLineProcessorTest {

    public InputLineProcessorTest() {
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "classpath:/groktoolspatterns/server_log",
        "classpath://groktoolspatterns/server_log",
        "//groktoolspatterns/server_log",
        "/groktoolspatterns/server_log"
    })
    public void testSingleLineMode(String patternDefinitionsFromClasspath) throws IOException {
        final Grok grok = new GrokBuilder()
                .namedOnly(true)
                .registerDefaultPatterns(true)
                .patternDefinitionsFromClasspath(patternDefinitionsFromClasspath)
                .pattern("%{WILDFLY_SERVERLOG}")
                .build();
        final MatchingLineMode matchingLineMode = MatchingLineMode.singleLineMode;
        try (final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw)) {
            final IOutputGrokResultConverter outputGrokResultConverter = new OutputGrokResultAsIs(pw);
            final int readMaxLinesCount = -1;
            final InputLineProcessor inputLineProcessor = new InputLineProcessor(
                    grok,
                    matchingLineMode,
                    outputGrokResultConverter,
                    readMaxLinesCount);
            final InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/server.log");
            assertNotNull(is);
            try (
                    final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                    final BufferedReader br = new BufferedReader(isr)) {
                inputLineProcessor.processLines(br);
            }
            pw.flush();
            sw.flush();

            final String s = sw.toString();
            assertNotNull(s);
            assertTrue(s.length() > 0);
            assertTrue(Arrays.asList(105862, 105597).contains(s.length()),
                    "length: " + s.length()
            );
            // line words chars
            final String wcResult = Unix4j.wc(new StringInput(s)).toStringResult().replaceAll("[^0-9]+", " ");
            assertTrue(
                    Arrays.asList(
                            " 265 10212 105862 ",
                            " 265 10212 105597 "
                    ).contains(wcResult), wcResult);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "classpath:/groktoolspatterns/server_log",
        "classpath://groktoolspatterns/server_log",
        "//groktoolspatterns/server_log",
        "/groktoolspatterns/server_log"
    })
    public void testMultiLinesMode(String patternDefinitionsFromClasspath) throws IOException {
        final Grok grok = new GrokBuilder()
                .namedOnly(true)
                .registerDefaultPatterns(true)
                .patternDefinitionsFromClasspath(patternDefinitionsFromClasspath)
                .pattern("%{WILDFLY_SERVERLOG}")
                .build();
        final MatchingLineMode matchingLineMode = MatchingLineMode.multiLinesMode;
        try (final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw)) {
            final IOutputGrokResultConverter outputGrokResultConverter = new OutputGrokResultAsIs(pw);
            final int readMaxLinesCount = -1;
            final InputLineProcessor inputLineProcessor = new InputLineProcessor(
                    grok,
                    matchingLineMode,
                    outputGrokResultConverter,
                    readMaxLinesCount);
            final InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples/server.log");
            assertNotNull(is);
            try (
                    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr)) {
                inputLineProcessor.processLines(br);
            }
            pw.flush();
            sw.flush();

            final String s = sw.toString();
            assertNotNull(s);
            assertTrue(s.length() > 0);
            assertTrue(Arrays.asList(175290, 175025).contains(s.length()),
                    "length: " + s.length()
            );
            // line words chars
            final String wcResult = Unix4j.wc(new StringInput(s)).toStringResult().replaceAll("[^0-9]+", " ");
            assertTrue(
                    Arrays.asList(
                            " 1016 12251 175290 ",
                            " 1016 12251 175025 ").contains(wcResult),
                    wcResult
            );
        }
    }
}
