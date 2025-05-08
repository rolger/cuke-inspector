package org.cuke.inspector.checker;

import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Step;
import org.cuke.inspector.CucumberSupplier;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.cuke.inspector.checker.CucumberStreamingHelper.stepStream;

public class InvalidStepKeywordChecker {
    private final List<String> forbiddenStepKeywords;

    public InvalidStepKeywordChecker(List<String> forbiddenStepKeywords) {
        this.forbiddenStepKeywords = Collections.unmodifiableList(forbiddenStepKeywords);
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
        List<String> normalizedForbiddenStepKeywords = normalizeToCucumberStepKeywords(forbiddenStepKeywords, feature.getLanguage());

        return stepStream(feature)
                .filter(step -> normalizedForbiddenStepKeywords.contains(step.getKeyword()))
                .map(step -> InvalidStepKeywordViolation.buildViolation(gherkinDocument, step))
                .toList();
    }

    private static List<String> normalizeToCucumberStepKeywords(List<String> inputKeywords, String language) {
        GherkinDialect dialect = new GherkinDialectProvider(language).getDefaultDialect();
        Objects.requireNonNull(dialect, "GherkinDialectProvider provider is null");

        return dialect.getStepKeywords().stream()
                .filter(stepKeyword -> inputKeywords.stream()
                        .anyMatch(invalidKeyword -> stepKeyword.trim().equals(invalidKeyword.trim())))
                .toList();
    }

    record InvalidStepKeywordViolation(String message, FeatureLocation featureLocation) implements CukeViolation {

        private static final String STEP_MESSAGE = "Step '%s' starts with invalid step keyword: '%s'.";

        private static CukeViolation buildViolation(GherkinDocument gherkinDocument, Step step) {
            return new InvalidStepKeywordViolation(
                    STEP_MESSAGE.formatted(step.getText(), step.getKeyword()),
                    new FeatureLocation(gherkinDocument.getUri(),
                            step.getKeyword() + step.getText(),
                            step.getLocation().getLine(),
                            step.getLocation().getColumn().orElse(0L)));
        }

    }
}
