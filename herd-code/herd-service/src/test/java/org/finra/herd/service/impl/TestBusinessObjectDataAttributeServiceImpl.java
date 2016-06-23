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

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.finra.herd.dao.config.DaoSpringModuleConfig;
import org.finra.herd.model.api.xml.BusinessObjectDataAttribute;
import org.finra.herd.model.api.xml.BusinessObjectDataAttributeCreateRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataAttributeKey;
import org.finra.herd.model.api.xml.BusinessObjectDataAttributeKeys;
import org.finra.herd.model.api.xml.BusinessObjectDataAttributeUpdateRequest;
import org.finra.herd.model.api.xml.BusinessObjectDataKey;

/**
 * This is a business object data attribute service implementation for testing.
 */
@Service
@Transactional(value = DaoSpringModuleConfig.HERD_TRANSACTION_MANAGER_BEAN_NAME)
@Primary
public class TestBusinessObjectDataAttributeServiceImpl extends BusinessObjectDataAttributeServiceImpl
{
    /**
     * {@inheritDoc}
     * <p/>
     * This implementation keeps the current transaction context.
     */
    @Override
    public BusinessObjectDataAttribute createBusinessObjectDataAttribute(BusinessObjectDataAttributeCreateRequest request)
    {
        return createBusinessObjectDataAttributeImpl(request);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation keeps the current transaction context.
     */
    @Override
    public BusinessObjectDataAttribute getBusinessObjectDataAttribute(BusinessObjectDataAttributeKey businessObjectDataAttributeKey)
    {
        return getBusinessObjectDataAttributeImpl(businessObjectDataAttributeKey);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation keeps the current transaction context.
     */
    @Override
    public BusinessObjectDataAttribute updateBusinessObjectDataAttribute(BusinessObjectDataAttributeKey businessObjectDataAttributeKey,
        BusinessObjectDataAttributeUpdateRequest request)
    {
        return updateBusinessObjectDataAttributeImpl(businessObjectDataAttributeKey, request);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation keeps the current transaction context.
     */
    @Override
    public BusinessObjectDataAttribute deleteBusinessObjectDataAttribute(BusinessObjectDataAttributeKey businessObjectDataAttributeKey)
    {
        return deleteBusinessObjectDataAttributeImpl(businessObjectDataAttributeKey);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation keeps the current transaction context.
     */
    @Override
    public BusinessObjectDataAttributeKeys getBusinessObjectDataAttributes(BusinessObjectDataKey businessObjectDataKey)
    {
        return getBusinessObjectDataAttributesImpl(businessObjectDataKey);
    }
}
