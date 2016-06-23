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
package org.finra.herd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.finra.herd.dao.CustomDdlDao;
import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.annotation.NamespacePermission;
import org.finra.herd.model.api.xml.BusinessObjectFormatKey;
import org.finra.herd.model.api.xml.CustomDdl;
import org.finra.herd.model.api.xml.CustomDdlCreateRequest;
import org.finra.herd.model.api.xml.CustomDdlKey;
import org.finra.herd.model.api.xml.CustomDdlKeys;
import org.finra.herd.model.api.xml.CustomDdlUpdateRequest;
import org.finra.herd.model.api.xml.NamespacePermissionEnum;
import org.finra.herd.model.jpa.BusinessObjectFormatEntity;
import org.finra.herd.model.jpa.CustomDdlEntity;
import org.finra.herd.service.CustomDdlService;
import org.finra.herd.service.helper.BusinessObjectFormatDaoHelper;
import org.finra.herd.service.helper.BusinessObjectFormatHelper;
import org.finra.herd.service.helper.CustomDdlDaoHelper;
import org.finra.herd.service.helper.CustomDdlHelper;

/**
 * The custom DDL service implementation.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
public class CustomDdlServiceImpl implements CustomDdlService
{
    @Autowired
    private BusinessObjectFormatDaoHelper businessObjectFormatDaoHelper;

    @Autowired
    private BusinessObjectFormatHelper businessObjectFormatHelper;

    @Autowired
    private CustomDdlDao customDdlDao;

    @Autowired
    private CustomDdlDaoHelper customDdlDaoHelper;

    @Autowired
    private CustomDdlHelper customDdlHelper;

    /**
     * Creates a new custom DDL.
     *
     * @param request the information needed to create a custom DDL
     *
     * @return the newly created custom DDL information
     */
    @NamespacePermission(fields = "#request.customDdlKey.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    public CustomDdl createCustomDdl(CustomDdlCreateRequest request)
    {
        // Validate and trim the key.
        customDdlHelper.validateCustomDdlKey(request.getCustomDdlKey());

        // Validate and trim the DDL.
        Assert.hasText(request.getDdl(), "DDL must be specified.");
        request.setDdl(request.getDdl().trim());

        // Get the business object format and ensure it exists.
        BusinessObjectFormatEntity businessObjectFormatEntity = businessObjectFormatDaoHelper.getBusinessObjectFormatEntity(
            new BusinessObjectFormatKey(request.getCustomDdlKey().getNamespace(), request.getCustomDdlKey().getBusinessObjectDefinitionName(),
                request.getCustomDdlKey().getBusinessObjectFormatUsage(), request.getCustomDdlKey().getBusinessObjectFormatFileType(),
                request.getCustomDdlKey().getBusinessObjectFormatVersion()));

        // Ensure a custom DDL with the specified name doesn't already exist for the specified business object format.
        CustomDdlEntity customDdlEntity = customDdlDao.getCustomDdlByKey(request.getCustomDdlKey());
        if (customDdlEntity != null)
        {
            throw new AlreadyExistsException(String
                .format("Unable to create custom DDL with name \"%s\" because it already exists for the business object format {%s}.",
                    request.getCustomDdlKey().getCustomDdlName(),
                    businessObjectFormatHelper.businessObjectFormatEntityAltKeyToString(businessObjectFormatEntity)));
        }

        // Create a custom DDL entity from the request information.
        customDdlEntity = createCustomDdlEntity(businessObjectFormatEntity, request);

        // Persist the new entity.
        customDdlEntity = customDdlDao.saveAndRefresh(customDdlEntity);

        // Create and return the custom DDL object from the persisted entity.
        return createCustomDdlFromEntity(customDdlEntity);
    }

    /**
     * Gets an existing custom DDL by key.
     *
     * @param customDdlKey the custom DDL key
     *
     * @return the custom DDL information
     */
    @NamespacePermission(fields = "#customDdlKey.namespace", permissions = NamespacePermissionEnum.READ)
    @Override
    public CustomDdl getCustomDdl(CustomDdlKey customDdlKey)
    {
        // Validate and trim the key.
        customDdlHelper.validateCustomDdlKey(customDdlKey);

        // Retrieve and ensure that a custom DDL exists with the specified key.
        CustomDdlEntity customDdlEntity = customDdlDaoHelper.getCustomDdlEntity(customDdlKey);

        // Create and return the custom DDL object from the persisted entity.
        return createCustomDdlFromEntity(customDdlEntity);
    }

    /**
     * Updates an existing custom DDL by key.
     *
     * @param customDdlKey the custom DDL key
     *
     * @return the custom DDL information
     */
    @NamespacePermission(fields = "#customDdlKey.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    public CustomDdl updateCustomDdl(CustomDdlKey customDdlKey, CustomDdlUpdateRequest request)
    {
        // Validate and trim the key.
        customDdlHelper.validateCustomDdlKey(customDdlKey);

        // Validate and trim the DDL.
        Assert.hasText(request.getDdl(), "DDL must be specified.");
        request.setDdl(request.getDdl().trim());

        // Retrieve and ensure that a custom DDL exists with the specified key.
        CustomDdlEntity customDdlEntity = customDdlDaoHelper.getCustomDdlEntity(customDdlKey);

        // Update the entity with the new values.
        customDdlEntity.setDdl(request.getDdl());

        // Persist the entity.
        customDdlEntity = customDdlDao.saveAndRefresh(customDdlEntity);

        // Create and return the custom DDL object from the persisted entity.
        return createCustomDdlFromEntity(customDdlEntity);
    }

    /**
     * Deletes an existing custom DDL by key.
     *
     * @param customDdlKey the custom DDL key
     *
     * @return the custom DDL that got deleted
     */
    @NamespacePermission(fields = "#customDdlKey.namespace", permissions = NamespacePermissionEnum.WRITE)
    @Override
    public CustomDdl deleteCustomDdl(CustomDdlKey customDdlKey)
    {
        // Validate and trim the key.
        customDdlHelper.validateCustomDdlKey(customDdlKey);

        // Retrieve and ensure that a custom DDL already exists with the specified key.
        CustomDdlEntity customDdlEntity = customDdlDaoHelper.getCustomDdlEntity(customDdlKey);

        // Delete the custom DDL.
        customDdlDao.delete(customDdlEntity);

        // Create and return the custom DDL object from the deleted entity.
        return createCustomDdlFromEntity(customDdlEntity);
    }

    /**
     * Gets a list of keys for all existing custom DDLs.
     *
     * @return the custom DDL keys
     */
    @NamespacePermission(fields = "#businessObjectFormatKey.namespace", permissions = NamespacePermissionEnum.READ)
    @Override
    public CustomDdlKeys getCustomDdls(BusinessObjectFormatKey businessObjectFormatKey)
    {
        // Validate and trim the business object format key.
        businessObjectFormatHelper.validateBusinessObjectFormatKey(businessObjectFormatKey);

        // Ensure that the business object format exists.
        businessObjectFormatDaoHelper.getBusinessObjectFormatEntity(businessObjectFormatKey);

        // Create and populate a list of custom DDL keys.
        CustomDdlKeys customDdlKeys = new CustomDdlKeys();
        customDdlKeys.getCustomDdlKeys().addAll(customDdlDao.getCustomDdls(businessObjectFormatKey));

        return customDdlKeys;
    }

    /**
     * Creates a new custom DDL entity from the business object format entity and the request information.
     *
     * @param businessObjectFormatEntity the business object format entity
     * @param request the custom DDL create request
     *
     * @return the newly created custom DDL entity
     */
    private CustomDdlEntity createCustomDdlEntity(BusinessObjectFormatEntity businessObjectFormatEntity, CustomDdlCreateRequest request)
    {
        // Create a new entity.
        CustomDdlEntity customDdlEntity = new CustomDdlEntity();

        customDdlEntity.setBusinessObjectFormat(businessObjectFormatEntity);
        customDdlEntity.setCustomDdlName(request.getCustomDdlKey().getCustomDdlName());
        customDdlEntity.setDdl(request.getDdl());

        return customDdlEntity;
    }

    /**
     * Creates the custom DDL from the persisted entity.
     *
     * @param customDdlEntity the custom DDL entity
     *
     * @return the custom DDL
     */
    private CustomDdl createCustomDdlFromEntity(CustomDdlEntity customDdlEntity)
    {
        // Create the custom DDL.
        CustomDdl customDdl = new CustomDdl();

        customDdl.setId(customDdlEntity.getId());
        customDdl.setCustomDdlKey(new CustomDdlKey(customDdlEntity.getBusinessObjectFormat().getBusinessObjectDefinition().getNamespace().getCode(),
            customDdlEntity.getBusinessObjectFormat().getBusinessObjectDefinition().getName(), customDdlEntity.getBusinessObjectFormat().getUsage(),
            customDdlEntity.getBusinessObjectFormat().getFileType().getCode(), customDdlEntity.getBusinessObjectFormat().getBusinessObjectFormatVersion(),
            customDdlEntity.getCustomDdlName()));
        customDdl.setDdl(customDdlEntity.getDdl());

        return customDdl;
    }
}
