package com.hopper.tests.definitions.util;

import com.google.common.collect.ImmutableMap;
import com.hopper.tests.constants.GlobalConstants;
import com.hopper.tests.constants.RequestType;
import com.hopper.tests.config.model.TestConfig;
import com.hopper.tests.data.model.response.RoomPrice;
import com.hopper.tests.definitions.model.TestContext;
import com.hopper.tests.data.model.request.booking.CreditCard;
import com.hopper.tests.data.model.request.booking.Customer;
import com.hopper.tests.data.model.response.Link;
import com.hopper.tests.data.model.response.prebooking.PreBookingResponse;
import com.hopper.tests.data.model.response.shopping.BedGroups;
import com.hopper.tests.data.model.response.shopping.Property;
import com.hopper.tests.data.model.response.shopping.Rate;
import com.hopper.tests.data.model.response.shopping.Room;
import com.hopper.tests.data.model.response.shopping.ShoppingResponse;
import com.hopper.tests.data.ResponseSupplierFactory;
import com.hopper.tests.util.logging.LoggingUtil;
import com.hopper.tests.data.parser.BookingResponseParser;
import com.hopper.tests.data.parser.BookingRetrieveResponseParser;
import com.hopper.tests.data.parser.PreBookingResponseParser;
import com.hopper.tests.data.parser.ShoppingResponseParser;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class for Booking Test scenarios
 */
public class BookingTestHelper
{
    public static void runShoppingAndPreBookingForBooking(final TestContext context)
    {
        final Response shopResponse = ResponseSupplierFactory.create(
                context,
                GlobalConstants.GET,
                RequestType.SHOPPING).get();

        context.setResponse(RequestType.SHOPPING, shopResponse);

        final ShoppingResponse shoppingResponse = ShoppingResponseParser.parse(context.getResponse(RequestType.SHOPPING));
        context.setShoppingResponse(shoppingResponse);

        if (shoppingResponse == null || shoppingResponse.getProperties().isEmpty())
        {
            Assert.fail("No available properties returned in Shopping");
        }

        // Run pre-booking until we find matched response.
        int i =0;
        boolean foundPropertyWithAvailability = false;
        for (Property property : shoppingResponse.getProperties())
        {
        	if(foundPropertyWithAvailability) break;
        	
            for (Room room : property.getRooms())
            {
            	if(foundPropertyWithAvailability) break;
            	
                for (Rate rate : room.getRates())
                {
                	if(foundPropertyWithAvailability) break;
                	
                    for (BedGroups bedGroup : rate.getBedGroups())
                    {
                    	if(foundPropertyWithAvailability) break;
                    	
                        final Link priceCheckLink = bedGroup.getLinks().get("price_check");
                        context.setApiPath(RequestType.PRE_BOOKING, priceCheckLink.getHref());

                        final Response response = ResponseSupplierFactory.create(
                                context,
                                priceCheckLink.getMethod(),
                                RequestType.PRE_BOOKING).get();

                        if (response.getStatusCode() == 200 || response.getStatusCode() == 409)
                        {
                            final PreBookingResponse preBookingResponse = PreBookingResponseParser.parse(response);
                            if (preBookingResponse != null && (preBookingResponse.getStatus().equals("matched") || preBookingResponse.getStatus().equals("price_change")))
                            {
                                context.setPreBookingResponse(preBookingResponse);
                                context.setResponse(RequestType.PRE_BOOKING, response);
                                foundPropertyWithAvailability = true;
                                break;
                            }
                        }

                    }
                }

            }
        }

        Assert.assertTrue("Prebooking failed for all properties / rooms", foundPropertyWithAvailability);
    }

