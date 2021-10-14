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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.Callable;
import org.huberb.groktools.GrokMain.GrokIt.GrokResult;
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

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GrokMain()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        GrokIt grokIt = new GrokIt();
        Grok grok = grokIt.setUp(this.pattern);
        try (final Reader logReader = new ReaderFactory(logFile).createUtf8Reader();
                BufferedReader br = new BufferedReader(logReader)) {
            for (String line; (line = br.readLine()) != null;) {
                GrokResult grokResult = grokIt.match(grok, line);
                post_process(grokResult);
            }
        }
        return 0;
    }

    void post_process(GrokResult grokResult) {
        System.out.println(grokResult);
    }

    static class GrokIt {

        public GrokIt() {
        }

        Grok setUp(String pattern) {
            final ZoneId defaultTimeZone = ZoneOffset.systemDefault();
            final boolean namedOnly = true;
            final GrokCompiler grokCompiler = GrokCompiler.newInstance();
            grokCompiler.registerDefaultPatterns();
            final Grok grok = grokCompiler.compile(pattern, defaultTimeZone, namedOnly);

            return grok;
        }

        GrokResult match(Grok grok, String line) {
            final Match match = grok.match(line);
            final Map<String, Object> mc = match.capture();
            //Map<String, Object> cf = match.captureFlattened();
            final GrokResult grokResult = new GrokResult(
                    match.getSubject(),
                    match.getStart(),
                    match.getEnd(),
                    mc
            );

            return grokResult;
        }

        static class GrokResult {

            CharSequence subject;
            int start;
            int end;
            Map<String, Object> m;

            public GrokResult(CharSequence subject, int start, int end, Map<String, Object> m) {
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
