package com.demo.springbootbiometrico.models;

//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.JsonProperty;
import com.machinezoo.sourceafis.FingerprintTemplate;

//@JsonAutoDetect
public class UserDetails {
    // @JsonProperty
    public int id;
    // @JsonProperty
    public String name;
    // @JsonProperty
    public FingerprintTemplate template;
    
    public UserDetails(int id, String name, FingerprintTemplate template) {
        this.id = id;
        this.name = name;
        this.template = template;
    }
}
