Feature: Feature with matching steps

  Scenario: First
    Given arrange with 5
    When act
    Then assert with "me"

  Scenario Outline: Second
    When act
    Then assert <value> <integer>
    Examples:
      | value               | integer |
      | "string with words" | 5       |