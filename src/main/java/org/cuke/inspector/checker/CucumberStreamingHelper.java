package org.cuke.inspector.checker;

import io.cucumber.messages.types.*;

import java.util.Optional;
import java.util.stream.Stream;

public class CucumberStreamingHelper {

    private CucumberStreamingHelper() {
        // don't create helper with static methods
    }

    public static Stream<Scenario> scenarioStream(Feature feature) {
        return feature.getChildren().stream()
                .map(FeatureChild::getScenario)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public static Stream<Background> backgroundStream(Feature feature) {
        return feature.getChildren().stream()
                .map(FeatureChild::getBackground)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public static Stream<Step> stepStream(Feature feature) {
        return Stream.concat(
                scenarioStream(feature)
                        .flatMap(scenario -> scenario.getSteps().stream()),
                backgroundStream(feature)
                        .flatMap(child -> child.getSteps().stream())
        );
    }
}
