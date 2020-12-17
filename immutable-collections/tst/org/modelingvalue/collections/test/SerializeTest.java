//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2020 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
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

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.*;

public class SerializeTest {
    @Test
    public void serializeSetOfNotSerializable() {
        assertThrows(NotSerializableException.class, () -> serialize(Set.of(new Object())));
    }

    @Test
    public void serializeShortList() throws IOException, ClassNotFoundException {
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        assertEquals(5, list.size());
        assertEquals(list, deserialize(serialize(list)));
    }

    @Test
    public void serializeLongList() throws IOException, ClassNotFoundException {
        List<Integer> list = List.of(x -> x, IntStream.range(0, 1000).boxed().toArray(Integer[]::new));
        assertEquals(1000, list.size());
        assertEquals(list, deserialize(serialize(list)));
    }

    @Test
    public void serializeSmallSet() throws IOException, ClassNotFoundException {
        Set<Integer> set = Set.of(1, 2, 3, 4, 5);
        assertEquals(5, set.size());
        assertEquals(set, deserialize(serialize(set)));
    }

    @Test
    public void serializeLargeSet() throws IOException, ClassNotFoundException {
        Set<Integer> set = Set.of(x -> x, IntStream.range(0, 1000).boxed().toArray(Integer[]::new));
        assertEquals(1000, set.size());
        assertEquals(set, deserialize(serialize(set)));
    }

    @Test
    public void serializeQualifiedSet() throws IOException, ClassNotFoundException {
        QualifiedSet<String, String> qset = QualifiedSet.of(s -> s, "a", "b", "c", "d", "e");
        assertEquals(5, qset.size());
        assertEquals(qset, deserialize(serialize(qset)));
    }

    @Test
    public void serializeMap() throws IOException, ClassNotFoundException {
        Map<String, String> map = Map.of(
                Entry.of("a", "0"),
                Entry.of("b", "1"),
                Entry.of("c", "2"),
                Entry.of("d", "3"),
                Entry.of("e", "4")
        );
        assertEquals(5, map.size());
        assertEquals(map, deserialize(serialize(map)));
    }

    @Test
    public void serializeDefaultMap() throws IOException, ClassNotFoundException {
        DefaultMap<String, String> dmap = DefaultMap.of(s -> s,
                Entry.of("a", "0"),
                Entry.of("b", "1"),
                Entry.of("c", "2"),
                Entry.of("d", "3"),
                Entry.of("e", "4")
        );
        assertEquals(5, dmap.size());
        assertEquals(dmap, deserialize(serialize(dmap)));
    }

    @Test
    public void serializeSetOfObjectsWithEquals() throws IOException, ClassNotFoundException {
        Set<TestObjectWithEquals> set = Set.of(TestObjectWithEquals::new, IntStream.range(0, 1000).boxed().toArray(Integer[]::new));
        assertEquals(1000, set.size());
        assertEquals(set, deserialize(serialize(set)));
    }

    @Test
    public void serializeSetOfObjectsWithoutEquals() throws IOException, ClassNotFoundException {
        Set<TestObjectWithoutEquals> orig = Set.of(TestObjectWithoutEquals::new, IntStream.range(0, 1000).boxed().toArray(Integer[]::new));
        assertEquals(1000, orig.size());

        Set<TestObjectWithoutEquals> copy = deserialize(serialize(orig));
        assertEquals(1000, copy.size());
        assertNotEquals(orig, copy); // should not be equal because two TestObjectWithoutEquals are never equal!

        // verify that all are represented in the original and in the copy:
        Boolean[] inOrig = new Boolean[orig.size()];
        Boolean[] inCopy = new Boolean[copy.size()];

        orig.forEach(e -> inOrig[e.i] = true);
        copy.forEach(e -> inCopy[e.i] = true);

        assertTrue(Stream.of(inOrig).allMatch(x -> x));
        assertTrue(Stream.of(inCopy).allMatch(x -> x));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static class TestObjectWithoutEquals implements Serializable {
        public static final long serialVersionUID = 1636023751463490430L;
        public final        int  i;

        public TestObjectWithoutEquals(int i) {
            this.i = i;
        }

        @Override
        public String toString() {
            return "#" + i + "#";
        }
    }

    static class TestObjectWithEquals extends TestObjectWithoutEquals {
        public static final long serialVersionUID = 1636023751463490431L;

        public TestObjectWithEquals(int i) {
            super(i);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestObjectWithoutEquals that = (TestObjectWithoutEquals) o;
            return i == that.i;
        }

        @Override
        public int hashCode() {
            return Objects.hash(i);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    private <T extends Serializable> T deserialize(String encoded) throws IOException, ClassNotFoundException {
        byte[] decoded = Base64.getDecoder().decode(encoded);
        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(decoded))) {
            return (T) in.readObject();
        }
    }

    private <T extends Serializable> String serialize(T toTest) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out;
            out = new ObjectOutputStream(bos);
            out.writeObject(toTest);
            out.close();
            String s = Base64.getEncoder().encodeToString(bos.toByteArray());
            System.err.printf("serialization yielded %d length string\n", s.length());
            return s;
        }
    }
}
