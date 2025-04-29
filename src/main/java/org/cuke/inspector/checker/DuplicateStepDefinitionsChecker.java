package org.cuke.inspector.checker;

import org.cuke.inspector.CukeCachingGlue;
import org.cuke.inspector.CukeInspectorStepDefinition;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;

import java.util.List;

public class DuplicateStepDefinitionsChecker {

    public List<CukeViolation> inspect(CukeCachingGlue glue) {
        return glue.getStepDefinitionsByPattern().values().stream()
                .filter(entry -> entry.size() > 1)
                .map(DuplicateStepDefinitionsViolation::buildViolation)
                .toList();
    }

    static class DuplicateStepDefinitionsViolation implements CukeViolation {
        private static final String SCENARIO_MESSAGE = "The step definition expression '%s' was found %d times.";

        private final String message;
        private final List<CukeInspectorStepDefinition> steps;


        public static CukeViolation buildViolation(List<CukeInspectorStepDefinition> steps) {
            return new DuplicateStepDefinitionsViolation(
                    SCENARIO_MESSAGE.formatted(steps.getFirst().getPattern(), steps.size()), steps);
        }

        public DuplicateStepDefinitionsViolation(String message, List<CukeInspectorStepDefinition> steps) {
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
                sb.append(indentation)
                        .append(stepDefinition.getLocation())
                        .append(": @")
                        .append(stepDefinition.getCucumberAnnotation())
                        .append("(\"")
                        .append(stepDefinition.getPattern())
                        .append("\")")
                        .append("\n");
            });
            return sb.toString();
        }

        @Override
        public String toString() {
            return "DuplicateStepDefinitionsViolation{" +
                    "message='" + message + '\'' +
                    ", steps=" + steps +
                    '}';
        }
    }
}
