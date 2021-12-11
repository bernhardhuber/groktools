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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author berni3
 */
public class MatchGatherOutput {

    private Wrapper wrapperStored;

    MatchGatherOutput() {
        this.wrapperStored = null;
    }

    /**
     *
     * @param readLineCount
     * @param subject
     * @param start
     * @param end
     * @param captureMap
     * @return
     */
    public Optional<Result> gatherMatch(
            int readLineCount,
            String subject, int start, int end, Map<String, Object> captureMap) {
        final Optional<Result> resultOptional;
        final Wrapper w = new Wrapper(readLineCount, subject, start, end, captureMap);
        if (w.isHoldingAMatch()) {
            // return wrapperStored
            resultOptional = createResultOptional();

            // store w as new wrapperStored
            this.wrapperStored = w;
        } else if (wrapperStored != null && wrapperStored.isHoldingAMatch()) {
            // return wrappedStore + extra
            wrapperStored.appendExtra(subject);
            resultOptional = Optional.empty();
        } else {
            // uup nothing to return
            // may happen if singe-line-mode in case of non matching line
            resultOptional = Optional.empty();
        }
        return resultOptional;
    }

    /**
     * Just return current wrapperStored, if any.
     *
     * @return
     */
    public Optional<Result> retrieveResult() {
        final Optional<Result> resultOptional = createResultOptional();
        return resultOptional;
    }

    /**
     * Create optional result from current wrapperStored.
     *
     * @return
     */
    Optional<Result> createResultOptional() {
        final Optional<Result> resultOptional;
        if (this.wrapperStored != null) {
            resultOptional = Optional.of(new Result(this.wrapperStored));
        } else {
            resultOptional = Optional.empty();
        }
        return resultOptional;

    }

    /**
     * Wrap up fields of current matched result.
     */
    static class Wrapper {

        final int readLineCount;
        final String subject;
        final int start;
        final int end;
        final Map<String, Object> m;

        final StringBuilder extra;

        public Wrapper(int readLineCount, String subject, int start, int end, Map<String, Object> m) {
            this.readLineCount = readLineCount;
            this.subject = subject;
            this.start = start;
            this.end = end;
            this.m = m;
            this.extra = new StringBuilder();
        }

        /**
         * Decide if current fields of a possible match, represent a match.
         *
         * @return
         */
        boolean isHoldingAMatch() {
            boolean isHoldingAMatch = true;
            isHoldingAMatch = isHoldingAMatch && subject != null;
            isHoldingAMatch = isHoldingAMatch && !subject.isEmpty();
            // 0 <= start
            // start <= end
            // 0 <= end
            isHoldingAMatch = isHoldingAMatch && (0 <= start);
            isHoldingAMatch = isHoldingAMatch && (start <= end);
            isHoldingAMatch = isHoldingAMatch && (0 <= end);
            //
            isHoldingAMatch = isHoldingAMatch && m != null;
            isHoldingAMatch = isHoldingAMatch && !m.isEmpty();
            return isHoldingAMatch;
        }

        void appendExtra(String s) {
            extra.append(s).append("\n");
        }

    }

    static class Result {

        private final Wrapper w;

        public Result(Wrapper w) {
            this.w = w;
        }

        Wrapper wrapper() {
            return this.w;
        }

        Wrapper wrapperWithExtraToMap() {
            final Wrapper w = new Wrapper(
                    this.w.readLineCount,
                    this.w.subject,
                    this.w.start, this.w.end,
                    extraToMap());
            return w;
        }

        Map<String, Object> extraToMap() {
            final Map<String, Object> wmMerged = new HashMap<>(this.w.m);
            wmMerged.put("extra", this.w.extra.toString());
            return wmMerged;
        }
    }
}
