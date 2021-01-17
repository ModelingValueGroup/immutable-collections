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

package generator;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StructGenerator extends StructGeneratorBase {
    private final Path       genBaseDir;
    private final List<Path> previouslyGenerated = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("arg error: 3 arg are expected: <max-struct-size> <dir-to-gen-in> <package>");
            System.exit(53);
        }
        int    maxNumTypeArgs       = Integer.parseInt(args[0]);
        Path   genBaseDir           = Paths.get(args[1]);
        String interfaceJavaPackage = args[2];

        new StructGenerator(maxNumTypeArgs, genBaseDir, interfaceJavaPackage).generateAll();
    }

    public StructGenerator(int maxNumTypeArgs, Path genBaseDir, String interfaceJavaPackage) throws IOException {
        super(maxNumTypeArgs, interfaceJavaPackage);
        this.genBaseDir = genBaseDir;

        if (!Files.isDirectory(genBaseDir)) {
            throw new Error("no such dir: " + genBaseDir);
        }

        Path interfacesDir = pathFromPackageName(interfaceJavaPackage);
        Path implementsDir = pathFromPackageName(implementJavaPackage);
        try {
            Files.createDirectories(interfacesDir);
            Files.createDirectories(implementsDir);
        } catch (IOException e) {
            throw new Error("could not create dir: " + interfacesDir + " or " + implementsDir, e);
        }
        Stream.concat(Files.list(interfacesDir), Files.list(implementsDir))//
                .filter(Files::isRegularFile)//
                .filter(f -> f.getFileName().toString().matches("^Struct[0-9][0-9]*(Impl)?\\.java$"))//
                .forEach(previouslyGenerated::add);
    }

    @Override
    public Writer getWriter(String className) throws IOException {
        return new OutputStreamWriter(Files.newOutputStream(pathFromClassName(className)));
    }

    @Override
    protected void write(String className, List<String> lines) throws IOException {
        super.write(className, lines);
        previouslyGenerated.remove(pathFromClassName(className));
    }

    protected void generateAll() throws IOException {
        super.generateAll();
        removeLeftOvers();
    }

    private Path pathFromClassName(String className) {
        return genBaseDir.resolve(className.replace('.', '/') + ".java");
    }

    private Path pathFromPackageName(String className) {
        return genBaseDir.resolve(className.replace('.', '/'));
    }

    private void removeLeftOvers() throws IOException {
        for (Path file : previouslyGenerated) {
            System.err.println("- deleted      : " + file);
            Files.delete(file);
        }
    }
}
