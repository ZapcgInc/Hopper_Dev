package com.hopper.tests.util.validations;

import com.hopper.tests.constants.RequestType;
import com.hopper.tests.model.TestContext;
import io.restassured.response.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import javax.validation.constraints.NotNull;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Util class for Response Validations
 */
public class ResponseValidationUtil {
    public static void validateHTTPResponseCode(@NotNull final Response restResponse, final int expectedCode) {
        final int responseCode = restResponse.getStatusCode();
        final String errorMessage = "Expected response code : " + expectedCode + "and actual response code : " + responseCode + "are not matching";
        Assert.assertTrue(errorMessage, expectedCode == responseCode);
    }


    public static void validateResponseBody(final Response restResponse, final Map<String, String> expectedResponseMap, final String field) {
        final Map<String, String> fieldResponseMap = restResponse.jsonPath().get(field);

        for (String key : expectedResponseMap.keySet()) {
            final String actualValue = fieldResponseMap.get(key);
            final String expectedValue = expectedResponseMap.get(key);
            final String errorMessage = "For field" + field + "Expected value : " + expectedValue + "and actual value : " + actualValue + "are not matching";

            Assert.assertEquals(errorMessage, expectedValue, actualValue);
        }
    }

    public static void validateArraySize(final Response restResponse, final String field, final int expectedSize) {
        final List<Object> fieldValues = restResponse.jsonPath().get(field);
        final String errorMessage = "Expected Array size for element \"" + field + " is: " + expectedSize + "and actual size is : " + fieldValues.size();
        Assert.assertTrue(errorMessage, expectedSize == fieldValues.size());
    }

    public static void validateArraySizeAv(final Response restResponse, String field2, String minValue, String maxValue) {
        switch (field2) {
            case "available_rooms":
                validateAvailableRooms(restResponse, maxValue);
                break;
        }


    }

    public static void validateNodeforValues(final Response restResponse, final String node, List<String> expectedValues) throws ParseException {
        switch (node) {
            case "occupancy_SHOPPING":
                validateOccupancy(restResponse, expectedValues);
                break;
            case "occupancy_PREBOOKING":
                validateOccupancyPreCheck(restResponse,expectedValues);
                break;
            case "nighthly_SHOPPING":
                validateNightlyArrSize(restResponse,expectedValues.get(0));
                break;
            case "nightly_PRE_BOOKING":
                validateNightlyArrSizePreCheck(restResponse, expectedValues.get(0));
                break;
            case "currency_SHOPPING":
                validateCurrencyCode(restResponse,expectedValues.get(0));
                break;
        }
    }

    private static void validateStayNodePreBooking(Response restResponse, String expectedValues) {
        String[] allowedTypes = expectedValues.split("\\|");
        HashMap<String, HashMap> roomPriceCheckMap = restResponse.jsonPath().get(".");
        HashMap<String, HashMap> occupancies = roomPriceCheckMap.get("occupancies");
        for (Map.Entry<String, HashMap> occupancy : occupancies.entrySet()) {

            HashMap<String,ArrayList> roomRates = occupancy.getValue();
            ArrayList<HashMap> stayNodeList =roomRates.get("stay_node");
            final String errorMessage = "Expected Stay node is" + expectedValues + "and actual values are : " + stayNodeList;
            if(stayNodeList!=null) {
                for (HashMap stayNode : stayNodeList) {
                    String type = (String) stayNode.get("type");
                    Assert.assertTrue(errorMessage, Arrays.stream(allowedTypes).anyMatch(type::equals));
                }
            }
        }

    }

    private static void validateOccupancyPreCheck(Response restResponse, List<String> expectedValues) {
        HashMap<String, HashMap> roomPriceCheckMap = restResponse.jsonPath().get(".");
        HashMap<String, HashMap> occupancies = roomPriceCheckMap.get("occupancies");
        List<String> responseValues = new ArrayList<String>(occupancies.keySet());
        final String errorMessage = "Expected Occupancies is" + expectedValues + "and actual values are : " + responseValues;
        Assert.assertTrue(errorMessage, CollectionUtils.isEqualCollection(responseValues, expectedValues));

    }

