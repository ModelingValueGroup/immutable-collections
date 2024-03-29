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

package org.modelingvalue.collections.util;

public interface Internable {

    default boolean isInternable() {
        return true;
    }

    static boolean isInternable(Object value) {
        if (value instanceof Internable) {
            return ((Internable) value).isInternable();
        } else if (value instanceof Boolean || value instanceof Byte || value instanceof Enum) {
            return true;
        } else if (value instanceof Integer) {
            int i = (int) value;
            return i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE;
        } else if (value instanceof Short) {
            short s = (short) value;
            return s >= Byte.MIN_VALUE && s <= Byte.MAX_VALUE;
        } else if (value instanceof Long) {
            long l = (long) value;
            return l >= Byte.MIN_VALUE && l <= Byte.MAX_VALUE;
        } else {
            return value == null;
        }
    }

}
