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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import java.time.format.DateTimeFormatterBuilder;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author berni3
 */
public class GrokItServerLogTest {

    final String serverlogPatterndefinitions = "/groktoolspatterns/server_log";

    @ParameterizedTest
    @MethodSource(value = "wildflyserverlog")
    public void testWildflyServerlog_2(String line) throws IOException {
        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{WILDFLY_SERVERLOG}")
                .patternDefinitionsFromClasspath(serverlogPatterndefinitions)
                .namedOnly(true);
        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
        assertNotNull(grokMatchResult);
        final String m = String.format("grokMatchResult %s", grokMatchResult);
        assertFalse(grokMatchResult.m.isEmpty(), m);
        assertEquals("2019-03-04 22:30:18,900", grokMatchResult.m.get("timestampIso8601"), m);
        // TODO parse date+time

        assertEquals("INFO", grokMatchResult.m.get("level"), m);
        assertEquals("org.jboss.as.server", grokMatchResult.m.get("category"), m);
        assertEquals("Controller Boot Thread", grokMatchResult.m.get("thread"), m);
        assertEquals("WFLYSRV0039: Creating http management service using socket-binding (management-http)", grokMatchResult.m.get("message"), m);
    }

    static Stream<String> wildflyserverlog() {
        final List<String> l = Arrays.asList(
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
        );
        final Stream<String> result = l.stream();
        return result;
    }

    @Test
    public void testParsingDateAsFromServerLog() {

        final String dateAndTime1 = "2019-03-04 22:30:18,987";
        final DateTimeFormatter dtf = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .optionalStart()
                //.appendFraction(NANO_OF_SECOND, 0, 9, true)
                .appendLiteral(',')
                .appendValue(MILLI_OF_SECOND, 3)
                .toFormatter();

        final LocalDateTime ldt = LocalDateTime.parse(dateAndTime1, dtf);
        assertAll(
                () -> assertEquals(2019, ldt.getYear()),
                () -> assertEquals(Month.MARCH, ldt.getMonth()),
                () -> assertEquals(4, ldt.getDayOfMonth()),
                () -> assertEquals(4, ldt.getDayOfMonth()),
                () -> assertEquals(22, ldt.getHour()),
                () -> assertEquals(30, ldt.getMinute()),
                () -> assertEquals(18, ldt.getSecond()),
                () -> assertEquals(987000000, ldt.getNano())
        );
    }

    @Test
    public void testParsingDateAsSupportedByISO() {
        final String dateAndTime2 = "2019-03-04T22:30:18";
        final LocalDateTime ldt = LocalDateTime.parse(dateAndTime2, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertAll(
                () -> assertEquals(2019, ldt.getYear()),
                () -> assertEquals(Month.MARCH, ldt.getMonth()),
                () -> assertEquals(4, ldt.getDayOfMonth()),
                () -> assertEquals(22, ldt.getHour()),
                () -> assertEquals(30, ldt.getMinute()),
                () -> assertEquals(18, ldt.getSecond()),
                () -> assertEquals(0, ldt.getNano())
        );
    }
}
