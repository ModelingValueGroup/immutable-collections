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

package generator;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class StructGenerator {
    private static final String IMPL = "impl";

    private int        maxNumTypeArgs;
    private Path       interfaceSrcGenDir;
    private Path       implementSrcGenDir;
    private String     interfaceJavaPackage;
    private String     implementJavaPackage;
    private List<Path> previouslyGenerated = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new StructGenerator().prepare(Arrays.asList(args)).generate();
    }

    private StructGenerator prepare(List<String> args) throws IOException {
        if (args.size() != 3) {
            System.err.println("arg error: 3 arg are expected: <max-struct-size> <dir-to-gen-in> <package>");
            System.exit(53);
        }
        maxNumTypeArgs = Integer.parseInt(args.get(0));

        Path genDir           = Paths.get(args.get(1));
        Path interfaceGenPack = Paths.get(args.get(2).replace('.', '/'));
        Path implementGenPack = interfaceGenPack.resolve(IMPL);

        interfaceSrcGenDir = genDir.resolve(interfaceGenPack);
        implementSrcGenDir = genDir.resolve(implementGenPack);
        interfaceJavaPackage = interfaceGenPack.toString().replace(File.separatorChar, '.');
        implementJavaPackage = implementGenPack.toString().replace(File.separatorChar, '.');

        if (!Files.isDirectory(genDir)) {
            throw new Error("no such dir: " + genDir);
        }
        try {
            Files.createDirectories(implementSrcGenDir);
        } catch (IOException e) {
            throw new Error("could not create dir: " + implementSrcGenDir);
        }
        Stream.concat(Files.list(interfaceSrcGenDir), Files.list(implementSrcGenDir))//
                .filter(f -> Files.isRegularFile(f))//
                .filter(f -> f.getFileName().toString().matches("^Struct[0-9][0-9]*(Impl)?\\.java$"))//
                .forEach(f1 -> previouslyGenerated.add(f1));
        return this;
    }

    private void generate() throws IOException {
        for (int i = 0; i < maxNumTypeArgs; i++) {
            overwrite(interfaceSrcGenDir.resolve(structName(i, false) + ".java"), generateStructInterface(i));
            overwrite(implementSrcGenDir.resolve(structName(i, true) + ".java"), generateStructImplementation(i));
        }
        removeLeftOvers();
    }

    private void overwrite(Path file, List<String> lines) throws IOException {
        if (Files.notExists(file)) {
            System.err.println("+ generated  : " + file);
            Files.write(file, lines);
        } else {
            List<String> old = Files.readAllLines(file);
            if (!lines.equals(old)) {
                System.err.println("+ regenerated: " + file);
                Files.write(file, lines);
            } else {
                System.err.println("+ already ok : " + file);
            }
        }
        previouslyGenerated.remove(file);
    }

    private void removeLeftOvers() throws IOException {
        for (Path file: previouslyGenerated) {
            System.err.println("- deleted      : " + file);
            Files.delete(file);
        }
    }

    private List<String> generateStructInterface(int i) {
        List<String> f    = new ArrayList<>();
        int          prev = i - 1;
        f.add("package " + interfaceJavaPackage + ";");
        f.add("");
        f.add("public interface " + structNameWithTypeArgs(i) + " extends " + structNameWithTypeArgs(prev) + " {");
        if (0 != i) {
            f.add("    T" + prev + " get" + prev + " ();");
        }
        f.add("}");
        return f;
    }

    private List<String> generateStructImplementation(int i) {
        List<String> f    = new ArrayList<>();
        int          prev = i - 1;
        f.add("package " + implementJavaPackage + ";");
        f.add("");
        f.add("import " + interfaceJavaPackage + ".*;");
        f.add("");
        if (0 != i) {
            f.add("@SuppressWarnings({\"unchecked\", \"unused\"})");
        }
        f.add("public class " + structNameWithTypeArgsImpl(i) + " extends " + structNameWithTypeArgsImpl(prev) + " implements " + structNameWithTypeArgs(i) + " {");
        f.add("");
        f.add("    private static final long serialVersionUID = " + String.format("0x%08X_%08XL", 0x47114711, structName(i, true).hashCode()) + ";");
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
