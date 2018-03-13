package net.moznion.arnold.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import lombok.extern.slf4j.Slf4j;
import net.moznion.arnold.annotation.Required;
import net.moznion.arnold.exception.BuildingFailedException;

@Slf4j
@SupportedAnnotationTypes({
    "net.moznion.arnold.annotation.ArnoldBuilder",
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ArnoldProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return true;
        }

        for (final TypeElement annotation : annotations) {
            for (final Element annotated : roundEnv.getElementsAnnotatedWith(annotation)) {
                final Map<String, Object> fieldName2Builder = new HashMap<>();

                // TODO check modifier? STATIC
                final String packageNameOfAnnotatedClass =
                    processingEnv.getElementUtils().getPackageOf(annotated).toString();
                final String builderClassNameBase = annotated.getSimpleName() + "Builder";

                final List<Element> fieldsForBuilding =
                    collectFieldsForBuilding(annotated.getEnclosedElements());
                if (fieldsForBuilding.isEmpty()) {
                    continue;
                }
                final int fieldNum = fieldsForBuilding.size();

                int cursor = 0;
                for (final Element targetField : fieldsForBuilding) {
                    // build builder for each field

                    final String generatedClassName = appendSuffix(builderClassNameBase, cursor);
                    final String fieldName = targetField.getSimpleName().toString();
                    final TypeName fieldType = TypeName.get(targetField.asType());

                    final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
                    if (cursor == 0) {
                        constructorBuilder.addModifiers(javax.lang.model.element.Modifier.PUBLIC);
                    }

                    final TypeSpec.Builder classBuilder =
                        TypeSpec.classBuilder(generatedClassName)
                                .addModifiers(
                                    javax.lang.model.element.Modifier.PUBLIC,
                                    javax.lang.model.element.Modifier.FINAL
                                )
                                .addField(
                                    fieldType, fieldName,
                                    javax.lang.model.element.Modifier.PRIVATE
                                );

                    for (int i = 0; i < cursor; i++) {
                        final String varName = appendSuffix("__b", i);
                        final String builderClassName = appendSuffix(builderClassNameBase, i);

                        constructorBuilder.addParameter(
                            ClassName.get(packageNameOfAnnotatedClass, builderClassName),
                            varName
                        ).addStatement("this.$N = $N", varName, varName);

                        classBuilder.addField(
                            ClassName.get(packageNameOfAnnotatedClass, builderClassName),
                            varName,
                            javax.lang.model.element.Modifier.PRIVATE,
                            javax.lang.model.element.Modifier.FINAL
                        );
                    }

                    final MethodSpec setter =
                        buildSetterMethod(fieldName, fieldType, packageNameOfAnnotatedClass,
                                          builderClassNameBase + (cursor + 1), cursor
                        );

                    final TypeSpec generatedClass =
                        classBuilder.addMethod(constructorBuilder.build())
                                    .addMethod(setter)
                                    .build();

                    try {
                        outputJavaFile(
                            packageNameOfAnnotatedClass, generatedClass, generatedClassName
                        );
                    } catch (IOException e) {
                        log.error("Failed annotation processing", e);
                        return false;
                    }

                    fieldName2Builder.put(fieldName, appendSuffix("__b", cursor));

                    cursor++;
                }

                // build termination builder

                final String terminationBuilderClassName = builderClassNameBase + fieldNum;

                final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
                final TypeSpec.Builder classBuilder =
                    TypeSpec.classBuilder(terminationBuilderClassName)
                            .addModifiers(
                                javax.lang.model.element.Modifier.PUBLIC,
                                javax.lang.model.element.Modifier.FINAL
                            );

                for (int i = 0; i < fieldNum; i++) {
                    final String varName = appendSuffix("__b", i);
                    final String builderClassName = appendSuffix(builderClassNameBase, i);

                    constructorBuilder
                        .addParameter(
                            ClassName.get(packageNameOfAnnotatedClass, builderClassName), varName
                        )
                        .addStatement("this.$N = $N", varName, varName);

                    classBuilder.addField(
                        ClassName.get(packageNameOfAnnotatedClass, builderClassName),
                        appendSuffix("__b", i),
                        javax.lang.model.element.Modifier.PRIVATE,
                        javax.lang.model.element.Modifier.FINAL
                    );
                }
                final MethodSpec constructor = constructorBuilder.build();

                final MethodSpec.Builder builderBuilder =
                    MethodSpec.methodBuilder("build")
                              .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                              .returns(TypeName.get(annotated.asType()))
                              .beginControlFlow("try")
                              .addStatement(
                                  "final $T __obj = $T.class.newInstance()", annotated.asType(),
                                  annotated.asType()
                              )
                              .addStatement(
                                  "final Class<?> __clazz = __obj.getClass()"
                              );

                int cnt = 0;
                for (final Map.Entry<String, Object> n2b : fieldName2Builder.entrySet()) {
                    builderBuilder.addStatement(
                        "final $T __field$L = __clazz.getDeclaredField($S)",
                        Field.class, cnt, n2b.getKey()
                    );
                    builderBuilder.addStatement("__field$L.setAccessible(true)", cnt);
                    builderBuilder.addStatement(
                        "final $T __value$L = $N.getClass().getDeclaredField($S)", Field.class, cnt,
                        n2b.getValue(), n2b.getKey()
                    );
                    builderBuilder.addStatement("__value$L.setAccessible(true)", cnt);
                    builderBuilder.addStatement(
                        "__field$L.set(__obj, __value$L.get($N))", cnt, cnt, n2b.getValue()
                    );
                    cnt++;
                }

                final MethodSpec builder = builderBuilder.addStatement("return __obj")
                                                         .nextControlFlow(
                                                             "catch ($T|$T|$T e)",
                                                             IllegalAccessException.class,
                                                             InstantiationException.class,
                                                             NoSuchFieldException.class
                                                         )
                                                         .addStatement(
                                                             "throw new $T(e)",
                                                             BuildingFailedException.class
                                                         )
                                                         .endControlFlow()
                                                         .build();
                final TypeSpec terminationBuilder = classBuilder.addMethod(constructor)
                                                                .addMethod(builder)
                                                                .build();

                try {
                    outputJavaFile(
                        packageNameOfAnnotatedClass, terminationBuilder,
                        terminationBuilderClassName
                    );
                } catch (IOException e) {
                    log.error("Failed annotation processing", e);
                    return false;
                }
            }
        }

        return true;
    }

    private void outputJavaFile(final String packageName,
                                final TypeSpec generatedClass,
                                final String generatedClassName
    ) throws IOException {
        final JavaFile javaFile = JavaFile.builder(packageName, generatedClass).build();
        final Filer filer = super.processingEnv.getFiler();
        final JavaFileObject f = filer.createSourceFile(packageName + "." + generatedClassName);

        try (Writer writer = f.openWriter()) {
            javaFile.writeTo(writer);
            writer.close();
        }
    }

    private static List<Element> collectFieldsForBuilding(final List<? extends Element> elements) {
        return elements.stream()
                       .filter(e -> e.getKind().isField())
                       .filter(e -> {
                           final Set<javax.lang.model.element.Modifier> mods = e.getModifiers();
                           final boolean isFinal =
                               mods.contains(javax.lang.model.element.Modifier.FINAL);
                           final Boolean isStatic =
                               mods.contains(javax.lang.model.element.Modifier.STATIC);
                           final boolean isRequired = e.getAnnotation(Required.class) != null;

                           return (isFinal || isRequired) && !isStatic;
                       })
                       .collect(Collectors.toList());
    }

    private static String appendSuffix(final String base, final int suffix) {
        return base + (suffix == 0 ? "" : suffix);
    }

    private static MethodSpec buildSetterMethod(final String fieldName,
                                                final TypeName fieldType,
                                                final String packageName,
                                                final String nextBuilderClassName,
                                                final int numOfBuilder
    ) {
        String serializedArgsToInstantiate = IntStream.range(0, numOfBuilder)
                                                      .mapToObj(i -> appendSuffix("__b", i))
                                                      .collect(Collectors.joining(","));
        if (!serializedArgsToInstantiate.isEmpty()) {
            serializedArgsToInstantiate = serializedArgsToInstantiate + ",";
        }

        return MethodSpec.methodBuilder(fieldName)
                         .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                         .returns(ClassName.get(packageName, nextBuilderClassName))
                         .addParameter(fieldType, fieldName)
                         .addStatement("this.$N = $N", fieldName, fieldName)
                         .addStatement(
                             "return new $N(" + serializedArgsToInstantiate + "this)",
                             nextBuilderClassName
                         )
                         .build();
    }
}
