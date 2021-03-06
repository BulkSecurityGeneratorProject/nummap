package com.numlab.nummap.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Multimap;
import com.numlab.nummap.domain.enumerations.SocialNetworkEnum;

import java.util.List;

/**
 * Created by christo on 11/03/15.
 */
public class CompanyContactInformation {
    private String name;
    private String phone;
    private String email;
    private Address address;
    private String website;
    private List<SocialNetwork> socialNetworkList;

    @JsonCreator
    public CompanyContactInformation(
        @JsonProperty("name") String name,
        @JsonProperty("phone") String phone,
        @JsonProperty("email") String email,
        @JsonProperty("Address") Address address,
        @JsonProperty("website") String website,
        @JsonProperty("socialNetworkList") List<SocialNetwork> socialNetworkList) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.website = website;
        this.socialNetworkList = socialNetworkList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<SocialNetwork> getSocialNetworkList() {
        return socialNetworkList;
    }

    public void setSocialNetworkList(List<SocialNetwork> socialNetworkList) {
        socialNetworkList = socialNetworkList;
    }
}
