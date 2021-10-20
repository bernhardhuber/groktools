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
public class GrokItActivemqLogTest {

    final String serverlogPatterndefinitions = "/patterns/server_log";

    @ParameterizedTest
    @MethodSource(value = "activmqlog")
    public void testActiveMqActiveMqlog(String line) throws IOException {
        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{ACTIVEMQ_ACTIVEMQLOG}")
                .patternDefinitionsFromClasspath(serverlogPatterndefinitions)
                .namedOnly(true);

        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
        assertNotNull(grokMatchResult);
        final String m = String.format("grokMatchResult %s", grokMatchResult);
        assertFalse(grokMatchResult.m.isEmpty(), m);
        assertTrue(grokMatchResult.m.size() >= 6, m);
        assertEquals("2020-05-02", grokMatchResult.m.get("date"), m);
        assertEquals("07:26:24", grokMatchResult.m.get("time"), m);
        assertEquals("INFO", grokMatchResult.m.get("level"), m);
        assertEquals("org.apache.activemq.broker.TransportConnector", grokMatchResult.m.get("category"), m);
        assertEquals("main", grokMatchResult.m.get("thread"), m);
        assertEquals("Connector amqp started", grokMatchResult.m.get("message"), m);
    }

    @ParameterizedTest
    @MethodSource(value = "activmqlog")
    public void testActiveMqActiveMqlog_2(String line) throws IOException {
        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{ACTIVEMQ_ACTIVEMQLOG_2}")
                .patternDefinitionsFromClasspath(serverlogPatterndefinitions)
                .namedOnly(true);

        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
        assertNotNull(grokMatchResult);
        final String m = String.format("grokMatchResult %s", grokMatchResult);
        assertFalse(grokMatchResult.m.isEmpty(), m);
        assertTrue(grokMatchResult.m.size() >= 5, m);
        assertEquals("2020-05-02 07:26:24,895", grokMatchResult.m.get("timestampIso8601"), m);
        assertEquals("INFO", grokMatchResult.m.get("level"), m);
        assertEquals("org.apache.activemq.broker.TransportConnector", grokMatchResult.m.get("category"), m);
        assertEquals("main", grokMatchResult.m.get("thread"), m);
        assertEquals("Connector amqp started", grokMatchResult.m.get("message"), m);
    }

    static Stream<String> activmqlog() {
        final List<String> l = Arrays.asList(
                // example 1 
                // as is from a log file
                // two spaces after INFO
                "2020-05-02 07:26:24,895 | INFO  | Connector amqp started | org.apache.activemq.broker.TransportConnector | main",
                // example 2 
                // four spaces before | except for field message
                "2020-05-02 07:26:24,895    | INFO    | Connector amqp started | org.apache.activemq.broker.TransportConnector    | main    ",
                // example 3 
                // no spaces
                "2020-05-02 07:26:24,895|INFO| Connector amqp started | org.apache.activemq.broker.TransportConnector| main"
        );
        final Stream<String> result = l.stream();
        return result;
    }
}
