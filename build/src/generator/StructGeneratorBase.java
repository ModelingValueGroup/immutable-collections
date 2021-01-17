package generator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class StructGeneratorBase {
    protected static final String IMPL = "impl";

    protected final int    maxNumTypeArgs;
    protected final String interfaceJavaPackage;
    protected final String implementJavaPackage;

    public StructGeneratorBase(int maxNumTypeArgs, String interfaceJavaPackage) {
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
        return StructGeneratorBase.structName(i, false) + StructGeneratorBase.argTypes(i);
    }

    private static String structNameWithTypeArgsImpl(int i) {
        return StructGeneratorBase.structName(i, true) + StructGeneratorBase.argTypes(i);
    }

    private static String argTypes(int i) {
        return i <= 0 ? "" : "<" + StructGeneratorBase.seq(i, "T%d") + ">";
    }

    private static String argTypesWithParams(int i) {
        return i <= 0 ? "" : StructGeneratorBase.seq(i, "T%d t%d");
    }

    private static String argParams(int i) {
        return i <= 0 ? "" : StructGeneratorBase.seq(i, "t%d");
    }

    private static String seq(int n, String fmt) {
        return IntStream.range(0, n).mapToObj(i -> String.format(fmt, i, i, i, i, i, i, i, i)).collect(Collectors.joining(", "));
    }

    protected List<String> generateStructInterface(int i) {
        List<String> f    = new ArrayList<>();
        int          prev = i - 1;
        f.add("package " + interfaceJavaPackage + ";");
        f.add("");
        f.add("public interface " + StructGeneratorBase.structNameWithTypeArgs(i) + " extends " + StructGeneratorBase.structNameWithTypeArgs(prev) + " {");
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
        f.add("import " + interfaceJavaPackage + "." + StructGeneratorBase.structName(i, false) + ";");
        f.add("");
        if (0 != i) {
            f.add("@SuppressWarnings({\"unchecked\", \"unused\"})");
        }
        f.add("public class " + StructGeneratorBase.structNameWithTypeArgsImpl(i) + " extends " + StructGeneratorBase.structNameWithTypeArgsImpl(prev) + " implements " + StructGeneratorBase.structNameWithTypeArgs(i) + " {");
        f.add("");
        f.add("    private static final long serialVersionUID = " + String.format("0x%08X_%08XL", 0x47114711, StructGeneratorBase.structName(i, true).hashCode()) + ";");
        f.add("");
        f.add("    public Struct" + i + "Impl(" + StructGeneratorBase.argTypesWithParams(i) + ") {");
        if (0 != i) {
            f.add("        this((Object) " + StructGeneratorBase.argParams(i) + ");");
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
