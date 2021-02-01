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
package org.finra.herd.service;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import org.finra.herd.dao.NamespaceIamRoleAuthorizationDao;
import org.finra.herd.model.AlreadyExistsException;
import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.api.xml.NamespaceIamRoleAuthorization;
import org.finra.herd.model.api.xml.NamespaceIamRoleAuthorizationCreateRequest;
import org.finra.herd.model.api.xml.NamespaceIamRoleAuthorizationKey;
import org.finra.herd.model.api.xml.NamespaceIamRoleAuthorizationKeys;
import org.finra.herd.model.api.xml.NamespaceIamRoleAuthorizationUpdateRequest;
import org.finra.herd.model.jpa.NamespaceEntity;
import org.finra.herd.model.jpa.NamespaceIamRoleAuthorizationEntity;
import org.finra.herd.service.helper.AlternateKeyHelper;
import org.finra.herd.service.helper.NamespaceDaoHelper;
import org.finra.herd.service.impl.NamespaceIamRoleAuthorizationServiceImpl;

public class NamespaceIamRoleAuthorizationServiceTest extends AbstractServiceTest
{
    @Mock
    private AlternateKeyHelper alternateKeyHelper;

    @Mock
    private NamespaceDaoHelper namespaceDaoHelper;

    @Mock
    private NamespaceIamRoleAuthorizationDao namespaceIamRoleAuthorizationDao;

