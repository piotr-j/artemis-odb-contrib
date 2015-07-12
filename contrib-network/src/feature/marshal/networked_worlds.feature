Feature: Server managed entity state between worlds

  Background:
    Given Bob is connected to server via <marshal>

    Examples:
      | marshal |
      | kryonet |

  Scenario: Local entity created on host
    Given Bob sees no particles
    And particles are not networked
    When Host spawns particles
    And Some time passes
    Then Bob should see no particles

  Scenario: Networked entity is created on host
    Given Bob is unaware of fido
    And fido is networked
    When Host spawns fido
    And Some time passes
    Then Bob should see fido

  Scenario: Networked entity is deleted from host
    Given Bob sees fido
    And Server sees fido
    And fido is networked
    When Host deletes fido
    And Some time passes
    Then Bob should no longer see fido

  Scenario: Entity moved on host
    Given Bob sees fido
    And Server sees fido
    And fido is networked
    When Host moves fido
    And Some time passes
    Then Bob should see fido move to the same location
