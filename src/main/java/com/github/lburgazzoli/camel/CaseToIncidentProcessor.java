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

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.lburgazzoli.camel.salesforce.model.Case;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowIncident;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowIncidentImportRequest;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowUser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;

public class CaseToIncidentProcessor implements Processor {
    private static final ServiceNowIncident EMPTY_INCIDENT_RESP = new ServiceNowIncident();

    @Override
    public void process(Exchange exchange) throws Exception {
        Case source = exchange.getIn().getBody(Case.class);
        ServiceNowIncident oldIncident = getOldIncident(exchange);

        boolean toUpdate = false;

        ServiceNowIncidentImportRequest incident = new ServiceNowIncidentImportRequest();
        incident.setExternalId("SF-" + source.getId() + "-" + source.getCaseNumber());

        toUpdate |= setIfDifferent(oldIncident::getOpenedAt, () -> zonedDateTimeToDate(source.getCreatedDate()), incident::setOpenedAt);
        toUpdate |= setIfDifferent(oldIncident::getClosedAt, () -> zonedDateTimeToDate(source.getClosedDate()), incident::setClosedAt);
        toUpdate |= setIfDifferent(oldIncident::getShortDescription, source::getSubject, incident::setShortDescription);
        toUpdate |= setIfDifferent(oldIncident::getDescription, source::getDescription, incident::setDescription);

        ServiceNowUser user = exchange.getIn().getHeader("ServiceNowUserId", ServiceNowUser.class);
        if (user != null) {
            toUpdate |= setIfDifferent(oldIncident::getCallerId, user::getSysId, incident::setCallerId);
        }

        if (source.getOrigin() != null) {
            toUpdate |= setIfDifferent(oldIncident::getContactType, () -> source.getOrigin().value().toLowerCase(), incident::setContactType);
        }

        if (source.getPriority() != null) {
            switch (source.getPriority()) {
            case HIGH:
                toUpdate |= setIfDifferent(oldIncident::getImpact, () -> 1, incident::setImpact);
                break;
            case MEDIUM:
                toUpdate |= setIfDifferent(oldIncident::getImpact, () -> 2, incident::setImpact);
                break;
            case LOW:
                toUpdate |= setIfDifferent(oldIncident::getImpact, () -> 3, incident::setImpact);
                break;
            }
        }

        if (source.getStatus() != null) {
            switch (source.getStatus()) {
            case CLOSED:
                toUpdate |= setIfDifferent(oldIncident::getState, () -> 7, incident::setState);
                toUpdate |= setIfDifferent(oldIncident::getEscalation, () -> 0, incident::setEscalation);
                break;
            case NEW:
                toUpdate |= setIfDifferent(oldIncident::getState, () -> 1, incident::setState);
                toUpdate |= setIfDifferent(oldIncident::getEscalation, () -> 0, incident::setEscalation);
                break;
            case WORKING:
                toUpdate |= setIfDifferent(oldIncident::getState, () -> 2, incident::setState);
                toUpdate |= setIfDifferent(oldIncident::getEscalation, () -> 0, incident::setEscalation);
                break;
            case ESCALATED:
                toUpdate |= setIfDifferent(oldIncident::getState, () -> 2, incident::setState);
                toUpdate |= setIfDifferent(oldIncident::getEscalation, () -> 1, incident::setEscalation);
                break;
            }
        }

        if (source.getType() != null) {
            toUpdate |= setIfDifferent(oldIncident::getCategory, () -> source.getType().value().toLowerCase(), incident::setCategory);
        }

        exchange.getIn().setHeader("ServiceNowUpdate", toUpdate);
        exchange.getIn().setBody(incident);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean setIfDifferent(Supplier<T> target, Supplier<T> source, Consumer<T> setter) {
        T t = target != null ? target.get() : null;
        T s = source != null ? source.get() : null;

        if (s instanceof String) {
            s = (T) StringUtils.trimToNull((String)s);
        }
        if (t instanceof String) {
            t = (T)StringUtils.trimToNull((String)t);
        }

        if (!Objects.equals(t, s)) {
            setter.accept(s);
            return true;
        } else {
            return false;
        }
    }

    private ServiceNowIncident getOldIncident(Exchange exchange) {
        return exchange.getIn().getHeader("ServiceNowOldIncident", EMPTY_INCIDENT_RESP, ServiceNowIncident.class);
    }

    private Date zonedDateTimeToDate(ZonedDateTime zdt) {
        return zdt != null ? Date.from(zdt.toInstant()) : null;
    }
}
