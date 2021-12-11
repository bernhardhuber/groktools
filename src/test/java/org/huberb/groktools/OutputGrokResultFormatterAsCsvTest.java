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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import org.huberb.groktools.OutputGrokResultFormatters.OutputGrokResultFormatterAsCsv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author berni3
 */
public class OutputGrokResultFormatterAsCsvTest {

    /**
     * Test of outputGrokResultAsCsv method, of class OutputGrokResult.
     */
    @Test
    public void testOutputGrokResultAsCsv() throws IOException {
        final int readLineCount = 1;
        final Map<String, Object> m = new HashMapBuilder<String, Object>()
                .addKeyValue("k1", "v1")
                .addKeyValue("k2", "v2")
                .build();
        final GrokMatchResult grokResult = new GrokMatchResult("subject", 0, 5, m);
        try (final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw)) {
            try (final OutputGrokResultFormatterAsCsv instance = new OutputGrokResultFormatterAsCsv(pw)) {
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

    /**
     * Test of outputGrokResultAsCsv method, of class OutputGrokResult.
     */
    @ParameterizedTest
    @MethodSource("createSampleWithKeysK1K2")
    public void testOutputGrokResultAsCsv_(Map<String, Object> data) throws IOException {
        final Function<String, Object> peekFromDataMap = (dataKey) -> {
            return data.computeIfAbsent(dataKey, (missingDataKey) -> {
                throw new IllegalArgumentException("Missing key " + missingDataKey);
            });
        };
        final Integer readLineCount = (Integer) peekFromDataMap.apply("lineno");
        final Map<String, Object> m = new HashMapBuilder<String, Object>()
                .addKeyValue("k1", peekFromDataMap.apply("k1"))
                .addKeyValue("k2", peekFromDataMap.apply("k2"))
                .build();
        final GrokMatchResult grokResult = new GrokMatchResult("subject", 0, 5, m);
        try (final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw)) {
            try (final OutputGrokResultFormatterAsCsv instance = new OutputGrokResultFormatterAsCsv(pw)) {
                instance.start();
                instance.output(readLineCount, grokResult);
                instance.end();
            }
            final String result = sw.toString().replaceAll("[\\r\\n]", "");
            final String v1 = (String) peekFromDataMap.apply("v1Csv");
            final String v2 = (String) peekFromDataMap.apply("v2Csv");
            final String expected = String.format("\"lineno\",\"k1\",\"k2\""
                    + "\"1\",%s,%s", v1, v2);
            assertEquals(expected, result);
        }
    }

    static Stream createSampleWithKeysK1K2() {
        final Map<String, Object> sampleSimpleV1V2 = new HashMapBuilder<String, Object>()
                .addKeyValue("lineno", 1)
                .addKeyValue("k1", "v1")
                .addKeyValue("k2", "v2")
                .addKeyValue("v1Csv", "\"v1\"")
                .addKeyValue("v2Csv", "\"v2\"")
                .build();
        final Map<String, Object> sampleEscapeVariantBlankV1V2 = new HashMapBuilder<String, Object>()
                .addKeyValue("lineno", 1)
                .addKeyValue("k1", "v 1")
                .addKeyValue("k2", "v 2")
                .addKeyValue("v1Csv", "\"v 1\"")
                .addKeyValue("v2Csv", "\"v 2\"")
                .build();
        final Map<String, Object> sampleEscapeVariantDoubleQuoteV1V2 = new HashMapBuilder<String, Object>()
                .addKeyValue("lineno", 1)
                .addKeyValue("k1", "v \"1")
                .addKeyValue("k2", "v \"2")
                .addKeyValue("v1Csv", "\"v \"\"1\"")
                .addKeyValue("v2Csv", "\"v \"\"2\"")
                .build();
        final Map<String, Object> sampleEscapeVariantAllV1V2 = new HashMapBuilder<String, Object>()
                .addKeyValue("lineno", 1)
                .addKeyValue("k1", "v '\"\r1")
                .addKeyValue("k2", "v '\"\n2")
                .addKeyValue("v1Csv", "\"v '\"\"1\"")
                .addKeyValue("v2Csv", "\"v '\"\"2\"")
                .build();

        final List<Map<String, Object>> resultList = Arrays.asList(
                sampleSimpleV1V2,
                sampleEscapeVariantBlankV1V2,
                sampleEscapeVariantDoubleQuoteV1V2,
                sampleEscapeVariantAllV1V2
        );
        final Stream resultStream = resultList.stream();
        return resultStream;
    }

    static class HashMapBuilder<K, V> {

        final Map<K, V> m = new HashMap<>();

        HashMapBuilder<K, V> addKeyValue(K k, V v) {
            this.m.put(k, v);
            return this;
        }

        Map<K, V> build() {
            return this.m;
        }
    }
}
