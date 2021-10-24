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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.huberb.groktools.GrokIt.GrokMatchResult;

/**
 * Encapsulate outputting a {@link GrokMatchResult}.
 *
 * @author berni3
 */
class OutputGrokResult {

    final PrintWriter pwOut;

    public OutputGrokResult(PrintWriter pwOut) {
        this.pwOut = pwOut;
    }

    /**
     * Output just line-number, and {@link GrokMatchResult#toString()}.
     *
     * @param readLineCount
     * @param grokResult
     */
    void outputGrokResultAsIs(int readLineCount, GrokMatchResult grokResult) {
        printOut("%d %s%n", readLineCount, grokResult);
    }

    static class GrokResultTransformer {

        private final List<String> keys = new ArrayList<>();
        private final List<String> values = new ArrayList<>();

        GrokResultTransformer addKeyValue(String k, String v) {
            keys.add(k);
            values.add(v);
            return this;
        }

        GrokResultTransformer addKeys(List<String> keysAllowed, Map<String, Object> m) {
            keys.addAll(keysAllowed);
            for (String k : keysAllowed) {
                final Object o = m.getOrDefault(k, "");
                final String v = convertObjectToString(o);
                values.add(v);
            }
            return this;
        }

        List<String> keys() {
            return this.keys;
        }

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

    /**
     * Output {@link GrokMatchResult} as csv.
     *
     * @param readLineCount
     * @param grokResult
     */
    void outputGrokResultAsCsv(int readLineCount, GrokMatchResult grokResult) {
        final List<String> keysSortedList = grokResult.m.keySet().stream().sorted().collect(Collectors.toList());
        final GrokResultTransformer grokResultTransformer = new GrokResultTransformer()
                .addKeyValue("lineno", String.valueOf(readLineCount))
                .addKeys(keysSortedList, grokResult.m);
        if (readLineCount == 1) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < grokResultTransformer.keys.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                final String k = grokResultTransformer.keys.get(i);
                sb.append(String.format("\"%s\"", k));
            }
            this.println(sb.toString());
        }
        {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < grokResultTransformer.values.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                final String v = grokResultTransformer.values.get(i);
                sb.append(String.format("\"%s\"", v));
            }
            this.println(sb.toString());
        }
    }

    /**
     * Output {@link GrokMatchResult} as json.
     *
     * @param readLineCount
     * @param grokResult
     */
    void outputGrokResultAsJson(int readLineCount, GrokMatchResult grokResult) {
        final List<String> keysSortedList = grokResult.m.keySet().stream().sorted().collect(Collectors.toList());
        final GrokResultTransformer grokResultTransformer = new GrokResultTransformer()
                .addKeyValue("lineno", String.valueOf(readLineCount))
                .addKeys(keysSortedList, grokResult.m);

        final StringBuilder sb = new StringBuilder();
        if (readLineCount > 1) {
            sb.append(String.format(",%n"));
        }
        sb.append(String.format("\"entry\": {%n"));
        for (int i = 0; i < grokResultTransformer.keys.size(); i++) {
            if (i > 0) {
                sb.append(String.format(",%n"));
            }
            String k = grokResultTransformer.keys.get(i);
            String v = grokResultTransformer.values.get(i);
            sb.append(String.format("\"%s\": \"%s\"", k, v));
        }
        sb.append(String.format("%n}"));
        print(sb.toString());
    }

    private void printOut(String format, Object... args
    ) {
        final String str = String.format(format, args);
        this.pwOut.print(str);
    }

    private void println(String str
    ) {
        this.pwOut.println(str);
    }

    private void print(String str
    ) {
        this.pwOut.print(str);
    }

}
