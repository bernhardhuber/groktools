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
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
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
public class GrokItFlumeLogTest {

    final String serverlogPatterndefinitions = "/groktoolspatterns/server_log";

    @ParameterizedTest
    @MethodSource(value = "flumelog")
    public void testFlumeFlumelog(String line) throws IOException {
        final GrokBuilder grokBuilder = new GrokBuilder()
                .pattern("%{FLUME_FLUMELOG}")
                .patternDefinitionsFromClasspath(serverlogPatterndefinitions)
                .namedOnly(true);
        final Grok grok = grokBuilder.build();
        final GrokIt grokIt = new GrokIt();
        final GrokMatchResult grokMatchResult = grokIt.match(grok, line);
        assertNotNull(grokMatchResult);
        final String m = String.format("grokMatchResult %s", grokMatchResult);
        assertFalse(grokMatchResult.m.isEmpty(), m);
        assertEquals("05 Nov 2017 08:24:47,502", grokMatchResult.m.get("timestamp"), m);
        // TODO parse date+time

        assertEquals("INFO", grokMatchResult.m.get("level"), m);
        assertEquals("org.apache.flume.node.PollingPropertiesFileConfigurationProvider.start:62", grokMatchResult.m.get("category"), m);
        assertEquals("lifecycleSupervisor-1-0", grokMatchResult.m.get("thread"), m);
        assertEquals("Configuration provider starting", grokMatchResult.m.get("message"), m);
    }

    static Stream<String> flumelog() {
        final List<String> l = Arrays.asList(
                // example 1
                // all fields separated by single space
                "05 Nov 2017 08:24:47,502 INFO  [lifecycleSupervisor-1-0] "
                + "(org.apache.flume.node.PollingPropertiesFileConfigurationProvider.start:62)  "
                + "- Configuration provider starting"
        );
        final Stream<String> result = l.stream();
        return result;
    }

    @Test
    public void testParsingDateAsFromServerLog() {

        final String dateAndTime1 = "05 Nov 2017 08:24:47,502";
        final DateTimeFormatter dtf = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendValue(java.time.temporal.ChronoField.DAY_OF_MONTH)
                .appendLiteral(' ')
                .appendText(java.time.temporal.ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
                .appendLiteral(' ')
                .appendValue(java.time.temporal.ChronoField.YEAR)
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
                () -> assertEquals(2017, ldt.getYear()),
                () -> assertEquals(Month.NOVEMBER, ldt.getMonth()),
                () -> assertEquals(5, ldt.getDayOfMonth()),
                () -> assertEquals(8, ldt.getHour()),
                () -> assertEquals(24, ldt.getMinute()),
                () -> assertEquals(47, ldt.getSecond()),
                () -> assertEquals(502000000, ldt.getNano())
        );
    }

}
