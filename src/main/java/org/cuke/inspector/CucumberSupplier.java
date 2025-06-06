package org.cuke.inspector;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.DefaultObjectFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.feature.Options;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.FeatureSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.java.JavaBackendProviderService;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.net.URI;
import java.time.Clock;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Collections.singletonList;

public class CucumberSupplier {

    private final Set<Entry<String, InputStream>> featureSources;
    private final List<URI> featureURIs;
    private final URI glueDirectoryUri;
    private List<Feature> features;
    private List<GherkinDocument> gherkinDocuments;
    private CukeCachingGlue glue;

    public CucumberSupplier(Map<String, InputStream> featureSources, List<URI> featureURIs, URI glueDirectoryUri) {
        this.featureSources = featureSources.entrySet();
        this.featureURIs = featureURIs;
        this.glueDirectoryUri = glueDirectoryUri;

        features = new ArrayList<>();
        gherkinDocuments = new ArrayList<>();
    }

    public List<Feature> getFeatures() {
        if (features.isEmpty()) {
            EventBus eventBus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
            final FeatureParser parser = new FeatureParser(eventBus::generateId);
            Options runtimeOptions = () -> featureURIs;

            final FeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(() -> Thread.currentThread().getContextClassLoader(), runtimeOptions, parser);
            features = featureSupplier.get();
        }
        return features;
    }

    public CukeCachingGlue getGlue() {
        if (glue == null) {
            EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
            glue = new CukeCachingGlue(bus);

            DefaultObjectFactory lookup = new DefaultObjectFactory();
            Backend backend = new JavaBackendProviderService().create(lookup, lookup, () -> Thread.currentThread().getContextClassLoader());
            backend.loadGlue(glue, singletonList(glueDirectoryUri));

            glue.prepareGlue(new StepTypeRegistry(Locale.getDefault()));
        }
        return glue;
    }

    @SneakyThrows
    public List<GherkinDocument> getGherkinDocuments() {
        if (gherkinDocuments.isEmpty()) {
            GherkinParser parser = GherkinParser.builder()
                    .includeGherkinDocument(true)
                    .includeSource(true)
                    .includePickles(true)
                    .build();

            gherkinDocuments = featureSources.stream()
                    .map(entry -> getGherkinDocuments(entry, parser))
                    .toList();
        }
        return gherkinDocuments;
    }

    @SneakyThrows
    private static GherkinDocument getGherkinDocuments(Entry<String, InputStream> entry, GherkinParser parser) {
        return parser.parse(entry.getKey(), entry.getValue())
                .map(Envelope::getGherkinDocument)
                .filter(Optional::isPresent)
                .map(Optional::get).findFirst().orElseThrow(() -> new RuntimeException("No gherkin document"));
    }

}
