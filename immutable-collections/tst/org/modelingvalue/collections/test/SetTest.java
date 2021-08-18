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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.RecursiveAction;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.impl.HashCollectionImpl;
import org.modelingvalue.collections.util.Context;
import org.modelingvalue.collections.util.ContextThread;

public class SetTest {
    private static final Context<Object> CONTEXT = Context.of();
    private static final long            SEED    = 267835244387707587L;

    @Test
    public void test() {
        Set<String> set1 = Set.of("noot", "mies", "teun", "mies", "jet", "aap");
        System.err.println(set1);
        assertEquals(5, set1.size());
        Set<String> set2 = Set.of("aap", "jet", "mies", "noot", "noot", "noot", "teun");
        System.err.println(set2);
        assertEquals(5, set2.size());
        assertEquals(set1.hashCode(), set2.hashCode());
        assertEquals(set1, set2);
        assertTrue(set1.containsAll(set2));
        assertTrue(set2.containsAll(set1));
        set1.forEachOrdered(obj -> assertTrue(set2.contains(obj)));
        set2.forEachOrdered(obj -> assertTrue(set1.contains(obj)));
        String expected = "aap" + "jet" + "mies" + "noot" + "teun";
        String reduce1 = set1.sequential().reduce("", (a, b) -> a + b);
        String reduce2 = set2.sequential().reduce("", (a, b) -> a + b);
        assertEquals(expected.length(), reduce1.length());
        assertEquals(expected.length(), reduce2.length());
        assertEquals(expected, reduce1);
        assertEquals(expected, reduce2);
    }

    @SuppressWarnings("serial")
    @Test
    public void bigTest() {
        ContextThread.createPool().invoke(new RecursiveAction() {
            @Override
            protected void compute() {
                Object ctx = new Object();
                CONTEXT.run(ctx, () -> {
                    Set<Long> set = Collection.of(LongStream.range(Long.MAX_VALUE - 10_000_000, Long.MAX_VALUE)).reduce(Set.of(), (s, i) -> {
                        assertEquals(ctx, CONTEXT.get());
                        return s.add(i);
                    }, (x, y) -> {
                        assertEquals(ctx, CONTEXT.get());
                        return x.addAll(y);
                    });
                    assertEquals(10_000_000, set.size());
                    double sum = set.reduce(0d, (s, e) -> {
                        assertEquals(ctx, CONTEXT.get());
                        return s + e;
                    }, (s1, s2) -> {
                        assertEquals(ctx, CONTEXT.get());
                        return s1 + s2;
                    });
                    assertEquals(ctx, CONTEXT.get());
                    System.err.println(sum + " / " + set.size() + " = " + (sum / set.size()));
                });
            }
        });
    }

    @Test
    public void randomtest() {
        Random random = new Random();
        random.setSeed(SEED);
        Set<Integer> set1 = Collection.of((Supplier<Integer>) random::nextInt).limit(5_000_000).toSet();
        assertEquals(4_997_213, set1.size());
        random.setSeed(SEED);
        Set<Integer> set2 = Collection.of((Supplier<Integer>) random::nextInt).limit(5_000_000).toSet();
        assertEquals(4_997_213, set2.size());
    }

