package org.cuke.inspector.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

    @Given("test")
    public void givenExpression() {
    }

    @When("test")
    public void whenExpression() {
    }

    @Given("test {string}")
    public void cucumberExpression(String string) {
    }

    @Then("test .*")
    public void regularExpression(String string) {
    }

    @Given("arrange")
    @When("act")
    @Then("assert")
    public void multipleAnnotations() {
    }


}