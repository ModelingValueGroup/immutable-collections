//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2025 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
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

package org.modelingvalue.logic.impl;

import java.lang.reflect.Proxy;

import org.modelingvalue.logic.Lists.ListCons;
import org.modelingvalue.logic.Logic.Functor;
import org.modelingvalue.logic.Logic.Structure;

public final class ListImpl<E extends Structure> extends StructureImpl<ListCons<E>> {
    private static final long                                   serialVersionUID = -916406585584150604L;

    public org.modelingvalue.collections.List<StructureImpl<E>> list;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ListImpl(FunctorImpl functor) {
        super(functor);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ListImpl(Functor functor, E head, ListCons<E> tail) {
        super(functor, head, tail);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ListImpl(FunctorImpl functor, StructureImpl<E> head, ListImpl<E> tail) {
        super(functor, head, tail);
    }

    private ListImpl(Object[] args) {
        super(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ListCons<E> proxy() {
        return (ListCons<E>) Proxy.newProxyInstance(type().getClassLoader(), new Class[]{ListCons.class}, this);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ListImpl<E> struct(Object[] array) {
        return new ListImpl(array);
    }

    @SuppressWarnings("unchecked")
    protected StructureImpl<E> head() {
        return (StructureImpl<E>) get(1);
    }

    @SuppressWarnings("unchecked")
    protected ListImpl<E> tail() {
        return (ListImpl<E>) get(2);
    }

    @Override
    public String toString() {
        return list().toString().substring(4);
    }

    public org.modelingvalue.collections.List<StructureImpl<E>> list() {
        if (list == null) {
            list = length() == 3 ? tail().list().prepend(head()) : org.modelingvalue.collections.List.of();
        }
        return list;
    }

    @Override
    public ListImpl<E> set(int i, Object... a) {
        return (ListImpl<E>) super.set(i, a);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class<ListCons<E>> type() {
        return (Class) ListCons.class;
    }
}
