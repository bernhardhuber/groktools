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
import org.huberb.groktools.GrokMain.GrokIt;
import org.huberb.groktools.GrokMain.GrokIt.GrokResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class GrokItTest {

    public GrokItTest() {
    }

    @Test
    public void testMatch_ServerLog_line1() {
        final GrokIt grokit = new GrokIt();
        final String pattern = "%{TIMESTAMP_ISO8601:datetime} "
                + "%{LOGLEVEL:level}%{SPACE}"
                + "\\[%{DATA:category}\\]%{SPACE}"
                + "\\(%{DATA:thread}\\)%{SPACE}"
                + "%{GREEDYDATA:message}";
        final Grok grok = grokit.setUp(pattern);
        final String line = "2019-03-04 22:30:16,563 "
                + "DEBUG "
                + "[org.jboss.as.config] "
                + "(MSC service thread 1-2) "
                + "Configured system properties:";
        String line2 = "2019-03-04 22:30:17,879 "
                + "INFO  "
                + "[org.wildfly.security] "
                + "(ServerService Thread Pool -- 27) "
                + "ELY00001: WildFly Elytron version 1.8.0.Final";
        final GrokResult grokResult = grokit.match(grok, line);
        assertNotNull(grokResult);
        assertEquals("GrokResult{"
                + "subject=2019-03-04 22:30:16,563 DEBUG [org.jboss.as.config] (MSC service thread 1-2) Configured system properties:, "
                + "start=0, "
                + "end=106, "
                + "m={"
                + "datetime=2019-03-04 22:30:16,563, "
                + "level=DEBUG, "
                + "thread=MSC service thread 1-2, "
                + "category=org.jboss.as.config, "
                + "message=Configured system properties:}"
                + "}", grokResult.toString());
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
        final GrokIt grokit = new GrokIt();
        final String pattern = "%{TIMESTAMP_ISO8601:datetime} "
                + "%{LOGLEVEL:level}%{SPACE}"
                + "\\[%{DATA:category}\\]%{SPACE}"
                + "\\(%{DATA:thread}\\)%{SPACE}"
                + "%{GREEDYDATA:message}";
        final Grok grok = grokit.setUp(pattern);
        String line = "2019-03-04 22:30:17,879 "
                + "INFO  "
                + "[org.wildfly.security] "
                + "(ServerService Thread Pool -- 27) "
                + "ELY00001: WildFly Elytron version 1.8.0.Final";
        final GrokResult grokResult = grokit.match(grok, line);
        assertNotNull(grokResult);
        assertEquals(line, grokResult.subject.toString());
        assertEquals("GrokResult{"
                + "subject=2019-03-04 22:30:17,879 INFO  [org.wildfly.security] (ServerService Thread Pool -- 27) ELY00001: WildFly Elytron version 1.8.0.Final, "
                + "start=0, "
                + "end=132, "
                + "m={"
                + "datetime=2019-03-04 22:30:17,879, "
                + "level=INFO, "
                + "thread=ServerService Thread Pool -- 27, "
                + "category=org.wildfly.security, "
                + "message=ELY00001: WildFly Elytron version 1.8.0.Final"
                + "}"
                + "}", grokResult.toString());
        assertEquals(0, grokResult.start);
        assertEquals(132, grokResult.end);
        assertEquals("2019-03-04 22:30:17,879", grokResult.m.get("datetime"));
        assertEquals("INFO", grokResult.m.get("level"));
        assertEquals("org.wildfly.security", grokResult.m.get("category"));
        assertEquals("ServerService Thread Pool -- 27", grokResult.m.get("thread"));
        assertEquals("ELY00001: WildFly Elytron version 1.8.0.Final", grokResult.m.get("message"));
    }
}
