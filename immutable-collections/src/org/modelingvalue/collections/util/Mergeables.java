//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2021 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
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

import java.util.Arrays;
import java.util.Objects;

public interface Mergeables {

    @SafeVarargs
    static <T> T merge(T base, T... branches) {
        return merge(base, branches, branches.length);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static <T> T merge(T base, T[] branches, int l) {
        return merge(base, (b, bs, bl) -> {
            Mergeable merger = b instanceof Mergeable ? (Mergeable) ((Mergeable) b).getMerger() : null;
            if (merger == null) {
                for (int i = 0; i < bl; i++) {
                    if (bs[i] instanceof Mergeable) {
                        merger = (Mergeable) ((Mergeable) bs[i]).getMerger();
                        break;
                    }
                }
            }
            if (merger == null || (b != null && !(b instanceof Mergeable))) {
                throw new NotMergeableException(b + " -> " + Arrays.toString(bs));
            }
            for (int i = 0; i < bl; i++) {
                if (bs[i] == null) {
                    bs[i] = (T) merger;
                }
            }
            return (T) (b == null ? merger : (Mergeable) b).merge(bs, (int) bl);
        }, branches, l);
    }

    static <T> T merge(T base, TriFunction<T, T[], Integer, T> merger, T[] branches, int length) {
        boolean copied = false;
        for (int i = 0; i < length; i++) {
            if (Objects.equals(branches[i], base) || contains(branches, branches[i], i)) {
                if (i < --length) {
                    if (!copied) {
                        branches = Arrays.copyOf(branches, length + 1);
                        copied = true;
                    }
                    System.arraycopy(branches, i + 1, branches, i, length - i--);
                }
            }
        }
        if (length == 0) {
            return base;
        } else if (length == 1) {
            return branches[0];
        } else {
            return merger.apply(base, !copied ? Arrays.copyOf(branches, length) : branches, length);
        }
    }

    static <T> boolean contains(T[] all, T e, int max) {
        for (int i = 0; i < max; i++) {
            if (Objects.equals(all[i], e)) {
                return true;
            }
        }
        return false;
    }
}
