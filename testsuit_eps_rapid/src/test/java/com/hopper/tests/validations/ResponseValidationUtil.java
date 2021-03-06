package com.hopper.tests.validations;

import com.hopper.tests.constants.RequestType;
import com.hopper.tests.definitions.model.TestContext;
import com.hopper.tests.validations.booking.BookingValidationUtil;
import com.hopper.tests.validations.constants.ResponseValidationField;
import com.hopper.tests.validations.model.Range;
import com.hopper.tests.validations.paymentoptions.PaymentOptionsResponseValidationUtil;
import com.hopper.tests.validations.prebooking.PreBookingValidationUtil;
import com.hopper.tests.validations.retrievebooking.RetrieveBookingValidation;
import com.hopper.tests.validations.shopping.ShoppingResponseValidationUtil;
import io.restassured.response.Response;
import org.junit.Assert;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Util class for Response Validations
 */
public class ResponseValidationUtil
{
    public static void validateHTTPResponseCode(@NotNull final Response restResponse, final int expectedCode)
    {
        if(restResponse!=null)
        {
            final int responseCode = restResponse.getStatusCode();
            final String errorMessage = "Expected response code : " + expectedCode + "and actual response code : " + responseCode + "are not matching";
            Assert.assertEquals(errorMessage, expectedCode, responseCode);
        }
    }

    public static void validateElementForRetrieveBooking(@NotNull final Response response, final ResponseValidationField element) {

        RetrieveBookingValidation.validateElement(response, element);

    }



    public static void validateResponseBody(final Response restResponse, final Map<String, String> expectedResponseMap, final String field)
    {
        final Map<String, String> fieldResponseMap = restResponse.jsonPath().get(field);

        for (String key : expectedResponseMap.keySet())
        {
            final String actualValue = fieldResponseMap.get(key);
            final String expectedValue = expectedResponseMap.get(key);
            final String errorMessage = "For field" + field + "Expected value : " + expectedValue + "and actual value : " + actualValue + "are not matching";

            Assert.assertEquals(errorMessage, expectedValue, actualValue);
        }
    }

    public static void validate(final ResponseValidationField validationField, final RequestType requestType, final TestContext context, final int count)
    {
        switch (requestType)
        {
            case SHOPPING:
            {
                ShoppingResponseValidationUtil.validate(context, validationField, count);
                break;
            }
            case PAYMENT_OPTIONS:
            {
                PaymentOptionsResponseValidationUtil.validate(context, validationField, count);
                break;
            }
            case PRE_BOOKING:
            {
                PreBookingValidationUtil.validate(context, validationField, count);
                break;
            }
            case BOOKING:
            {
                BookingValidationUtil.validate(context, validationField, count);
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Request Type : [" + requestType + "], Unsupported for Count Validation");
            }
        }
    }

    public static void validate(final ResponseValidationField validationField, final RequestType requestType, final TestContext context, final Range<Integer>  expectedRange)
    {
        switch (requestType)
        {
            case SHOPPING:
            {
                ShoppingResponseValidationUtil.validate(context, validationField, expectedRange);
                break;
            }
            case PAYMENT_OPTIONS:
            {
                PaymentOptionsResponseValidationUtil.validate(context, validationField, expectedRange);
                break;
            }
            case PRE_BOOKING:
            {
                PreBookingValidationUtil.validate(context, validationField, expectedRange);
                break;
            }
            case BOOKING:
            {
                BookingValidationUtil.validate(context, validationField, expectedRange);
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Request Type : [" + requestType + "], Unsupported for Range Validation");
            }
        }
    }


    public static void validate(final ResponseValidationField validationField, final RequestType requestType, final TestContext context, final List<String> expectedValues)
    {
        switch (requestType)
        {
            case SHOPPING:
            {
                ShoppingResponseValidationUtil.validate(context, validationField, expectedValues);
                break;
            }
            case PAYMENT_OPTIONS:
            {
                PaymentOptionsResponseValidationUtil.validate(context, validationField, expectedValues);
                break;
            }
            case PRE_BOOKING:
            {
                PreBookingValidationUtil.validate(context, validationField, expectedValues);
                break;
            }
            case BOOKING:
            {
                BookingValidationUtil.validate(context, validationField, expectedValues);
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Request Type : [" + requestType + "], Unsupported for Expected Values Validation");
            }
        }
    }

    public static void validate(final ResponseValidationField validationField, final RequestType requestType, final TestContext context)
    {
        switch (requestType)
        {
            case SHOPPING:
            {
                ShoppingResponseValidationUtil.validate(context, validationField);
                break;
            }
            case PAYMENT_OPTIONS:
            {
                PaymentOptionsResponseValidationUtil.validate(context, validationField);
                break;
            }
            case PRE_BOOKING:
            {
                PreBookingValidationUtil.validate(context, validationField);
                break;
            }
            case BOOKING:
            {
                BookingValidationUtil.validate(context, validationField);
                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Request Type : [" + requestType + "], Unsupported for Validation");
            }
        }
    }

    public static void validateElementForRetrieveBooking(Response response, ResponseValidationField element, List<String> listOfValues) {

        RetrieveBookingValidation.validateElement(response,element,listOfValues);


    }
}
