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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author berni3
 */
class GrokIt {

    public GrokIt() {
    }

    /**
     * Get a default Grok instance using the default pattern files loaded from
     * the classpath.
     *
     * @param pattern Grok pattern to compile
     * @return Grok object
     */
    Grok setUp(String pattern) {
        return setUp(pattern, Collections.emptyMap());
    }

    /**
     * Get a default Grok instance using the default pattern files loaded from
     * the classpath.
     *
     * @param pattern Grok pattern to compile
     * @param patternDefinitions custom patterns to be registered
     * @return Grok object
     */
    public Grok setUp(String pattern, Map<String, String> patternDefinitions) {
        Objects.requireNonNull(pattern, "Grok pattern to compile is null");
        final ZoneId defaultTimeZone = ZoneOffset.systemDefault();
        final boolean namedOnly = false;
        final GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        if (patternDefinitions != null && !patternDefinitions.isEmpty()) {
            grokCompiler.register(patternDefinitions);
        }
        final Grok grok = grokCompiler.compile(pattern, defaultTimeZone, namedOnly);
        return grok;
    }

    GrokMatchResult match(Grok grok, String line) {
        Objects.requireNonNull(grok, "Grok is null");
        Objects.requireNonNull(line, "Line is null");
        final Match match = grok.match(line);
        final Map<String, Object> mc = match.capture();
        //Map<String, Object> cf = match.captureFlattened();
        final GrokMatchResult grokResult = new GrokMatchResult(match.getSubject(), match.getStart(), match.getEnd(), mc);
        return grokResult;
    }

    Map<String, String> defaultPatterns() {
        final GrokCompiler grokCompiler = GrokCompiler.newInstance();
        grokCompiler.registerDefaultPatterns();
        Map<String, String> registeredDefaultPatterns = grokCompiler.getPatternDefinitions();
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
