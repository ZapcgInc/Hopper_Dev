package com.hopper.tests.data.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopper.tests.data.model.response.prebooking.PreBookingResponse;
import io.restassured.response.Response;
import org.junit.Assert;

import java.io.IOException;
/**
 * Parses PreBooking API Response.
 */
public class PreBookingResponseParser
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static PreBookingResponse parse(final Response apiResponse)
    {
        Assert.assertNotNull("PreBooking API response is null", apiResponse);

        try
        {
            return OBJECT_MAPPER.readValue(apiResponse.getBody().asString(), PreBookingResponse.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("Failed to parse PreBooking Response");
        }

        return null;
    }
}
