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

import java.util.HashMap;
import java.util.Map;
import org.huberb.groktools.MatchGatherOutput.Wrapper;
import org.huberb.groktools.OutputGrokResultFormatterAsCsvTest.HashMapBuilder;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author berni3
 */
public class WrapperTest {

    public WrapperTest() {
    }

    @Test
    public void test_isHoldingAMatch() {
        final int readLineCount = 1;
        final String subject = "A subject line";
        final int subjectLength = subject.length();
        final int start = 0;
        final int end = subjectLength;
        final Map<String, Object> m = new HashMapBuilder<String, Object>().addKeyValue("key1", "value1").build();
        final Wrapper instance = new Wrapper(readLineCount, subject, start, end, m);
        assertTrue(instance.isHoldingAMatch());

        assertAll(
                () -> assertEquals(1, instance.readLineCount),
                () -> assertEquals(0, instance.start),
                () -> assertEquals(subjectLength, instance.end),
                () -> assertEquals("value1", instance.m.get("key1"))
        );
    }

    @Test
    public void test_Extra() {
        final int readLineCount = 1;
        final String subject = "A subject line";
        final int subjectLength = subject.length();
        final int start = 0;
        final int end = subjectLength;
        final Map<String, Object> m = new HashMapBuilder<String, Object>().addKeyValue("key1", "value1").build();
        final Wrapper instance = new Wrapper(readLineCount, subject, start, end, m);

        instance.appendExtra("extra line");
        assertTrue(instance.isHoldingAMatch());

        assertAll(
                () -> assertEquals(1, instance.readLineCount),
                () -> assertEquals(0, instance.start),
                () -> assertEquals(subjectLength, instance.end),
                () -> assertEquals("extra line\n", instance.extra.toString()),
                () -> assertEquals("value1", instance.m.get("key1")),
                () -> assertEquals(null, instance.m.get("extra"))
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
        // matching sample
        "1,'a subject line',0,5,'value1',true",
        // non-matching sample
        "1,,0,0,, false",
        // non realistic variants
        "1,,0,5,'value1', false",
        "1,,0,0,, false",}
    )
    public void test_isHoldingAMatch_vary(
            int readLineCount, String subject, int start, int end, String value1, boolean result
    ) {
        final String m = String.format(""
                + "readLineCount %d, "
                + "subject %s, "
                + "start %d, "
                + "end %d, "
                + "value1 %s, "
                + "result %s",
                readLineCount,
                subject,
                start,
                end,
                value1,
                result);
        final Map<String, Object> map = new HashMap<>();
        if (value1 != null) {
            map.put("key1", value1);
        }
        final Wrapper instance = new Wrapper(readLineCount, subject, start, end, map);

        instance.appendExtra("extra line");
        assertEquals(result, instance.isHoldingAMatch(), m);
    }
}
