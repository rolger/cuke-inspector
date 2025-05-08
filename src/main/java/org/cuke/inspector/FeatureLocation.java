package org.cuke.inspector;

import java.util.Optional;

public record FeatureLocation(String fileName,
                              String tokenName,
                              Long line,
                              Long column) {

    public FeatureLocation(Optional<String> uri, String name, Long line, Optional<Long> column) {
        this(uri.orElseGet(() -> "unknown uri"), name, line, column.orElseGet(() -> 0L));
    }

    @Override
    public String toString() {
        return "FeatureLocation{" +
                "fileName='" + fileName + '\'' +
                ", tokenName='" + tokenName + '\'' +
                ", line=" + line +
                ", column=" + column +
                '}';
    }
}
