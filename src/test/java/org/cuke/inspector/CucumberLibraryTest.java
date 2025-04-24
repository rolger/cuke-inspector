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
import io.cucumber.java.JavaBackendProviderService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class CucumberLibraryTest {

    @Test
    void canLoadCucumberJavaMethods() {
        DefaultObjectFactory lookup = new DefaultObjectFactory();
        Backend backend = new JavaBackendProviderService().create(lookup, lookup, () -> Thread.currentThread().getContextClassLoader());
        EventBus eventBus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

        CukeCachingGlue glue = new CukeCachingGlue(eventBus);
        backend.loadGlue(glue, singletonList(URI.create("classpath:org.cuke.inspector.steps.matching.steps")));
        glue.prepareGlue(new StepTypeRegistry(Locale.getDefault()));

        Map<String, List<CukeInspectorStepDefinition>> stepDefinitionsByPattern = glue.getStepDefinitionsByPattern();

        assertThat(stepDefinitionsByPattern).hasSize(4);
    }

    @Test
    void canLoadCucumberFeatures() {
        EventBus eventBus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        final FeatureParser parser = new FeatureParser(eventBus::generateId);
        Options runtimeOptions = () -> List.of(Paths.get("src/test/resources/nousage/matching_steps.feature").toUri());
        final FeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(() -> Thread.currentThread().getContextClassLoader(), runtimeOptions, parser);

        Collection<Feature> features = featureSupplier.get();

        assertThat(features).hasSize(1);
    }

}