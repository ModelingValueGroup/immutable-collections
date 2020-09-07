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
//     Wim Bast, Tom Brus, Ronald Krijgsheld                                                                           ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Carel Bast                                                                                           ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.util;

import org.modelingvalue.collections.struct.impl.Struct6Impl;

@SuppressWarnings("unused")
public class Sextuple<A, B, C, D, E, F> extends Struct6Impl<A, B, C, D, E, F> {

    private static final long serialVersionUID = 4261851930218225793L;

    public static <X, Y, Z, Q, R, S> Sextuple<X, Y, Z, Q, R, S> of(X a, Y b, Z c, Q d, R e, S f) {
        return new Sextuple<>(a, b, c, d, e, f);
    }

    protected Sextuple(A a, B b, C c, D d, E e, F f) {
        super(a, b, c, d, e, f);
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

    public F f() {
        return get5();
    }

}
