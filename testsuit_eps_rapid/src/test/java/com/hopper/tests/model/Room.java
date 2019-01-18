package com.hopper.tests.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room
{
    @JsonProperty("id")
    private String id;

    @JsonProperty("room_name")
    private String name;

    @JsonProperty("rates")
    private List<Rate> rates;

    public Room(){}

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public List<Rate> getRates()
    {
        return rates;
    }
}
