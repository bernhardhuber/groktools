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

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import java.util.Map;
import java.util.Objects;

/**
 * Mediator for setting up {@link GrokCompiler}, and {@link Grok}.
 *
 * @author berni3
 */
class GrokIt {

    public GrokIt() {
    }

    /**
     * Match a line.
     *
     * @param grok
     * @param line
     * @return GrokMatchResult
     */
    public GrokMatchResult match(Grok grok, String line) {
        Objects.requireNonNull(grok, "Grok is null");
        Objects.requireNonNull(line, "Line is null");
        final Match match = grok.match(line);
        final Map<String, Object> mc = match.capture();
        //Map<String, Object> cf = match.captureFlattened();
        final GrokMatchResult grokResult = new GrokMatchResult(
                match.getSubject(),
                match.getStart(), match.getEnd(),
                mc);
        return grokResult;
    }

    /**
     * Retrieve pattern definitions.
     *
     * @param grok
     * @return
     */
    public Map<String, String> retrievePatterndefinitions(Grok grok) {
        final Map<String, String> registeredDefaultPatterns = grok.getPatterns();
        return registeredDefaultPatterns;
    }

    /**
     * Encapsulate results of a match call.
     */
    static class GrokMatchResult {

        final CharSequence subject;
        final int start;
        final int end;
        final Map<String, Object> m;

        /**
         * Store subject line, start-, and end-index of match, and map of
         * matched sub strings of the subject line.
         *
         * @param subject
         * @param start
         * @param end
         * @param m
         */
        public GrokMatchResult(
                CharSequence subject,
                int start,
                int end,
                Map<String, Object> m) {
            this.subject = subject;
            this.start = start;
            this.end = end;
            this.m = m;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + Objects.hashCode(this.subject);
            hash = 23 * hash + this.start;
            hash = 23 * hash + this.end;
            hash = 23 * hash + Objects.hashCode(this.m);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GrokMatchResult other = (GrokMatchResult) obj;
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if (!Objects.equals(this.subject, other.subject)) {
                return false;
            }
            if (!Objects.equals(this.m, other.m)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final String toString = String.format("GrokResult { "
                    + "subject: %s, "
                    + "start: %s, "
                    + "end: %s, "
                    + "m: %s }", subject, start, end, m);
            return toString;
        }
    }

}
