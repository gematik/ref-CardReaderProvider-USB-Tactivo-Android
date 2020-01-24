/*
 * Copyright (c) 2020 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.ti.cardreader.provider.usb.tactivo.control;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TactivoCardReaderProviderTest {

    private static TactivoCardReaderProvider tactivoCardReaderProvider;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        tactivoCardReaderProvider = new TactivoCardReaderProvider();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testGetCardReaderController() {
        Assert.assertThat(tactivoCardReaderProvider.getCardReaderController(), IsNull.notNullValue());
    }

    @Test
    public void testGetDescriptor() {

        Assert.assertThat(tactivoCardReaderProvider.getDescriptor().getName(), Is.is("Gematik Tactivo-Provider"));
        Assert.assertThat(tactivoCardReaderProvider.getDescriptor().getDescription(), Is.is("This Provider makes Tactivo Cardreader available."));
        Assert.assertThat(tactivoCardReaderProvider.getDescriptor().getLicense(), Is.is("Gematik internernal use only, details tbd"));

    }

}
