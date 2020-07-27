package processor;

import annotation.FluentBuilder;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created on: 7/21/20
 *
 * @author Denis Citaku
 **/
@SupportedAnnotationTypes({"annotation.FluentBuilder", "annotation.Required"})
@AutoService(Processor.class)
public class FluentBuilderProcessor extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(FluentBuilder.class);

    elementsAnnotatedWith.forEach(element -> {
      boolean hasNoArgsConstructor = element.getEnclosedElements()
          .stream()
          .filter(x -> x instanceof ExecutableElement)
          .map(x -> (ExecutableElement) x)
          .anyMatch(x -> x.getParameters().size() == 0);

      if (!hasNoArgsConstructor) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Class: " + element.getSimpleName().toString() +
                " is annotated with @FluentBuilder but does not have no-args constructor");
      }
    });

    Map<Name, Set<String>> excludedFieldsByName = elementsAnnotatedWith.stream()
        .collect(Collectors.toMap(Element::getSimpleName,
            x -> Set.of(x.getAnnotation(FluentBuilder.class).excludeFields())));

    Map<Name, List<VariableElement>> fieldsByClassName = elementsAnnotatedWith.stream()
        .collect(Collectors.toMap(Element::getSimpleName, x -> ElementFilter.fieldsIn(x.getEnclosedElements())
            .stream()
            .filter(y -> y.getKind().isField())
            .filter(y -> y.getAnnotation(FluentBuilder.Exclude.class) == null)
            .filter(y -> !excludedFieldsByName.get(y.getEnclosingElement().getSimpleName()).contains(y.getSimpleName().toString()))
            .collect(Collectors.toList())));

    fieldsByClassName.forEach((className, fields) -> {
      if (fields.isEmpty()) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Class: " + className.toString() +
                " is annotated with @FluentBuilder but does not have any fields annotated with @Required");
      }
      try {
        writeBuilderFile(className.toString(), fields);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    return true;
  }

  private void writeBuilderFile(String className, List<VariableElement> fields) throws IOException {
    String packageName = null;
    int lastDot = className.lastIndexOf('.');
    if (lastDot > 0) {
      packageName = className.substring(0, lastDot);
    }

    String simpleClassName = className.substring(lastDot + 1);
    String builderClassName = simpleClassName + "FluentBuilder";
    String builderSimpleClassName = builderClassName.substring(lastDot + 1);

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

      if (packageName != null) {
        out.print("package ");
        out.print(packageName);
        out.println(";");
        out.println();
      }

      out.print("public class ");
      out.print(builderSimpleClassName);
      out.println(" {");
      out.println();

      // Builder method
      out.println("\tpublic static Builder builder() {");
      out.print("\t\treturn ");
      for (int i = 0; i < fields.size(); i++) {
        if (i + 1 == fields.size()) {
          AtomicInteger index = new AtomicInteger(0);
          String assignValues = fields.stream()
              .map(x -> String.format("\t\t\tobj.set%s(x%s);", capitalizeFirstLetter(x.getSimpleName().toString()), index.getAndIncrement()))
              .collect(Collectors.joining("\n"));

          out.println(String.format("x%s -> { \n\t\t\t%s obj = new %s();", i, simpleClassName, simpleClassName));
          out.println(assignValues);
          out.println("\t\t\treturn obj;");
          out.println("\t\t};");
        } else {
          out.print("x" + i + " -> ");
        }
      }
      out.println("\t}");

      out.println();

      // Defining Builder interface
      out.println("\tpublic interface Builder {");
      for (int i = 0; i < fields.size(); i++) {
        VariableElement field = fields.get(i);
        String fieldName = field.getSimpleName().toString();
        String fieldType = field.asType().toString();
        String nextStageOrClass = i + 1 == fields.size() ? simpleClassName : String.format("%sStage%s", simpleClassName, i + 1);
        if (i == 0) {
          out.println(String.format("\t\t%s %s(final %s %s);", nextStageOrClass, fieldName, fieldType, field.getSimpleName()));
        } else {
          out.println(String.format("\t\tinterface %sStage%s {", simpleClassName, i));
          out.println(String.format("\t\t\t%s %s(final %s %s);", nextStageOrClass, fieldName, fieldType, fieldName));
          out.println("\t\t}");
        }
        out.println();
      }
      out.println("\t}");

      // End of the class
      out.println("}");
    }
  }

  private static String capitalizeFirstLetter(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
