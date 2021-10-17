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
import org.huberb.groktools.GrokIt.GrokMatchResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author berni3
 */
public class GrokItServerLogTest {

    @ParameterizedTest
    @ValueSource(strings = {
        // example 1
        // all fields separated by single space
        "2019-03-04 22:30:18,900 INFO [org.jboss.as.server] (Controller Boot Thread) "
        + "WFLYSRV0039: Creating http management service using socket-binding (management-http)",
        // example 2
        // as-is from a log file 
        // two spaces after INFO
        "2019-03-04 22:30:18,900 INFO  [org.jboss.as.server] (Controller Boot Thread) "
        + "WFLYSRV0039: Creating http management service using socket-binding (management-http)",
        // example 3
        // all fields separate by 4 spaces
        "2019-03-04 22:30:18,900    "
        + "INFO    "
        + "[org.jboss.as.server]    "
        + "(Controller Boot Thread)    "
        + "WFLYSRV0039: Creating http management service using socket-binding (management-http)"

    })
    public void testWildflyServerlog(String line) {
        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{WILDFLY_SERVERLOG}")
                .patternDefinitionsFromClasspath("/server_log")
                .namedOnly(true);

        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
        assertNotNull(grokMatchResult);
        final String m = String.format("grokMatchResult %s", grokMatchResult);
        assertFalse(grokMatchResult.m.isEmpty(), m);
        assertEquals("2019-03-04", grokMatchResult.m.get("date"), m);
        assertEquals("22:30:18", grokMatchResult.m.get("time"), m);
        assertEquals("INFO", grokMatchResult.m.get("level"), m);
        assertEquals("org.jboss.as.server", grokMatchResult.m.get("category"), m);
        assertEquals("Controller Boot Thread", grokMatchResult.m.get("thread"), m);
        assertEquals("WFLYSRV0039: Creating http management service using socket-binding (management-http)", grokMatchResult.m.get("message"), m);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // example 1 
        // as is from a log file
        // two spaces after INFO
        "2020-05-02 07:26:24,895 | INFO  | Connector amqp started | org.apache.activemq.broker.TransportConnector | main",
        // example 1 
        // four spaces before | except for field message
        "2020-05-02 07:26:24,895    | INFO    | Connector amqp started | org.apache.activemq.broker.TransportConnector    | main    "
    })
    public void testActiveMqActiveMqlog(String line) {
        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{ACTIVEMQ_ACTIVEMQLOG}")
                .patternDefinitionsFromClasspath("/server_log")
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
    @ValueSource(strings = {
        // example 1 
        // as is from a log file
        // one spaces after INFO
        "[2020-01-30T22:27:01,337][INFO ][logstash.agent           ] Successfully started Logstash API endpoint {:port=>9600}"
    })
    public void testElkstackLogstashlog(String line) {
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
}
