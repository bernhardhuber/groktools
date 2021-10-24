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
import java.util.List;
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

    /**
     * Ouput {@link GrokMatchResult} as csv.
     *
     * @param readLineCount
     * @param grokResult
     */
    void outputGrokResultAsCsv(int readLineCount, GrokMatchResult grokResult) {
        final List<String> keysSortedList = grokResult.m.keySet().stream().sorted().collect(Collectors.toList());
        if (readLineCount == 1) {
            final StringBuilder sb = new StringBuilder();
            int cols = 0;
            for (String k : keysSortedList) {
                final Object o = k;
                final String v = convertObjectToString(o);
                if (cols > 0) {
                    sb.append(",");
                }
                sb.append(String.format("\"%s\"", v));
                cols += 1;
            }
            printOut("%s%n", sb.toString());
        }
        {
            final StringBuilder sb = new StringBuilder();
            int cols = 0;
            for (String k : keysSortedList) {
                final Object o = grokResult.m.getOrDefault(k, "");
                final String v = convertObjectToString(o);
                if (cols > 0) {
                    sb.append(",");
                }
                sb.append(String.format("\"%s\"", v));
                cols += 1;
            }
            printOut("%s%n", sb.toString());
        }
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

    void printOut(String format, Object... args) {
        final String str = String.format(format, args);
        this.pwOut.print(str);
    }

}
