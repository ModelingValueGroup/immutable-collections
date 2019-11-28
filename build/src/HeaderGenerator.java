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

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class HeaderGenerator extends ScavangerBase {
    private static final Set<String> EXT_WITH_HEADER = new HashSet<>(Arrays.asList(
            "java"
    ));

    private Path         headerFile = Paths.get("build", "header").toAbsolutePath();
    private List<String> header;

    public static void main(String[] args) throws IOException {
        new HeaderGenerator().prepare(Arrays.asList(args)).generate();
    }

    private HeaderGenerator prepare(List<String> args) {
        if (args.size() != 1) {
            System.err.println("arg error: one arg expected: <header-template-file>");
            System.exit(53);
        }
        headerFile = Paths.get(args.get(0));

        if (!Files.isRegularFile(headerFile)) {
            throw new Error("no such file: " + headerFile);
        }
        return this;
    }

    private void generate() throws IOException {
        allFiles()
                .filter(this::needsHeader)
                .forEach(this::replaceHeader);
    }

    private boolean needsHeader(Path f) {
        String           filename = f.getFileName().toString();
        Optional<String> ext      = getExtension(filename);
        try {
            if (Files.size(f) == 0) {
                return false;
            }
            if (f.equals(headerFile)) {
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
            List<String> lines = f.equals(headerFile) ? new ArrayList<>() : Files.readAllLines(f);
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

    private List<String> readHeader() throws IOException {
        return border(cleanup(Files.readAllLines(headerFile)));
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

    private List<String> getHeader() {
        try {
            if (header == null) {
                header = readHeader();
            }
            return header;
        } catch (IOException e) {
            throw new Error("could not read header: " + headerFile);
        }
    }

}
