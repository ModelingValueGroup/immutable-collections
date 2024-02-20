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

package generator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("resource")
public class StructGeneratorTests {
    @Test
    public void testGenerator() throws IOException {
        Path d = Path.of("build/tmp/test-generation");
        new TestStructGenerator(4, d, "only.testing").generateAll();

        Path interfaceDir = d.resolve("only/testing");
        Path implDir      = d.resolve("only/testing/impl");

        assertTrue(Files.isDirectory(d));
        assertTrue(Files.isDirectory(implDir));
        assertEquals(5, Files.list(interfaceDir).count());
        assertEquals(4, Files.list(implDir).count());
        assertTrue(Files.list(interfaceDir).map(f -> f.getFileName().toString()).allMatch(f -> f.equals("impl") || f.matches("Struct[0-9]+[.]java")));
    }

    public static class TestStructGenerator extends StructGenerator {
        private final Path       genBaseDir;
        private final List<Path> previouslyGenerated;

        public TestStructGenerator(int maxNumTypeArgs, Path genBaseDir, String pack) throws IOException {
            super(maxNumTypeArgs, pack);
            this.genBaseDir = genBaseDir;

            Path interfacesDir = pathFromPackageName(interfaceJavaPackage);
            Path implementsDir = pathFromPackageName(implementJavaPackage);
            try {
                Files.createDirectories(interfacesDir);
                Files.createDirectories(implementsDir);
            } catch (IOException e) {
                throw new Error("could not create dir: " + interfacesDir + " or " + implementsDir, e);
            }
            previouslyGenerated = Stream.concat(Files.list(interfacesDir), Files.list(implementsDir))
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().matches("^Struct[0-9][0-9]*(Impl)?\\.java$"))
                    .collect(Collectors.toList());
        }

        private Path pathFromPackageName(String className) {
            return genBaseDir.resolve(className.replace('.', '/'));
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

        @Override
        protected void generateAll() throws IOException {
            super.generateAll();
            for (Path file : previouslyGenerated) {
                System.err.println("- deleted      : " + file);
                Files.delete(file);
            }
        }

        private Path pathFromClassName(String className) {
            return genBaseDir.resolve(className.replace('.', '/') + ".java");
        }
    }
}