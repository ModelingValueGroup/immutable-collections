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

package org.modelingvalue.collections.struct.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.modelingvalue.collections.struct.Struct;
import org.modelingvalue.collections.util.Age;
import org.modelingvalue.collections.util.Internable;
import org.modelingvalue.collections.util.StringUtil;

public abstract class StructImpl implements Struct {
    private static final long serialVersionUID = -1849579252791770119L;

    private Object[]          data;

    protected StructImpl(Object... data) {
        this.data = postCreate(data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj.getClass() != getClass()) {
            return false;
        }
        StructImpl other = (StructImpl) obj;
        if (other.data == data) {
            return true;
        } else if (!Arrays.equals(data, other.data)) {
            return false;
        } else if (Age.age(data) > Age.age(other.data)) {
            other.data = data;
            return true;
        } else {
            data = other.data;
            return true;
        }
    }

    @Override
    public Object get(int i) {
        return data[i];
    }

    protected Object set(int i, Object val) {
        return data[i] = val;
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public String toString() {
        return StringUtil.toString(data);
    }

    @Override
    public boolean isInternable() {
        for (Object obj : data) {
            if (!Internable.isInternable(obj)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<Object> iterator() {
        return Arrays.stream(data).iterator();
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        for (Object o : data) {
            action.accept(o);
        }
    }

    @Override
    public Spliterator<Object> spliterator() {
        return Arrays.spliterator(data);
    }
}
