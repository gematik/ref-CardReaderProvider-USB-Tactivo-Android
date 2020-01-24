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

package de.gematik.ti.cardreader.provider.usb.tactivo.entities;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.precisebiometrics.android.mtk.api.smartcardio.ATR;
import com.precisebiometrics.android.mtk.api.smartcardio.Card;
import com.precisebiometrics.android.mtk.api.smartcardio.CardTerminal;

import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.ICard;

public class TactivoCardReaderTest {
    private static TactivoCardReader tactivoCardReader;
    private static Card preciseCard;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        CardTerminal cardTerminal = Mockito.mock(CardTerminal.class);
        Mockito.when(cardTerminal.getName()).thenReturn("test-dummy");
        Mockito.when(cardTerminal.isCardPresent()).thenReturn(false);
        Mockito.when(cardTerminal.waitForCardAbsent(1000)).thenReturn(true);
        Mockito.when(cardTerminal.waitForCardPresent(1000)).thenReturn(false);
        preciseCard = Mockito.mock(Card.class);
        Mockito.when(cardTerminal.connect("T=1")).thenReturn(preciseCard);
        ATR preciseATR = new ATR(
                new byte[] { (byte) 0x3B, (byte) 0xDD, (byte) 0x00, (byte) 0xFF, (byte) 0x81, (byte) 0x50, (byte) 0xFE, (byte) 0xF0, (byte) 0x03,
                        (byte) 0x01, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00,
                        (byte) 0x00, (byte) 0x00
                });
        // configure preciseCard with ATR and channel
        Mockito.when(preciseCard.getATR()).thenReturn(preciseATR);
        tactivoCardReader = new TactivoCardReader(cardTerminal);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testConnect() {
        try {
            ICard iCard = tactivoCardReader.connect();
            Assert.assertThat(iCard, IsNull.notNullValue());
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testGetName() {
        Assert.assertThat(tactivoCardReader.getName(), Is.is("test-dummy"));
    }

    @Test
    public void testIsCardPresent() {
        try {
            Assert.assertThat(tactivoCardReader.isCardPresent(), Is.is(false));
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testWaitForCardAbsent() {
        try {
            boolean result = tactivoCardReader.waitForCardAbsent(1000);
            Assert.assertThat(result, Is.is(true));
        } catch (CardException e) {
            Assert.fail(e.toString());
        }

    }

    @Test
    public void testWaitForCardPresent() {
        try {
            boolean result = tactivoCardReader.waitForCardPresent(1000);
            Assert.assertThat(result, Is.is(false));
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

}
