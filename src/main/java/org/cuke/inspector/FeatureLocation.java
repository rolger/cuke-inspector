package org.cuke.inspector;

public record FeatureLocation(String fileName,
                              String tokenName,
                              Long line,
                              Long column) {

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
