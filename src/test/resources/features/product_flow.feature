@regression
Feature: New1C product Quote & Policy flows
  As a QA engineer
  I want each product's Quote/Policy journey to run as a named scenario
  So that Jira/Xray reporting and Extent reports map 1:1 to a readable test case

  # Each row still reads its data from TestData.xls (sheets listed in
  # "activeSheets") by TestCaseName - same source of truth as the old
  # @DataProvider("allData"), just driven by Cucumber instead of TestNG.
  # Rows where Execution != Yes are skipped by the step definition, exactly
  # like the old "Yes".equalsIgnoreCase(executionFlag) check.

  @JIRA-PVII-7860
  Scenario Outline: Execute Shop test case "<TestCaseName>"
    Given test case "<TestCaseName>" data is loaded
    When I execute the product flow for test case "<TestCaseName>"
    Then test case "<TestCaseName>" should have completed

    Examples:
      | TestCaseName       |
      | Shop_TC01_Quote    |
      | Shop_TC02_Policy   |

  Scenario Outline: Execute Property Owners test case "<TestCaseName>"
    Given test case "<TestCaseName>" data is loaded
    When I execute the product flow for test case "<TestCaseName>"
    Then test case "<TestCaseName>" should have completed

    Examples:
      | TestCaseName       |
      | PO_TC01_Quote      |
      | PO_TC02_Policy     |
