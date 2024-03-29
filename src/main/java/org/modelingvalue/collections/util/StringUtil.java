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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class StringUtil {

    private StringUtil() {
    }

    @SuppressWarnings("rawtypes")
    public static String toString(Object o) {
        if (o == null) {
            return "null";
        } else if (o.getClass().isArray()) {
            Object[] a = (Object[]) o;
            int iMax = a.length - 1;
            if (iMax == -1) {
                return "[]";
            } else {
                StringBuilder b = new StringBuilder();
                b.append('[');
                for (int i = 0;; i++) {
                    b.append(toString(a[i]));
                    if (i == iMax)
                        return b.append(']').toString();
                    b.append(",");
                }
            }
        } else if (o instanceof Class) {
            return ((Class) o).getSimpleName();
        } else if (o instanceof Method) {
            return ((Method) o).getDeclaringClass().getSimpleName() + ":" + ((Method) o).getName();
        } else if (o instanceof Parameter) {
            return ((Parameter) o).getDeclaringExecutable().getName() + ":" + ((Parameter) o).getName();
        } else if (o instanceof Package) {
            return ((Package) o).getName();
        } else {
            return o.toString();
        }
    }

}
