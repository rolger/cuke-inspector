package org.cuke.inspector;

import io.cucumber.core.backend.*;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import lombok.Getter;

import java.util.*;

public final class CukeCachingGlue implements Glue {
    protected final List<StepDefinition> stepDefinitions = new ArrayList<>();
    @Getter
    private final Map<String, List<CukeInspectorStepDefinition>> stepDefinitionsByPattern = new TreeMap<>();
    @Getter
    private final List<ParameterTypeDefinition> parameterTypes = new ArrayList<>();
    private final EventBus bus;

    CukeCachingGlue(EventBus bus) {
        this.bus = bus;
    }

    public void addBeforeAllHook(StaticHookDefinition beforeAllHook) {
        // not needed
    }

    public void addAfterAllHook(StaticHookDefinition afterAllHook) {
        // not needed
    }

    public void addStepDefinition(StepDefinition stepDefinition) {
        this.stepDefinitions.add(stepDefinition);
    }

    public void addBeforeHook(HookDefinition hookDefinition) {
        // not needed
    }

    public void addAfterHook(HookDefinition hookDefinition) {
        // not needed
    }

    public void addBeforeStepHook(HookDefinition hookDefinition) {
        // not needed
    }

    public void addAfterStepHook(HookDefinition hookDefinition) {
        // not needed
    }

    public void addParameterType(ParameterTypeDefinition parameterType) {
        this.parameterTypes.add(parameterType);
    }

    public void addDataTableType(DataTableTypeDefinition dataTableType) {
        // not needed
    }

    public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
        // not needed
    }

    public void addDefaultDataTableEntryTransformer(DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer) {
        // not needed
    }

    public void addDefaultDataTableCellTransformer(DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer) {
        // not needed
    }

    public void addDocStringType(DocStringTypeDefinition docStringType) {
        // not needed
    }

    public List<CukeInspectorStepDefinition> getCukeStepDefinitions() {
        return this.stepDefinitionsByPattern.values().stream().flatMap(Collection::stream).toList();
    }

    public void prepareGlue(StepTypeRegistry stepTypeRegistry) {
        parameterTypes.stream()
                .forEach(parameterType -> stepTypeRegistry.defineParameterType(parameterType.parameterType()));

        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(stepTypeRegistry, this.bus);
        this.stepDefinitions.forEach(stepDefinition -> {
            StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
            CukeInspectorStepDefinition inspectorStepDefinition = new CukeInspectorStepDefinition(stepDefinition, expression);

            stepDefinitionsByPattern
                    .computeIfAbsent(inspectorStepDefinition.getExpression().getSource(), key -> new ArrayList<>())
                    .add(inspectorStepDefinition);
        });
    }
}