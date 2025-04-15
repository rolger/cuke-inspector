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
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        sb.append(cukeViolation.featureLocation().fileName())
                .append(":[")
                .append(cukeViolation.featureLocation().line())
                .append(",")
                .append(cukeViolation.featureLocation().column().orElse(0L))
                .append("] ")
                .append(cukeViolation.message())
                .append("\n");
        return sb.toString();
    }
}
