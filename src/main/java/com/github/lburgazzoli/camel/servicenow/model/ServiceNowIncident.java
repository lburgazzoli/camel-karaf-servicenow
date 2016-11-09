/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lburgazzoli.camel.servicenow.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceNowIncident {
    private String sysId;
    private String number;
    private String description;
    private String shortDescription;
    private String externalId;
    private String reporter;
    private String category;
    private Date openedAt;
    private int impact;
    private ServiceNowUser caller;
    private String contactType;

    public ServiceNowIncident() {
    }

    @JsonProperty("sys_id")
    public String getSysId() {
        return sysId;
    }

    @JsonProperty("sys_id")
    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    @JsonProperty("number")
    public String getNumber() {
        return number;
    }

    @JsonProperty("number")
    public void setNumber(String number) {
        this.number = number;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("short_description")
    public String getShortDescription() {
        return shortDescription;
    }

    @JsonProperty("short_description")
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @JsonProperty("u_external_id")
    public String getExternalId() {
        return externalId;
    }

    @JsonProperty("u_external_id")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @JsonProperty("reporter")
    public String getReporter() {
        return reporter;
    }

    @JsonProperty("reporter")
    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("opened_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:dd", timezone="CET")
    public Date getOpenedAt() {
        return openedAt;
    }

    @JsonProperty("opened_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:dd", timezone="CET")
    public void setOpenedAt(Date openedAt) {
        this.openedAt = openedAt;
    }

    @JsonProperty("impact")
    public int getImpact() {
        return impact;
    }

    @JsonProperty("impact")
    public void setImpact(int impact) {
        this.impact = impact;
    }

    @JsonProperty("caller_id")
    public String getCallerId() {
        return caller.getSysId();
    }

    @JsonProperty("caller_id")
    public void setCallerId(ServiceNowUser caller) {
        this.caller = caller;
    }

    @JsonProperty("contact_type")
    public String getContactType() {
        return contactType;
    }

    @JsonProperty("contact_type")
    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}

