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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author berni3
 */
public class ListKeysAndValuesBuilderTest {

    /**
     * Test of addKeyValue method, of class ListKeysAndValuesBuilder.
     */
    @Test
    public void testAddKeyValue() {
        final String k = "k1";
        final String v = "v1";
        final ListKeysAndValuesBuilder instance = new ListKeysAndValuesBuilder();
        final ListKeysAndValuesBuilder result = instance.addKeyValue(k, v);
        assertAll(
                () -> assertEquals(1, result.keys().size()),
                () -> assertEquals("k1", result.keys().get(0))
        );
        assertAll(
                () -> assertEquals(1, result.values().size()),
                () -> assertEquals("v1", result.values().get(0))
        );
    }

    /**
     * Test of addKeyValue method, of class ListKeysAndValuesBuilder.
     */
    @ParameterizedTest
    @CsvSource(
            value = {
                "k1,v1",
                "k2,v2",
                "k3,v3"
            })
    public void testAddKeyValue_ParameterizedTest(String key, String value) {
        final String k = key;
        final String v = value;
        final ListKeysAndValuesBuilder instance = new ListKeysAndValuesBuilder();
        final ListKeysAndValuesBuilder result = instance.addKeyValue(k, v);
        assertAll(
                () -> assertEquals(1, result.keys().size()),
                () -> assertEquals(k, result.keys().get(0))
        );
        assertAll(
                () -> assertEquals(1, result.values().size()),
                () -> assertEquals(v, result.values().get(0))
        );
    }

    /**
     * Test of addKeys method, of class ListKeysAndValuesBuilder.
     */
    @Test
    public void testAddKeys() {
        final List<String> keysAllowed = Arrays.asList("k1", "k2", "k3");
        final Map<String, Object> m = new HashMap<>();
        m.put("k0", "v0");
        m.put("k1", "v1");
        m.put("k2", "v2");
        m.put("k3", "v3");
        m.put("k4", "v4");
        final ListKeysAndValuesBuilder instance = new ListKeysAndValuesBuilder();
        final ListKeysAndValuesBuilder result = instance.addKeys(keysAllowed, m);
        assertAll(
                () -> assertEquals(3, result.keys().size()),
                () -> assertEquals("k1", result.keys().get(0)),
                () -> assertEquals("k2", result.keys().get(1)),
                () -> assertEquals("k3", result.keys().get(2))
        );
        assertAll(
                () -> assertEquals(3, result.values().size()),
                () -> assertEquals("v1", result.values().get(0)),
                () -> assertEquals("v2", result.values().get(1)),
                () -> assertEquals("v3", result.values().get(2))
        );
    }

    /**
     * Test of keys method, of class ListKeysAndValuesBuilder.
     */
    @Test
    public void testKeys() {
        final ListKeysAndValuesBuilder instance = new ListKeysAndValuesBuilder();
        final List<String> result = instance.keys();
        assertEquals(0, result.size());
    }

    /**
     * Test of values method, of class ListKeysAndValuesBuilder.
     */
    @Test
    public void testValues() {
        final ListKeysAndValuesBuilder instance = new ListKeysAndValuesBuilder();
        final List<String> result = instance.values();
        assertEquals(0, result.size());
    }

    /**
     * Test of convertObjectToString method, of class ListKeysAndValuesBuilder.
     */
    @ParameterizedTest
    @MethodSource(value = "convertObjectToStringValues")
    public void testConvertObjectToString(String expected, Object o) {
        final ListKeysAndValuesBuilder instance = new ListKeysAndValuesBuilder();
        final String m = String.format("expected: %s, value %s", expected, o);
        assertEquals(expected, instance.convertObjectToString(o), m);
    }

    static Stream<Object[]> convertObjectToStringValues() {
        List<Object[]> l = new ArrayList<>();
        l.add(new Object[]{"", null});
        l.add(new Object[]{"", ""});
        Arrays.asList("A", "ABC", "abcABC", "012345abcDEFghi", "!§$%&/()=")
                .forEach((String s) -> {
                    l.add(new Object[]{s, s});
                    l.add(new Object[]{s, new StringBuilder(s)});
                    l.add(new Object[]{s, new StringBuffer(s)});
                });
        //---
        for (int i = -10; i <= 10; i++) {
            l.add(new Object[]{"" + i, Short.valueOf((short) i)});
            l.add(new Object[]{"" + i, Integer.valueOf(i)});
            l.add(new Object[]{"" + i, Long.valueOf(i)});
            l.add(new Object[]{"" + i, BigInteger.valueOf(i)});
            l.add(new Object[]{"" + i, BigDecimal.valueOf(i)});
        }
        return l.stream();
    }
}
