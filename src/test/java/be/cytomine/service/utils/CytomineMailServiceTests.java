package be.cytomine.service.utils;

/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.CytomineCoreApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_SUPER_ADMIN", username = "superadmin")
@Transactional
public class CytomineMailServiceTests {

    @Autowired
    CytomineMailService cytomineMailService;

    @Test
    @Disabled("no spam please...:-)")
    public void test_send_email() throws MessagingException {
        cytomineMailService.send(
                "loic.rollus@cytomine.com",
                new String[] {"loic.rollus@cytomine.com"},
                new String[] {},
                new String[] {},
                "Congratulations",
                "HELLOOOOOOO!!!!"
                );
    }

}
