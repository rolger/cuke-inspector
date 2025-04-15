package org.cuke.inspector;

import io.cucumber.core.backend.*;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.StepTypeRegistry;

import java.util.*;

public final class CukeCachingGlue implements Glue {
    private final List<ParameterTypeDefinition> parameterTypeDefinitions = new ArrayList();
    private final List<DataTableTypeDefinition> dataTableTypeDefinitions = new ArrayList();
    private final List<StepDefinition> stepDefinitions = new ArrayList();
    private final Map<String, String> stepPatternByStepText = new HashMap();
    private final Map<String, List<CukeInspectorStepDefinition>> stepDefinitionsByPattern = new TreeMap();
    private final EventBus bus;

    CukeCachingGlue(EventBus bus) {
        this.bus = bus;
    }

    public void addBeforeAllHook(StaticHookDefinition beforeAllHook) {
    }

    public void addAfterAllHook(StaticHookDefinition afterAllHook) {
    }

    public void addStepDefinition(StepDefinition stepDefinition) {
        this.stepDefinitions.add(stepDefinition);
    }

    public void addBeforeHook(HookDefinition hookDefinition) {
    }

    public void addAfterHook(HookDefinition hookDefinition) {
    }

    public void addBeforeStepHook(HookDefinition hookDefinition) {
    }

    public void addAfterStepHook(HookDefinition hookDefinition) {
    }

    public void addParameterType(ParameterTypeDefinition parameterType) {
        this.parameterTypeDefinitions.add(parameterType);
    }

    public void addDataTableType(DataTableTypeDefinition dataTableType) {
        this.dataTableTypeDefinitions.add(dataTableType);
    }

    public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
    }

    public void addDefaultDataTableEntryTransformer(DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer) {
    }

    public void addDefaultDataTableCellTransformer(DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer) {
    }

    public void addDocStringType(DocStringTypeDefinition docStringType) {
    }

    Collection<ParameterTypeDefinition> getParameterTypeDefinitions() {
        return this.parameterTypeDefinitions;
    }

    Collection<DataTableTypeDefinition> getDataTableTypeDefinitions() {
        return this.dataTableTypeDefinitions;
    }

    Collection<StepDefinition> getStepDefinitions() {
        return this.stepDefinitions;
    }

    Map<String, String> getStepPatternByStepText() {
        return this.stepPatternByStepText;
    }

    public Map<String, List<CukeInspectorStepDefinition>> getStepDefinitionsByPattern() {
        return this.stepDefinitionsByPattern;
    }

    public void prepareGlue(StepTypeRegistry stepTypeRegistry) {
        StepExpressionFactory stepExpressionFactory = new StepExpressionFactory(stepTypeRegistry, this.bus);
        this.stepDefinitions.forEach((stepDefinition) -> {
            StepExpression expression = stepExpressionFactory.createExpression(stepDefinition);
            CukeInspectorStepDefinition inspectorStepDefinition = new CukeInspectorStepDefinition(this.bus.generateId(), stepDefinition, expression);

            stepDefinitionsByPattern
                    .computeIfAbsent(inspectorStepDefinition.getExpression().getSource(), key -> new ArrayList<>())
                    .add(inspectorStepDefinition);
        });
    }
}