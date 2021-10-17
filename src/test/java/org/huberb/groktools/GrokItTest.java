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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author berni3
 */
public class GrokItTest {

    @Test
    public void testMatch_ServerLog_line1() {
        final GrokIt grokit = new GrokIt();
        final String pattern = "%{TIMESTAMP_ISO8601:datetime} "
                + "%{LOGLEVEL:level}%{SPACE:UNWANTED}"
                + "\\[%{DATA:category}\\]%{SPACE:UNWANTED}"
                + "\\(%{DATA:thread}\\)%{SPACE:UNWANTED}"
                + "%{GREEDYDATA:message}";
        final Grok grok = grok_setUp(pattern);
        final String line = "2019-03-04 22:30:16,563 "
                + "DEBUG "
                + "[org.jboss.as.config] "
                + "(MSC service thread 1-2) "
                + "Configured system properties:";
        final GrokMatchResult grokResult = grokit.match(grok, line);
        assertNotNull(grokResult);
        assertEquals("GrokResult{"
                + "subject=2019-03-04 22:30:16,563 DEBUG [org.jboss.as.config] (MSC service thread 1-2) Configured system properties:, "
                + "start=0, "
                + "end=106, "
                + "m={"
                + "datetime=2019-03-04 22:30:16,563, "
                + "YEAR=2019, "
                + "MONTHNUM=03, "
                + "HOUR=[22, null], "
                + "level=DEBUG, "
                + "MINUTE=[30, null], "
                + "SECOND=16,563, "
                + "thread=MSC service thread 1-2, "
                + "category=org.jboss.as.config, "
                + "message=Configured system properties:, "
                + "ISO8601_TIMEZONE=null, "
                + "MONTHDAY=04}}", grokResult.toString());
        assertEquals(0, grokResult.start);
        assertEquals(106, grokResult.end);
        assertEquals("2019-03-04 22:30:16,563", grokResult.m.get("datetime"));
        assertEquals("DEBUG", grokResult.m.get("level"));
        assertEquals("org.jboss.as.config", grokResult.m.get("category"));
        assertEquals("MSC service thread 1-2", grokResult.m.get("thread"));
        assertEquals("Configured system properties:", grokResult.m.get("message"));
    }

    @Test
    public void testMatch_ServerLog_line2() {
        final String pattern = "%{TIMESTAMP_ISO8601:datetime} "
                + "%{LOGLEVEL:level}%{SPACE:UNWANTED}"
                + "\\[%{DATA:category}\\]%{SPACE:UNWANTED}"
                + "\\(%{DATA:thread}\\)%{SPACE:UNWANTED}"
                + "%{GREEDYDATA:message}";
        final GrokIt grokit = new GrokIt();
        final Grok grok = grok_setUp(pattern);
        final String line = "2019-03-04 22:30:17,879 "
                + "INFO  "
                + "[org.wildfly.security] "
                + "(ServerService Thread Pool -- 27) "
                + "ELY00001: WildFly Elytron version 1.8.0.Final";
        final GrokMatchResult grokResult = grokit.match(grok, line);
        assertNotNull(grokResult);
        assertEquals(line, grokResult.subject.toString());
        assertEquals("GrokResult{"
                + "subject=2019-03-04 22:30:17,879 INFO  [org.wildfly.security] (ServerService Thread Pool -- 27) ELY00001: WildFly Elytron version 1.8.0.Final, "
                + "start=0, "
                + "end=132, "
                + "m={"
                + "datetime=2019-03-04 22:30:17,879, "
                + "YEAR=2019, MONTHNUM=03, HOUR=[22, null], "
                + "level=INFO, MINUTE=[30, null], "
                + "SECOND=17,879, "
                + "thread=ServerService Thread Pool -- 27, "
                + "category=org.wildfly.security, "
                + "message=ELY00001: WildFly Elytron version 1.8.0.Final, "
                + "ISO8601_TIMEZONE=null, "
                + "MONTHDAY=04}}", grokResult.toString());
        assertEquals(0, grokResult.start);
        assertEquals(132, grokResult.end);
        assertEquals("2019-03-04 22:30:17,879", grokResult.m.get("datetime"));
        assertEquals("INFO", grokResult.m.get("level"));
        assertEquals("org.wildfly.security", grokResult.m.get("category"));
        assertEquals("ServerService Thread Pool -- 27", grokResult.m.get("thread"));
        assertEquals("ELY00001: WildFly Elytron version 1.8.0.Final", grokResult.m.get("message"));
    }

