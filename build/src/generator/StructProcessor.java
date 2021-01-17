package generator;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("generator.Structs")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class StructProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.stream()
                .map(roundEnv::getElementsAnnotatedWith)
                .forEach(annotatedElements -> annotatedElements.forEach(element -> {
                    String packageName    = element.asType().toString();
                    int    maxNumTypeArgs = element.getAnnotation(Structs.class).value();
                    System.err.println("@@@@@ init - " + maxNumTypeArgs + " - " + packageName);
                    try {
                        new StructGeneratorForAnnotation(maxNumTypeArgs, packageName).generateAll();
                    } catch (IOException ioException) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "problem during struct generation: " + ioException.getMessage(), element);
                    }
                }));
        return true;
    }

    private class StructGeneratorForAnnotation extends StructGeneratorBase {
        public StructGeneratorForAnnotation(int maxNumTypeArgs, String interfaceJavaPackage) {
            super(maxNumTypeArgs, interfaceJavaPackage);
        }

        @Override
        public Writer getWriter(String className) throws IOException {
            System.err.println("@@@@@   gen " + className);
            return processingEnv.getFiler().createSourceFile(className).openWriter();
        }
    }
}
