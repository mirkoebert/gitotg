Feature: Login redirect
  After a successful OAuth login (Google or GitHub), the user should land on the cockpit
  rather than any page they may have been trying to reach before authenticating.

  Scenario: Successful login redirects to the cockpit
    When the user successfully logs in
    Then the cockpit page is loaded
