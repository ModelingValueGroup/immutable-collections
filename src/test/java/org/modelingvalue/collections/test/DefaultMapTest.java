//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2024 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
//                                                                                                                       ~
//  Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in       ~
//  compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0   ~
//  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on  ~
//  an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the   ~
//  specific language governing permissions and limitations under the License.                                           ~
//                                                                                                                       ~
//  Maintainers:                                                                                                         ~
//      Wim Bast, Tom Brus                                                                                               ~
//                                                                                                                       ~
//  Contributors:                                                                                                        ~
//      Ronald Krijgsheld ✝, Arjan Kok, Carel Bast                                                                       ~
// --------------------------------------------------------------------------------------------------------------------- ~
//  In Memory of Ronald Krijgsheld, 1972 - 2023                                                                          ~
//      Ronald was suddenly and unexpectedly taken from us. He was not only our long-term colleague and team member      ~
//      but also our friend. "He will live on in many of the lines of code you see below."                               ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.modelingvalue.collections.DefaultMap;

public class DefaultMapTest {

    private static String reverse(String a) {
        return new StringBuilder(a).reverse().toString();
    }

    private static final String aap  = "aap";
    private static final String noot = "noot";
    private static final String mies = "mies";
    private static final String zus  = "zus";
    private static final String jet  = "jet";
    private static final String teun = "teun";

    @Test
    public void test() {
        DefaultMap<String, String> dm = DefaultMap.of(DefaultMapTest::reverse)
                .put(aap, aap)
                .put(noot, noot)
                .put(mies, mies).put(zus, zus).put(jet, jet);

        assertEquals("nuet", dm.get(teun));
        assertEquals(aap, dm.get(aap));

        dm = dm.removeKey(aap);
        assertEquals("paa", dm.get(aap));

        dm = dm.put(aap, aap);
        assertEquals(aap, dm.get(aap));

        dm = dm.add(noot, noot, (a, b) -> a + b);
        assertEquals("nootnoot", dm.get(noot));

        dm = dm.removeKey(noot);
        dm = dm.put(noot, noot);

        dm = dm.remove(noot, noot, (a, b) -> null);
        assertNull(dm.get(noot));

        dm = dm.put(noot, noot);
        assertEquals(noot, dm.get(noot));

        dm = dm.remove(noot, noot, (a, b) -> a + b);
        assertEquals("nootnoot", dm.get(noot));

        dm = dm.put(noot, "toon");
        assertEquals("toon", dm.get(noot));

        dm = dm.add(noot, noot, (a, b) -> a + b);
        assertEquals(noot, dm.get(noot));
    }
}
