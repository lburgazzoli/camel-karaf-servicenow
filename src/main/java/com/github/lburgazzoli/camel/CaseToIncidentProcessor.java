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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.lburgazzoli.camel.salesforce.model.Case;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowIncident;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowUser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaseToIncidentProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaseToIncidentProcessor.class);
    private static final ServiceNowIncident EMPTY_INCIDENT_RESP = new ServiceNowIncident();

    @Override
    public void process(Exchange exchange) throws Exception {
        Case source = exchange.getIn().getBody(Case.class);
        ServiceNowIncident oldIncident = getOldIncidentFromHeader(exchange);

        boolean toUpdate = false;

        ServiceNowIncident incident = new ServiceNowIncident();
        incident.setCorrelationId("SF-" + source.getId() + "-" + source.getCaseNumber());

        toUpdate |= setIfDifferent("OpenedAt", oldIncident::getOpenedAt, () -> dateTimeToDate(source.getCreatedDate()), incident::setOpenedAt);
        toUpdate |= setIfDifferent("ClosedAt", oldIncident::getClosedAt, () -> dateTimeToDate(source.getClosedDate()), incident::setClosedAt);
        toUpdate |= setIfDifferent("ShortDescription", oldIncident::getShortDescription, source::getSubject, incident::setShortDescription);
        toUpdate |= setIfDifferent("Description", oldIncident::getDescription, source::getDescription, incident::setDescription);

        ServiceNowUser user = exchange.getIn().getHeader("ServiceNowUserId", ServiceNowUser.class);
        if (user != null) {
            toUpdate |= setIfDifferent("CallerId", oldIncident::getCallerId, user::getSysId, incident::setCallerId);
        }

        if (source.getOrigin() != null) {
            toUpdate |= setIfDifferent("ContactType", oldIncident::getContactType, () -> source.getOrigin().value().toLowerCase(), incident::setContactType);
        }

        if (source.getPriority() != null) {
            switch (source.getPriority()) {
            case HIGH:
                toUpdate |= setIfDifferent("Impact", oldIncident::getImpact, () -> 1, incident::setImpact);
                break;
            case MEDIUM:
                toUpdate |= setIfDifferent("Impact", oldIncident::getImpact, () -> 2, incident::setImpact);
                break;
            case LOW:
                toUpdate |= setIfDifferent("Impact", oldIncident::getImpact, () -> 3, incident::setImpact);
                break;
            }
        }

        if (source.getStatus() != null) {
            switch (source.getStatus()) {
            case CLOSED:
                toUpdate |= setIfDifferent("State", oldIncident::getState, () -> 7, incident::setState);
                toUpdate |= setIfDifferent("Escalation", oldIncident::getEscalation, () -> 0, incident::setEscalation);
                break;
            case NEW:
                toUpdate |= setIfDifferent("State", oldIncident::getState, () -> 1, incident::setState);
                toUpdate |= setIfDifferent("Escalation", oldIncident::getEscalation, () -> 0, incident::setEscalation);
                break;
            case WORKING:
                toUpdate |= setIfDifferent("State", oldIncident::getState, () -> 2, incident::setState);
                toUpdate |= setIfDifferent("Escalation", oldIncident::getEscalation, () -> 0, incident::setEscalation);
                break;
            case ESCALATED:
                toUpdate |= setIfDifferent("State", oldIncident::getState, () -> 2, incident::setState);
                toUpdate |= setIfDifferent("Escalation", oldIncident::getEscalation, () -> 1, incident::setEscalation);
                break;
            }
        }

        if (source.getType() != null) {
            toUpdate |= setIfDifferent("Category", oldIncident::getCategory, () -> source.getType().value().toLowerCase(), incident::setCategory);
        }

        exchange.getIn().setHeader("ServiceNowUpdate", toUpdate);
        exchange.getIn().setBody(incident);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean setIfDifferent(String fieldName, Supplier<T> oldValue, Supplier<T> newValue, Consumer<T> setter) {
        T o = oldValue != null ? oldValue.get() : null;
        T n = newValue != null ? newValue.get() : null;

        if (o instanceof String) {
            o = (T)StringUtils.trimToNull((String)o);
        }
        if (n instanceof String) {
            n = (T) StringUtils.trimToNull((String)n);
        }

        if (!Objects.equals(o, n)) {
            LOGGER.debug("Update {} -> old: {}, new: {}", fieldName, o, n);
            setter.accept(n);
            return true;
        } else {
            return false;
        }
    }

    private ServiceNowIncident getOldIncidentFromHeader(Exchange exchange) {
        return exchange.getIn().getHeader("ServiceNowOldIncident", EMPTY_INCIDENT_RESP, ServiceNowIncident.class);
    }

    private Date dateTimeToDate(DateTime zdt) {
        return zdt != null ? zdt.withZone(DateTimeZone.UTC).toDate() : null;
    }
}
