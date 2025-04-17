package org.cuke.inspector.checker;

import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import org.cuke.inspector.CukeViolation;
import org.cuke.inspector.FeatureLocation;
import org.cuke.inspector.ViolationChecker;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ForbiddenFeatureTagChecker implements ViolationChecker {

    private final Pattern forbiddenTagPattern;

    public ForbiddenFeatureTagChecker(String forbiddenRegex) {
        Objects.requireNonNull(forbiddenRegex);

        forbiddenTagPattern = Pattern.compile(forbiddenRegex);
    }

    public Collection<? extends CukeViolation> inspect(List<GherkinDocument> gherkinDocuments) {
        return gherkinDocuments.stream()
                .flatMap(gherkinDocument -> {
                    Feature feature = gherkinDocument.getFeature().orElseThrow(() -> new RuntimeException("No feature in " + gherkinDocument.getUri()));

                    return feature.getTags().stream()
                            .filter(tag -> forbiddenTagPattern.matcher(tag.getName()).matches())
                            .map(tag -> ForbiddenFeatureTagChecker.ForbiddenFeatureTagViolation.build(gherkinDocument, feature, tag.getName()));
                })
                .toList();
    }

    record ForbiddenFeatureTagViolation(String message, FeatureLocation featureLocation) implements CukeViolation {
        private static final String FEATURE_MESSAGE = "Feature '%s' contains forbidden tag (%s).";

        private static CukeViolation build(GherkinDocument gherkinDocument, Feature feature, String forbiddenTagName) {
            return new ForbiddenFeatureTagChecker.ForbiddenFeatureTagViolation(
                    FEATURE_MESSAGE.formatted(feature.getName(), forbiddenTagName),
                    new FeatureLocation(
                            gherkinDocument.getUri().orElse("unknown uri"),
                            feature.getName(),
                            feature.getLocation().getLine(),
                            feature.getLocation().getColumn()));
        }

    }
}