    public static void runBooking(final TestContext context, final boolean holdBooking)
    {
        final Link bookingLink = context.getPreBookingResponse().getLinks().get("book");
      //  int numRooms = context.getPreBookingResponse().getRoomPriceByOccupany().keySet().size();
        context.setApiPath(RequestType.BOOKING, bookingLink.getHref());

        final String affiliateId = RandomStringUtils.randomAlphanumeric(28);

            context.setPostBody(
                    _getBookingBodyAsMap(affiliateId, holdBooking, context.getTestConfig()),
                    RequestType.BOOKING
            );


        final Response response = ResponseSupplierFactory.create(
                context,
                bookingLink.getMethod(),
                RequestType.BOOKING).get();

        LoggingUtil.log("Affiliate ID used for booking : [" + affiliateId + "]");
        if (response.getStatusCode() == 200)
        {
            context.setBookingAffiliateId(affiliateId);
        }

        context.setResponse(RequestType.BOOKING, response);
        context.setBookingResponse(BookingResponseParser.parse(response));
    }

    private static Map<String, Object> _getBookingBodyAsMap(final String affiliateId, final boolean holdBooking, final TestConfig config)
    {

        Object[] customers = new Object[] {
                Customer.create(config.getCustomerInfoPath()).getAsMap()
        };

        final Object[] payments = new Object[] {
                CreditCard.create(config.getCreditCardInfoPath()).getAsMap()
        };
        //    roomList.add(customers);
        //    paymentList.add(payments);


        final ImmutableMap.Builder<String, Object> body = ImmutableMap.builder();
        body.put("affiliate_reference_id", affiliateId);
        body.put("hold", holdBooking);
        body.put("rooms", customers);
        body.put("payments", payments);

        return body.build();
    }

    public static void retrieveBooking(final TestContext context)
    {
        final Link bookingRetrieveLink = context.getBookingResponse().getLinks().get("retrieve");
        final Customer customer = Customer.create(context.getTestConfig().getCustomerInfoPath());

        context.addHeader("affiliate_reference_id", context.getBookingAffiliateId());
        context.addHeader("email", customer.getEmail());
        context.setApiPath(RequestType.RETRIEVE_BOOKING, bookingRetrieveLink.getHref());

        final Response response = ResponseSupplierFactory.create(
                context,
                bookingRetrieveLink.getMethod(),
                RequestType.RETRIEVE_BOOKING).get();

        context.setResponse(RequestType.RETRIEVE_BOOKING, response);
        context.setBookingRetrieveResponse(BookingRetrieveResponseParser.parse(response));
    }

    public static void cancelBooking(final TestContext context)
    {
        final Link cancelLink = context.getBookingRetrieveResponse()
                .getLinks()
                .get("cancel");

        context.setApiPath(RequestType.CANCEL_BOOKING, cancelLink.getHref());

        final Response response = ResponseSupplierFactory.create(
                context,
                cancelLink.getMethod(),
                RequestType.CANCEL_BOOKING).get();

        context.setResponse(RequestType.CANCEL_BOOKING, response);
    }

    public static void cancelRoomBooking(TestContext context)
    {
        if (context.getBookingRetrieveResponse() == null)
        {
            System.out.println("Error 1");
        }
        if (context.getBookingRetrieveResponse().getRooms() == null)
        {
            System.out.println("Error 2");
        }

        final Link cancelLink = context.getBookingRetrieveResponse()
                .getRooms()
                .get(0)
                .getLinks()
                .get("cancel");

        context.setApiPath(RequestType.CANCEL_BOOKING, cancelLink.getHref());

        final Response response = ResponseSupplierFactory.create(
                context,
                cancelLink.getMethod(),
                RequestType.CANCEL_BOOKING).get();

        context.setResponse(RequestType.CANCEL_BOOKING, response);
    }

    public static void resumeBooking(TestContext context)
    {
        final Link resumeBookingLink = context.getBookingResponse().getLinks().get("resume");
        context.setApiPath(RequestType.RETRIEVE_BOOKING, resumeBookingLink.getHref());

        final Response response = ResponseSupplierFactory.create(
                context,
                resumeBookingLink.getMethod(),
                RequestType.RETRIEVE_BOOKING).get();

        context.setResponse(RequestType.RESUME_BOOKING, response);
    }
}
