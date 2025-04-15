package org.cuke.inspector;

import io.cucumber.messages.types.Feature;

import java.util.List;

public interface CukeViolation {
    String message();

    FeatureLocation featureLocation();

    default List<FeatureLocation> featureLocations() {
        return List.of(featureLocation());
    }

    default String format() {
        return ViolationFormatter.format(this);
    }
}
