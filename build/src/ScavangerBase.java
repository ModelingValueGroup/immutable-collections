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

import static java.lang.Integer.*;
import static java.nio.file.StandardOpenOption.*;

@SuppressWarnings({"WeakerAccess"})
public abstract class ScavangerBase {
    static final         Path MODULE_DIR  = Paths.get("immutable-collections").toAbsolutePath();
    static final         Path BASE_DIR    = MODULE_DIR.getParent();
    private static final Path HEADER_FILE = Paths.get("build", "header").toAbsolutePath();

    static final Set<Path> FORBIDDEN_DIRS = new HashSet<>(Arrays.asList(
            BASE_DIR.resolve(".git"),
            BASE_DIR.resolve(".idea"),
            BASE_DIR.resolve("out"),
            BASE_DIR.resolve("lib")
    ));

    protected List<Path>   changedFiles        = new ArrayList<>();
    private   List<String> header;
    private   List<Path>   previouslyGenerated = new ArrayList<>();

    void generate() throws IOException {
        removeLeftOvers();
        System.err.println("#changes=" + changedFiles.size());
        if (!changedFiles.isEmpty()) {
            String markerName = System.getenv("CHANGES_MADE_MARKER");
            if (markerName == null) {
                markerName = "changes-made";
            }
            Path markerFile = BASE_DIR.resolve(markerName);
            changedFiles.forEach(f-> {
                try {
                    String line = f + "\n";
                    Files.write(markerFile, line.getBytes(), APPEND, CREATE);
                } catch (IOException e) {
                    System.err.println("ERROR: could not write markerfile: "+ markerFile);
                    e.printStackTrace();
                    System.exit(34);
                }
            });
        }
    }

    void prepare() throws IOException {
        if (!Files.isDirectory(MODULE_DIR)) {
            throw new Error("no such dir: " + MODULE_DIR);
        }
        if (!Files.isDirectory(MODULE_DIR.resolve("src"))) {
            throw new Error("no such dir: " + MODULE_DIR.resolve("src"));
        }
        if (!Files.isRegularFile(HEADER_FILE)) {
            throw new Error("no such file: " + HEADER_FILE);
        }
    }

    void overwrite(Path file, List<String> lines) throws IOException {
        overwrite(file, lines, false);
    }

    void overwrite(Path file, List<String> lines, boolean forced) throws IOException {
        if (forced || !Files.isRegularFile(file)) {
            System.err.println("+ generated  : " + file);
            Files.write(file, lines);
            changedFiles.add(file);
        } else {
            List<String> old = Files.readAllLines(file);
            if (!lines.equals(old)) {
                System.err.println("+ regenerated: " + file);
                Files.write(file, lines);
                changedFiles.add(file);
            } else {
                System.err.println("+ already ok : " + file);
            }
        }
        previouslyGenerated.remove(file);
    }

    void addToPreviouslyGenerated(Path f) {
        previouslyGenerated.add(f);
    }

    private void removeLeftOvers() throws IOException {
        for (Path file : previouslyGenerated) {
            System.err.println("- deleted      : " + file);
            Files.delete(file);
            changedFiles.add(file);
        }
    }

    private List<String> readHeader() throws IOException {
        return border(cleanup(Files.readAllLines(HEADER_FILE)));
    }

    private List<String> cleanup(List<String> inFile) {
        List<String> h = inFile
                .stream()
                .map(String::stripTrailing)
                .filter(l -> !l.matches("^//~~*$"))
                .map(l -> l.replaceAll("^//", ""))
                .map(l -> l.replaceAll("~$", ""))
                .map(String::stripTrailing)
                .collect(Collectors.toList());
        int indent = calcIndent(h);
        if (0 < indent) {
            h = h.stream().map(l -> l.substring(min(l.length(), indent))).collect(Collectors.toList());
        }
        while (!h.isEmpty() && h.get(0).trim().isEmpty()) {
            h.remove(0);
        }
        while (!h.isEmpty() && h.get(h.size() - 1).trim().isEmpty()) {
            h.remove(h.size() - 1);
        }
        if (h.isEmpty()) {
            h.add("no header available");
        }
        return h;
    }

    private List<String> border(List<String> cleaned) {
        //noinspection OptionalGetWithoutIsPresent
        int          len      = cleaned.stream().mapToInt(String::length).max().getAsInt();
        String       border   = "//~" + String.format("%" + len + "s", "").replace(' ', '~') + "~~";
        List<String> bordered = cleaned.stream().map(l -> String.format("// %-" + len + "s ~", l)).collect(Collectors.toList());
        bordered.add(0, border);
        bordered.add(border);
        bordered.add("");
        return bordered;
    }

    private int calcIndent(List<String> h) {
        int indent = Integer.MAX_VALUE;
        for (String l : h) {
            if (l.trim().length() != 0) {
                indent = min(indent, l.replaceAll("[^ ].*", "").length());
            }
        }
        return indent;
    }

    List<String> getHeader() {
        try {
            if (header == null) {
                header = readHeader();
            }
            return header;
        } catch (IOException e) {
            throw new Error("could not read header: " + HEADER_FILE);
        }
    }

    Optional<String> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1));
    }
}
