package org.cuke.inspector;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.cuke.inspector.checker.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class CukeInspector {

    private final List<CukeViolation> violations;
    private final CucumberRepository cucumberRepository;

    public CukeInspector(CucumberRepository cucumberRepository) {
        violations = new ArrayList<>();
        this.cucumberRepository = cucumberRepository;
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

    public CukeInspector checkInvalidTagCombinations(Set<String> invalidTagCombination) {
        violations.addAll(new InvalidTagCombinationsChecker(invalidTagCombination).inspect(cucumberRepository.getGherkinDocuments()));
        return this;
    }

    public CukeInspector checkInvalidInvalidKeywords(List<String> forbiddenStepKeywords) {
        violations.addAll(new InvalidStepKeywordChecker(forbiddenStepKeywords).inspect(cucumberRepository.getGherkinDocuments()));
        return this;
    }

    public CukeInspector findDuplicateScenarioNames() {
        violations.addAll(new DuplicateScenariosChecker().inspect(cucumberRepository.getGherkinDocuments()));
        return this;
    }

    public CukeInspector findScenariosMissingRequiredTags(String requiredRegex) {
        violations.addAll(new MissingRequiredTagChecker(requiredRegex).inspect(cucumberRepository.getGherkinDocuments()));
        return this;
    }

    public CukeInspector findFeaturesWithDisallowedTags(String forbiddenRegex) {
        violations.addAll(new ForbiddenFeatureTagChecker(forbiddenRegex).inspect(cucumberRepository.getGherkinDocuments()));
        return this;
    }

    public CukeInspector findDuplicateStepDefinitions() {
        violations.addAll(new DuplicateStepDefinitionsChecker().inspect(cucumberRepository.createGlue()));
        return this;
    }

    public CukeInspector findUnusedStepDefinitions() {
        violations.addAll(new UnusedStepDefinitionsChecker().inspect(cucumberRepository.getFeatures(), cucumberRepository.createGlue()));
        return this;
    }

    public List<CukeViolation> getViolations() {
        return Collections.unmodifiableList(violations);
    }

    public void hasNoViolations() {
        Assertions.assertThat(violations).withFailMessage(() -> ViolationFormatter.format(violations)).isEmpty();
    }

    public static class CukeInspectorBuilder {
        private final Map<String, InputStream> featureSources;
        private final List<URI> featureURIs;
        private URI glueDirectoryUri;

        private CukeInspectorBuilder() {
            featureSources = new HashMap<>();
            featureURIs = new ArrayList<>();
            glueDirectoryUri = null;
        }

        @SneakyThrows
        private void addSource(String uriAsString, InputStream inputStream) {
            featureURIs.add(new URI(uriAsString));
            featureSources.put(uriAsString, inputStream);
        }

        public CukeInspectorBuilder withFeatureFile(String featureSource, InputStream inputStream) {
            addSource(featureSource, inputStream);
            return this;
        }

        public CukeInspectorBuilder withFeatureDirectory(Path directory) throws IOException {
            try (Stream<Path> files = Files.walk(directory)) {
                addAllFeatureFiles(files);
            }
            return this;
        }

        private void addAllFeatureFiles(Stream<Path> files) {
            files.filter(file -> !Files.isDirectory(file))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".feature"))
                    .forEach(this::addSource);
        }

        @SneakyThrows
        private void addSource(Path file) {
            addSource(file.toUri().toString(), new ByteArrayInputStream(Files.readAllBytes(file)));
        }

        public CukeInspectorBuilder withJavaPackage(String packageName) {
            glueDirectoryUri = URI.create("classpath:" + packageName);
            return this;
        }

        public CukeInspector should() {
            return new CukeInspector(new CucumberRepository(featureSources, featureURIs, glueDirectoryUri));
        }
    }
}
