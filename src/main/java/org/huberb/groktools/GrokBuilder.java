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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Builder for building a {@link Grok} instance.
 *
 * @author berni3
 */
public class GrokBuilder {

    private String pattern;
    private Map<String, String> patternDefinitions = Collections.emptyMap();
    private ZoneId defaultTimeZone = ZoneOffset.systemDefault();
    private boolean namedOnly = false;
    private boolean registerDefaultPatterns = true;
    private String patternDefinitionsFromClasspath;
    private File patternDefinitionsFromFile;

    /**
     * GrokBuilder shall use this pattern.
     *
     * @param pattern Grok pattern to compile
     * @return Grok object
     */
    public GrokBuilder pattern(String pattern) {
        new Validations()
                .isStringNullSafeEmpty(pattern)
                .throwIllegalArgumentExceptionIf("Grok pattern to compile is null");
        this.pattern = pattern;
        return this;
    }

    /**
     * GrokBuilder shall register additionally pattern definitions.
     *
     * @param patternDefinitionsMap custom patterns to be registered
     * @return Grok object
     */
    public GrokBuilder patternDefinitions(Map<String, String> patternDefinitionsMap) {
        new Validations()
                .isNull(patternDefinitionsMap)
                .throwIllegalArgumentExceptionIf("Grok map of pattern definitions is null");
        if (patternDefinitionsMap != null) {
            this.patternDefinitions = patternDefinitionsMap;
        }
        return this;
    }

    public GrokBuilder patternDefinitionsFromClasspath(String resource) {
        new Validations()
                .isNull(resource)
                .throwIllegalArgumentExceptionIf("Grok resource of pattern definitions is null");
        // remove leading classpath:
        // replace leading double / (//) by single /
        if (resource != null) {
            String normalizedResource = resource;
            normalizedResource = normalizedResource.replace("classpath:", "");
            normalizedResource = normalizedResource.replace("//", "/");
            this.patternDefinitionsFromClasspath = normalizedResource;
        }
        return this;
    }

    public GrokBuilder patternDefinitionsFromFile(File file) {
        new Validations()
                .isNull(file)
                .isFileNotExisting(file)
                .isFileNotReadable(file)
                .throwIllegalArgumentExceptionIf("Grok file of pattern definitions is not accessible");
        if (file != null) {
            this.patternDefinitionsFromFile = file;
        }
        return this;
    }

    /**
     * Grok shall return only named patterns.
     *
     * @param namedOnly
     * @return
     */
    public GrokBuilder namedOnly(boolean namedOnly) {
        this.namedOnly = namedOnly;
        return this;
    }

    /**
     * GrokBuilder shall register default patterns.
     *
     * @param registerDefaultPatterns
     * @return
     */
    public GrokBuilder registerDefaultPatterns(boolean registerDefaultPatterns) {
        this.registerDefaultPatterns = registerDefaultPatterns;
        return this;
    }

    public Grok build() throws IOException {
        //---        
        final GrokCompiler grokCompiler = GrokCompiler.newInstance();
        if (registerDefaultPatterns) {
            grokCompiler.registerDefaultPatterns();
        }
        if (patternDefinitions != null && !patternDefinitions.isEmpty()) {
            grokCompiler.register(patternDefinitions);
        }
        if (patternDefinitionsFromClasspath != null) {
            grokCompiler.registerPatternFromClasspath(patternDefinitionsFromClasspath);
        }
        if (patternDefinitionsFromFile != null) {
            try (Reader reader = new FileReader(patternDefinitionsFromFile)) {
                grokCompiler.register(reader);
            }
        }

        final Grok grok = grokCompiler.compile(pattern, defaultTimeZone, namedOnly);
        return grok;
    }

    static class Validations {

        boolean value;

        public Validations() {
            this(false);
        }

        public Validations(boolean b) {
            this.value = b;
        }

        public boolean isValue() {
            return value;
        }

        void throwIllegalArgumentExceptionIf(String m) {
            if (value) {
                throw new IllegalArgumentException(m);
            }
        }

        //---
        Validations isNull(Object obj) {
            boolean isNull = this.value;
            isNull = isNull || obj == null;
            this.value = isNull;
            return this;
        }

        Validations isCollectionNullSafeEmpty(Collection<?> collection) {
            boolean isCollectionNullSafeEmpty = this.value;
            isCollectionNullSafeEmpty = isCollectionNullSafeEmpty || collection == null;
            isCollectionNullSafeEmpty = isCollectionNullSafeEmpty || collection.isEmpty();
            this.value = isCollectionNullSafeEmpty;
            return this;
        }

        Validations isStringNullSafeEmpty(String s) {
            boolean isEmpty = this.value;
            isEmpty = isEmpty || s == null;
            isEmpty = isEmpty || s.isEmpty();
            this.value = isEmpty;
            return this;
        }

        Validations isStringNullSafeBlank(String s) {
            boolean isBlank = this.value;
            isBlank = isBlank || s == null;
            isBlank = isBlank || s.isEmpty();
            isBlank = isBlank || s.trim().isEmpty();
            this.value = isBlank;
            return this;
        }

        Validations isFileNotExisting(File file) {
            boolean isFileNotExisting = this.value;
            isFileNotExisting = isFileNotExisting || !file.exists();
            this.value = isFileNotExisting;
            return this;
        }

        Validations isFileNotReadable(File file) {
            boolean isFileNotReadable = this.value;
            isFileNotReadable = isFileNotReadable || !file.canRead();
            this.value = isFileNotReadable;
            return this;
        }

    }
}
