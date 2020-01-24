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
import org.hamcrest.core.IsEqual;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.precisebiometrics.android.mtk.api.smartcardio.ATR;
import com.precisebiometrics.android.mtk.api.smartcardio.Card;
import com.precisebiometrics.android.mtk.api.smartcardio.CardChannel;

import de.gematik.ti.cardreader.provider.api.card.Atr;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.CardProtocol;
import de.gematik.ti.cardreader.provider.api.card.ICardChannel;

public class TactivoCardTest {

    private static TactivoCard tactivoCard;
    private static CardChannel mockBasechannel;
    private static Card precisedCard;
    private static CardChannel newLogicalChannel;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // mock card
        precisedCard = Mockito.mock(Card.class);
        Mockito.when(precisedCard.getATR()).thenReturn(
                new ATR(new byte[] { (byte) 0x3B, (byte) 0xDD, (byte) 0x00, (byte) 0xFF, (byte) 0x81, (byte) 0x50, (byte) 0xFE, (byte) 0xF0, (byte) 0x03,
                        (byte) 0x01, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00,
                        (byte) 0x00, (byte) 0x00 }));
        Mockito.when(precisedCard.getProtocol()).thenReturn("T=0");
        int command = 0;
        byte[] cmd = new byte[] { 0x00, (byte) 0xA4, 0x00, 0x00 };
        Mockito.when(precisedCard.transmitControlCommand(command, cmd)).thenReturn(new byte[] { (byte) 0x90, 0x00 });
        newLogicalChannel = Mockito.mock(CardChannel.class);
        Mockito.when(precisedCard.openLogicalChannel()).thenReturn(newLogicalChannel);
        Mockito.when(newLogicalChannel.getChannelNumber()).thenReturn(1);

        // mock channel
        mockBasechannel = Mockito.mock(CardChannel.class);
        Mockito.when(precisedCard.getBasicChannel()).thenReturn(mockBasechannel);
        // Mockito.when(mockBasechannel.getCard()).thenReturn(precisedCard);

        tactivoCard = new TactivoCard(precisedCard);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    @Test
    public void testGetATR() {
        Atr atr = tactivoCard.getATR();
        Assert.assertThat(atr.getBytes(),
                IsEqual.equalTo(
                        new byte[] { (byte) 0x3B, (byte) 0xDD, (byte) 0x00, (byte) 0xFF, (byte) 0x81, (byte) 0x50, (byte) 0xFE, (byte) 0xF0, (byte) 0x03,
                                (byte) 0x01, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x00 }));
    }

    @Test
    public void testGetProtocol() {
        Assert.assertThat(tactivoCard.getProtocol(), Is.is(CardProtocol.T0));
    }

    @Test
    public void testGetBasicChannel() {
        Assert.assertThat(tactivoCard.getBasicChannel().getCard(), Is.is(tactivoCard));
    }

    @Test
    public void testOpenLogicalChannel() {
        try {
            ICardChannel cardChannel = tactivoCard.openLogicalChannel();
            Assert.assertThat(cardChannel.getChannelNumber(), Is.is(1));
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testTransmitControlCommand() {
        int command = 0;
        byte[] b = new byte[] { 0x00, (byte) 0xA4, 0x00, 0x00 };
        try {
            byte[] result = tactivoCard.transmitControlCommand(command, b);
            Assert.assertThat(result, IsEqual.equalTo(new byte[] { (byte) 0x90, 0x00 }));
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testDisconnect() {
        try {
            tactivoCard.disconnect(true);
            ExpectedException.none();
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

}
