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

@SuppressWarnings({"WeakerAccess"})
public abstract class ScavangerBase {
    static final Set<Path> FORBIDDEN_DIRS = new HashSet<>(Arrays.asList(
            Paths.get(".git"),
            Paths.get(".idea"),
            Paths.get("out"),
            Paths.get("lib")
    ));

    Stream<Path> allFiles() throws IOException {
        return Files.walk(Paths.get("."))
                .filter(p -> FORBIDDEN_DIRS.stream().noneMatch(p::startsWith))
                .filter(Files::isRegularFile);
    }

    void overwrite(Path file, List<String> lines) throws IOException {
        overwrite(file, lines, false);
    }

    void overwrite(Path file, List<String> lines, boolean forced) throws IOException {
        if (forced || !Files.isRegularFile(file)) {
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
    }

    static Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1));
    }
}
