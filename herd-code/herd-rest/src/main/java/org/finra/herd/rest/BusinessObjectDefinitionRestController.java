/*
* Copyright 2015 herd contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.finra.herd.rest;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.finra.herd.model.api.xml.BusinessObjectDefinition;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionCreateRequest;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionKey;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionKeys;
import org.finra.herd.model.api.xml.BusinessObjectDefinitionUpdateRequest;
import org.finra.herd.model.dto.SecurityFunctions;
import org.finra.herd.service.BusinessObjectDefinitionService;
import org.finra.herd.ui.constants.UiConstants;

/**
 * The REST controller that handles business object definition REST requests.
 */
@RestController
@RequestMapping(value = UiConstants.REST_URL_BASE, produces = {"application/xml", "application/json"})
@Api(tags = "Business Object Definition")
public class BusinessObjectDefinitionRestController extends HerdBaseController
{
    @Autowired
    private BusinessObjectDefinitionService businessObjectDefinitionService;

    /**
     * Creates a new business object definition.
     * <p>Requires WRITE permission on namespace</p>
     *
     * @param request the information needed to create the business object definition.
     *
     * @return the created business object definition.
     */
    @RequestMapping(value = "/businessObjectDefinitions", method = RequestMethod.POST, consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_BUSINESS_OBJECT_DEFINITIONS_POST)
    public BusinessObjectDefinition createBusinessObjectDefinition(@RequestBody BusinessObjectDefinitionCreateRequest request)
    {
        return businessObjectDefinitionService.createBusinessObjectDefinition(request);
    }

    /**
     * Updates an existing business object definition by key.
     * <p>Requires WRITE permission on namespace</p>
     *
     * @param namespace the namespace code
     * @param businessObjectDefinitionName the name of the business object definition to update
     * @param request the information needed to update the business object definition
     *
     * @return the updated business object definition
     */
    @RequestMapping(value = "/businessObjectDefinitions/namespaces/{namespace}/businessObjectDefinitionNames/{businessObjectDefinitionName}",
        method = RequestMethod.PUT,
        consumes = {"application/xml", "application/json"})
    @Secured(SecurityFunctions.FN_BUSINESS_OBJECT_DEFINITIONS_PUT)
    public BusinessObjectDefinition updateBusinessObjectDefinition(@PathVariable("namespace") String namespace,
        @PathVariable("businessObjectDefinitionName") String businessObjectDefinitionName, @RequestBody BusinessObjectDefinitionUpdateRequest request)
    {
        BusinessObjectDefinitionKey businessObjectDefinitionKey = new BusinessObjectDefinitionKey(namespace, businessObjectDefinitionName);
        return businessObjectDefinitionService.updateBusinessObjectDefinition(businessObjectDefinitionKey, request);
    }

    /**
     * Gets an existing business object definition by key.
     * <p>Requires READ permission on namespace</p>
     *
     * @param namespace the namespace code
     * @param businessObjectDefinitionName the business object definition name
     *
     * @return the retrieved business object definition.
     */
    @RequestMapping(value = "/businessObjectDefinitions/namespaces/{namespace}/businessObjectDefinitionNames/{businessObjectDefinitionName}",
        method = RequestMethod.GET)
    @Secured(SecurityFunctions.FN_BUSINESS_OBJECT_DEFINITIONS_GET)
    public BusinessObjectDefinition getBusinessObjectDefinition(@PathVariable("namespace") String namespace,
        @PathVariable("businessObjectDefinitionName") String businessObjectDefinitionName)
    {
        BusinessObjectDefinitionKey businessObjectDefinitionKey = new BusinessObjectDefinitionKey(namespace, businessObjectDefinitionName);
        return businessObjectDefinitionService.getBusinessObjectDefinition(businessObjectDefinitionKey);
    }

    /**
     * Deletes an existing business object definition by key.
     * <p>Requires WRITE permission on namespace</p>
     *
     * @param namespace the namespace code
     * @param businessObjectDefinitionName the business object definition name
     *
     * @return the business object definition information of the entity that got deleted
     */
    @RequestMapping(value = "/businessObjectDefinitions/namespaces/{namespace}/businessObjectDefinitionNames/{businessObjectDefinitionName}",
        method = RequestMethod.DELETE)
    @Secured(SecurityFunctions.FN_BUSINESS_OBJECT_DEFINITIONS_DELETE)
    public BusinessObjectDefinition deleteBusinessObjectDefinition(@PathVariable("namespace") String namespace,
        @PathVariable("businessObjectDefinitionName") String businessObjectDefinitionName)
    {
        BusinessObjectDefinitionKey businessObjectDefinitionKey = new BusinessObjectDefinitionKey(namespace, businessObjectDefinitionName);
        return businessObjectDefinitionService.deleteBusinessObjectDefinition(businessObjectDefinitionKey);
    }

    /**
     * Gets the list of business object definitions that are defined in the system.
     *
     * @return the retrieved business object definition list.
     */
    @RequestMapping(value = "/businessObjectDefinitions", method = RequestMethod.GET)
    @Secured(SecurityFunctions.FN_BUSINESS_OBJECT_DEFINITIONS_ALL_GET)
    public BusinessObjectDefinitionKeys getBusinessObjectDefinitions()
    {
        return businessObjectDefinitionService.getBusinessObjectDefinitions();
    }

    /**
     * Gets the list of business object definitions that are defined in the system.
     * <p>Requires READ permission on namespace</p>
     *
     * @param namespace the namespace code
     *
     * @return the retrieved business object definition list
     */
    @RequestMapping(value = "/businessObjectDefinitions/namespaces/{namespace}", method = RequestMethod.GET)
    @Secured(SecurityFunctions.FN_BUSINESS_OBJECT_DEFINITIONS_ALL_GET)
    public BusinessObjectDefinitionKeys getBusinessObjectDefinitions(@PathVariable("namespace") String namespace)
    {
        return businessObjectDefinitionService.getBusinessObjectDefinitions(namespace);
    }
}
