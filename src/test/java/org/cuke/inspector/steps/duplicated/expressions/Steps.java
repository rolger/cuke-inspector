package org.cuke.inspector.steps.duplicated.expressions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

    @Given("an expression")
    @When("act")
    @Then("assert")
    public void first() {
        // only for testing purposes
    }

    @When("an expression")
    public void second() {
        // only for testing purposes
    }

    @Then("an expression")
    public void third() {
        // only for testing purposes
    }

}