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

import org.finra.herd.dao.SesDao;
import org.finra.herd.dao.helper.AwsHelper;
import org.finra.herd.model.dto.AwsParamsDto;
import org.finra.herd.model.dto.EmailDto;
import org.finra.herd.service.SesService;

/**
 * The SES service implementation
 */
@Service
public class SesServiceImpl implements SesService
{
    @Autowired
    private AwsHelper awsHelper;

    @Autowired
    private SesDao sesDao;

    @Override
    public void sendEmail(final EmailDto emailDto)
    {
        // Fetch AWS information
        AwsParamsDto awsParamsDto = awsHelper.getAwsParamsDto();

        // Delegate to the corresponding DAO
        sesDao.sendEmail(awsParamsDto, emailDto);
    }
}
