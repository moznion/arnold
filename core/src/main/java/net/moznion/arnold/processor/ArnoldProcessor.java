package net.moznion.arnold.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import lombok.AllArgsConstructor;
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
                final List<FieldUnit> fieldUnits = new ArrayList<>();

                // TODO check modifier? STATIC
                final String packageName =
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
                    final String rawFieldName = targetField.getSimpleName().toString();
                    final String internalFieldName = "__" + rawFieldName + cursor;
                    final TypeName fieldType = TypeName.get(targetField.asType());
                    fieldUnits.add(new FieldUnit(rawFieldName, internalFieldName, fieldType));

                    final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
                    if (cursor == 0) {
                        constructorBuilder.addModifiers(javax.lang.model.element.Modifier.PUBLIC);
                    }

                    final TypeSpec.Builder classBuilder =
                        TypeSpec.classBuilder(generatedClassName)
                                .addModifiers(
                                    javax.lang.model.element.Modifier.PUBLIC,
                                    javax.lang.model.element.Modifier.FINAL
                                );

                    for (int i = 0; i < cursor; i++) {
                        final FieldUnit fieldUnit = fieldUnits.get(i);
                        final String varName = fieldUnit.internalFieldName;
                        final TypeName type = fieldUnit.typeName;

                        constructorBuilder.addParameter(type, varName)
                                          .addStatement("this.$N = $N", varName, varName);

                        classBuilder.addField(
                            type,
                            varName,
                            javax.lang.model.element.Modifier.PRIVATE,
                            javax.lang.model.element.Modifier.FINAL
                        );
                    }

                    final MethodSpec setter =
                        buildSetterMethod(fieldUnits, rawFieldName, fieldType, packageName,
                                          builderClassNameBase + (cursor + 1), cursor
                        );

                    final TypeSpec generatedClass =
                        classBuilder.addMethod(constructorBuilder.build())
                                    .addMethod(setter)
                                    .build();

                    try {
                        outputJavaFile(
                            packageName, generatedClass, generatedClassName
                        );
                    } catch (IOException e) {
                        log.error("Failed annotation processing", e);
                        return false;
                    }

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
                    final FieldUnit fieldUnit = fieldUnits.get(i);
                    final String varName = fieldUnit.internalFieldName;
                    final TypeName type = fieldUnit.typeName;

                    constructorBuilder
                        .addParameter(type, varName)
                        .addStatement("this.$N = $N", varName, varName);

                    classBuilder.addField(
                        type,
                        varName,
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
                for (final FieldUnit fieldUnit : fieldUnits) {
                    builderBuilder.addStatement(
                        "final $T __field$L = __clazz.getDeclaredField($S)",
                        Field.class,
                        cnt,
                        fieldUnit.rawFieldName
                    ).addStatement(
                        "__field$L.setAccessible(true)", cnt
                    ).addStatement(
                        "__field$L.set(__obj, this.$N)", cnt,
                        fieldUnit.internalFieldName
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
                    outputJavaFile(packageName, terminationBuilder, terminationBuilderClassName);
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
            log.debug((javaFile.toString()));
            writer.close();
        }
    }

    private static List<Element> collectFieldsForBuilding(final List<? extends Element> elements) {

        final Supplier<Stream<? extends Element>> streamSupplier = () ->
            elements.stream()
                    .filter(e -> e.getKind().isField())
                    .filter(e -> {
                        final Set<Modifier> mods = e.getModifiers();
                        final boolean isFinal = mods.contains(Modifier.FINAL);
                        final Boolean isStatic = mods.contains(Modifier.STATIC);
                        final boolean isRequired = e.getAnnotation(Required.class) != null;
                        return (isFinal || isRequired) && !isStatic;
                    });

        final Stream<? extends Element> orderedFields =
            streamSupplier.get()
                          .filter(
                              e -> e.getAnnotation(Required.class) != null
                                  && e.getAnnotation(Required.class).order() >= 0
                          )
                          .sorted(
                              Comparator.comparing(e -> e.getAnnotation(Required.class).order())
                          );
        final Stream<? extends Element> nonOrderedFields =
            streamSupplier.get().filter(
                e -> e.getAnnotation(Required.class) == null
                    || e.getAnnotation(Required.class).order() < 0
            );

        return Stream.concat(orderedFields, nonOrderedFields)
                     .collect(Collectors.toList());
    }

    private static String appendSuffix(final String base, final int suffix) {
        return base + (suffix == 0 ? "" : suffix);
    }

    private static MethodSpec buildSetterMethod(final List<FieldUnit> fieldUnits,
                                                final String fieldName,
                                                final TypeName fieldType,
                                                final String packageName,
                                                final String nextBuilderClassName,
                                                final int numOfBuilder
    ) {
        String serializedArgsToInstantiate = IntStream.range(0, numOfBuilder)
                                                      .mapToObj(
                                                          i -> fieldUnits.get(i).internalFieldName
                                                      )
                                                      .collect(Collectors.joining(","));
        if (!serializedArgsToInstantiate.isEmpty()) {
            serializedArgsToInstantiate = serializedArgsToInstantiate + ",";
        }

        return MethodSpec.methodBuilder(fieldName)
                         .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                         .returns(ClassName.get(packageName, nextBuilderClassName))
                         .addParameter(fieldType, fieldName)
                         .addStatement(
                             "return new $N(" + serializedArgsToInstantiate + fieldName + ")",
                             nextBuilderClassName
                         )
                         .build();
    }

    @AllArgsConstructor
    private static class FieldUnit {
        private final String rawFieldName;
        private final String internalFieldName;
        private final TypeName typeName;
    }
}
