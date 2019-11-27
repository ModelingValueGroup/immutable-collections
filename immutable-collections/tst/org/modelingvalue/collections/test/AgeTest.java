//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2019 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Maintainers:                                                                                                        ~
//     Wim Bast, Carel Bast, Tom Brus                                                                                  ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Ronald Krijgsheld                                                                                    ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.test;

import org.junit.*;
import org.modelingvalue.collections.util.*;

import static org.junit.Assert.*;

public class AgeTest {

    @Test
    public void age() {
        Object[] a = new Object[1];
        Object o = new Object();
        a[0] = o;
        assertEquals("pre age = " + Age.age(a), 0, Age.age(a));
        assertEquals("pre age = " + Age.age(o), 0, Age.age(o));
        Object cg = null;
        @SuppressWarnings("unused")
        int h = 0;
        for (int i = 0; i < 100_000_000; i++) {
            cg = new Object();
            h += cg.hashCode();
        }
        cg = null;
        assertTrue("post age = " + Age.age(a), Age.age(a) > 0);
        assertTrue("post age = " + Age.age(o), Age.age(o) > 0);
    }

}
