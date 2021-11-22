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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

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
                () -> assertEquals(1, result.keys.size()),
                () -> assertEquals("k1", result.keys.get(0))
        );
        assertAll(
                () -> assertEquals(1, result.values.size()),
                () -> assertEquals("v1", result.values.get(0))
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
                () -> assertEquals(3, result.keys.size()),
                () -> assertEquals("k1", result.keys.get(0)),
                () -> assertEquals("k2", result.keys.get(1)),
                () -> assertEquals("k3", result.keys.get(2))
        );
        assertAll(
                () -> assertEquals(3, result.values.size()),
                () -> assertEquals("v1", result.values.get(0)),
                () -> assertEquals("v2", result.values.get(1)),
                () -> assertEquals("v3", result.values.get(2))
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
    @Test
    public void testConvertObjectToString() {
        final ListKeysAndValuesBuilder instance = new ListKeysAndValuesBuilder();
        assertEquals("", instance.convertObjectToString(null));
        assertEquals("", instance.convertObjectToString(""));
        //---
        assertEquals("A", instance.convertObjectToString("A"));
        final StringBuilder sb = new StringBuilder("A");
        assertEquals("A", instance.convertObjectToString(sb));
        //---
        assertEquals("1", instance.convertObjectToString(new Integer(1)));
        assertEquals("1", instance.convertObjectToString(new Long(1)));
        assertEquals("1", instance.convertObjectToString(BigInteger.ONE));
        assertEquals("1", instance.convertObjectToString(BigDecimal.ONE));
    }

}
