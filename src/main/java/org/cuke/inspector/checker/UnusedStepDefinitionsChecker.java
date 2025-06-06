package org.cuke.inspector.checker;

import io.cucumber.core.gherkin.Step;
import org.cuke.inspector.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnusedStepDefinitionsChecker {
    public Collection<CukeViolation> inspect(CucumberSupplier cucumberSupplier) {
        Set<String> stepsUsedInFeatureFiles = cucumberSupplier.getFeatures().stream()
                .flatMap(feature -> feature.getPickles().stream())
                .flatMap(pickle -> pickle.getSteps().stream())
                .map(Step::getText)
                .collect(Collectors.toSet());

        return cucumberSupplier.getGlue().getCukeStepDefinitions().stream()
                .filter(stepDefinition -> stepDefinition.isNotUsedInAnyFeature(stepsUsedInFeatureFiles))
                .map(UnusedStepDefinitionsViolation::buildViolation)
                .toList();
    }

    static class UnusedStepDefinitionsViolation implements CukeViolation {
        private static final String MESSAGE_TEMPLATE = "The step definition '%s' is not used in any feature file.";

        private final String message;
        private final CukeInspectorStepDefinition step;

        public static CukeViolation buildViolation(CukeInspectorStepDefinition step) {
            return new UnusedStepDefinitionsViolation(
                    MESSAGE_TEMPLATE.formatted(step.getPattern()), step);
        }

        public UnusedStepDefinitionsViolation(String message, CukeInspectorStepDefinition step) {
            this.message = message;
            this.step = step;
        }

        @Override
        public String message() {
            return message;
        }

        @Override
        public FeatureLocation featureLocation() {
            return null;
        }

        @Override
        public List<FeatureLocation> featureLocations() {
            return List.of();
        }

        @Override
        public String format() {
            return "%n%s%n   %s: @%s(\"%s\")%n".formatted(message(), step.getLocation(), step.getCucumberAnnotation(), step.getPattern());
        }
    }
}
