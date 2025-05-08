package org.cuke.inspector.checker;

import io.cucumber.messages.types.*;

import java.util.Optional;
import java.util.stream.Stream;

public final class CucumberStreamingHelper {

    private CucumberStreamingHelper() {
        // don't create helper with static methods
    }

    public static Stream<Scenario> scenarioStream(Feature feature) {
        Stream<Scenario> ruleScenarioStream = ruleStream(feature)
                .flatMap(rule -> rule.getChildren().stream())
                .map(RuleChild::getScenario)
                .filter(Optional::isPresent)
                .map(Optional::get);

        return Stream.concat(
                ruleScenarioStream,
                feature.getChildren().stream()
                        .map(FeatureChild::getScenario)
                        .filter(Optional::isPresent)
                        .map(Optional::get));
    }

    private static Stream<Rule> ruleStream(Feature feature) {
        return feature.getChildren().stream()
                .map(FeatureChild::getRule)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public static Stream<Background> backgroundStream(Feature feature) {
        Stream<Background> ruleBackgroundStream = ruleStream(feature)
                .flatMap(rule -> rule.getChildren().stream())
                .map(RuleChild::getBackground)
                .filter(Optional::isPresent)
                .map(Optional::get);

        return Stream.concat(ruleBackgroundStream,
                feature.getChildren().stream()
                        .map(FeatureChild::getBackground)
                        .filter(Optional::isPresent)
                        .map(Optional::get));
    }

    public static Stream<Step> stepStream(Feature feature) {
        return Stream.concat(
                scenarioStream(feature)
                        .flatMap(scenario -> scenario.getSteps().stream()),
                backgroundStream(feature)
                        .flatMap(background -> background.getSteps().stream())
        );
    }
}