    @InjectMocks
    private NamespaceIamRoleAuthorizationServiceImpl namespaceIamRoleAuthorizationServiceImpl;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateNamespaceIamRoleAuthorization()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey =
            new NamespaceIamRoleAuthorizationKey(NAMESPACE, IAM_ROLE_NAME);
        NamespaceIamRoleAuthorizationCreateRequest expectedRequest =
            new NamespaceIamRoleAuthorizationCreateRequest(namespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        NamespaceIamRoleAuthorizationEntity expectedNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME, IAM_ROLE_DESCRIPTION);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey)).thenReturn(null);
        doAnswer((Answer<Void>) invocation -> {
            // Get the namespace IAM role authorization entity and set its primary key.
            NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity = (NamespaceIamRoleAuthorizationEntity) invocation.getArguments()[0];
            namespaceIamRoleAuthorizationEntity.setId(ID);
            return null;
        }).when(namespaceIamRoleAuthorizationDao).saveAndRefresh(any(NamespaceIamRoleAuthorizationEntity.class));

        // Call the method being tested.
        NamespaceIamRoleAuthorization response =
            namespaceIamRoleAuthorizationServiceImpl.createNamespaceIamRoleAuthorization(expectedRequest);

        // Validate the results.
        assertEquals(ID, new Long(response.getId()));
        assertEquals(IAM_ROLE_DESCRIPTION, response.getIamRoleDescription());
        assertEquals(IAM_ROLE_NAME, response.getNamespaceIamRoleAuthorizationKey().getIamRoleName());
        assertEquals(NAMESPACE, response.getNamespaceIamRoleAuthorizationKey().getNamespace());

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verify(namespaceIamRoleAuthorizationDao).saveAndRefresh(expectedNamespaceIamRoleAuthorizationEntity);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testCreateNamespaceIamRoleAuthorizationAssertErrorWhenAuthorizationAlreadyExist()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, IAM_ROLE_NAME);
        NamespaceIamRoleAuthorizationCreateRequest expectedRequest =
            new NamespaceIamRoleAuthorizationCreateRequest(namespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        NamespaceIamRoleAuthorizationEntity existingNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME, IAM_ROLE_DESCRIPTION);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey))
            .thenReturn(existingNamespaceIamRoleAuthorizationEntity);

        try
        {
            // Call method being tested.
            namespaceIamRoleAuthorizationServiceImpl.createNamespaceIamRoleAuthorization(expectedRequest);
            fail();
        }
        catch (Exception exception)
        {
            // Validate response
            assertEquals(AlreadyExistsException.class, exception.getClass());
            assertEquals(String.format("Namespace IAM role authorization with namespace \"%s\" and IAM role name \"%s\" already exists",
                NAMESPACE, IAM_ROLE_NAME), exception.getMessage());
        }

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testCreateNamespaceIamRoleAuthorizationAssertErrorWhenRoleNameIsBlank()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, BLANK_TEXT);
        NamespaceIamRoleAuthorizationCreateRequest expectedRequest =
            new NamespaceIamRoleAuthorizationCreateRequest(namespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(expectedNamespaceEntity)).thenReturn(
            Collections.singletonList(new NamespaceIamRoleAuthorizationEntity()));

        try
        {
            // Call method being tested.
            namespaceIamRoleAuthorizationServiceImpl.createNamespaceIamRoleAuthorization(expectedRequest);
            fail();
        }
        catch (Exception e)
        {
            // Validate response
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("IAM role name must be specified", e.getMessage());
        }

        // Verify the interactions.
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testCreateNamespaceIamRoleAuthorizationAssertErrorWhenRoleNameIsNull()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, null);
        NamespaceIamRoleAuthorizationCreateRequest expectedRequest =
            new NamespaceIamRoleAuthorizationCreateRequest(namespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(expectedNamespaceEntity)).thenReturn(
            Collections.singletonList(new NamespaceIamRoleAuthorizationEntity()));

        try
        {
            // Call method being tested.
            namespaceIamRoleAuthorizationServiceImpl.createNamespaceIamRoleAuthorization(expectedRequest);
            fail();
        }
        catch (Exception e)
        {
            // Validate response
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("IAM role name must be specified", e.getMessage());
        }

        // Verify the interactions.
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testCreateNamespaceIamRoleAuthorizationAssertErrorWhenNamespaceIsBlank()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(BLANK_TEXT, IAM_ROLE_NAME);
        NamespaceIamRoleAuthorizationCreateRequest expectedRequest =
            new NamespaceIamRoleAuthorizationCreateRequest(namespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(expectedNamespaceEntity)).thenReturn(
            Collections.singletonList(new NamespaceIamRoleAuthorizationEntity()));

        try
        {
            // Call method being tested.
            namespaceIamRoleAuthorizationServiceImpl.createNamespaceIamRoleAuthorization(expectedRequest);
            fail();
        }
        catch (Exception e)
        {
            // Validate response
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Namespace must be specified", e.getMessage());
        }

        // Verify the interactions.
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testCreateNamespaceIamRoleAuthorizationAssertErrorWhenNamespaceIsNull()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(null, IAM_ROLE_NAME);
        NamespaceIamRoleAuthorizationCreateRequest expectedRequest =
            new NamespaceIamRoleAuthorizationCreateRequest(namespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(expectedNamespaceEntity)).thenReturn(
            Collections.singletonList(new NamespaceIamRoleAuthorizationEntity()));

        try
        {
            // Call method being tested.
            namespaceIamRoleAuthorizationServiceImpl.createNamespaceIamRoleAuthorization(expectedRequest);
            fail();
        }
        catch (Exception e)
        {
            // Validate response
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Namespace must be specified", e.getMessage());
        }

        // Verify the interactions.
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testCreateNamespaceIamRoleAuthorizationAssertInputsTrimmed()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey =
            new NamespaceIamRoleAuthorizationKey(addWhitespace(NAMESPACE), addWhitespace(IAM_ROLE_NAME));
        NamespaceIamRoleAuthorizationCreateRequest expectedRequest =
            new NamespaceIamRoleAuthorizationCreateRequest(namespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        NamespaceIamRoleAuthorizationEntity expectedNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME, IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntity.setId(ID);

        List<NamespaceIamRoleAuthorizationEntity> expectedNamespaceIamRoleAuthorizationEntityList = new ArrayList<>();

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(expectedNamespaceEntity))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntityList);
        doAnswer((Answer<Void>) invocation -> {
            // Get the namespace IAM role authorization entity and set its primary key.
            NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity = (NamespaceIamRoleAuthorizationEntity) invocation.getArguments()[0];
            namespaceIamRoleAuthorizationEntity.setId(ID);
            return null;
        }).when(namespaceIamRoleAuthorizationDao).saveAndRefresh(any(NamespaceIamRoleAuthorizationEntity.class));

        // Call the method being tested.
        NamespaceIamRoleAuthorization response = namespaceIamRoleAuthorizationServiceImpl.createNamespaceIamRoleAuthorization(expectedRequest);

        // Validate response
        assertEquals(ID, new Long(response.getId()));
        assertEquals(IAM_ROLE_DESCRIPTION, response.getIamRoleDescription());
        assertEquals(IAM_ROLE_NAME, response.getNamespaceIamRoleAuthorizationKey().getIamRoleName());
        assertEquals(NAMESPACE, response.getNamespaceIamRoleAuthorizationKey().getNamespace());

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorizations(expectedNamespaceEntity);
        verify(namespaceIamRoleAuthorizationDao).saveAndRefresh(expectedNamespaceIamRoleAuthorizationEntity);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testDeleteNamespaceIamRoleAuthorization()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, IAM_ROLE_NAME);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        NamespaceIamRoleAuthorizationEntity expectedNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME, IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntity.setId(ID);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntity);

        // Call the method being tested.
        NamespaceIamRoleAuthorization response =
            namespaceIamRoleAuthorizationServiceImpl.deleteNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);

        // Validate the results.
        assertEquals(ID, new Long(response.getId()));
        assertEquals(IAM_ROLE_DESCRIPTION, response.getIamRoleDescription());
        assertEquals(IAM_ROLE_NAME, response.getNamespaceIamRoleAuthorizationKey().getIamRoleName());
        assertEquals(NAMESPACE, response.getNamespaceIamRoleAuthorizationKey().getNamespace());

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verify(namespaceIamRoleAuthorizationDao).delete(expectedNamespaceIamRoleAuthorizationEntity);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }


    @Test
    public void testDeleteNamespaceIamRoleAuthorizationAssertInputsAreTrimmed()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey =
            new NamespaceIamRoleAuthorizationKey(addWhitespace(NAMESPACE_2), addWhitespace(IAM_ROLE_NAME_2));

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE_2);

        NamespaceIamRoleAuthorizationEntity expectedNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME_2, IAM_ROLE_DESCRIPTION_2);
        expectedNamespaceIamRoleAuthorizationEntity.setId(ID);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE_2)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntity);

        // Call the method being tested.
        NamespaceIamRoleAuthorization response =
            namespaceIamRoleAuthorizationServiceImpl.deleteNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);

        // Validate the results.
        assertEquals(ID, new Long(response.getId()));
        assertEquals(IAM_ROLE_DESCRIPTION_2, response.getIamRoleDescription());
        assertEquals(IAM_ROLE_NAME_2, response.getNamespaceIamRoleAuthorizationKey().getIamRoleName());
        assertEquals(NAMESPACE_2, response.getNamespaceIamRoleAuthorizationKey().getNamespace());

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE_2);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verify(namespaceIamRoleAuthorizationDao).delete(expectedNamespaceIamRoleAuthorizationEntity);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testDeleteNamespaceIamRoleAuthorizationAssertErrorWhenDaoReturnsEmpty()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey =
            new NamespaceIamRoleAuthorizationKey(addWhitespace(NAMESPACE), addWhitespace(IAM_ROLE_NAME));

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey)).thenReturn(null);

        try
        {
            // Call the method being tested.
            namespaceIamRoleAuthorizationServiceImpl.deleteNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
            fail();
        }
        catch (Exception e)
        {
            // Validate the results.
            assertEquals(ObjectNotFoundException.class, e.getClass());
            assertEquals(String.format("Namespace IAM role authorization for namespace \"%s\" and IAM role name \"%s\" does not exist",
                namespaceIamRoleAuthorizationKey.getNamespace(), namespaceIamRoleAuthorizationKey.getIamRoleName()),
                e.getMessage());
        }

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }


    @Test
    public void testDeleteNamespaceIamRoleAuthorizationAssertErrorWhenNamespaceIsBlank()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(BLANK_TEXT, IAM_ROLE_NAME);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        try
        {
            // Call the method being tested.
            namespaceIamRoleAuthorizationServiceImpl.deleteNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
            fail();
        }
        catch (Exception e)
        {
            // Validate the results.
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Namespace must be specified", e.getMessage());
        }

        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testDeleteNamespaceIamRoleAuthorizationAssertErrorWhenIamRoleNameIsBlank()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, BLANK_TEXT);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        try
        {
            // Call the method being tested.
            namespaceIamRoleAuthorizationServiceImpl.deleteNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
            fail();
        }
        catch (Exception e)
        {
            // Validate the results.
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("IAM role name must be specified", e.getMessage());
        }

        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorization()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, IAM_ROLE_NAME);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        NamespaceIamRoleAuthorization expectedNamespaceIamRoleAuthorization =
            new NamespaceIamRoleAuthorization(ID, namespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceIamRoleAuthorizationEntity expectedNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME, IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntity.setId(ID);

        // Configure interactions.
        when(alternateKeyHelper.validateStringParameter("namespace", NAMESPACE)).thenReturn(NAMESPACE);
        when(alternateKeyHelper.validateStringParameter("An", "IAM role name", IAM_ROLE_NAME)).thenReturn(IAM_ROLE_NAME);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey)).thenReturn(
            expectedNamespaceIamRoleAuthorizationEntity);

        // Call method being tested.
        NamespaceIamRoleAuthorization namespaceIamRoleAuthorization =
            namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);

        // Validate results.
        assertEquals(expectedNamespaceIamRoleAuthorization, namespaceIamRoleAuthorization);

        // Verify interactions.
        verify(alternateKeyHelper).validateStringParameter("namespace", NAMESPACE);
        verify(alternateKeyHelper).validateStringParameter("An", "IAM role name", IAM_ROLE_NAME);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verifyNoMoreInteractions(alternateKeyHelper, namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorizationAssertInputsTrimmed()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey =
            new NamespaceIamRoleAuthorizationKey(addWhitespace(NAMESPACE), addWhitespace(IAM_ROLE_NAME));

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        NamespaceIamRoleAuthorizationKey expectedNamespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, IAM_ROLE_NAME);
        NamespaceIamRoleAuthorization expectedNamespaceIamRoleAuthorization =
            new NamespaceIamRoleAuthorization(ID, expectedNamespaceIamRoleAuthorizationKey, IAM_ROLE_DESCRIPTION);

        NamespaceIamRoleAuthorizationEntity expectedNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME, IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntity.setId(ID);

        // Configure interactions.
        when(alternateKeyHelper.validateStringParameter("namespace", addWhitespace(NAMESPACE))).thenReturn(NAMESPACE);
        when(alternateKeyHelper.validateStringParameter("An", "IAM role name", addWhitespace(IAM_ROLE_NAME))).thenReturn(IAM_ROLE_NAME);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey)).thenReturn(
            expectedNamespaceIamRoleAuthorizationEntity);

        // Call method being tested.
        NamespaceIamRoleAuthorization namespaceIamRoleAuthorization =
            namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);

        // Validate results.
        assertEquals(expectedNamespaceIamRoleAuthorization, namespaceIamRoleAuthorization);

        // Verify interactions.
        verify(alternateKeyHelper).validateStringParameter("namespace", addWhitespace(NAMESPACE));
        verify(alternateKeyHelper).validateStringParameter("An", "IAM role name", addWhitespace(IAM_ROLE_NAME));
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verifyNoMoreInteractions(alternateKeyHelper, namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorizationAssertErrorWhenNoEntityFound()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, IAM_ROLE_NAME);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        // Configure interactions.
        when(alternateKeyHelper.validateStringParameter("namespace", NAMESPACE)).thenReturn(NAMESPACE);
        when(alternateKeyHelper.validateStringParameter("An", "IAM role name", IAM_ROLE_NAME)).thenReturn(IAM_ROLE_NAME);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey)).thenReturn(null);

        try
        {
            namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
            fail();
        }
        catch (Exception e)
        {
            assertEquals(ObjectNotFoundException.class, e.getClass());
            assertEquals(String.format("Namespace IAM role authorization for namespace \"%s\" and IAM role name \"%s\" does not exist",
                namespaceIamRoleAuthorizationKey.getNamespace(), namespaceIamRoleAuthorizationKey.getIamRoleName()),
                e.getMessage());
        }

        // Verify interactions.
        verify(alternateKeyHelper).validateStringParameter("namespace", NAMESPACE);
        verify(alternateKeyHelper).validateStringParameter("An", "IAM role name", IAM_ROLE_NAME);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verifyNoMoreInteractions(alternateKeyHelper, namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorizations()
    {
        // Setup the objects needed for testing.
        NamespaceEntity namespaceEntity1 = new NamespaceEntity();
        namespaceEntity1.setCode(NAMESPACE);

        NamespaceEntity namespaceEntity2 = new NamespaceEntity();
        namespaceEntity2.setCode(NAMESPACE_2);

        List<NamespaceIamRoleAuthorizationEntity> expectedNamespaceIamRoleAuthorizationEntities = new ArrayList<>();
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity1 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity1.setNamespace(namespaceEntity1);
        namespaceIamRoleAuthorizationEntity1.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity1.setDescription(IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity1);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity2 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity2.setNamespace(namespaceEntity1);
        namespaceIamRoleAuthorizationEntity2.setIamRoleName(IAM_ROLE_NAME_2);
        namespaceIamRoleAuthorizationEntity2.setDescription(IAM_ROLE_DESCRIPTION_2);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity2);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity3 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity3.setNamespace(namespaceEntity2);
        namespaceIamRoleAuthorizationEntity3.setIamRoleName(IAM_ROLE_NAME_3);
        namespaceIamRoleAuthorizationEntity3.setDescription(IAM_ROLE_DESCRIPTION_3);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity3);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity4 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity4.setNamespace(namespaceEntity2);
        namespaceIamRoleAuthorizationEntity4.setIamRoleName(IAM_ROLE_NAME_4);
        namespaceIamRoleAuthorizationEntity4.setDescription(IAM_ROLE_DESCRIPTION_4);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity4);

        // Setup the interactions.
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(null)).thenReturn(expectedNamespaceIamRoleAuthorizationEntities);

        // Call method being tested.
        NamespaceIamRoleAuthorizationKeys result = namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorizations();

        // Validate the results.
        assertNotNull(result);
        assertNotNull(result.getNamespaceIamRoleAuthorizationKeys());
        assertEquals(4, result.getNamespaceIamRoleAuthorizationKeys().size());

        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey1 = result.getNamespaceIamRoleAuthorizationKeys().get(0);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey2 = result.getNamespaceIamRoleAuthorizationKeys().get(1);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey3 = result.getNamespaceIamRoleAuthorizationKeys().get(2);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey4 = result.getNamespaceIamRoleAuthorizationKeys().get(3);

        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey1.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey1.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey2.getNamespace());
        assertEquals(IAM_ROLE_NAME_2, namespaceIAMRoleAuthorizationKey2.getIamRoleName());
        assertEquals(NAMESPACE_2, namespaceIAMRoleAuthorizationKey3.getNamespace());
        assertEquals(IAM_ROLE_NAME_3, namespaceIAMRoleAuthorizationKey3.getIamRoleName());
        assertEquals(NAMESPACE_2, namespaceIAMRoleAuthorizationKey4.getNamespace());
        assertEquals(IAM_ROLE_NAME_4, namespaceIAMRoleAuthorizationKey4.getIamRoleName());

        // Verify the interactions.
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorizations(null);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorizationsAssertResultEmptyWhenDaoReturnsEmpty()
    {
        // Setup the interactions.
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(null)).thenReturn(Collections.emptyList());

        // Call method being tested.
        NamespaceIamRoleAuthorizationKeys result = namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorizations();

        // Validate the results.
        assertNotNull(result);
        assertNotNull(result.getNamespaceIamRoleAuthorizationKeys());
        assertEquals(0, result.getNamespaceIamRoleAuthorizationKeys().size());

        // Verify the interactions.
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorizations(null);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorizationsByIamRoleName()
    {
        // Setup the objects needed for testing.
        NamespaceEntity namespaceEntity1 = new NamespaceEntity();
        namespaceEntity1.setCode(NAMESPACE);

        NamespaceEntity namespaceEntity2 = new NamespaceEntity();
        namespaceEntity2.setCode(NAMESPACE_2);

        List<NamespaceIamRoleAuthorizationEntity> expectedNamespaceIamRoleAuthorizationEntities = new ArrayList<>();
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity1 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity1.setNamespace(namespaceEntity1);
        namespaceIamRoleAuthorizationEntity1.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity1.setDescription(IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity1);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity2 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity2.setNamespace(namespaceEntity1);
        namespaceIamRoleAuthorizationEntity2.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity2.setDescription(IAM_ROLE_DESCRIPTION_2);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity2);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity3 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity3.setNamespace(namespaceEntity2);
        namespaceIamRoleAuthorizationEntity3.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity3.setDescription(IAM_ROLE_DESCRIPTION_3);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity3);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity4 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity4.setNamespace(namespaceEntity2);
        namespaceIamRoleAuthorizationEntity4.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity4.setDescription(IAM_ROLE_DESCRIPTION_4);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity4);

        // Setup the interactions.
        when(alternateKeyHelper.validateStringParameter("An", "IAM role name", IAM_ROLE_NAME)).thenReturn(IAM_ROLE_NAME);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizationsByIamRoleName(IAM_ROLE_NAME))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntities);

        // Call method being tested.
        NamespaceIamRoleAuthorizationKeys result = namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorizationsByIamRoleName(IAM_ROLE_NAME);

        // Validate the results.
        assertNotNull(result);
        assertNotNull(result.getNamespaceIamRoleAuthorizationKeys());
        assertEquals(4, result.getNamespaceIamRoleAuthorizationKeys().size());

        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey1 = result.getNamespaceIamRoleAuthorizationKeys().get(0);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey2 = result.getNamespaceIamRoleAuthorizationKeys().get(1);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey3 = result.getNamespaceIamRoleAuthorizationKeys().get(2);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey4 = result.getNamespaceIamRoleAuthorizationKeys().get(3);

        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey1.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey1.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey2.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey2.getIamRoleName());
        assertEquals(NAMESPACE_2, namespaceIAMRoleAuthorizationKey3.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey3.getIamRoleName());
        assertEquals(NAMESPACE_2, namespaceIAMRoleAuthorizationKey4.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey4.getIamRoleName());

        // Verify the interactions.
        verify(alternateKeyHelper).validateStringParameter("An", "IAM role name", IAM_ROLE_NAME);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorizationsByIamRoleName(IAM_ROLE_NAME);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorizationsByIamRoleNameAssertInputsTrimmed()
    {
        // Setup the objects needed for testing.
        NamespaceEntity namespaceEntity1 = new NamespaceEntity();
        namespaceEntity1.setCode(NAMESPACE);

        NamespaceEntity namespaceEntity2 = new NamespaceEntity();
        namespaceEntity2.setCode(NAMESPACE_2);

        List<NamespaceIamRoleAuthorizationEntity> expectedNamespaceIamRoleAuthorizationEntities = new ArrayList<>();
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity1 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity1.setNamespace(namespaceEntity1);
        namespaceIamRoleAuthorizationEntity1.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity1.setDescription(IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity1);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity2 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity2.setNamespace(namespaceEntity1);
        namespaceIamRoleAuthorizationEntity2.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity2.setDescription(IAM_ROLE_DESCRIPTION_2);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity2);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity3 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity3.setNamespace(namespaceEntity2);
        namespaceIamRoleAuthorizationEntity3.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity3.setDescription(IAM_ROLE_DESCRIPTION_3);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity3);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity4 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity4.setNamespace(namespaceEntity2);
        namespaceIamRoleAuthorizationEntity4.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity4.setDescription(IAM_ROLE_DESCRIPTION_4);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity4);

        // Setup the interactions.
        when(alternateKeyHelper.validateStringParameter("An", "IAM role name", addWhitespace(IAM_ROLE_NAME))).thenReturn(IAM_ROLE_NAME);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizationsByIamRoleName(IAM_ROLE_NAME))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntities);

        // Call method being tested.
        NamespaceIamRoleAuthorizationKeys result =
            namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorizationsByIamRoleName(addWhitespace(IAM_ROLE_NAME));

        // Validate the results.
        assertNotNull(result);
        assertNotNull(result.getNamespaceIamRoleAuthorizationKeys());
        assertEquals(4, result.getNamespaceIamRoleAuthorizationKeys().size());

        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey1 = result.getNamespaceIamRoleAuthorizationKeys().get(0);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey2 = result.getNamespaceIamRoleAuthorizationKeys().get(1);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey3 = result.getNamespaceIamRoleAuthorizationKeys().get(2);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey4 = result.getNamespaceIamRoleAuthorizationKeys().get(3);

        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey1.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey1.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey2.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey2.getIamRoleName());
        assertEquals(NAMESPACE_2, namespaceIAMRoleAuthorizationKey3.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey3.getIamRoleName());
        assertEquals(NAMESPACE_2, namespaceIAMRoleAuthorizationKey4.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey4.getIamRoleName());

        // Verify the interactions.
        verify(alternateKeyHelper).validateStringParameter("An", "IAM role name", addWhitespace(IAM_ROLE_NAME));
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorizationsByIamRoleName(IAM_ROLE_NAME);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorizationsByNamespaceAssertInputsTrimmed()
    {
        // Setup the objects needed for testing.
        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        List<NamespaceIamRoleAuthorizationEntity> expectedNamespaceIamRoleAuthorizationEntities = new ArrayList<>();
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity1 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity1.setNamespace(expectedNamespaceEntity);
        namespaceIamRoleAuthorizationEntity1.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity1.setDescription(IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity1);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity2 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity2.setNamespace(expectedNamespaceEntity);
        namespaceIamRoleAuthorizationEntity2.setIamRoleName(IAM_ROLE_NAME_2);
        namespaceIamRoleAuthorizationEntity2.setDescription(IAM_ROLE_DESCRIPTION_2);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity2);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity3 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity3.setNamespace(expectedNamespaceEntity);
        namespaceIamRoleAuthorizationEntity3.setIamRoleName(IAM_ROLE_NAME_3);
        namespaceIamRoleAuthorizationEntity3.setDescription(IAM_ROLE_DESCRIPTION_3);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity3);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity4 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity4.setNamespace(expectedNamespaceEntity);
        namespaceIamRoleAuthorizationEntity4.setIamRoleName(IAM_ROLE_NAME_4);
        namespaceIamRoleAuthorizationEntity4.setDescription(IAM_ROLE_DESCRIPTION_4);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity4);

        // Setup the interactions.
        when(alternateKeyHelper.validateStringParameter("namespace", addWhitespace(NAMESPACE))).thenReturn(NAMESPACE);
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(expectedNamespaceEntity))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntities);

        // Call method being tested.
        NamespaceIamRoleAuthorizationKeys result =
            namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorizationsByNamespace(addWhitespace(NAMESPACE));

        // Validate the results.
        assertNotNull(result);
        assertNotNull(result.getNamespaceIamRoleAuthorizationKeys());
        assertEquals(4, result.getNamespaceIamRoleAuthorizationKeys().size());

        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey1 = result.getNamespaceIamRoleAuthorizationKeys().get(0);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey2 = result.getNamespaceIamRoleAuthorizationKeys().get(1);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey3 = result.getNamespaceIamRoleAuthorizationKeys().get(2);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey4 = result.getNamespaceIamRoleAuthorizationKeys().get(3);

        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey1.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey1.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey2.getNamespace());
        assertEquals(IAM_ROLE_NAME_2, namespaceIAMRoleAuthorizationKey2.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey3.getNamespace());
        assertEquals(IAM_ROLE_NAME_3, namespaceIAMRoleAuthorizationKey3.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey4.getNamespace());
        assertEquals(IAM_ROLE_NAME_4, namespaceIAMRoleAuthorizationKey4.getIamRoleName());

        // Verify the interactions.
        verify(alternateKeyHelper).validateStringParameter("namespace", addWhitespace(NAMESPACE));
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorizations(expectedNamespaceEntity);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testGetNamespaceIamRoleAuthorizationsByNamespace()
    {
        // Setup the objects needed for testing.
        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        List<NamespaceIamRoleAuthorizationEntity> expectedNamespaceIamRoleAuthorizationEntities = new ArrayList<>();
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity1 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity1.setNamespace(expectedNamespaceEntity);
        namespaceIamRoleAuthorizationEntity1.setIamRoleName(IAM_ROLE_NAME);
        namespaceIamRoleAuthorizationEntity1.setDescription(IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity1);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity2 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity2.setNamespace(expectedNamespaceEntity);
        namespaceIamRoleAuthorizationEntity2.setIamRoleName(IAM_ROLE_NAME_2);
        namespaceIamRoleAuthorizationEntity2.setDescription(IAM_ROLE_DESCRIPTION_2);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity2);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity3 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity3.setNamespace(expectedNamespaceEntity);
        namespaceIamRoleAuthorizationEntity3.setIamRoleName(IAM_ROLE_NAME_3);
        namespaceIamRoleAuthorizationEntity3.setDescription(IAM_ROLE_DESCRIPTION_3);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity3);
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity4 = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity4.setNamespace(expectedNamespaceEntity);
        namespaceIamRoleAuthorizationEntity4.setIamRoleName(IAM_ROLE_NAME_4);
        namespaceIamRoleAuthorizationEntity4.setDescription(IAM_ROLE_DESCRIPTION_4);
        expectedNamespaceIamRoleAuthorizationEntities.add(namespaceIamRoleAuthorizationEntity4);

        // Setup the interactions.
        when(alternateKeyHelper.validateStringParameter("namespace", NAMESPACE)).thenReturn(NAMESPACE);
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorizations(expectedNamespaceEntity))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntities);

        // Call method being tested.
        NamespaceIamRoleAuthorizationKeys result = namespaceIamRoleAuthorizationServiceImpl.getNamespaceIamRoleAuthorizationsByNamespace(NAMESPACE);

        // Validate the results.
        assertNotNull(result);
        assertNotNull(result.getNamespaceIamRoleAuthorizationKeys());
        assertEquals(4, result.getNamespaceIamRoleAuthorizationKeys().size());

        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey1 = result.getNamespaceIamRoleAuthorizationKeys().get(0);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey2 = result.getNamespaceIamRoleAuthorizationKeys().get(1);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey3 = result.getNamespaceIamRoleAuthorizationKeys().get(2);
        NamespaceIamRoleAuthorizationKey namespaceIAMRoleAuthorizationKey4 = result.getNamespaceIamRoleAuthorizationKeys().get(3);

        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey1.getNamespace());
        assertEquals(IAM_ROLE_NAME, namespaceIAMRoleAuthorizationKey1.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey2.getNamespace());
        assertEquals(IAM_ROLE_NAME_2, namespaceIAMRoleAuthorizationKey2.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey3.getNamespace());
        assertEquals(IAM_ROLE_NAME_3, namespaceIAMRoleAuthorizationKey3.getIamRoleName());
        assertEquals(NAMESPACE, namespaceIAMRoleAuthorizationKey4.getNamespace());
        assertEquals(IAM_ROLE_NAME_4, namespaceIAMRoleAuthorizationKey4.getIamRoleName());

        // Verify the interactions.
        verify(alternateKeyHelper).validateStringParameter("namespace", NAMESPACE);
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorizations(expectedNamespaceEntity);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testUpdateNamespaceIamRoleAuthorization()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, IAM_ROLE_NAME);
        NamespaceIamRoleAuthorizationUpdateRequest expectedRequest = new NamespaceIamRoleAuthorizationUpdateRequest(IAM_ROLE_DESCRIPTION_2);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        NamespaceIamRoleAuthorizationEntity expectedNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME, IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntity.setId(ID);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntity);

        // Call the method being tested.
        NamespaceIamRoleAuthorization response =
            namespaceIamRoleAuthorizationServiceImpl.updateNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey, expectedRequest);

        // Validate the results.
        assertEquals(ID, new Long(response.getId()));
        assertEquals(IAM_ROLE_DESCRIPTION_2, response.getIamRoleDescription());
        assertEquals(IAM_ROLE_NAME, response.getNamespaceIamRoleAuthorizationKey().getIamRoleName());
        assertEquals(NAMESPACE, response.getNamespaceIamRoleAuthorizationKey().getNamespace());

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verify(namespaceIamRoleAuthorizationDao).saveAndRefresh(expectedNamespaceIamRoleAuthorizationEntity);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testUpdateNamespaceIamRoleAuthorizationAssertInputsTrimmed()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey =
            new NamespaceIamRoleAuthorizationKey(addWhitespace(NAMESPACE), addWhitespace(IAM_ROLE_NAME));
        NamespaceIamRoleAuthorizationUpdateRequest expectedRequest = new NamespaceIamRoleAuthorizationUpdateRequest(IAM_ROLE_DESCRIPTION_2);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        NamespaceIamRoleAuthorizationEntity expectedNamespaceIamRoleAuthorizationEntity =
            createNamespaceIamRoleAuthorizationEntity(expectedNamespaceEntity, IAM_ROLE_NAME, IAM_ROLE_DESCRIPTION);
        expectedNamespaceIamRoleAuthorizationEntity.setId(ID);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey))
            .thenReturn(expectedNamespaceIamRoleAuthorizationEntity);

        // Call the method being tested.
        NamespaceIamRoleAuthorization response =
            namespaceIamRoleAuthorizationServiceImpl.updateNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey, expectedRequest);

        // Validate the results.
        assertEquals(ID, new Long(response.getId()));
        assertEquals(IAM_ROLE_DESCRIPTION_2, response.getIamRoleDescription());
        assertEquals(IAM_ROLE_NAME, response.getNamespaceIamRoleAuthorizationKey().getIamRoleName());
        assertEquals(NAMESPACE, response.getNamespaceIamRoleAuthorizationKey().getNamespace());

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verify(namespaceIamRoleAuthorizationDao).saveAndRefresh(expectedNamespaceIamRoleAuthorizationEntity);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testUpdateNamespaceIamRoleAuthorizationAssertErrorWhenDaoReturnsEmpty()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey =
            new NamespaceIamRoleAuthorizationKey(addWhitespace(NAMESPACE), addWhitespace(IAM_ROLE_NAME));
        NamespaceIamRoleAuthorizationUpdateRequest expectedRequest = new NamespaceIamRoleAuthorizationUpdateRequest(IAM_ROLE_DESCRIPTION_2);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        // Setup the interactions.
        when(namespaceDaoHelper.getNamespaceEntity(NAMESPACE)).thenReturn(expectedNamespaceEntity);
        when(namespaceIamRoleAuthorizationDao.getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey)).thenReturn(null);

        try
        {
            // Call the method being tested.
            namespaceIamRoleAuthorizationServiceImpl.updateNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey, expectedRequest);
            fail();
        }
        catch (Exception e)
        {
            // Validate the results.
            assertEquals(ObjectNotFoundException.class, e.getClass());
            assertEquals(String.format("Namespace IAM role authorization for namespace \"%s\" and IAM role name \"%s\" does not exist",
                namespaceIamRoleAuthorizationKey.getNamespace(), namespaceIamRoleAuthorizationKey.getIamRoleName()),
                e.getMessage());
        }

        // Verify the interactions.
        verify(namespaceDaoHelper).getNamespaceEntity(NAMESPACE);
        verify(namespaceIamRoleAuthorizationDao).getNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey);
        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    @Test
    public void testUpdateNamespaceIamRoleAuthorizationAssertErrorWhenRoleNameIsBlank()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(NAMESPACE, BLANK_TEXT);
        NamespaceIamRoleAuthorizationUpdateRequest expectedRequest = new NamespaceIamRoleAuthorizationUpdateRequest(IAM_ROLE_DESCRIPTION_2);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        try
        {
            // Call the method being tested.
            namespaceIamRoleAuthorizationServiceImpl.updateNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey, expectedRequest);
            fail();
        }
        catch (Exception e)
        {
            // Validate the results.
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("IAM role name must be specified", e.getMessage());
        }

        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }


    @Test
    public void testUpdateNamespaceIamRoleAuthorizationAssertErrorWhenNamespaceIsBlank()
    {
        // Setup the objects needed for the test.
        NamespaceIamRoleAuthorizationKey namespaceIamRoleAuthorizationKey = new NamespaceIamRoleAuthorizationKey(BLANK_TEXT, IAM_ROLE_NAME);
        NamespaceIamRoleAuthorizationUpdateRequest expectedRequest = new NamespaceIamRoleAuthorizationUpdateRequest(IAM_ROLE_DESCRIPTION_2);

        NamespaceEntity expectedNamespaceEntity = new NamespaceEntity();
        expectedNamespaceEntity.setCode(NAMESPACE);

        try
        {
            // Call the method being tested.
            namespaceIamRoleAuthorizationServiceImpl.updateNamespaceIamRoleAuthorization(namespaceIamRoleAuthorizationKey, expectedRequest);
            fail();
        }
        catch (Exception e)
        {
            // Validate the results.
            assertEquals(IllegalArgumentException.class, e.getClass());
            assertEquals("Namespace must be specified", e.getMessage());
        }

        verifyNoMoreInteractions(namespaceDaoHelper, namespaceIamRoleAuthorizationDao);
    }

    /**
     * Creates a new NamespaceIamRoleAuthorizationEntity from the given parameters.
     *
     * @param namespaceEntity The namespace entity
     * @param iamRoleName The IAM role name
     * @param iamRoleDescription The IAM role description
     *
     * @return The NamespaceIamRoleAuthorizationEntity
     */
    private NamespaceIamRoleAuthorizationEntity createNamespaceIamRoleAuthorizationEntity(NamespaceEntity namespaceEntity, String iamRoleName,
        String iamRoleDescription)
    {
        NamespaceIamRoleAuthorizationEntity namespaceIamRoleAuthorizationEntity = new NamespaceIamRoleAuthorizationEntity();
        namespaceIamRoleAuthorizationEntity.setNamespace(namespaceEntity);
        namespaceIamRoleAuthorizationEntity.setIamRoleName(iamRoleName.trim());
        if (StringUtils.isNotBlank(iamRoleDescription))
        {
            namespaceIamRoleAuthorizationEntity.setDescription(iamRoleDescription.trim());
        }
        return namespaceIamRoleAuthorizationEntity;
    }
}
