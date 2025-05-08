package org.cuke.inspector;

import io.cucumber.core.backend.*;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.java.StepDefinitionAnnotation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class CukeInspectorStepDefinition implements StepDefinition {

    private final StepDefinition stepDefinition;
    private final StepExpression expression;

    public CukeInspectorStepDefinition(StepDefinition stepDefinition, StepExpression expression) {
        this.stepDefinition = stepDefinition;
        this.expression = expression;
    }

    public StepExpression getExpression() {
        return this.expression;
    }

    public StepDefinition getStepDefinition() {
        return this.stepDefinition;
    }

    @Override
    public void execute(Object[] objects) throws CucumberBackendException, CucumberInvocationTargetException {
        // not used for inspections
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return List.of();
    }

    @Override
    public String getPattern() {
        return stepDefinition.getPattern();
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public String getLocation() {
        return stepDefinition.getLocation();
    }

    @Override
    public Optional<SourceReference> getSourceReference() {
        return stepDefinition.getSourceReference();
    }

    public String getRegEx() {
        // can't access expression.expression!
        return expression.getSource();
    }

    public boolean isNotUsedInAnyFeature(Set<String> stepsUsedInFeatureFiles) {
        // matches directly - no parameters
        if (stepsUsedInFeatureFiles.contains(getExpression().getSource()))
            return false;

        // does not match including parameters
        return stepsUsedInFeatureFiles.stream()
                .map(step -> getExpression().match(step))
                .filter(Objects::nonNull)
                .findAny()
                .isEmpty();
    }

    public String getCucumberAnnotation() {
        return getCucumberAnnotation((JavaMethodReference) getSourceReference().get());
    }

    @SneakyThrows
    private String getCucumberAnnotation(JavaMethodReference reference) {
        return Arrays.stream(Class.forName(reference.className()).getDeclaredMethods())
                .filter(m -> m.getName().equals(reference.methodName()))
                .map(this::getStepDefinitionAnnotation)
                .findFirst()
                .orElse("");
    }

    private String getStepDefinitionAnnotation(Method method) {
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (isStepDefinitionAnnotation(annotation) && matchesExpression(annotation)) {
                return annotation.annotationType().getSimpleName();
            }
        }
        return "";
    }

    private boolean matchesExpression(Annotation annotation) {
        for (Method annotationMethod : annotation.annotationType().getDeclaredMethods()) {
            try {
                Object value = annotationMethod.invoke(annotation);
                if (expression.getSource().equals(value.toString()))
                    return true;
            } catch (Exception e) {
                log.error("Fehler beim Lesen des Werts: " + e.getMessage());
            }
        }
        return false;
    }

    // copied from MethodScanner
    private static boolean isStepDefinitionAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.getAnnotation(StepDefinitionAnnotation.class) != null;
    }
}


