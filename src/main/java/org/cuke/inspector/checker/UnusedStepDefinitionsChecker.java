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
    public Collection<? extends CukeViolation> inspect(List<GherkinDocument> gherkinDocuments, CukeCachingGlue glue) {
        Set<String> stepsUsedInFeatureFiles = gherkinDocuments.stream()
                .flatMap(gherkinDocument -> {
                    Feature feature = gherkinDocument.getFeature().orElseThrow(() -> new RuntimeException("No feature in " + gherkinDocument.getUri()));
                    return CucumberStreamingHelper.stepStream(feature);
                })
                .map(step -> step.getText())
                .distinct()
                .collect(Collectors.toSet());

        Set<String> stepDefinitionsInStepFiles = glue.getStepDefinitionsByPattern().keySet();

        Collection<List<CukeInspectorStepDefinition>> values = glue.getStepDefinitionsByPattern().values();

//        values.stream().flatMap(Collection::stream)
//                .map(s -> s.getExpression().)



        Set<String> unusedSteps = new HashSet<>(stepDefinitionsInStepFiles);
        unusedSteps.removeAll(stepsUsedInFeatureFiles);

        return unusedSteps.stream()
                .map(s -> new CukeViolation() {
                    @Override
                    public String message() {
                        return s;
                    }

                    @Override
                    public FeatureLocation featureLocation() {
                        return new FeatureLocation("q", "t", 0L, Optional.empty());
                    }
                })
                .toList();
    }
}
