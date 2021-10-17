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

    //----
    public GrokMatchResult match(Grok grok, String line) {
        Objects.requireNonNull(grok, "Grok is null");
        Objects.requireNonNull(line, "Line is null");
        final Match match = grok.match(line);
        final Map<String, Object> mc = match.capture();
        //Map<String, Object> cf = match.captureFlattened();
        final GrokMatchResult grokResult = new GrokMatchResult(match.getSubject(), match.getStart(), match.getEnd(), mc);
        return grokResult;
    }

    public Map<String, String> defaultPatterns() {
        final GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        final Map<String, String> registeredDefaultPatterns = grokCompiler.getPatternDefinitions();
        return registeredDefaultPatterns;
    }

    static class GrokMatchResult {

        final CharSequence subject;
        final int start;
        final int end;
        final Map<String, Object> m;

        public GrokMatchResult(CharSequence subject, int start, int end, Map<String, Object> m) {
            this.subject = subject;
            this.start = start;
            this.end = end;
            this.m = m;
        }

        @Override
        public String toString() {
            return "GrokResult{" + "subject=" + subject + ", start=" + start + ", end=" + end + ", m=" + m + '}';
        }
    }

}
