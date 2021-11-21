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
import java.util.List;
import java.util.stream.Collectors;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import org.huberb.groktools.OutputGrokResult.GrokResultTransformer;

/**
 *
 * @author berni3
 */
public class OutputGrokResultConverters {

    static interface IOutputGrokResultConverter extends AutoCloseable {

        void start();

        void output(int readLineCount, GrokMatchResult grokResult);

        void end();

        @Override
        void close() throws IOException;
    }

    /**
     * Output {@link GrokMatchResult} as plain text.
     */
    static class OutputGrokResultAsIs implements IOutputGrokResultConverter {

        final PrintWriter pwOut;

        public OutputGrokResultAsIs(PrintWriter pwOut) {
            this.pwOut = pwOut;
        }

        @Override
        public void start() {
        }

        /**
         * Output just line-number, and {@link GrokMatchResult#toString()}.
         *
         * @param readLineCount
         * @param grokResult
         */
        @Override
        public void output(int readLineCount, GrokMatchResult grokResult) {
            printOut("%d %s%n", readLineCount, grokResult);
        }

        @Override
        public void end() {
        }

        @Override
        public void close() {
            if (this.pwOut != null) {
                this.pwOut.close();
            }
        }

        private void printOut(String format, Object... args) {
            final String str = String.format(format, args);
            this.pwOut.print(str);
        }

    }

    /**
     * Output {@link GrokMatchResult} as csv text.
     */
    static class OutputGrokResultAsCsv implements IOutputGrokResultConverter {

        final PrintWriter pwOut;

        public OutputGrokResultAsCsv(PrintWriter pwOut) {
            this.pwOut = pwOut;
        }

        @Override
        public void start() {
        }

        /**
         * Output {@link GrokMatchResult} as csv.
         *
         * @param readLineCount
         * @param grokResult
         */
        @Override
        public void output(int readLineCount, GrokMatchResult grokResult) {
            final List<String> keysSortedList = grokResult.m.keySet().stream().sorted().collect(Collectors.toList());
            final GrokResultTransformer grokResultTransformer = new GrokResultTransformer()
                    .addKeyValue("lineno", String.valueOf(readLineCount))
                    .addKeys(keysSortedList, grokResult.m);
            if (readLineCount == 1) {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < grokResultTransformer.keys().size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    final String k = grokResultTransformer.keys().get(i);
                    sb.append(String.format("\"%s\"", k));
                }
                this.println(sb.toString());
            }
            {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < grokResultTransformer.values().size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    final String v = grokResultTransformer.values().get(i);
                    sb.append(String.format("\"%s\"", v));
                }
                this.println(sb.toString());
            }
        }

        @Override
        public void end() {
        }

        @Override
        public void close() {
            if (this.pwOut != null) {
                this.pwOut.close();
            }
        }

        private void println(String str) {
            this.pwOut.println(str);
        }

    }

    /**
     * Output {@link GrokMatchResult} as json text.
     */
    static class outputGrokResultAsJson implements IOutputGrokResultConverter {

        final PrintWriter pwOut;

        public outputGrokResultAsJson(PrintWriter pwOut) {
            this.pwOut = pwOut;
        }

        @Override
        public void start() {
            final StringBuilder sb = new StringBuilder();
            sb.append(String.format("[%n"));
            this.print(sb.toString());
        }

        /**
         * Output {@link GrokMatchResult} as json.
         *
         * @param readLineCount
         * @param grokResult
         */
        @Override
        public void output(int readLineCount, GrokMatchResult grokResult) {
            final List<String> keysSortedList = grokResult.m.keySet().stream().sorted().collect(Collectors.toList());
            final GrokResultTransformer grokResultTransformer = new GrokResultTransformer()
                    .addKeyValue("lineno", String.valueOf(readLineCount))
                    .addKeys(keysSortedList, grokResult.m);

            final StringBuilder sb = new StringBuilder();
            if (readLineCount > 1) {
                sb.append(String.format(",%n"));
            }
            sb.append(String.format("\"entry\": {%n"));
            for (int i = 0; i < grokResultTransformer.keys().size(); i++) {
                if (i > 0) {
                    sb.append(String.format(",%n"));
                }
                String k = grokResultTransformer.keys().get(i);
                String v = grokResultTransformer.values().get(i);
                sb.append(String.format("\"%s\": \"%s\"", k, v));
            }
            sb.append(String.format("%n}"));
            this.print(sb.toString());
        }

        @Override
        public void end() {
            final StringBuilder sb = new StringBuilder();
            sb.append(String.format("%n]%n"));
            this.print(sb.toString());
        }

        @Override
        public void close() {
            if (this.pwOut != null) {
                this.pwOut.close();
            }
        }

        private void print(String str) {
            this.pwOut.print(str);
        }
    }

}
