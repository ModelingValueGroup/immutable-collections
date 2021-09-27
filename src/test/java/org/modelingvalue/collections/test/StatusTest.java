//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2021 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Maintainers:                                                                                                        ~
//     Wim Bast, Tom Brus, Ronald Krijgsheld                                                                           ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Carel Bast                                                                                           ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.test;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.modelingvalue.collections.util.StatusProvider;
import org.modelingvalue.collections.util.StatusProvider.AbstractStatus;
import org.modelingvalue.collections.util.StatusProvider.StatusIterator;

public class StatusTest {
    @Test
    public void simple() {
        StatusFeeder               feeder   = new StatusFeeder();
        StatusIterator<TestStatus> iterator = feeder.iterator();

        assertTimeoutPreemptively(ofMillis(20), () -> assertNext(feeder, iterator, "$start$", 0, true, 0));
    }

    @Test
    public void complex() {
        StatusFeeder               feeder   = new StatusFeeder("A", "B", "100:C", "100:D", "E");
        StatusIterator<TestStatus> iterator = feeder.iterator();

        assertTimeoutPreemptively(ofMillis(300), () -> {
            assertNext(feeder, iterator, "$start$", 0, false, 0);
            assertNext(feeder, iterator, "A", 1, false, 0);
            assertNext(feeder, iterator, "B", 2, false, 0);
            assertNext(feeder, iterator, "100:C", 3, false, 100);
            assertNext(feeder, iterator, "100:D", 4, false, 100);
            assertNext(feeder, iterator, "E", 5, true, 0);
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void assertNext(StatusFeeder feeder, StatusIterator<TestStatus> iterator, String expectedName, int i, boolean expectStopped, long expectTime) {
        long t0 = System.currentTimeMillis();
        assertTrue(iterator.hasNext());
        TestStatus testStatus = iterator.next();
        assertNotNull(testStatus);
        assertEquals(expectedName, testStatus.name);
        assertSame(feeder.get(i), testStatus);
        assertEquals(expectStopped, testStatus.isStopped());
        long t1 = System.currentTimeMillis();
        if (expectTime == 0) {
            assertTrue(0 <= (t1 - t0));
            assertTrue((t1 - t0) <= 20);
        } else {
            assertTrue((expectTime * 90) / 100 <= (t1 - t0));
            assertTrue((t1 - t0) <= (expectTime * 110) / 100);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static class TestStatus extends AbstractStatus {
        private final String  name;
        private final long    preDelay;
        private final boolean stop;

        public TestStatus(String name, boolean last) {
            this.name     = name;
            this.preDelay = extracDelay(name);
            this.stop     = last;
        }

        private long extracDelay(String name) {
            String[] a = name.split(":");
            if (a.length == 2) {
                return Long.parseLong(a[0]);
            }
            return 0;
        }

        @Override
        public boolean isStopped() {
            return stop;
        }
    }

    private static class StatusFeeder extends Thread {
        private final StatusProvider<TestStatus> testStatusProvider;
        private final List<TestStatus>           statusList = new ArrayList<>();

        public StatusFeeder(String... args) {
            statusList.add(new TestStatus("$start$", args.length == 0));
            for (int i = 0; i < args.length; i++) {
                statusList.add(new TestStatus(args[i], args.length - 1 == i));
            }
            testStatusProvider = new StatusProvider<>(get(0));
            setDaemon(true);
            start();
        }

        @Override
        public void run() {
            for (int i = 1; i < statusList.size(); i++) {
                TestStatus next = statusList.get(i);
                if (next.preDelay > 0) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(next.preDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                testStatusProvider.setNext(pre -> next);
            }
        }

        public StatusIterator<TestStatus> iterator() {
            return testStatusProvider.iterator();
        }

        public TestStatus get(int i) {
            return statusList.get(i);
        }
    }
}
