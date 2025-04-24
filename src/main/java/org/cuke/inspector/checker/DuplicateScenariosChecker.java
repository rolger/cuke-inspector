package org.cuke.inspector.checker;

import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cuke.inspector.checker.CucumberStreamingHelper.scenarioStream;

public class DuplicateScenariosChecker {

    public List<CukeViolation> inspect(List<GherkinDocument> gherkinDocuments) {
        Map<String, List<ScenarioWithGherkinDocument>> groupedByScenarioName = gherkinDocuments.stream()
                .flatMap(DuplicateScenariosChecker::combineScenariosWithDoc)
                .collect(Collectors.groupingBy(
                        combination -> combination.scenario().getName(),
                        Collectors.toList()
                ));

        return groupedByScenarioName.values().stream()
                .filter(entry -> entry.size() > 1)
                .map(DuplicatedScenariosViolation::buildViolation)
                .toList();
    }

    private static Stream<ScenarioWithGherkinDocument> combineScenariosWithDoc(GherkinDocument doc) {
        return doc.getFeature()
                .map(feature -> scenarioStream(feature)
                        .map(scenario -> new ScenarioWithGherkinDocument(scenario, doc)))
                .orElse(Stream.empty());
    }

    public record ScenarioWithGherkinDocument(Scenario scenario, GherkinDocument doc) {
    }

    static class DuplicatedScenariosViolation implements CukeViolation {
        private static final String SCENARIO_MESSAGE = "Duplicated scenarios '%s' in %d feature files.";
        private final String message;
        private final List<FeatureLocation> locations;

        public static CukeViolation buildViolation(final List<ScenarioWithGherkinDocument> scenarios) {
            return new DuplicatedScenariosViolation(
                    SCENARIO_MESSAGE.formatted(scenarios.getFirst().scenario().getName(), scenarios.size()),
                    scenarios.stream()
                            .map(combination ->
                                    new FeatureLocation(combination.doc().getUri().orElse("unknown uri"),
                                            combination.scenario().getName(),
                                            combination.scenario().getLocation().getLine(),
                                            combination.scenario().getLocation().getColumn()))
                            .toList());
        }

        public DuplicatedScenariosViolation(String message, List<FeatureLocation> locations) {
            this.message = message;
            this.locations = locations;
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
            return locations;
        }

        @Override
        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            sb.append(message()).append("\n");
            featureLocations().forEach(location -> {
                String indentation = "   ";
                sb.append(indentation)
                        .append(location.fileName())
                        .append(":[")
                        .append(location.line())
                        .append(",")
                        .append(location.column().orElse(0L))
                        .append("] ")
                        .append("\n");
            });
            return sb.toString();
        }
    }
}
