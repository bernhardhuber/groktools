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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import org.huberb.groktools.GrokIt.GrokMatchResult;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *
 * @author berni3
 */
@Command(name = "grokMain",
        mixinStandardHelpOptions = true,
        version = "grokMain 1.0-SNAPSHOT",
        description = "parse log files")
public class GrokMain implements Callable<Integer> {

    @Option(names = {"-f", "--file"},
            description = "read from log file, if not specified read log from stdin")
    private File logFile;
    @Option(names = {"-p", "--pattern"},
            description = "grok pattern")
    private String pattern;
    @Option(names = {"--show-default-patterns"},
            description = "show grok default patterns")
    private boolean showDefaultPatterns;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GrokMain()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        final GrokIt grokIt = new GrokIt();

        if (showDefaultPatterns) {
            showDefaultPatterns(grokIt.defaultPatterns());
        } else {
            Grok grok = grokIt.setUp(this.pattern);
            try (final Reader logReader = new ReaderFactory(logFile).createUtf8Reader();
                    BufferedReader br = new BufferedReader(logReader)) {
                for (String line; (line = br.readLine()) != null;) {
                    GrokMatchResult grokResult = grokIt.match(grok, line);
                    post_process(grokResult);
                }
            }
        }
        return 0;
    }

    void showDefaultPatterns(Map<String, String> defaultPatterns) {
        System.out.format("grok default patterns:%n");
        defaultPatterns.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach((e) -> {
                    System.out.format("%s: %s%n", e.getKey(), e.getValue());
                });
    }

    void post_process(GrokMatchResult grokResult) {
        System.out.println(grokResult);
    }

    /**
     * Factory for creating a {@link Reader}.
     *
     * <p>
     * Use this reader for reading TGF data.
     */
    static class ReaderFactory {

        private final File f;

        public ReaderFactory(File f) {
            this.f = f;
        }

        Reader createUtf8Reader() throws FileNotFoundException {
            final Reader r;
            if (f != null) {
                final FileInputStream fis = new FileInputStream(this.f);
                final InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                r = isr;
            } else {
                final InputStream is = new java.io.BufferedInputStream(System.in);
                final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                r = isr;
            }
            return r;
        }
    }
}
