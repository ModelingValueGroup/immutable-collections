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
//     Wim Bast, Carel Bast, Tom Brus                                                                                  ~
// Contributors:                                                                                                       ~
//     Arjan Kok, Ronald Krijgsheld                                                                                    ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class StructGenerator extends ScavangerBase {
    private static final int    MAX_NUM_TYPE_ARGS       = 25;
    //
    private static final Path   INTERFACE_PATH          = Paths.get("org", "modelingvalue", "collections", "struct");
    private static final Path   IMPL_PATH               = INTERFACE_PATH.resolve("impl");
    //
    private static final Path   BASE_SRCGEN_DIR         = MODULE_DIR.resolve("src");
    private static final Path   INTERFACE_SRCGEN_DIR    = BASE_SRCGEN_DIR.resolve(INTERFACE_PATH);
    private static final Path   IMPL_SRCGEN_DIR         = BASE_SRCGEN_DIR.resolve(IMPL_PATH);
    //
    private static final String INTERFACES_JAVA_PACKAGE = INTERFACE_PATH.toString().replace(File.separatorChar, '.');
    private static final String IMPL_JAVA_PACKAGE       = IMPL_PATH.toString().replace(File.separatorChar, '.');


    public static void main(String[] args) throws IOException {
        new StructGenerator().generate();
    }

    void generate() throws IOException {
        prepare();
        for (int i = 0; i < MAX_NUM_TYPE_ARGS; i++) {
            overwrite(INTERFACE_SRCGEN_DIR.resolve(structName(i, false) + ".java"), generateStructInterface(i));
            overwrite(IMPL_SRCGEN_DIR.resolve(structName(i, true) + ".java"), generateStructImplementation(i));
        }
        super.generate();
    }

    void prepare() throws IOException {
        super.prepare();
        try {
            Files.createDirectories(IMPL_SRCGEN_DIR);
        } catch (IOException e) {
            throw new Error("could not create dir: " + IMPL_SRCGEN_DIR);
        }
        Stream.concat(Files.list(INTERFACE_SRCGEN_DIR), Files.list(IMPL_SRCGEN_DIR))//
                .filter(f -> Files.isRegularFile(f))//
                .filter(f -> f.getFileName().toString().matches("^Struct[0-9][0-9]*(Impl)?\\.java$"))//
                .forEach(this::addToPreviouslyGenerated);
    }

    private List<String> generateStructInterface(int i) {
        List<String> f    = new ArrayList<>(getHeader());
        int          prev = i - 1;
        f.add("package " + INTERFACES_JAVA_PACKAGE + ";");
        f.add("");
        f.add("public interface " + structNameWithTypeArgs(i) + " extends " + structNameWithTypeArgs(prev) + " {");
        if (0 != i) {
            f.add("    T" + prev + " get" + prev + " ();");
        }
        f.add("}");
        return f;
    }

    private List<String> generateStructImplementation(int i) {
        List<String> f    = new ArrayList<>(getHeader());
        int          prev = i - 1;
        f.add("package " + IMPL_JAVA_PACKAGE + ";");
        f.add("");
        f.add("import " + INTERFACES_JAVA_PACKAGE + ".*;");
        f.add("");
        if (0 != i) {
            f.add("@SuppressWarnings(\"unchecked\")");
        }
        f.add("public class " + structNameWithTypeArgsImpl(i) + " extends " + structNameWithTypeArgsImpl(prev) + " implements " + structNameWithTypeArgs(i) + " {");
        f.add("");
        f.add("    private static final long serialVersionUID = -85170218" + i + "710134661L;");
        f.add("");
        f.add("    public Struct" + i + "Impl(" + argTypesWithParams(i) + ") {");
        if (0 != i) {
            f.add("        this((Object) " + argParams(i) + ");");
        } else {
            f.add("        super();");
        }
        f.add("    }");
        f.add("");
        f.add("    protected Struct" + i + "Impl(Object...data){");
        f.add("        super(data);");
        f.add("    }");

        if (0 != i) {
            f.add("");
            f.add("    public T" + prev + " get" + prev + "() {");
            f.add("        return (T" + prev + ") get(" + prev + ");");
            f.add("    }");
        }
        f.add("}");
        return f;
    }

    private static String structName(int i, boolean impl) {
        return "Struct" + (i < 0 ? "" : i) + (impl ? "Impl" : "");
    }

    private static String structNameWithTypeArgs(int i) {
        return structName(i, false) + argTypes(i);
    }

    private static String structNameWithTypeArgsImpl(int i) {
        return structName(i, true) + argTypes(i);
    }

    private static String argTypes(int i) {
        return i <= 0 ? "" : "<" + seq(i, "T%d") + ">";
    }

    private static String argTypesWithParams(int i) {
        return i <= 0 ? "" : seq(i, "T%d t%d");
    }

    private static String argParams(int i) {
        return i <= 0 ? "" : seq(i, "t%d");
    }

    private static String seq(int n, String fmt) {
        return IntStream.range(0, n).mapToObj(i -> String.format(fmt, i, i, i, i, i, i, i, i)).collect(Collectors.joining(","));
    }
}
