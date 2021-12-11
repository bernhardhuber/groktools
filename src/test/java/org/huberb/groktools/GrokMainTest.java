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

import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

/**
 *
 * @author berni3
 */
public class GrokMainTest {

    GrokMain app;
    CommandLine cmd;
    StringWriter swOut;
    StringWriter swErr;

    @BeforeEach
    public void setUp() {
        app = new GrokMain();
        cmd = new CommandLine(app);

        swOut = new StringWriter();
        cmd.setOut(new PrintWriter(swOut));

        swErr = new StringWriter();
        cmd.setErr(new PrintWriter(swErr));
        //---
    }

    @ParameterizedTest
    @ValueSource(strings = {"--help", "-h"})
    public void testCommandLine_help(String helpOption) {
        //---
        final int exitCode = cmd.execute(helpOption);
        assertEquals(0, exitCode);
        assertEquals("", swErr.toString(), "stderr");
        final String swOutAsString = swOut.toString();
        final String m = String.format("stdout helpOption %s, stderr: %s", helpOption, swOutAsString);
        assertAll(
                () -> assertNotEquals(null, swOutAsString, m),
                () -> assertNotEquals(0, swOutAsString.length(), m),
                () -> assertTrue(swOutAsString.contains("Usage:"), m),
                () -> assertTrue(swOutAsString.contains("--pattern"), m),
                () -> assertTrue(swOutAsString.contains("-h"), m),
                () -> assertTrue(swOutAsString.contains("--help"), m),
                () -> assertTrue(swOutAsString.contains("-V"), m),
                () -> assertTrue(swOutAsString.contains("--version"), m)
        );
    }

    @Test
    public void testLaunch_pattern_ACTIVEMQ_ACTIVEMQLOG() {
//#OUTPUT_MODE="--output-matchresult=asIs"
//OUTPUT_MODE="--output-matchresult=asCsv"
//#OUTPUT_MODE="--output-matchresult=asJson"
//
//#MATCHING_LINE_MODE="--matching-line-mode=singleLineMode"
//MATCHING_LINE_MODE="--matching-line-mode=multiLinesMode"

//ACTIVEMQ_ACTIVEMQLOG_FILE=${LOG_FILES_BASEDIR}/activemq.log
//
//    CMD_OPTIONS="\
// --pattern-definitions-classpath=//groktoolspatterns/server_log \
// --read-max-lines-count=${READ_MAX_LINES_COUNT} \
// ${OUTPUT_MODE} \
// --pattern=%{ACTIVEMQ_ACTIVEMQLOG} \
// --file=${ACTIVEMQ_ACTIVEMQLOG_FILE} \
// ${MATCHING_LINE_MODE} "
        final String[] option = new String[]{
            "--output-matchresult=asCsv",
            "--matching-line-mode=singleLineMode",
            "--pattern-definitions-classpath=//groktoolspatterns/server_log",
            "--read-max-lines-count=3",
            "--pattern=%{ACTIVEMQ_ACTIVEMQLOG}",
            "--file=target/classes/examples/activemq.log"};

        final int exitCode = cmd.execute(option);

        final String swErrAsString = swErr.toString();
        final String swOutAsString = swOut.toString();
        final String m = String.format("---%n"
                + "stdout %s%n"
                + "stderr %s%n", swOutAsString, swErrAsString);
        assertEquals(0, exitCode, m);
        assertEquals("register pattern definitions from classpath: //groktoolspatterns/server_log", swErrAsString.trim(), m);
        assertAll(
                () -> assertNotEquals(null, swOutAsString, m),
                () -> assertNotEquals(0, swOutAsString.length(), m)
        );
    }

    @Test
    public void testLaunch_pattern_WILDFLY_SERVERLOG_FILE() {
//#OUTPUT_MODE="--output-matchresult=asIs"
//OUTPUT_MODE="--output-matchresult=asCsv"
//#OUTPUT_MODE="--output-matchresult=asJson"
//
//WILDFLY_SERVERLOG_FILE=${LOG_FILES_BASEDIR}/server.log
//
//#MATCHING_LINE_MODE="--matching-line-mode=singleLineMode"
//MATCHING_LINE_MODE="--matching-line-mode=multiLinesMode"
//    CMD_OPTIONS="\
// --pattern-definitions-classpath=//groktoolspatterns/server_log \
// --read-max-lines-count=${READ_MAX_LINES_COUNT} \
// ${OUTPUT_MODE} \
// --pattern=%{WILDFLY_SERVERLOG} \
// --file=${WILDFLY_SERVERLOG_FILE} \
// ${MATCHING_LINE_MODE} "
        final String[] option = new String[]{
            "--output-matchresult=asCsv",
            "--matching-line-mode=singleLineMode",
            "--pattern-definitions-classpath=//groktoolspatterns/server_log",
            "--read-max-lines-count=3",
            "--pattern=%{WILDFLY_SERVERLOG}",
            "--file=target/classes/examples/server.log"};

        final int exitCode = cmd.execute(option);

        final String swErrAsString = swErr.toString();
        final String swOutAsString = swOut.toString();
        final String m = String.format("---%n"
                + "stdout %s%n"
                + "stderr %s%n", swOutAsString, swErrAsString);
        assertEquals(0, exitCode, m);
        assertEquals("register pattern definitions from classpath: //groktoolspatterns/server_log", swErrAsString.trim(), m);
        assertAll(
                () -> assertNotEquals(null, swOutAsString, m),
                () -> assertNotEquals(0, swOutAsString.length(), m)
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
        "%{ELKSTACK_LOGSTASHLOG}, target/classes/examples/logstash-plain.log",
        "%{FLUME_FLUMELOG}, target/classes/examples/flume.log",}
    )
    public void testCommandLine_pattern_filename(String pattern, String filename) {
        final String[] option = new String[]{
            "--output-matchresult=asCsv",
            "--matching-line-mode=singleLineMode",
            "--pattern-definitions-classpath=//groktoolspatterns/server_log",
            "--read-max-lines-count=3",
            "--pattern=" + pattern,
            "--file=" + filename};

        final int exitCode = cmd.execute(option);

        final String swErrAsString = swErr.toString();
        final String swOutAsString = swOut.toString();
        final String m = String.format("---%n"
                + "stdout %s%n"
                + "stderr %s%n", swOutAsString, swErrAsString);
        assertEquals(0, exitCode, m);
        assertEquals("register pattern definitions from classpath: //groktoolspatterns/server_log", swErrAsString.trim(), m);
        assertAll(
                () -> assertNotEquals(null, swOutAsString, m),
                () -> assertNotEquals(0, swOutAsString.length(), m)
        );
    }
}
