package org.cuke.inspector.checker;

import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import org.cuke.inspector.CucumberSupplier;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class MissingRequiredTagChecker {

    private final Pattern requiredTagPattern;

    public MissingRequiredTagChecker(String requiredRegex) {
        requiredTagPattern = Pattern.compile(requiredRegex);
    }

    public Collection<CukeViolation> inspect(CucumberSupplier cucumberSupplier) {
        return cucumberSupplier.getGherkinDocuments().stream()
                .flatMap(gherkinDocument -> {
                    Feature feature = gherkinDocument.getFeature().orElseThrow(() -> new RuntimeException("No feature in " + gherkinDocument.getUri()));
                    return inspect(gherkinDocument, feature).stream();
                })
                .toList();
    }

    private List<CukeViolation> inspect(GherkinDocument gherkinDocument, Feature feature) {
        return CucumberStreamingHelper.scenarioStream(feature)
                .filter(scenario -> scenario.getTags().stream()
                        .noneMatch(tag -> requiredTagPattern.matcher(tag.getName()).find()))
                .map(scenario -> MissingRequiredTagViolation.buildViolation(gherkinDocument, scenario))
                .toList();
    }

    record MissingRequiredTagViolation(String message, FeatureLocation featureLocation) implements CukeViolation {
        private static final String SCENARIO_MESSAGE = "Scenario '%s' does not contain a reference tag to an User Story.";

        private static CukeViolation buildViolation(GherkinDocument gherkinDocument, Scenario scenario) {
            return new MissingRequiredTagViolation(
                    SCENARIO_MESSAGE.formatted(scenario.getName()),
                    new FeatureLocation(gherkinDocument.getUri(),
                            scenario.getName(),
                            scenario.getLocation().getLine(),
                            scenario.getLocation().getColumn().orElse(0L)));
        }

        @Override
        public String toString() {
            return "MissingRequiredTagViolation{" +
                    "featureFile='" + featureLocation.fileName() + '\'' +
                    "message='" + message + '\'' +
                    "tokenName='" + featureLocation.tokenName() + '\'' +
                    ", line=" + featureLocation.line() +
                    ", column=" + featureLocation.column() +
                    '}';
        }
    }
}
