package org.cuke.inspector.steps.matching.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class MatchingSteps {

    @When("act")
    public void noParams() {
        // only for testing purposes
    }

    @Given("arrange with {int}")
    public void withIntParam(int value) {
        // only for testing purposes
    }

    @Then("assert with {string}")
    public void withStringParams(String string, int value) {
        // only for testing purposes
    }

    @Then("assert {string} {int}")
    public void withMixedParams(String string, int value) {
        // only for testing purposes
    }
}