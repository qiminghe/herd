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
package org.finra.herd.service.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.api.xml.BusinessObjectDataKey;
import org.finra.herd.model.jpa.BusinessObjectDataEntity;
import org.finra.herd.model.jpa.StorageUnitEntity;
import org.finra.herd.service.AbstractServiceTest;

/**
 * This class tests functionality within the StorageUnitDaoHelper class.
 */
public class StorageUnitDaoHelperTest extends AbstractServiceTest
{
    @Test
    public void testGetStorageUnitEntity()
    {
        // Create and persist test database entities.
        StorageUnitEntity storageUnitEntity =
            createStorageUnitEntity(STORAGE_NAME, NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE,
                NO_SUBPARTITION_VALUES, DATA_VERSION, true, BDATA_STATUS, STORAGE_UNIT_STATUS, NO_STORAGE_DIRECTORY_PATH);
        BusinessObjectDataEntity businessObjectDataEntity = storageUnitEntity.getBusinessObjectData();

        // Try to retrieve a non existing storage unit.
        try
        {
            storageUnitDaoHelper.getStorageUnitEntity("I_DO_NOT_EXIST", businessObjectDataEntity);
            fail("Should throw an ObjectNotFoundException.");
        }
        catch (ObjectNotFoundException e)
        {
            assertEquals(String.format("Could not find storage unit in \"I_DO_NOT_EXIST\" storage for the business object data {%s}.", businessObjectDataHelper
                .businessObjectDataKeyToString(
                    new BusinessObjectDataKey(NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE,
                        NO_SUBPARTITION_VALUES, DATA_VERSION))), e.getMessage());
        }

        // Retrieve an existing storage unit entity.
        StorageUnitEntity resultStorageUnitEntity = storageUnitDaoHelper.getStorageUnitEntity(STORAGE_NAME, businessObjectDataEntity);
        assertNotNull(resultStorageUnitEntity);
        assertEquals(storageUnitEntity.getId(), resultStorageUnitEntity.getId());
    }

    @Test
    public void testUpdateStorageUnitStatus()
    {
        // Create and persist a storage unit entity.
        StorageUnitEntity storageUnitEntity =
            createStorageUnitEntity(STORAGE_NAME, BDEF_NAMESPACE, BDEF_NAME, FORMAT_USAGE_CODE, FORMAT_FILE_TYPE_CODE, FORMAT_VERSION, PARTITION_VALUE,
                SUBPARTITION_VALUES, DATA_VERSION, LATEST_VERSION_FLAG_SET, BDATA_STATUS, STORAGE_UNIT_STATUS, NO_STORAGE_DIRECTORY_PATH);

        // Create and persist a storage status entity.
        createStorageUnitStatusEntity(STORAGE_UNIT_STATUS_2);

        // Update the storage unit status.
        storageUnitDaoHelper.updateStorageUnitStatus(storageUnitEntity, STORAGE_UNIT_STATUS_2, REASON);

        // Validate the results.
        assertEquals(STORAGE_UNIT_STATUS_2, storageUnitEntity.getStatus().getCode());
    }
}
