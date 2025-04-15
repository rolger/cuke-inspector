package org.cuke.inspector.checker;

import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;
import org.cuke.inspector.ViolationChecker;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class MissingRequiredTagChecker implements ViolationChecker {

    private final Pattern requiredTagPattern;

    public MissingRequiredTagChecker(String requiredRegex) {
        requiredTagPattern = Pattern.compile(requiredRegex);
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
        return CucumberStreamingHelper.scenarioStream(feature)
                .filter(scenario -> scenario.getTags().stream()
                        .noneMatch(tag -> requiredTagPattern.matcher(tag.getName()).find()))
                .map(scenario -> MissingRequiredTagViolation.buildViolation(gherkinDocument, scenario))
                .toList();
    }

    record MissingRequiredTagViolation(String message, FeatureLocation featureLocation) implements CukeViolation {
        private static final String SCENARIO_MESSAGE = "Scenario '%s' does not contain a reference tag to an User Story.";

        private static CukeViolation buildViolation(GherkinDocument gherkinDocument, Scenario scenario) {
            return new InvalidStepKeywordChecker.InvalidStepKeywordViolation(
                    SCENARIO_MESSAGE.formatted(scenario.getName()),
                    new FeatureLocation(gherkinDocument.getUri().orElse("unknown uri"),
                            scenario.getName(),
                            scenario.getLocation().getLine(),
                            scenario.getLocation().getColumn()));
        }

        @Override
        public String toString() {
            return "MissingUserStoryTagViolation{" +
                    "featureFile='" + featureLocation.fileName() + '\'' +
                    "message='" + message + '\'' +
                    "tokenName='" + featureLocation.tokenName() + '\'' +
                    ", line=" + featureLocation.line() +
                    ", column=" + featureLocation.column() +
                    '}';
        }
    }
}
