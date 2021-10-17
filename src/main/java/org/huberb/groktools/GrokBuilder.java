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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author berni3
 */
public class GrokBuilder {

    String pattern;
    Map<String, String> patternDefinitions = Collections.emptyMap();
    ZoneId defaultTimeZone = ZoneOffset.systemDefault();
    boolean namedOnly = false;
    boolean registerDefaultPatterns = true;

    /**
     * Get a default Grok instance using the default pattern files loaded from
     * the classpath.
     *
     * @param pattern Grok pattern to compile
     * @return Grok object
     */
    public GrokBuilder pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Get a default Grok instance using the default pattern files loaded from
     * the classpath.
     *
     * @param pattern Grok pattern to compile
     * @param patternDefinitions custom patterns to be registered
     * @return Grok object
     */
    public GrokBuilder patternDefinitions(String pattern, Map<String, String> patternDefinitions) {
        Objects.requireNonNull(pattern, "Grok pattern to compile is null");
        this.pattern = pattern;
        this.patternDefinitions = patternDefinitions;
        return this;
    }

    public GrokBuilder namedOnly(boolean namedOnly) {
        this.namedOnly = namedOnly;
        return this;
    }

    public Grok build() {
        //---        
        final GrokCompiler grokCompiler = GrokCompiler.newInstance();
        if (registerDefaultPatterns) {
            grokCompiler.registerDefaultPatterns();
        }
        if (patternDefinitions != null && !patternDefinitions.isEmpty()) {
            grokCompiler.register(patternDefinitions);
        }
        final Grok grok = grokCompiler.compile(pattern, defaultTimeZone, namedOnly);
        return grok;
    }
}
