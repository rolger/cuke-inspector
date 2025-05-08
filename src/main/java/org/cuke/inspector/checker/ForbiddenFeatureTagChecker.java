package org.cuke.inspector.checker;

import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import org.cuke.inspector.CucumberSupplier;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

public class ForbiddenFeatureTagChecker {

    private final Pattern forbiddenTagPattern;

    public ForbiddenFeatureTagChecker(String forbiddenRegex) {
        Objects.requireNonNull(forbiddenRegex);

        forbiddenTagPattern = Pattern.compile(forbiddenRegex);
    }

    public Collection<CukeViolation> inspect(CucumberSupplier cucumberSupplier) {
        return cucumberSupplier.getGherkinDocuments().stream()
                .flatMap(gherkinDocument -> {
                    Feature feature = gherkinDocument.getFeature().orElseThrow(() -> new RuntimeException("No feature in " + gherkinDocument.getUri()));

                    return feature.getTags().stream()
                            .filter(tag -> forbiddenTagPattern.matcher(tag.getName()).matches())
                            .map(tag -> ForbiddenFeatureTagChecker.ForbiddenFeatureTagViolation.build(gherkinDocument, feature, tag.getName()));
                })
                .toList();
    }

    record ForbiddenFeatureTagViolation(String message, FeatureLocation featureLocation) implements CukeViolation {
        private static final String FEATURE_MESSAGE = "Feature '%s' contains forbidden tag: %s.";

        private static CukeViolation build(GherkinDocument gherkinDocument, Feature feature, String forbiddenTagName) {
            return new ForbiddenFeatureTagViolation(
                    FEATURE_MESSAGE.formatted(feature.getName(), forbiddenTagName),
                    new FeatureLocation(
                            gherkinDocument.getUri().orElse("unknown uri"),
                            feature.getName(),
                            feature.getLocation().getLine(),
                            feature.getLocation().getColumn().orElse(0L)));
        }

        @Override
        public String toString() {
            return "ForbiddenFeatureTagViolation{" +
                    "featureFile='" + featureLocation.fileName() + '\'' +
                    "message='" + message + '\'' +
                    "tokenName='" + featureLocation.tokenName() + '\'' +
                    ", line=" + featureLocation.line() +
                    ", column=" + featureLocation.column() +
                    '}';
        }
    }
}
