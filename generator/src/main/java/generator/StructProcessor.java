//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018-2022 Modeling Value Group B.V. (http://modelingvalue.org)                                        ~
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
                    try {
                        String packageName    = element.asType().toString();
                        int    maxNumTypeArgs = element.getAnnotation(Structs.class).value();
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
            return processingEnv.getFiler().createSourceFile(className).openWriter();
        }
    }
}
