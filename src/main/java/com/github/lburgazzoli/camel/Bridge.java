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
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowIncident;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowUser;
import org.apache.camel.Exchange;

public class Bridge {

    public void caseToIncident(Exchange exchange) {
        Case source = exchange.getIn().getBody(Case.class);

        ServiceNowIncident incident = new ServiceNowIncident();
        incident.setReporter(source.getCreatedById());
        incident.setOpenedAt(Date.from(source.getCreatedDate().toInstant()));
        incident.setExternalId("SF-" + source.getId() + "-" + source.getCaseNumber());
        incident.setShortDescription(source.getSubject());
        incident.setDescription(source.getDescription());
        incident.setCallerId(exchange.getIn().getHeader("ServiceNowUserId", ServiceNowUser.class));

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
        } else {
            incident.setImpact(3);
        }

        if (source.getType() != null) {
            incident.setCategory(source.getType().value());
        }

        exchange.getIn().setBody(incident);
    }

    public void incidentToCase(Exchange exchange) {
        ServiceNowIncident source = exchange.getIn().getBody(ServiceNowIncident.class);
    }
}
