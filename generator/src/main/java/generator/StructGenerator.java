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

package generator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class StructGenerator {
    protected static final String IMPL = "impl";

    protected final int    maxNumTypeArgs;
    protected final String interfaceJavaPackage;
    protected final String implementJavaPackage;

    public StructGenerator(int maxNumTypeArgs, String interfaceJavaPackage) {
        this.maxNumTypeArgs = maxNumTypeArgs;
        this.interfaceJavaPackage = interfaceJavaPackage;
        this.implementJavaPackage = interfaceJavaPackage + "." + IMPL;
    }

    public abstract Writer getWriter(String className) throws IOException;

    protected void generateAll() throws IOException {
        for (int i = 0; i < maxNumTypeArgs; i++) {
            write(interfaceJavaPackage + "." + structName(i, false), generateStructInterface(i));
            write(implementJavaPackage + "." + structName(i, true), generateStructImplementation(i));
        }
    }

    protected void write(String className, List<String> lines) throws IOException {
        try (PrintWriter printWriter = new PrintWriter(getWriter(className))) {
            lines.forEach(printWriter::println);
        }
    }

    protected static String structName(int i, boolean impl) {
        return "Struct" + (i < 0 ? "" : i) + (impl ? "Impl" : "");
    }

    private static String structNameWithTypeArgs(int i) {
        return StructGenerator.structName(i, false) + StructGenerator.argTypes(i);
    }

    private static String structNameWithTypeArgsImpl(int i) {
        return StructGenerator.structName(i, true) + StructGenerator.argTypes(i);
    }

    private static String argTypes(int i) {
        return i <= 0 ? "" : "<" + StructGenerator.seq(i, "T%d") + ">";
    }

    private static String argTypesWithParams(int i) {
        return i <= 0 ? "" : StructGenerator.seq(i, "T%d t%d");
    }

    private static String argParams(int i) {
        return i <= 0 ? "" : StructGenerator.seq(i, "t%d");
    }

    private static String seq(int n, String fmt) {
        return IntStream.range(0, n).mapToObj(i -> String.format(fmt, i, i, i, i, i, i, i, i)).collect(Collectors.joining(", "));
    }

    protected List<String> generateStructInterface(int i) {
        List<String> f    = new ArrayList<>();
        int          prev = i - 1;
        f.add("package " + interfaceJavaPackage + ";");
        f.add("");
        f.add("public interface " + StructGenerator.structNameWithTypeArgs(i) + " extends " + StructGenerator.structNameWithTypeArgs(prev) + " {");
        if (0 != i) {
            f.add("    T" + prev + " get" + prev + " ();");
        }
        f.add("}");
        return f;
    }

    protected List<String> generateStructImplementation(int i) {
        List<String> f    = new ArrayList<>();
        int          prev = i - 1;
        f.add("package " + implementJavaPackage + ";");
        f.add("");
        f.add("import " + interfaceJavaPackage + "." + StructGenerator.structName(i, false) + ";");
        f.add("");
        if (0 != i) {
            f.add("@SuppressWarnings({\"unchecked\", \"unused\"})");
        }
        f.add("public class " + StructGenerator.structNameWithTypeArgsImpl(i) + " extends " + StructGenerator.structNameWithTypeArgsImpl(prev) + " implements " + StructGenerator.structNameWithTypeArgs(i) + " {");
        f.add("");
        f.add("    private static final long serialVersionUID = " + String.format("0x%08X_%08XL", 0x47114711, StructGenerator.structName(i, true).hashCode()) + ";");
        f.add("");
        f.add("    public Struct" + i + "Impl(" + StructGenerator.argTypesWithParams(i) + ") {");
        if (0 != i) {
            f.add("        this((Object) " + StructGenerator.argParams(i) + ");");
        } else {
            f.add("        super();");
        }
        f.add("    }");
        f.add("");
        f.add("    protected Struct" + i + "Impl(Object... data){");
        f.add("        super(data);");
        f.add("    }");

        if (0 != i) {
            f.add("");
            f.add("    @Override");
            f.add("    public T" + prev + " get" + prev + "() {");
            f.add("        return (T" + prev + ") get(" + prev + ");");
            f.add("    }");
        }
        f.add("}");
        return f;
    }
}
