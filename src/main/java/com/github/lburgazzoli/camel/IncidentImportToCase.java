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

import com.github.lburgazzoli.camel.salesforce.model.Case;
import com.github.lburgazzoli.camel.servicenow.model.ServiceNowImportSetResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class IncidentImportToCase implements Processor {
    public void process(Exchange exchange) throws Exception{
        ServiceNowImportSetResponse response = exchange.getIn().getBody(ServiceNowImportSetResponse.class);
        String salesforceId = exchange.getIn().getHeader("SalesForceId", String.class);

        if (salesforceId != null && response != null && response.getSysId() != null && response.getDisplayValue() != null) {
            Case c = new Case();
            c.setId(salesforceId);
            c.setInternalID__c("SN-" + response.getSysId() + "-" + response.getDisplayValue());

            exchange.getIn().setBody(c);
        } else {
            throw new IllegalArgumentException("Invalid SalesforceID: <" + salesforceId + "> or ImportSet response: <" + response + ">");
        }
    }
}
