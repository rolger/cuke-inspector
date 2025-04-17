package org.cuke.inspector;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.DefaultObjectFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.java.JavaBackendProviderService;
import io.cucumber.messages.types.GherkinDocument;
import org.assertj.core.api.Assertions;
import org.cuke.inspector.checker.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public final class CukeInspector {

    private Map<String, InputStream> featureSources;
    private List<URI> features;
    private List<CukeViolation> violations;
    private URI glueDirectoryUri;

    private CukeInspector() {
        violations = new ArrayList<>();
        featureSources = new HashMap<>();
        features = new ArrayList<>();
    }

    private void addSource(String uriAsString, InputStream inputStream) {
        try {
            features.add(new URI(uriAsString));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        featureSources.put(uriAsString, inputStream);
    }

    public static CukeInspectorBuilder withFeatureFile(Path source) throws IOException {
        Objects.requireNonNull(source);
        return new CukeInspectorBuilder().withFeatureFile(source.toUri().toString(), new ByteArrayInputStream(Files.readAllBytes(source)));
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

    public static CukeInspectorBuilder withJavaPackage(String packageName) throws IOException {
        Objects.requireNonNull(packageName);
        return new CukeInspectorBuilder().withJavaPackage(packageName);
    }

    public CukeInspector checkInvalidTagCombinations(Set<String> invalidTagCombination) {
        violations.addAll(new InvalidTagCombinationsChecker(invalidTagCombination).inspect(parseGherkinDocuments()));
        return this;
    }

    public CukeInspector checkInvalidInvalidKeywords(List<String> forbiddenStepKeywords) {
        violations.addAll(new InvalidStepKeywordChecker(forbiddenStepKeywords).inspect(parseGherkinDocuments()));
        return this;
    }

    public CukeInspector findDuplicateScenarioNames() {
        violations.addAll(new DuplicateScenariosChecker().inspect(parseGherkinDocuments()));
        return this;
    }

    public CukeInspector findScenariosMissingRequiredTags(String requiredRegex) {
        violations.addAll(new MissingRequiredTagChecker(requiredRegex).inspect(parseGherkinDocuments()));
        return this;
    }

    public CukeInspector findFeaturesWithDisallowedTags(String forbiddenRegex) {
        violations.addAll(new ForbiddenFeatureTagChecker(forbiddenRegex).inspect(parseGherkinDocuments()));
        return this;
    }

    public CukeInspector findDuplicateStepDefinitions() {
        violations.addAll(new DuplicateStepDefinitionsChecker().inspect(createGlue()));
        return this;
    }

    public CukeInspector findUnusedStepDefinitions() {
        violations.addAll(new UnusedStepDefinitionsChecker().inspect(parseGherkinDocuments(), createGlue()));
        return this;
    }

    private CukeCachingGlue createGlue() {
        DefaultObjectFactory lookup = new DefaultObjectFactory();
        Backend backend = new JavaBackendProviderService().create(lookup, lookup, () -> Thread.currentThread().getContextClassLoader());
        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        CukeCachingGlue glue = new CukeCachingGlue(bus);
        backend.loadGlue(glue, singletonList(glueDirectoryUri));
        glue.prepareGlue(new StepTypeRegistry(Locale.getDefault()));
        return glue;
    }

    private List<Feature> getFeatures() {
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(
                () -> Thread.currentThread().getContextClassLoader(),
                () -> features,
                new FeatureParser(UUID::randomUUID)
        );
        return featureSupplier.get();
    }

    private List<GherkinDocument> parseGherkinDocuments() {
        GherkinParser parser = GherkinParser.builder()
                .includeGherkinDocument(true)
                .includeSource(false)
                .includePickles(false)
                .build();

        return featureSources.entrySet().stream()
                .map(entry -> {
                    try {
                        return parser.parse(entry.getKey(), entry.getValue())
                                .findFirst().orElseThrow(() -> new RuntimeException("No envelope"))
                                .getGherkinDocument().orElseThrow(() -> new RuntimeException("No gherkin document"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    public List<CukeViolation> getViolations() {
        return Collections.unmodifiableList(violations);
    }

    public void hasNoViolations() {
        Assertions.assertThat(violations).withFailMessage(() -> ViolationFormatter.format(violations)).isEmpty();
    }

    public static class CukeInspectorBuilder {

        private CukeInspector cukeInspector;

        public CukeInspectorBuilder() {
            this.cukeInspector = new CukeInspector();
        }

        public CukeInspectorBuilder withFeatureFile(String featureSource, InputStream inputStream) {
            cukeInspector.addSource(featureSource, inputStream);
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

        private void addSource(Path file) {
            try {
                cukeInspector.addSource(file.toUri().toString(), new ByteArrayInputStream(Files.readAllBytes(file)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public CukeInspectorBuilder withJavaPackage(String packageName) {
            cukeInspector.glueDirectoryUri = URI.create("classpath:" + packageName);
            return this;
        }

        public CukeInspector should() {
            return cukeInspector;
        }

    }
}
