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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author berni3
 */
public class GrokItElkstackLogTest {

    final String serverlogPatterndefinitions = "/server_log";

    @ParameterizedTest
    @MethodSource(value = "logstashlog")
    public void testElkstackLogstashlog(String line) throws IOException {
        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{ELKSTACK_LOGSTASHLOG}")
                .patternDefinitionsFromClasspath("/server_log")
                .namedOnly(false);

        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
        assertNotNull(grokMatchResult);
        final String m = String.format("grokMatchResult %s", grokMatchResult);
        assertFalse(grokMatchResult.m.isEmpty(), m);
        assertTrue(grokMatchResult.m.size() >= 4, m);
        assertEquals("2020-01-30T22:27:01,337", grokMatchResult.m.get("timestampIso8601"), m);
        assertEquals("INFO", grokMatchResult.m.get("level"), m);
        assertEquals("logstash.agent", grokMatchResult.m.get("category"), m);
        assertEquals("Successfully started Logstash API endpoint {:port=>9600}", grokMatchResult.m.get("message"), m);
    }

    static Stream<String> logstashlog() {
        List<String> l = Arrays.asList(
                // example 1 
                // as is from a log file
                // one spaces after INFO
                "[2020-01-30T22:27:01,337][INFO ][logstash.agent           ] Successfully started Logstash API endpoint {:port=>9600}",
                // example 2
                // no padding space
                "[2020-01-30T22:27:01,337][INFO][logstash.agent] Successfully started Logstash API endpoint {:port=>9600}"
        );
        Stream<String> result = l.stream();
        return result;
    }
}
