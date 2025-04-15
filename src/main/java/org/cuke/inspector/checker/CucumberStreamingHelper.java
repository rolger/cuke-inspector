package org.cuke.inspector.checker;

import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;

import java.util.Optional;
import java.util.stream.Stream;

public class CucumberStreamingHelper {
    public static Stream<Scenario> scenarioStream(Feature feature) {
        return feature.getChildren().stream()
                .map(child -> child.getScenario())
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public static Stream<Background> backgroundStream(Feature feature) {
        return feature.getChildren().stream()
                .map(child -> child.getBackground())
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
