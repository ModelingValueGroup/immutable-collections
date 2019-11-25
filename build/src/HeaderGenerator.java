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

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class HeaderGenerator extends ScavangerBase {
    private static final Path        MODULE_DIR        = Paths.get("immutable-collections").toAbsolutePath();
    private static final Path        BASE_DIR          = MODULE_DIR.getParent();
    private static final Set<String> FILES_WITH_HEADER = new HashSet<>(Arrays.asList(
            "header"
    ));
    private static final Set<String> EXT_WITH_HEADER   = new HashSet<>(Arrays.asList(
            "java"
    ));

    public static void main(String[] args) throws IOException {
        new HeaderGenerator().generate();
    }

    void generate() throws IOException {
        prepare();
        Files.walk(BASE_DIR)
                .filter(f -> FORBIDDEN_DIRS.stream().noneMatch(f::startsWith))
                .filter(Files::isRegularFile)
                .filter(this::needsHeader)
                .forEach(this::replaceHeader);
        super.generate();
    }

    private boolean needsHeader(Path f) {
        String           filename = f.getFileName().toString();
        Optional<String> ext      = getExtension(filename);
        try {
            if (Files.size(f) == 0) {
                return false;
            }
            if (FILES_WITH_HEADER.contains(filename)) {
                return true;
            }
            if (ext.isEmpty()) {
                return false;
            }
            return EXT_WITH_HEADER.contains(ext.get());
        } catch (IOException e) {
            throw new Error("file size failed", e);
        }
    }

    private void replaceHeader(Path f) {
        try {
            List<String> lines = f.getFileName().toString().equals("header") ? new ArrayList<>() : Files.readAllLines(f);
            while (!lines.isEmpty() && isHeaderLine(lines.get(0))) {
                lines.remove(0);
            }
            lines.addAll(0, getHeader());
            overwrite(f, lines);
        } catch (IOException e) {
            throw new Error("could not read lines: " + f, e);
        }
    }

    private boolean isHeaderLine(String line) {
        return (line.startsWith("//") && line.endsWith("~")) || line.trim().isEmpty();
    }
}
