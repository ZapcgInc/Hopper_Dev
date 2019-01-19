@preBook
Feature: Validations for PreBooking API.

  Background:
    Given setup for partner with config at "expedia-config.yml"
    And with shopping query parameters
      | currency          | USD               |
      | language          | en-US             |
      | country_code      | US                |
      | property_id       | 20321             |
      | occupancy         | 2-9,4             |
      | sales_channel     | website           |
      | sales_environment | hotel_only        |
      | sort_type         | preferred         |
      | include           | all_rates         |
      | rate_option       | closed_user_group |
    And with request DateFormat "yyyy-MM-dd"
    And set checkin "90" from today with lengthOfStay "5"

  #######################   Rapid Test Scenarios
  @business_test
  Scenario: Run booking and validate Status
    Given set checkin "5" from today with lengthOfStay "3"
    And run shopping and preBooking for Booking
    And run booking
    Then the response code for "BOOKING" should be 200