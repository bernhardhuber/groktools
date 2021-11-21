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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import org.huberb.groktools.OutputGrokResultConverters.OutputGrokResultAsCsv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class OutputGrokResultAsCsvTest {

    /**
     * Test of outputGrokResultAsCsv method, of class OutputGrokResult.
     */
    @Test
    public void testOutputGrokResultAsCsv() throws IOException {
        final int readLineCount = 1;
        final Map<String, Object> m = new HashMap<>();
        m.put("k1", "v1");
        m.put("k2", "v2");
        final GrokMatchResult grokResult = new GrokMatchResult("subject", 0, 5, m);
        try (final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw)) {
            try (final OutputGrokResultAsCsv instance = new OutputGrokResultAsCsv(pw)) {
                instance.start();
                instance.output(readLineCount, grokResult);
                instance.end();
            }
            final String result = sw.toString().replaceAll("[\\r\\n]", "");
            assertEquals(
                    "\"lineno\",\"k1\",\"k2\""
                    + "\"1\",\"v1\",\"v2\"", result);
        }
    }

}
