package org.cuke.inspector.checker;

import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Tag;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;
import org.cuke.inspector.ViolationChecker;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cuke.inspector.checker.CucumberStreamingHelper.scenarioStream;

public class InvalidTagCombinationsChecker implements ViolationChecker {
    private final Set<String> invalidTagCombinations;

    public InvalidTagCombinationsChecker(Set<String> invalidTagCombinations) {
        this.invalidTagCombinations = invalidTagCombinations;
    }

    @Override
    public Collection<? extends CukeViolation> inspect(List<GherkinDocument> gherkinDocuments) {
        return gherkinDocuments.stream()
                .flatMap(gherkinDocument -> {
                    Feature feature = gherkinDocument.getFeature().orElseThrow(() -> new RuntimeException("No feature in " + gherkinDocument.getUri()));
                    return inspect(gherkinDocument, feature).stream();
                })
                .toList();
    }

    private List<CukeViolation> inspect(GherkinDocument gherkinDocument, Feature feature) {
        if (invalidTagCombinations.isEmpty()) {
            return List.of();
        }

        if (containsInvalidTagCombinations(feature.getTags(), invalidTagCombinations)) {
            return List.of(InvalidTagCombinationViolation.build(gherkinDocument, feature, invalidTagCombinations));
        }

        return scenarioStream(feature)
                .filter(scenario -> containsInvalidTagCombinations(mergeTags(feature, scenario), invalidTagCombinations))
                .map(scenario -> InvalidTagCombinationViolation.build(gherkinDocument, scenario, invalidTagCombinations))
                .toList();
    }

    private static boolean containsInvalidTagCombinations(List<Tag> tagList, Set<String> invalidTagCombination) {
        Set<String> tagNames = tagList.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        return invalidTagCombination.stream().allMatch(tagNames::contains);
    }

    private static List<Tag> mergeTags(Feature feature, Scenario scenario) {
        return Stream.concat(scenario.getTags().stream(), feature.getTags().stream())
                .distinct()
                .toList();
    }

    record InvalidTagCombinationViolation(String message, FeatureLocation featureLocation) implements CukeViolation {

        private static final String FEATURE_MESSAGE = "Feature '%s' contains invalid tag combination: %s.";
        private static final String SCENARIO_MESSAGE = "Scenario '%s' contains invalid tag combination: %s";

        private static CukeViolation build(GherkinDocument gherkinDocument, Feature feature, Set<String> invalidTagCombination) {
            return new InvalidTagCombinationViolation(
                    formatMessage(FEATURE_MESSAGE, feature.getName(), invalidTagCombination),
                    new FeatureLocation(
                            gherkinDocument.getUri().orElse("unknown uri"),
                            feature.getName(),
                            feature.getLocation().getLine(),
                            feature.getLocation().getColumn()));
        }

        private static CukeViolation build(GherkinDocument gherkinDocument, Scenario scenario, Set<String> invalidTagCombination) {
            return new InvalidTagCombinationViolation(
                    formatMessage(SCENARIO_MESSAGE, scenario.getName(), invalidTagCombination),
                    new FeatureLocation(
                            gherkinDocument.getUri().orElse("unknown uri"),
                            scenario.getName(),
                            scenario.getLocation().getLine(),
                            scenario.getLocation().getColumn()));
        }

        private static String formatMessage(String message, String name, Set<String> invalidTagCombination) {
            return message.formatted(name, invalidTagCombination.stream().collect(Collectors.joining(", ")));
        }

        @Override
        public String toString() {
            return "InvalidTagCombinationViolation{" +
                    "featureFile='" + featureLocation.fileName() + '\'' +
                    "message='" + message + '\'' +
                    "tokenName='" + featureLocation.tokenName() + '\'' +
                    ", line=" + featureLocation.line() +
                    ", column=" + featureLocation.column() +
                    '}';
        }

        @Override
        public String format() {
            return "";
        }
    }
}
