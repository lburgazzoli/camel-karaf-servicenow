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

package com.github.lburgazzoli.camel;

import java.util.Date;

import com.github.lburgazzoli.camel.salesforce.model.Case;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowIncidentRequest;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowIncidentResponse;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowUser;
import org.apache.camel.Exchange;

public class Bridge {

    public void caseToIncidentRequest(Exchange exchange) {
        Case source = exchange.getIn().getBody(Case.class);

        ServiceNowIncidentRequest incident = new ServiceNowIncidentRequest();
        incident.setReporter(source.getCreatedById());
        incident.setOpenedAt(Date.from(source.getCreatedDate().toInstant()));
        incident.setExternalId("SF-" + source.getId() + "-" + source.getCaseNumber());
        incident.setShortDescription(source.getSubject());
        incident.setDescription(source.getDescription());

        ServiceNowUser user = exchange.getIn().getHeader("ServiceNowUserId", ServiceNowUser.class);
        if (user != null) {
            incident.setCallerId(user.getSysId());
        }

        if (source.getOrigin() != null) {
            incident.setContactType(source.getOrigin().value());
        }

        if (source.getPriority() != null) {
            switch (source.getPriority()) {
            case HIGH:
                incident.setImpact(1);
                break;
            case MEDIUM:
                incident.setImpact(2);
                break;
            case LOW:
                incident.setImpact(3);
                break;
            }
        }

        if (source.getStatus() != null) {
            switch (source.getStatus()) {
            case CLOSED:
                incident.setState("Closed");
                break;
            case NEW:
                incident.setState("New");
                break;
            case WORKING:
                incident.setState("Active");
                break;
            case ESCALATED:
                incident.setState("Active");
                incident.setEscalation(1);
                break;
            }
        }


        if (source.getType() != null) {
            incident.setCategory(source.getType().value());
        }

        exchange.getIn().setBody(incident);
    }

    public void incidentToCase(Exchange exchange) {
        ServiceNowIncidentResponse source = exchange.getIn().getBody(ServiceNowIncidentResponse.class);
    }

    public void incidentToCaseId(Exchange exchange) {
        ServiceNowIncidentResponse source = exchange.getIn().getBody(ServiceNowIncidentResponse.class);
        String[] caseIds = source.getExternalId().split("-");

        if (caseIds.length == 3) {
            Case c = new Case();
            c.setId(caseIds[1]);
            c.setExtenralID__c("SN-" + source.getSysId() + "-" + source.getNumber());

            exchange.getIn().setBody(c);
        } else {
            throw new IllegalArgumentException("Invalid ExternalID: <" +  source.getExternalId() + ">");
        }
    }
}
