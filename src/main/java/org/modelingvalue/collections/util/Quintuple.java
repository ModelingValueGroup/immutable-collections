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

import org.modelingvalue.collections.struct.impl.Struct5Impl;

@SuppressWarnings("unused")
public class Quintuple<A, B, C, D, E> extends Struct5Impl<A, B, C, D, E> {

    private static final long serialVersionUID = -7572717838812193661L;

    public static <X, Y, Z, Q, R> Quintuple<X, Y, Z, Q, R> of(X a, Y b, Z c, Q d, R e) {
        return new Quintuple<>(a, b, c, d, e);
    }

    protected Quintuple(A a, B b, C c, D d, E e) {
        super(a, b, c, d, e);
    }

    public A a() {
        return get0();
    }

    public B b() {
        return get1();
    }

    public C c() {
        return get2();
    }

    public D d() {
        return get3();
    }

    public E e() {
        return get4();
    }

}
