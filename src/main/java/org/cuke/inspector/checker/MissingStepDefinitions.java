package org.cuke.inspector.checker;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import org.cuke.inspector.CucumberSupplier;
import org.cuke.inspector.CukeInspectorStepDefinition;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;

import java.util.Collection;
import java.util.List;

public class MissingStepDefinitions {
    public Collection<CukeViolation> inspect(CucumberSupplier cucumberSupplier) {
        final List<CukeInspectorStepDefinition> cukeStepDefinitions = cucumberSupplier.getGlue().getCukeStepDefinitions();

        return cucumberSupplier.getFeatures().stream()
                .flatMap(feature -> feature.getPickles().stream()
                        .flatMap(pickle -> pickle.getSteps().stream()
                                .filter(stepExpression -> cukeStepDefinitions.stream()
                                        .noneMatch(stepDefinition -> stepDefinition.getExpression().getSource().equals(stepExpression.getText())))
                                .map(step -> MissingStepDefinitionViolation.buildViolation(feature, step))))
                .toList();
    }

    record MissingStepDefinitionViolation(String message,
                                          FeatureLocation featureLocation) implements CukeViolation {
        private static final String MESSAGE_TEMPLATE = "The Gherkin step '%s%s' has no Java implementation.";

        public static CukeViolation buildViolation(Feature feature, Step step) {
            return new MissingStepDefinitionViolation(
                    MESSAGE_TEMPLATE.formatted(step.getKeyword(), step.getText()),
                    new FeatureLocation(feature.getUri().toString(),
                            step.getText(),
                            Long.valueOf(step.getLocation().getLine()),
                            Long.valueOf(step.getLocation().getColumn())));
        }
    }
}