    @Test
    public void equaltest() {
        int max = 1_000_000;
        Set<Integer> set1 = Collection.of(IntStream.range(0, max)).toSet();
        Set<Integer> set2 = Collection.of(IntStream.range(0, max).map(i -> max - i - 1)).toSet();
        Random random = new Random();
        java.util.Set<Integer> set = Collections.synchronizedSet(new HashSet<>());
        Set<Integer> set3 = Collection.of(() -> {
            int r = random.nextInt(max);
            while (!set.add(r)) {
                r = random.nextInt(max);
            }
            return r;
        }).limit(max).toSet();
        set1.forEachOrdered(obj -> assertTrue(set2.contains(obj)));
        set1.forEachOrdered(obj -> assertTrue(set3.contains(obj)));
        long start = System.currentTimeMillis();
        assertEquals(set1, set2);
        System.err.println("1st equals run = " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        assertEquals(set2, set1);
        System.err.println("2nd equals run = " + (System.currentTimeMillis() - start));
        assertEquals(set3, set2);
        assertEquals(set1, set3);
    }

    @Test
    public void subsetTest() {
        int max = 500_000;
        Set<Integer> set0 = Collection.of(IntStream.range(0, max * 2)).toSet();
        assertEquals(max * 2, set0.size());

        Set<Integer> set1 = Collection.of(IntStream.range(0, max).map(i -> i * 2)).toSet();
        Set<Integer> set2 = Collection.of(IntStream.range(0, max).map(i -> i * 2 + 1)).toSet();
        Set<Integer> set3 = Collection.of(IntStream.range(0, max)).toSet();
        Set<Integer> set4 = Collection.of(IntStream.range(max, max * 2)).toSet();

        Set<Integer> set1a2 = set1.addAll(set2);
        Set<Integer> set3a4 = set3.addAll(set4);
        Set<Integer> set1e2 = set1.exclusiveAll(set2);
        Set<Integer> set3e4 = set3.exclusiveAll(set4);

        assertEquals(set1.size() * 2, set0.size());
        assertEquals(set1.size(), set2.size());
        assertEquals(set2.size(), set3.size());
        assertEquals(set3.size(), set4.size());

        assertTrue(set0.containsAll(set1));
        assertTrue(set0.containsAll(set2));
        assertTrue(set0.containsAll(set3));
        assertTrue(set0.containsAll(set4));

        assertTrue(set1.retainAll(set2).isEmpty());
        assertTrue(set2.retainAll(set1).isEmpty());
        assertTrue(set3.retainAll(set4).isEmpty());
        assertTrue(set4.retainAll(set3).isEmpty());

        assertEquals(set1.removeAll(set2), set1);
        assertEquals(set3.removeAll(set4), set3);
        assertEquals(set0.removeAll(set1), set2);
        assertEquals(set0.removeAll(set3), set4);

        int half = max / 2;

        Set<Integer> set1RemHalf = set1.remove(half);
        assertEquals(set1RemHalf.size() + 1, set1.size());
        assertNotEquals(set1RemHalf, set1);
        assertEquals(set2.remove(half), set2);
        Set<Integer> set3RemHalf = set3.remove(half);
        assertEquals(set3RemHalf.size() + 1, set3.size());
        assertNotEquals(set3RemHalf, set3);
        assertEquals(set4.remove(half), set4);

        assertEquals(set0, set1a2);
        assertEquals(set0, set3a4);
        assertEquals(set0, set1e2);
        assertEquals(set0, set3e4);

        assertNotEquals(set1, set2);
        assertNotEquals(set2, set1);
        assertNotEquals(set3, set4);
        assertNotEquals(set4, set3);
        assertNotEquals(set0, set1);
        assertNotEquals(set0, set2);
        assertNotEquals(set0, set3);
        assertNotEquals(set0, set4);
    }

    @Test
    public void equalHashesTest() {
        Set<HashSharingInteger> set1 = Set.of();
        Set<HashSharingInteger> set2 = Set.of();
        for (int i = 0; i < 100; i++) {
            HashSharingInteger obj = new HashSharingInteger(i, i % 10);
            set1 = set1.add(obj);
            set2 = set2.add(obj);
        }
        assertEquals(100, set1.size());
        Set<HashSharingInteger> test2 = set2;
        set1.forEachOrdered(obj -> assertTrue(test2.contains(obj)));
        assertEquals(set1, set2);
    }

    @Test
    public void merge() {

        Set<Integer> setx = Set.of(-2000, -1900, -1800);
        Set<Integer> sety = Set.of(2000, 1900, 1800);
        System.err.println();
        setx.compare(sety).forEachOrdered(c -> System.err.println(Arrays.deepToString(c)));

        Set<Integer> seta = Set.of(-20, -19, 20);
        Set<Integer> setb = Set.of(-20, -19);
        System.err.println();
        seta.compare(setb).forEachOrdered(c -> System.err.println(Arrays.deepToString(c)));

        Set<Integer> set0 = Set.of(-20, -19, 0, 30, 40);
        Set<Integer> set1 = Set.of(-100, -99, -98, -70, -20, -19, 10);
        Set<Integer> set2 = Set.of(-20, -19, 10, 40, 70, 98, 99, 100);

        System.err.println();
        set0.compare(set1).forEachOrdered(c -> System.err.println(Arrays.deepToString(c)));

        System.err.println();
        set0.compare(set2).forEachOrdered(c -> System.err.println(Arrays.deepToString(c)));

        Set<Integer> merged = set0.merge(set1, set2);

        Set<Integer> set12 = set1.addAll(set2).remove(40);
        assertEquals(set12, merged);

        Set<Integer> all = set1.addAll(set0).addAll(set2);
        assertNotEquals(all, merged);

        Set<Long> lset0 = Collection.of(LongStream.range(20, 100)).toSet();
        Set<Long> lset1 = Collection.of(LongStream.range(40, 120)).toSet();

        System.err.println();
        lset0.compare(lset1).forEachOrdered(c -> System.err.println(Arrays.deepToString(c)));

        Set<Integer> setA = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        Set<Integer> setB = Set.of(10);
        System.err.println();
        setA.compare(setB).forEachOrdered(c -> System.err.println(Arrays.deepToString(c)));
    }

    @Test
    public void contains() {
        int max = 10_000_000;
        int step = Integer.MAX_VALUE / max;
        Set<Integer> set = Collection.of(IntStream.range(-max, max).map(i -> i * step)).toSet();
        assertTrue(IntStream.range(-max, max).map(i -> i * step).allMatch(set::contains));
    }

    @Test
    public void bigBigMerge() {
        int max = 10_000_000;
        int step = Integer.MAX_VALUE / max;
        int half = step / 2;
        Set<Integer> set1 = Collection.of(IntStream.range(-max, max).map(i -> i * step)).toSet();
        Set<Integer> set2 = Collection.of(IntStream.range(-max, max).map(i -> i * step + half)).toSet();
        Set<Integer> set3 = Set.<Integer> of().merge(set1, set2);
        assertTrue(IntStream.range(-max, max).map(i -> i * step).allMatch(i -> set3.contains(i) && set3.contains(i + half)));
    }

    @Test
    public void bigSmallMerge() {
        int max = 10_000_000;
        int min = 10;
        int step = Integer.MAX_VALUE / max;
        int half = step / 2;
        Set<Integer> set1 = Collection.of(IntStream.range(-min, min).map(i -> i * step)).toSet();
        Set<Integer> set2 = Collection.of(IntStream.range(-max, max).map(i -> i * step + half)).toSet();
        Set<Integer> set3 = Set.<Integer> of().merge(set1, set2);
        assertTrue(IntStream.range(-min, min).map(i -> i * step).allMatch(set3::contains));
        assertTrue(IntStream.range(-max, max).map(i -> i * step + half).allMatch(set3::contains));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void checkHashIntegrity() {
        int max = 10_000;
        Set<HashSharingInteger> set = Collection.of(IntStream.range(-max, max)).map(i -> new HashSharingInteger(i, i - i % 5)).toSet();
        assertNull(((HashCollectionImpl) set).checkHashIntegrity());
    }

    private static final class HashSharingInteger {
        private final int integer;
        private final int hashCode;

        private HashSharingInteger(int integer, int hashCode) {
            this.integer = integer;
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof HashSharingInteger && ((HashSharingInteger) other).integer == integer;
        }

        @Override
        public String toString() {
            return Integer.toString(integer);
        }
    }

}
