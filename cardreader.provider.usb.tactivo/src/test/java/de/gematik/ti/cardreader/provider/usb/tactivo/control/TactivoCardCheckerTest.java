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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.usb.tactivo.entities.TactivoCardReader;

public class TactivoCardCheckerTest {

    private static TactivoCardChecker tactivoCardChecker;
    private static ICardReader cardReader;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        cardReader = Mockito.mock(TactivoCardReader.class);
        Mockito.when(((TactivoCardReader) cardReader).waitForCardPresent(5000)).thenReturn(true);
        Mockito.when(((TactivoCardReader) cardReader).waitForCardAbsent(5000)).thenReturn(true);
        //
        tactivoCardChecker = new TactivoCardChecker(cardReader);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

}
