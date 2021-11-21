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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
class ListKeysAndValuesBuilder {

    final List<String> keys = new ArrayList<>();
    final List<String> values = new ArrayList<>();

    ListKeysAndValuesBuilder addKeyValue(String k, String v) {
        keys.add(k);
        values.add(v);
        return this;
    }

    ListKeysAndValuesBuilder addKeys(List<String> keysAllowed, Map<String, Object> m) {
        keys.addAll(keysAllowed);
        // peek only keys from map m
        for (String k : keysAllowed) {
            final Object o = m.getOrDefault(k, "");
            final String v = convertObjectToString(o);
            values.add(v);
        }
        return this;
    }

    /**
     * Return added keys
     *
     * @return
     */
    List<String> keys() {
        return this.keys;
    }

    /**
     * Return added values
     *
     * @return
     */
    List<String> values() {
        return this.values;
    }

    String convertObjectToString(Object o) {
        final String v;
        if (o == null) {
            v = "";
        } else if (o instanceof String) {
            v = (String) o;
        } else {
            v = String.valueOf(o);
        }
        return v;
    }
    
}