    @ParameterizedTest
    @CsvSource(value = {"xxx, false",
        "2019-03-04 22:30:18,900 INFO  [org.jboss.as.server] (Controller Boot Thread) WFLYSRV0039: Creating http management service using socket-binding (management-http), true",
        "2019-03-04 22:30:18,931 INFO  [org.xnio] (MSC service thread 1-2) XNIO version 3.6.5.Final, true",
        "2019-03-04 22:30:18,931 INFO  [org.xnio.nio] (MSC service thread 1-2) XNIO NIO Implementation Version 3.6.5.Final, true",
        "2019-03-04 22:30:19,025 INFO  [org.jboss.remoting] (MSC service thread 1-6) JBoss Remoting version 5.0.8.Final, true",
        "2019-03-04 22:30:19,041 INFO  [org.jboss.as.webservices] (ServerService Thread Pool -- 72) WFLYWS0002: Activating WebServices Extension, true",
        "2019-03-04 22:30:19,056 INFO  [org.jboss.as.jsf] (ServerService Thread Pool -- 56) WFLYJSF0007: Activated the following JSF Implementations: [main], true",
        "2019-03-04 22:30:19,056 INFO  [org.jboss.as.clustering.infinispan] (ServerService Thread Pool -- 49) WFLYCLINF0001: Activating Infinispan subsystem., true",
        "2019-03-04 22:30:19,041 INFO  [org.wildfly.extension.microprofile.opentracing] (ServerService Thread Pool -- 61) WFLYTRACEXT0001: Activating MicroProfile OpenTracing Subsystem, true"})
    public void testMatching_VaryLines(String line, boolean matches) {
        final String pattern = "%{TIMESTAMP_ISO8601:datetime} "
                + "%{LOGLEVEL:level}%{SPACE:UNWANTED}"
                + "\\[%{DATA:category}\\]%{SPACE:UNWANTED}"
                + "\\(%{DATA:thread}\\)%{SPACE:UNWANTED}"
                + "%{GREEDYDATA:message}";
        final GrokIt grokit = new GrokIt();
        final Grok grok = grok_setUp(pattern);
        final GrokMatchResult grokResult = grokit.match(grok, line);
        assertNotNull(grokResult);
        final String m = grokResult.toString();
        if (matches) {
            assertFalse(grokResult.m.isEmpty(), m);
            assertTrue(grokResult.start == 0 && grokResult.end > 0, m);
            assertEquals(5, grokResult.m.entrySet().size(), m);
        } else {
            assertTrue(grokResult.m.isEmpty(), m);
            assertTrue(grokResult.start == 0 && grokResult.end == 0, m);
            assertEquals(0, grokResult.m.entrySet().size(), m);
        }
    }

    @Test
    public void testParseCombinedAccessLog() {
        final String line = "112.169.19.192 "
                + "- "
                + "- "
                + "[06/Mar/2013:01:36:30 +0900] "
                + "\"GET / HTTP/1.1\" "
                + "200 "
                + "44346 "
                + "\"-\" "
                + "\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) "
                + "AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.152 Safari/537.22\"";
        final GrokIt grokit = new GrokIt();
        final Grok grok = grok_setUp("%{COMBINEDAPACHELOG}");
        final GrokMatchResult matchResult = grokit.match(grok, line);
        final Map<String, Object> map = matchResult.m;

        assertEquals("GET", map.get("verb"));
        assertEquals("06/Mar/2013:01:36:30 +0900", map.get("timestamp"));
        assertEquals("44346", map.get("bytes"));
        assertEquals("/", map.get("request"));
        assertEquals("1.1", map.get("httpversion"));
    }

    @Test
    public void testParseServerLogWithCustomPatternDefinitions() {
        final String line = "2019-05-17 "
                + "20:00:32,140 "
                + "INFO  "
                + "[xx.yyy.zzzz] "
                + "(Thread-99) "
                + "message response handled in: 62 ms; message counter: 2048; total message counter: 7094";
        final Map<String, String> patternDefinitions = Stream.of(new String[][]{
            {"MY_DATE", "%{YEAR}-%{MONTHNUM}-%{MONTHDAY}"},
            {"MY_TIMESTAMP", "%{MY_DATE:date} %{TIME:time},%{INT:millis}"},
            {"MY_MODULE", "\\[%{NOTSPACE}\\]"},
            {"MY_THREAD", "\\(%{NOTSPACE}\\)"},
            {"MY_SERVERLOG", "%{MY_TIMESTAMP} %{LOGLEVEL}%{SPACE:UNWANTED}%{MY_MODULE} %{MY_THREAD} "
                + "message response handled "
                + "in: %{INT:response_time} ms; "
                + "%{GREEDYDATA:UNWANTED}"},}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        final Grok grok = grok_setUp("%{MY_SERVERLOG}", patternDefinitions);
        final Map<String, Object> map = grok.match(line).capture();
        final String m = String.format("map %s", map.toString());
        assertEquals(16, map.size(), m);
        assertEquals("2019-05-17", map.get("date"), m);
        assertEquals("20:00:32", map.get("time"), m);
        assertEquals("140", map.get("millis"), m);
        assertEquals("62", map.get("response_time"), m);
    }

    private Grok grok_setUp(String pattern, Map<String, String> patternDefinitions) {
        final Grok grok = new GrokBuilder()
                .pattern(pattern)
                .patternDefinitions(patternDefinitions)
                .build();
        return grok;
    }

    private Grok grok_setUp(String pattern) {
        final Grok grok = new GrokBuilder()
                .pattern(pattern)
                .build();
        return grok;
    }
}