    private static void validateNightlyArrSizePreCheck(Response restResponse, String expectedValue) {
        HashMap<String, HashMap> roomPriceCheckMap = restResponse.jsonPath().get(".");
        HashMap<String, HashMap> occupancies = roomPriceCheckMap.get("occupancies");
        for (Map.Entry<String, HashMap> occupancy : occupancies.entrySet()) {
            HashMap roomRates = occupancy.getValue();
            ArrayList list = (ArrayList) roomRates.get("nightly");
            Assert.assertTrue(list.size() == Integer.parseInt(expectedValue));
        }
    }

    public static void validateFieldValueNotEqualTo(final Response restResponse, String field, String value) {
        switch (field) {
            case "price_check_href":
                validateHrefPriceCheck(restResponse);
            case "payment_option_href":
                validateHrefPaymentOption(restResponse);
            case "links.deposit_policies":
                validateDepositPolicies(restResponse);
        }
    }

    private static void validateOccupancy(final Response restResponse, final List<String> expectedValues) {
        ArrayList<LinkedHashMap> responseAsList = restResponse.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {
            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {
                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    LinkedHashMap<String, Object> occupancies = (LinkedHashMap) rate.get("occupancies");
                    List<String> responseValues = new ArrayList<String>(occupancies.keySet());
                    final String errorMessage = "Expected Occupancies for rate id \"" + roomId + " is: " + expectedValues + "and actual values are : " + responseValues;
                    Assert.assertTrue(errorMessage, CollectionUtils.isEqualCollection(responseValues, expectedValues));
                }

            }

        }
    }


    public static void validateResponseBodyForNode(String node, Map<String, String> paramMap, Response response) throws ParseException {
        switch (node) {
            case "cancel_policies_SHOPPING":
                validateCancelPoliciesForRefundableRates(paramMap, response);
                break;
            case "amenities_SHOPPING":
                validateAmenities(response);
                break;

        }
    }


    private static void validateCurrencyCode(Response response, String expectedValue) {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);

        for (LinkedHashMap<String, Object> responseMap : responseAsList) {

            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {

                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {

                    LinkedHashMap<String, LinkedHashMap> occupancies = (LinkedHashMap) rate.get("occupancies");
                    for (Map.Entry<String, LinkedHashMap> occupancy : occupancies.entrySet()) {
                        LinkedHashMap<String, Object> roomRates = occupancy.getValue();
                        ArrayList<ArrayList> nightlyList = (ArrayList) roomRates.get("nightly");
                        for (ArrayList<LinkedHashMap> nightly : nightlyList) {

                            for (LinkedHashMap<String, String> map : nightly) {

                                String currency = map.get("currency");
                                if (!expectedValue.equals(currency))
                                    Assert.fail("Response currency in nightly does not match with requested currency for room_id: " + roomId);
                            }
                        }

                        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> totals = (LinkedHashMap) roomRates.get("totals");

                        String currencyInclusive = totals.get("inclusive").get("billable_currency").get("currency");
                        if (!expectedValue.equals(currencyInclusive)) {
                            Assert.fail("Response currency in totals inclusive does not match with requested currency for room_id: " + roomId);
                        }
                        String currencyExclusive = totals.get("exclusive").get("billable_currency").get("currency");
                        if (!expectedValue.equals(currencyExclusive)) {
                            Assert.fail("Response currency in totals exclusive does not match with requested currency for room_id: " + roomId);
                        }
                        if (totals.get("strikethrough") != null) {
                            String currencyStrikethrough = totals.get("strikethrough").get("billable_currency").get("currency");
                            if (!expectedValue.equals(currencyStrikethrough)) {
                                Assert.fail("Response currency in totals strikethrough does not match with requested currency for room_id: " + roomId);
                            }
                        }
                        if (totals.get("marketing_fee") != null) {
                            String currencyMarketing = totals.get("marketing_fee").get("billable_currency").get("currency");
                            if (!expectedValue.equals(currencyMarketing)) {
                                Assert.fail("Response currency in totals marketing does not match with requested currency for room_id: " + roomId);
                            }
                        }
                        if (totals.get("minimum_selling_price") != null) {
                            String currencySP = totals.get("minimum_selling_price").get("billable_currency").get("currency");
                            if (!expectedValue.equals(currencySP)) {
                                Assert.fail("Response currency in totals selling_price does not match with requested currency for room_id: " + roomId);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void validateTotalPrice(Response response) {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        DecimalFormat df = new DecimalFormat("###.##");
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {

            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {

                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {

                    LinkedHashMap<String, LinkedHashMap> occupancies = (LinkedHashMap) rate.get("occupancies");
                    for (Map.Entry<String, LinkedHashMap> occupancy : occupancies.entrySet()) {
                        Double baseRate = 0.0;
                        Double taxRate = 0.0;
                        Double extraPersonfee = 0.0;
                        LinkedHashMap<String, Object> roomRates = occupancy.getValue();
                        ArrayList<ArrayList> list = (ArrayList) roomRates.get("nightly");
                        for (ArrayList<LinkedHashMap> n : list) {

                            for (LinkedHashMap map : n) {

                                String value = (String) map.get("value");
                                if (map.get("type").equals("base_rate"))
                                    baseRate = baseRate + Double.parseDouble(value);
                                else if (map.get("type").equals("extra_person_fee"))
                                    extraPersonfee = extraPersonfee + Double.parseDouble(value);
                                else
                                    taxRate = taxRate + Double.parseDouble(value);
                            }
                        }

                        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> j = (LinkedHashMap) roomRates.get("totals");

                        Double billableInclusiveTotal = Double.parseDouble(j.get("inclusive").get("billable_currency").get("value"));
                        Double expectedBillableIncTotal = Double.parseDouble(df.format(baseRate + taxRate + extraPersonfee));
                        Double billableExclusiveTotal = Double.parseDouble(j.get("exclusive").get("billable_currency").get("value"));
                        Double expectedBillableExTotal = Double.parseDouble(df.format(baseRate + extraPersonfee));
                        if (!billableInclusiveTotal.equals(expectedBillableIncTotal) ||
                                !billableExclusiveTotal.equals(expectedBillableExTotal)) {

                            Assert.fail("Expected totals does not match billableTotals for room_id: " + roomId);

                        }
                    }

                }
            }
        }
    }

    private static void validateNightlyArrSize(Response response, String expectedValue) {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {

            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {

                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {

                    LinkedHashMap<String, LinkedHashMap> occupancies = (LinkedHashMap) rate.get("occupancies");
                    for (Map.Entry<String, LinkedHashMap> e : occupancies.entrySet()) {

                        LinkedHashMap<String, Object> s = e.getValue();
                        ArrayList<ArrayList> list = (ArrayList) s.get("nightly");
                        Assert.assertTrue(list.size()==Integer.parseInt(expectedValue));
                    }
                }
            }
        }

    }

    private static void validateStayNode(Response response, String expectedValues) {
        String[] allowedTypes = expectedValues.split("\\|");
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {

            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {

                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    LinkedHashMap<String, LinkedHashMap> occupancies = (LinkedHashMap) rate.get("occupancies");
                    for (Map.Entry<String, LinkedHashMap> occupancy : occupancies.entrySet()) {
                        LinkedHashMap<String, Object> s = occupancy.getValue();
                        ArrayList<LinkedHashMap> stayNodes = (ArrayList) s.get("stay");
                        if (stayNodes != null) {
                            for (LinkedHashMap stayNode : stayNodes) {
                                String type = (String) stayNode.get("type");
                                boolean contains = Arrays.stream(allowedTypes).anyMatch(type::equals);
                                if (!contains) {
                                    Assert.fail("type: " + type + " is invalid for room_id: " + roomId);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private static void validateDepositPolicies(Response response) {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {

            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {

                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {

                    Boolean depositRequired = (Boolean) rate.get("deposit_required");
                    if (depositRequired) {

                        LinkedHashMap<String, LinkedHashMap> linksMap = (LinkedHashMap) rate.get("links");
                        LinkedHashMap<String, String> depositPolicies = linksMap.get("deposit_policies");
                        if (depositPolicies != null && StringUtils.isEmpty(depositPolicies.get("href"))) {

                            Assert.fail("link should be present for deposit policies when deposit_required is " +
                                    "true for room_id: " + roomId);
                        }
                    }
                }
            }
        }
    }


    private static void validateFencedDeal(Response response, String expectedValue) {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {
            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {
                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    Boolean value = (Boolean) rate.get("fenced_deal");
                    if (value != Boolean.valueOf(expectedValue)) {
                        Assert.fail("fenced_deal is true for roomId: " + roomId);
                    }
                }
            }
        }
    }

    private static void validateAmenities(Response response) {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {
            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {
                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    ArrayList<LinkedHashMap> amenities = (ArrayList<LinkedHashMap>) rate.get("amenities");
                    if (!CollectionUtils.isEmpty(amenities)) {
                        for (LinkedHashMap<String, Object> amenity : amenities) {
                            Integer id = (Integer) amenity.get("id");
                            String name = (String) amenity.get("name");
                            //TODO: Make it better
                            if ((id != null && StringUtils.isEmpty(name)) || (id == null && StringUtils.isNotEmpty(name))) {
                                Assert.fail("amenity ID and description both should be present or both should be absent for a valid response.");
                            }
                        }
                    }
                }
            }
        }
    }

    private static void validateCancelPoliciesForRefundableRates(Map<String, String> paramsMap, Response response) throws ParseException {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {
            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {
                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    boolean value = (boolean) rate.get("refundable");
                    if (value) {
                        String checkin = paramsMap.get("checkin");
                        String checkout = paramsMap.get("checkout");
                        ArrayList<LinkedHashMap> cancelPanaltyList = (ArrayList) rate.get("cancel_penalties");
                        for (LinkedHashMap<String, String> t : cancelPanaltyList) {
                            String startDate = t.get("start");
                            String endDate = t.get("end");
                            if (!validateStartEndDate(checkin, checkout, startDate, endDate)) {
                                Assert.fail("cancel policy start and end date are not within check in and check " +
                                        "out dates for roomId: " + roomId);
                            }

                        }
                    }
                }
            }
        }
    }

    private static boolean validateStartEndDate(String checkin, String checkout, String startDate, String endDate) throws ParseException {
        boolean flag = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (sdf.parse(startDate).before(sdf.parse(checkin)) && sdf.parse(endDate).before(sdf.parse(checkout))) {
            flag = true;
        }
        return flag;
    }

    private static void validateHrefPriceCheck(Response restResponse) {
        ArrayList<LinkedHashMap> responseAsList = restResponse.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {
            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {
                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    ArrayList<LinkedHashMap> bedGroupsList = (ArrayList) rate.get("bed_groups");
                    for (LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> bedgroup : bedGroupsList) {
                        String hrefLink = bedgroup.get("links").get("price_check").get("href");
                        if (StringUtils.isEmpty(hrefLink)) {
                            Assert.fail("hrefLink empty for roomId" + roomId);
                        }
                    }
                }
            }
        }
    }

    private static void validateHrefPaymentOption(Response response) {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {
            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {
                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    LinkedHashMap<String, LinkedHashMap> linksMap = (LinkedHashMap) rate.get("links");
                    LinkedHashMap<String, String> paymentOptionsMap = linksMap.get("payment_options");
                    String hrefLink = paymentOptionsMap.get("href");
                    if (StringUtils.isEmpty(hrefLink)) {
                        Assert.fail("hrefLink empty for roomId" + roomId);
                    }
                }
            }
        }
    }

    private static void validateMerchantOfRecord(Response response, String expectedValues) {
        String[] expectedArr = expectedValues.split("|");
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {
            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {
                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    String value = (String) rate.get("merchant_of_record");

                    if (!Arrays.stream(expectedArr).anyMatch(value::equals)) {
                        Assert.fail(" merchant record field does not match any of the expected values for roomId: " + roomId);
                    }
                }
            }
        }
    }

    private static void validateAvailableRooms(Response restResponse, String maxVal) {
        ArrayList<LinkedHashMap> responseAsList = restResponse.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {
            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {
                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {
                    int numAvailableRooms = (Integer) rate.get("available_rooms");
                    if (numAvailableRooms <= 0 || numAvailableRooms > Integer.parseInt(maxVal)) {
                        Assert.fail("Number of available rooms in the response is invalid for room_id: " + roomId);
                    }
                }
            }
        }
    }

  /*  private static void validatePropertyId(TestContext testContext)
    {
        ArrayList<LinkedHashMap> responseAsList = testContext.getResponse(RequestType.SHOPPING).as(ArrayList.class);
        List<String> requestPropIds = new ArrayList<>();
        ArrayList<String> responsePropIds = new ArrayList<>();
        for (LinkedHashMap response : responseAsList)
        {
            String value = response.get("property_id").toString();
            responsePropIds.add(value);
        }
        if (testContext.getParamsWithMultipleValues(RequestType.SHOPPING).get("property_id") != null)
        {
            requestPropIds.addAll(testContext.getParamsWithMultipleValues(RequestType.SHOPPING).get("property_id"));
        }
        if (testContext.getParams(RequestType.SHOPPING).get("property_id") != null)
        {
            requestPropIds.add(testContext.getParams(RequestType.SHOPPING).get("property_id"));
        }
        if (responsePropIds.size() <= requestPropIds.size())
        {
            requestPropIds.forEach(id->{
                if(!requestPropIds.contains(id))
                    Assert.fail("The propertyId: "+id+ " is not present in the request");
            });
        }
        else
        {
            Assert.fail("Property Ids in the response is more than the requested response");
        }
    })*/


    public static void validateFieldValueBelongsToExpectedValues(Response response, String field, String expectedArr) {
        switch (field) {
            case "merchant_of_record_SHOPPING":
                validateMerchantOfRecord(response, expectedArr);
                break;
            case "nightly_type_SHOPPING":
                validateNightlyTypes(response, expectedArr);
                break;
            case "stay_node_SHOPPING":
                validateStayNode(response, expectedArr);
                break;
            case "stay_node_PRE_BOOKING":
                validateStayNodePreBooking(response,expectedArr);
                break;
        }
    }

    private static void validateNightlyTypes(Response response, String expectedArr) {
        String[] allowedTypes = expectedArr.split("\\|");
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);
        for (LinkedHashMap<String, Object> responseMap : responseAsList) {

            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {

                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {

                    LinkedHashMap<String, LinkedHashMap> occupancies = (LinkedHashMap) rate.get("occupancies");
                    for (Map.Entry<String, LinkedHashMap> e : occupancies.entrySet()) {

                        LinkedHashMap<String, Object> s = e.getValue();
                        ArrayList<ArrayList> list = (ArrayList) s.get("nightly");
                        for (ArrayList<LinkedHashMap> n : list) {

                            for (LinkedHashMap map : n) {
                                String type = (String) map.get("type");
                                boolean contains = Arrays.stream(allowedTypes).anyMatch(type::equals);
                                if (!contains) {
                                    Assert.fail("type: " + type + " is invalid for room_id: " + roomId);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public static void validateFieldValueAsExpectedValue(String field, Response response, String expectedValue) {
        switch (field) {
            case "fenced_deal":
                validateFencedDeal(response, expectedValue);
                break;
            case "currency":
                validateCurrencyCode(response, expectedValue);
                break;
        }
    }

    public static void validateNodeInResponseBody(String field, Response response) {
        switch (field) {
            case "totals_SHOPPING":
                validateTotalPrice(response);
                break;
            case "totals_PRE_BOOKING":
                validateTotalPricePreBooking(response);
        }
    }

    private static void validateTotalPricePreBooking(Response response) {

        DecimalFormat df = new DecimalFormat("###.##");
        HashMap<String, HashMap> roomPriceCheckMap = response.jsonPath().get(".");
        HashMap<String, HashMap> occupancies = roomPriceCheckMap.get("occupancies");
        for (Map.Entry<String, HashMap> occupancy : occupancies.entrySet()) {
            Double baseRate = 0.0;
            Double taxRate = 0.0;
            Double extraPersonfee = 0.0;
            Double adjustment = 0.0;
            HashMap roomRates = occupancy.getValue();
            ArrayList<ArrayList> list = (ArrayList) roomRates.get("nightly");
            for (ArrayList<HashMap> n : list) {
                for (HashMap map : n) {

                    String value = (String) map.get("value");
                    if (map.get("type").equals("base_rate"))
                        baseRate = baseRate + Double.parseDouble(value);
                    else if (map.get("type").equals("extra_person_fee"))
                        extraPersonfee = extraPersonfee + Double.parseDouble(value);
                    else if (map.get("type").equals("adjustment"))
                        adjustment = adjustment + Double.parseDouble(value);
                    else
                        taxRate = taxRate + Double.parseDouble(value);
                }
            }

            HashMap<String, HashMap<String, HashMap<String, String>>> j = (HashMap) roomRates.get("totals");
            Double billableInclusiveTotal = Double.parseDouble(j.get("inclusive").get("billable_currency").get("value"));
            Double expectedBillableIncTotal = Double.parseDouble(df.format(baseRate + taxRate + extraPersonfee + adjustment));
            Double billableExclusiveTotal = Double.parseDouble(j.get("exclusive").get("billable_currency").get("value"));
            Double expectedBillableExTotal = Double.parseDouble(df.format(baseRate + extraPersonfee + adjustment));
            if (!billableInclusiveTotal.equals(expectedBillableIncTotal) ||
                    !billableExclusiveTotal.equals(expectedBillableExTotal))
            {

                Assert.fail("Expected totals does not match billableTotals: ");

            }
        }
    }







    public static void validateFieldsInResponseBody(String field1, Response response, String field2) {
        switch (field1){
            case "billable_currency":
                validateBillableCurrency(response,field2);
                break;
        }
    }

    private static void validateBillableCurrency(Response response,String field) {
        ArrayList<LinkedHashMap> responseAsList = response.as(ArrayList.class);

        for (LinkedHashMap<String, Object> responseMap : responseAsList) {

            ArrayList<LinkedHashMap> roomsArr = (ArrayList<LinkedHashMap>) responseMap.get("rooms");
            for (LinkedHashMap<String, Object> room : roomsArr) {

                ArrayList<LinkedHashMap> rateList = (ArrayList<LinkedHashMap>) room.get("rates");
                String roomId = (String) room.get("id");
                for (LinkedHashMap<String, Object> rate : rateList) {

                    LinkedHashMap<String, LinkedHashMap> occupancies = (LinkedHashMap) rate.get("occupancies");
                    for (Map.Entry<String, LinkedHashMap> occupancy : occupancies.entrySet()) {

                        LinkedHashMap<String, Object> roomRates = occupancy.getValue();
                        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> totals = (LinkedHashMap) roomRates.get("totals");
                        String currencyInclusiveBillable = totals.get("inclusive").get("billable_currency").get("currency");
                        String currencyInclusiveRequested = totals.get("inclusive").get(field).get("currency");
                        if (!currencyInclusiveBillable.equals(currencyInclusiveRequested)) {

                            Assert.fail("Billable currency in totals inclusive does not match with requested currency for room_id: " + roomId);

                        }

                        String currencyExclusiveBillable = totals.get("exclusive").get("billable_currency").get("currency");
                        String currencyExclusiveRequested = totals.get("exclusive").get(field).get("currency");
                        if (!currencyExclusiveBillable.equals(currencyExclusiveRequested)) {

                            Assert.fail("Billable currency in totals exclusive does not match with requested currency for room_id: " + roomId);

                        }
                    }
                }
            }
        }
    }
}
