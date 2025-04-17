package org.cuke.inspector.checker;

import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Step;
import org.cuke.inspector.CukeCachingGlue;
import org.cuke.inspector.CukeInspectorStepDefinition;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;

import java.util.*;
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

        return new HashMap<>(glue.getStepDefinitionsByPattern()).entrySet().stream()
                .filter(entry -> !stepsUsedInFeatureFiles.contains(entry.getKey()))
                .filter(entry ->
                        stepsUsedInFeatureFiles.stream()
                                .map(step -> entry.getValue().getFirst().getExpression().match(step))
                                .filter(Objects::nonNull)
                                .findAny().isEmpty()
                )
                .map(entry -> UnusedStepDefinitionsViolation.buildViolation(entry.getValue()))
                .toList();
    }

    static class UnusedStepDefinitionsViolation implements CukeViolation {
        private static final String MESSAGE_TEMPLATE = "The step definition '%s' is not used in any feature file.";

        private final String message;
        private final List<CukeInspectorStepDefinition> steps;

        public static CukeViolation buildViolation(List<CukeInspectorStepDefinition> steps) {
            return new UnusedStepDefinitionsViolation(
                    MESSAGE_TEMPLATE.formatted(steps.getFirst().getPattern(), steps.size()), steps);
        }

        public UnusedStepDefinitionsViolation(String message, List<CukeInspectorStepDefinition> steps) {
            this.message = message;
            this.steps = steps;
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
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append(message()).append("\n");
            steps.forEach(stepDefinition -> {
                String indentation = "   ";
                sb.append(indentation + stepDefinition.getLocation())
                        .append(": @")
                        .append(stepDefinition.getCucumberAnnotation())
                        .append("(\"")
                        .append(stepDefinition.getPattern())
                        .append("\")")
                        .append("\n");
            });
            return sb.toString();
        }
    }
}
