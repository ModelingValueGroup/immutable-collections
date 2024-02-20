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
import static org.modelingvalue.collections.util.Age.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

public class AgeTest {
    public static final int LOOPS = 100_000_000;

    @Test
    public void ageCheck() {
        Object[] arr = new Object[]{new Object()};

        int age_arr_pre = age(arr);
        int age_sub_pre = age(arr[0]);
        assertAll(
                () -> assertEquals(0, age_arr_pre, "age was " + age_arr_pre),
                () -> assertEquals(0, age_sub_pre, "age was " + age_sub_pre)
        );

        useLotsOfMemory(i -> System.out.printf("... i=%2d    age=%2d    age=%2d\n", i, age(arr), age(arr[0])));

        int age_arr_post = age(arr);
        int age_sub_post = age(arr[0]);
        assertAll(
                () -> assertTrue(0 < age_arr_post, "age was " + age_arr_post),
                () -> assertTrue(0 < age_sub_post, "age was " + age_sub_post)
        );
    }

    private void useLotsOfMemory(Consumer<Integer> r) {
        r.accept(0);
        for (int j = 1; j <= 10; j++) {
            for (int i = 0; i < LOOPS / 10; i++) {
                @SuppressWarnings("unused")
                int h = new Object().hashCode();
            }
            r.accept(j);
        }
    }
}
