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

public class OverflowWorkerThread extends DclareWorkerThread {
    public static final String OVERFLOW_WORKER_THREAD_NAME_TEMPLATE = ContextThread.WORKER_THREAD_NAME_TEMPLATE + "-OVERFLOW";

    public OverflowWorkerThread(ContextPool pool) {
        super(pool, ContextThread.POOL_SIZE + pool.incrementAndGetNumInOverflow(), OVERFLOW_WORKER_THREAD_NAME_TEMPLATE);
        setContextClassLoader(ClassLoader.getSystemClassLoader());
        System.err.println("WARNING: Overflow ForkJoinWorkerThread created, consider increasing POOL_SIZE (=" + ContextThread.POOL_SIZE + ") to at least " + getNr());
    }
}
