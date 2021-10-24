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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class GrokBuilderTest {

    public GrokBuilderTest() {
    }

    /**
     * Test of pattern method, of class GrokBuilder.
     */
    @Test
    public void testPattern() throws IOException {
        String pattern = "some-regexp";
        GrokBuilder instance = new GrokBuilder();
        Grok result = instance.pattern(pattern).build();
        assertNotNull(result);
    }

    /**
     * Test of patternDefinitions method, of class GrokBuilder.
     */
    @Test
    public void testPatternDefinitions() throws IOException {
        String pattern = "some-regexp";
        Map<String, String> patternDefinitions = new HashMap<>();
        GrokBuilder instance = new GrokBuilder();
        Grok result = instance.patternDefinitions(patternDefinitions).pattern(pattern).build();
        assertNotNull(result);
    }

    /**
     * Test of patternDefinitionsFromClasspath method, of class GrokBuilder.
     */
    @Test
    public void testPatternDefinitionsFromClasspath() throws IOException {
        String pattern = "some-regexp";
        String resource = "";
        GrokBuilder instance = new GrokBuilder();

        Grok result = instance.patternDefinitionsFromClasspath(resource).pattern(pattern).build();
        assertNotNull(result);
    }

    /**
     * Test of patternDefinitionsFromFile method, of class GrokBuilder.
     */
    @Test
    public void testPatternDefinitionsFromFile() throws IOException {
        String pattern = "some-regexp";
        File file = new File( "src/main/resources/groktoolspatterns", "server_log");
        GrokBuilder instance = new GrokBuilder();
        Grok result = instance.patternDefinitionsFromFile(file).pattern(pattern).build();
        assertNotNull(result);
    }

    /**
     * Test of namedOnly method, of class GrokBuilder.
     */
    @Test
    public void testNamedOnly() throws IOException {
        String pattern = "some-regexp";
        boolean namedOnly = false;
        GrokBuilder instance = new GrokBuilder();
        Grok result = instance.namedOnly(namedOnly).pattern(pattern).build();
        assertNotNull(result);
    }

    /**
     * Test of registerDefaultPatterns method, of class GrokBuilder.
     */
    @Test
    public void testRegisterDefaultPatterns() throws IOException {
        String pattern = "some-regexp";
        boolean registerDefaultPatterns = false;
        GrokBuilder instance = new GrokBuilder();
        Grok result = instance.registerDefaultPatterns(registerDefaultPatterns).pattern(pattern).build();
        assertNotNull(result);
    }

    /**
     * Test of build method, of class GrokBuilder.
     */
    @Test
    public void testBuild() throws IOException {
        String pattern = "some-regexp";
        GrokBuilder instance = new GrokBuilder();
        Grok result = instance.pattern(pattern).build();
        assertNotNull(result);
    }

}
