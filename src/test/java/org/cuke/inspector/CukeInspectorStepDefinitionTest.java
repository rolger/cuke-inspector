package org.cuke.inspector;

import io.cucumber.core.backend.SourceReference;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.stepexpression.StepExpression;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CukeInspectorStepDefinitionTest {

    @Test
    void shouldRetrieveAnnotationWhichMatchesExpression() throws ClassNotFoundException, NoSuchMethodException {
        StepDefinition stepDefinition = Mockito.mock(StepDefinition.class);
        Mockito.when(stepDefinition.getSourceReference()).thenReturn(getSourceReference("multipleAnnotations"));

        StepExpression stepExpression = Mockito.mock(StepExpression.class);
        Mockito.when(stepExpression.getSource()).thenReturn("assert");

        CukeInspectorStepDefinition definition = new CukeInspectorStepDefinition(stepDefinition, stepExpression);

        String cucumberAnnotation = definition.getCucumberAnnotation();

        assertThat(cucumberAnnotation).contains("Then");
    }

    @Test
    void shouldRetrieveAnnotation() throws ClassNotFoundException, NoSuchMethodException {
        StepDefinition stepDefinition = Mockito.mock(StepDefinition.class);
        Mockito.when(stepDefinition.getSourceReference()).thenReturn(getSourceReference("whenExpression"));

        StepExpression stepExpression = Mockito.mock(StepExpression.class);
        Mockito.when(stepExpression.getSource()).thenReturn("test");

        CukeInspectorStepDefinition definition = new CukeInspectorStepDefinition(stepDefinition, stepExpression);

        String cucumberAnnotation = definition.getCucumberAnnotation();

        assertThat(cucumberAnnotation).contains("When");
    }

    private Optional<SourceReference> getSourceReference(String methodName) throws ClassNotFoundException, NoSuchMethodException {
        return Optional.of(SourceReference.fromMethod(Class.forName("org.cuke.inspector.steps.matching.steps.MatchingSteps").getMethod(methodName)));
    }
}
