package org.cuke.inspector;

import org.cuke.inspector.checker.*;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class CukeInspector {

    private final List<CukeViolation> violations;
    private final CucumberSupplier cucumberSupplier;

    public CukeInspector(CucumberSupplier cucumberSupplier) {
        violations = new ArrayList<>();
        this.cucumberSupplier = cucumberSupplier;
    }

    public static CukeInspectorBuilder withFeatureFile(Path source) throws IOException {
        Objects.requireNonNull(source);
        return withFeatureFile(source.toUri().toString(), new ByteArrayInputStream(Files.readAllBytes(source)));
    }

    public static CukeInspectorBuilder withFeatureFile(String featureSource, InputStream inputStream) {
        Objects.requireNonNull(featureSource);
        Objects.requireNonNull(inputStream);
        return new CukeInspectorBuilder().withFeatureFile(featureSource, inputStream);
    }

    public static CukeInspectorBuilder withFeatureDirectory(Path directory) throws IOException {
        Objects.requireNonNull(directory);
        return new CukeInspectorBuilder().withFeatureDirectory(directory);
    }

    public static CukeInspectorBuilder withJavaPackage(String packageName) {
        Objects.requireNonNull(packageName);
        return new CukeInspectorBuilder().withJavaPackage(packageName);
    }

    public CukeInspector findInvalidTagCombinations(Set<String> invalidTagCombination) {
        violations.addAll(new InvalidTagCombinationsChecker(invalidTagCombination).inspect(cucumberSupplier));
        return this;
    }

    public CukeInspector findInvalidKeywords(List<String> forbiddenStepKeywords) {
        violations.addAll(new InvalidStepKeywordChecker(forbiddenStepKeywords).inspect(cucumberSupplier));
        return this;
    }

    public CukeInspector findDuplicateScenarioNames() {
        violations.addAll(new DuplicateScenariosChecker().inspect(cucumberSupplier));
        return this;
    }

    public CukeInspector findScenariosMissingRequiredTags(String requiredRegex) {
        violations.addAll(new MissingRequiredTagChecker(requiredRegex).inspect(cucumberSupplier));
        return this;
    }

    public CukeInspector findFeaturesWithDisallowedTags(String forbiddenRegex) {
        violations.addAll(new ForbiddenFeatureTagChecker(forbiddenRegex).inspect(cucumberSupplier));
        return this;
    }

    public CukeInspector findDuplicateStepDefinitions() {
        violations.addAll(new DuplicateStepDefinitionsChecker().inspect(cucumberSupplier));
        return this;
    }

    public CukeInspector findUnusedStepDefinitions() {
        violations.addAll(new UnusedStepDefinitionsChecker().inspect(cucumberSupplier));
        return this;
    }

    public List<CukeViolation> getViolations() {
        return Collections.unmodifiableList(violations);
    }

    public void hasNoViolations() {
        if (!violations.isEmpty()) {
            throw new AssertionFailedError(ViolationFormatter.format(violations));
        }
    }

}
