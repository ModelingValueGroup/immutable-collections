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

package org.modelingvalue.collections.util;

import java.io.*;
import java.util.*;
import java.util.function.*;

public class NonLockingPrintWriter extends PrintWriter {

    public static NonLockingPrintWriter of(Consumer<String> consumer) {
        return new NonLockingPrintWriter(consumer);
    }

    protected NonLockingPrintWriter(Consumer<String> consumer) {
        super(new OutputWriter(consumer));
    }

    @Override
    public void write(String s, int off, int len) {
        try {
            out.write(s, off, len);
        } catch (IOException e) {
            throw new Error("IOException in NonLockingPrintWriter.write(String,int,int)", e);
        }
    }

    @Override
    public void write(char[] buf, int off, int len) {
        try {
            out.write(buf, off, len);
        } catch (IOException e) {
            throw new Error("IOException in NonLockingPrintWriter.write(char[],int,int)", e);
        }
    }

    @Override
    public void write(int c) {
        try {
            out.write(c);
        } catch (IOException e) {
            throw new Error("IOException in NonLockingPrintWriter.write(int)", e);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @SuppressWarnings("resource")
    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
        new Formatter(this, l).format(l, format, args);
        return this;
    }

    @SuppressWarnings("resource")
    @Override
    public PrintWriter format(String format, Object... args) {
        new Formatter(this).format(Locale.getDefault(), format, args);
        return this;
    }

    @Override
    public void println() {
        print(System.lineSeparator());
    }

    @Override
    public void println(String x) {
        print(x + System.lineSeparator());
    }

    @Override
    public void println(boolean x) {
        print(x + System.lineSeparator());
    }

    @Override
    public void println(char x) {
        print(x + System.lineSeparator());
    }

    @Override
    public void println(char[] x) {
        write(x);
        println();
    }

    @Override
    public void println(double x) {
        print(x + System.lineSeparator());
    }

    @Override
    public void println(float x) {
        print(x + System.lineSeparator());
    }

    @Override
    public void println(int x) {
        print(x + System.lineSeparator());
    }

    @Override
    public void println(long x) {
        print(x + System.lineSeparator());
    }

    @Override
    public void println(Object x) {
        print(x + System.lineSeparator());
    }

    private static final class OutputWriter extends Writer {

        private final Consumer<String> consumer;

        public OutputWriter(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void write(String str, int off, int len) {
            consumer.accept(str.substring(off, off + len));
        }

        @Override
        public void write(char[] arg0, int arg1, int arg2) {
            String post = String.valueOf(arg0, arg1, arg2);
            write(post, 0, post.length());
        }

        @Override
        public void write(int c) {
            write(new char[]{(char) c}, 0, 1);
        }

    }

}
