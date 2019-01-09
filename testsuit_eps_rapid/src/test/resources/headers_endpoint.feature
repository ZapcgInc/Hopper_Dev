#Author: your.email@your.domain.com
#Keywords Summary :
#Feature: List of scenarios.
#Scenario: Business rule through list of steps with arguments.
#Given: Some precondition step
#When: Some key actions
#Then: To observe outcomes or validation
#And,But: To enumerate more Given,When,Then steps
#Scenario Outline: List of steps for data-driven as an Examples and <placeholder>
#Examples: Container for s table
#Background: List of steps run before each of the scenarios
#""" (Doc Strings)
#| (Data Tables)
#@ (Tags/Labels):To group Scenarios
#<> (placeholder)
#""
## (Comments)
#Sample Feature Definition Template
@tag
Feature: EAN API Version, Authorization, Invalid resource validations
  Raise request(s) and validate mandatory data elements checks, Validate HTTP response code and parse JSON response

  Background:
    Given setup for partner "EPS"
    And API at "https://test.ean.com"
    And for version "2.1"
    And with request headers
      | Accept          | application/json |
      | Accept-Encoding | gzip             |
      | Customer-Ip     | 127.0.0.1        |
      | User-Agent      | Hopper/1.0       |
    And Generate authHeaderKey with
      | apikey | mq7ijoev87orvkq4mqo8dr2tf |
      | secret | 587btntj2ihg5             |

#######################  Basic Validation Scenarios

  @basic_test
  Scenario: Missing Authorization header
    Given Basic web application is running
    And with shopping end point "properties/availability"
    And user sets header "Authorization" value "null"
    And run shopping
    Then the response code for "SHOPPING" should be 401
    And user should see json response with paris on the filtered "." node
      | type    | request_unauthenticated                                                                                                |
      | message | The authorization header is missing or invalid.  Ensure that your request follows the guidelines in our documentation. |

  @basic_test
  Scenario: Invalid Authorization header
    Given Basic web application is running
    And with shopping end point "properties/availability"
    And user sets header "Authorization" value "abc123"
    And run shopping
    Then the response code for "SHOPPING" should be 401
    And user should see json response with paris on the filtered "." node
      | type    | request_unauthenticated                                                                                                |
      | message | The authorization header is missing or invalid.  Ensure that your request follows the guidelines in our documentation. |

  @basic_test
  Scenario: Invalid resource
    Given Basic web application is running
    And with shopping end point "properties/availability123"
    And run shopping
    Then the response code for "SHOPPING" should be 404
    And user should see json response with paris on the filtered "." node
      | type    | resource_not_found                    |
      | message | The resource requested was not found. |

  @basic_test
  Scenario: Invalid version
    Given Basic web application is running
    And with shopping end point "properties/availability"
    And for version "1"
    And run shopping
    Then the response code for "SHOPPING" should be 400
    And user should see json response with paris on the filtered "." node
      | type    | version.required                                                            |
      | message | You have not specified a version, the supported versions are: [2, 2.1, 2.2] |
    And user should see json response with paris on the filtered "fields[0]" node
      | name  | version |
      | type  | path    |
      | value | missing |

  @basic_test
  Scenario: Missing version
    Given Basic web application is running
    And with shopping end point "properties/availability"
    And for version "null"
    And run shopping
    Then the response code for "SHOPPING" should be 400
	    
