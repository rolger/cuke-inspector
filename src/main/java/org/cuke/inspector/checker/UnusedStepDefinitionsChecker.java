package org.cuke.inspector.checker;

import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Step;
import org.cuke.inspector.CukeCachingGlue;
import org.cuke.inspector.CukeInspectorStepDefinition;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnusedStepDefinitionsChecker {
    public Collection<CukeViolation> inspect(List<GherkinDocument> gherkinDocuments, CukeCachingGlue glue) {
        Set<String> stepsUsedInFeatureFiles = gherkinDocuments.stream()
                .flatMap(gherkinDocument -> {
                    Feature feature = gherkinDocument.getFeature().orElseThrow(() -> new RuntimeException("No feature in " + gherkinDocument.getUri()));
                    return CucumberStreamingHelper.stepStream(feature);
                })
                .map(Step::getText)
                .collect(Collectors.toSet());


        return glue.getCukeStepDefinitions().stream()
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
            String indentation = "   ";
            return "%n%s%n%s%s: @%s(\"%s\")%n".formatted(message(), indentation, step.getLocation(), step.getCucumberAnnotation(), step.getPattern());
        }
    }
}
