package org.cuke.inspector;

import java.util.Optional;

public record FeatureLocation(String fileName,
                              String tokenName,
                              Long line,
                              Optional<Long> column) {
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
