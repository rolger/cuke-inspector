package org.cuke.inspector;

import java.util.List;
import java.util.stream.Collectors;

final class ViolationFormatter {
    private ViolationFormatter() {
    }

    public static String format(List<CukeViolation> cukeViolations) {
        return cukeViolations.stream().map(CukeViolation::format).collect(Collectors.joining());
    }

    public static String format(CukeViolation cukeViolation) {
        FeatureLocation featureLocation = cukeViolation.featureLocation();
        return "%n%s:[%d,%d] %s%n".formatted(featureLocation.fileName(), featureLocation.line(), featureLocation.column(), cukeViolation.message());
    }
}
