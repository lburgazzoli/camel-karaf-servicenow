/*
 * Salesforce DTO generated by camel-salesforce-maven-plugin
 * Generated on: Thu Nov 17 10:15:18 CET 2016
 */
package com.github.lburgazzoli.camel.salesforce.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Salesforce Enumeration DTO for picklist Status
 */
public enum Case_StatusEnum {

    // Closed
    CLOSED("Closed"),
    // Escalated
    ESCALATED("Escalated"),
    // New
    NEW("New"),
    // Working
    WORKING("Working");

    final String value;

    private Case_StatusEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static Case_StatusEnum fromValue(String value) {
        for (Case_StatusEnum e : Case_StatusEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException(value);
    }

}
