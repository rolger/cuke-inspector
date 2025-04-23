Feature: No tags feature

  Background:
    Given this is valid

  Scenario: No tags scenario
    But this is invalid
    Then this is valid

  Scenario: No tags scenario
    Given this is valid
    When this is valid
    Then this is valid

  Rule: Rule adds new level
    Background:
      But this is invalid

    Scenario: No tags scenario
      But this is invalid
      Then this is valid
