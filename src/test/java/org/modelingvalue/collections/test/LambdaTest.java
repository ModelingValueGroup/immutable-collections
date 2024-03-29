//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2023 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
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

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.util.SerializableBiFunction;

public class LambdaTest {

    public static String a(String i1, int i2) {
        return i1 + i2;
    }

    @Test
    public void lambdaReflection() {
        int x = 10;
        SerializableBiFunction<List<String>, Integer, String> f0 = (a, b) -> a.toString() + b + x;
        f0 = f0.of();
        assertEquals(f0.in().get(0), List.class);
        assertEquals(f0.in().get(1), Integer.class);
        assertEquals(f0.in().size(), 2);
        assertEquals(f0.out(), String.class);
        assertEquals("List[x]210", f0.invoke(List.of("x"), 2));

        SerializableBiFunction<String, Integer, String> f1 = LambdaTest::a;
        SerializableBiFunction<String, Integer, String> f2 = LambdaTest::a;
        f1 = f1.of();
        f2 = f2.of();
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertEquals(f1.toString(), f2.toString());
        assertEquals(f1.implMethod(), f2.implMethod());
        assertEquals(f1.implMethod(), reflect());
    }

    private Method reflect() {
        try {
            return LambdaTest.class.getMethod("a", String.class, int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new Error(e);
        }
    }

}
