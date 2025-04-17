package org.cuke.inspector.steps.matching.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class MatchingSteps {

    @When("act")
    public void noParams() {
    }

    @Given("arrange with {int}")
    public void withParam(int value) {
    }

    @Then("assert {string}")
    public void withParams(String string) {
    }

}