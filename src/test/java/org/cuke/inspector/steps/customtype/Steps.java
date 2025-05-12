package org.cuke.inspector.steps.customtype;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;

public class Steps {

    @ParameterType(".*")
    public CustomClass customClass(String value) {
        return new CustomClass(value);
    }

    @Given("{customClass} in an expression")
    public void this_uses_a_custom_class(CustomClass customClass) {
        // only for testing purposes
    }

}