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
public class EolCorrector extends ScavangerBase {
    private static final Set<String> TEXT_FILES         = new HashSet<>(Arrays.asList(
            "LICENSE",
            ".gitignore",
            "header"
    ));
    private static final Set<String> NO_TEXT_FILES      = new HashSet<>(Arrays.asList(
            ".DS_Store"
    ));
    private static final Set<String> TEXT_EXTENSIONS    = new HashSet<>(Arrays.asList(
            "java",
            "properties",
            "md",
            "yaml",
            "yml",
            "sh",
            "pom",
            "java",
            "xml"
    ));
    private static final Set<String> NO_TEXT_EXTENSIONS = new HashSet<>(Arrays.asList(
            "iml",
            "jar",
            "class"
    ));

    public static void main(String[] args) throws IOException {
        if (args.length!=0){
            System.err.println("no args expected");
            System.exit(31);
        }
        new EolCorrector().generate();
    }

    private void generate() throws IOException {
        allFiles()
                .filter(this::isTextType)
                .forEach(this::correctCRLF);
    }

    private void correctCRLF(Path f) {
        try {
            String       all   = Files.readString(f);
            int          numcr = all.replaceAll("[^\r]", "").length();
            int          numlf = all.replaceAll("[^\n]", "").length();
            List<String> lines = Files.readAllLines(f);
            if (numcr > 0 || lines.size() != numlf) {
                System.err.printf("rewriting file: %4d lines (%4d cr and %4d lf) - %s\n", lines.size(), numcr, numlf, f);
                overwrite(f, lines, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isTextType(Path f) {
        String           filename = f.getFileName().toString();
        Optional<String> ext      = getExtension(filename);
        if (size(f) == 0L) {
            return false;
        }
        if (TEXT_FILES.contains(filename)) {
            return true;
        }
        if (NO_TEXT_FILES.contains(filename)) {
            return false;
        }
        if (ext.isEmpty()) {
            return false;
        }
        if (TEXT_EXTENSIONS.contains(ext.get())) {
            return true;
        }
        if (NO_TEXT_EXTENSIONS.contains(ext.get())) {
            return false;
        }
        System.err.println("WARNING: unknown file type (not correcting cr/lf): " + f);
        return false;
    }

    private long size(Path f) {
        try {
            return Files.size(f);
        } catch (IOException e) {
            throw new Error("file size failed", e);
        }
    }
}
