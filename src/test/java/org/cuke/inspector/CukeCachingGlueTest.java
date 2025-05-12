package org.cuke.inspector;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.DefaultObjectFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.java.JavaBackendProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.util.Locale;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class CukeCachingGlueTest {
    private CukeCachingGlue glue;
    private Backend backend;

    @BeforeEach
    void setUp() {
        DefaultObjectFactory lookup = new DefaultObjectFactory();
        backend = new JavaBackendProviderService().create(lookup, lookup, () -> Thread.currentThread().getContextClassLoader());
        EventBus eventBus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        glue = new CukeCachingGlue(eventBus);
    }

    @Test
    void shouldLoadCustomTypes() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:org.cuke.inspector.steps.customtype")));

        assertThat(glue.getParameterTypes()).hasSize(1);
        assertThat(glue.getParameterTypes().getFirst().parameterType().getName()).isEqualTo("customClass");
    }

    @Test
    void shouldLoadStepDefinitions() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:org.cuke.inspector.steps.customtype")));

        assertThat(glue.stepDefinitions).hasSize(1);
    }

    @Test
    void shouldCreateStepExpressionsForCustomTypes() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:org.cuke.inspector.steps.customtype")));

        glue.prepareGlue(new StepTypeRegistry(Locale.getDefault()));

        assertThat(glue.getStepDefinitionsByPattern().values()).hasSize(1);
    }

}