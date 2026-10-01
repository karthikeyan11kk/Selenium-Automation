@regression
Feature: Shop and Property Owners insurance quotes, policies and renewals
  As an insurance broker using Acturis
  I want to create Shop and Property Owners quotes, convert them to bound
  policies, and process renewals
  So that customers can obtain and maintain the right cover

  # Login and navigation to the home page happen automatically once per
  # suite run (Hooks.setUpSuite/@BeforeAll) - not repeated per scenario, so
  # there's no separate "Given I am logged in" step here to keep pace with.
  #
  # Each scenario still reads its full business data (company details,
  # cover amounts, addresses, etc.) from TestData.xls by TestCaseName rather
  # than inline in this file - the real data here runs to ~50 fields per
  # test case (financial figures, addresses, dates), which would make the
  # feature file itself unreadable if inlined. What IS in this file is the
  # business intent of each scenario; TestCaseName is the link to its data.
  # Rows where Execution != Yes in the spreadsheet are skipped automatically.

  @shop @quote
  Scenario: Generate a new Shop quote
    Given the business data for test case "Shop_TC01_Quote" is prepared
    When I request a Shop quote for test case "Shop_TC01_Quote"
    Then a quote reference should be generated and recorded for test case "Shop_TC01_Quote"

  @shop @policy
  Scenario: Convert a Shop quote into a bound policy
    Given the business data for test case "Shop_TC02_Policy" is prepared
    When I convert the Shop quote to a bound policy for test case "Shop_TC02_Policy"
    Then a policy reference should be generated and recorded for test case "Shop_TC02_Policy"

  @shop @renewal
  Scenario Outline: Process a Shop policy renewal
    Given the business data for test case "<TestCaseName>" is prepared
    When I process a Shop policy renewal for test case "<TestCaseName>"
    Then the renewal should complete and be timestamped for test case "<TestCaseName>"

    Examples:
      | TestCaseName       |
      | Shop_TC03_Renewal  |
      | Shop_TC04_Renewal  |

  @po @quote
  Scenario: Generate a new Property Owners quote
    Given the business data for test case "PO_TC01_Quote" is prepared
    When I request a Property Owners quote for test case "PO_TC01_Quote"
    Then a quote reference should be generated and recorded for test case "PO_TC01_Quote"

  @po @policy
  Scenario: Convert a Property Owners quote into a bound policy
    Given the business data for test case "PO_TC02_Policy" is prepared
    When I convert the Property Owners quote to a bound policy for test case "PO_TC02_Policy"
    Then a policy reference should be generated and recorded for test case "PO_TC02_Policy"
