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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.precisebiometrics.android.mtk.api.smartcardio.ATR;
import com.precisebiometrics.android.mtk.api.smartcardio.Card;
import com.precisebiometrics.android.mtk.api.smartcardio.CardChannel;
import com.precisebiometrics.android.mtk.api.smartcardio.ResponseAPDU;

import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.command.CommandApdu;
import de.gematik.ti.cardreader.provider.api.command.IResponseApdu;

public class TactivoCardChannelTest {
    private static final Logger LOG = LoggerFactory.getLogger(TactivoCardChannelTest.class);
    private static TactivoCardChannel tactivoCardChannel;
    private static TactivoCard tactivoCard;
    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // arrange preciseChannel
        CardChannel preciseChannel = Mockito.mock(CardChannel.class);
        ResponseAPDU resp = new ResponseAPDU(new byte[] { (byte) 0x90, (byte) 0x00 });
        Mockito.when(preciseChannel.transmit(Mockito.any())).thenReturn(resp);

        // arrange preciseCard
        Card preciseCard = Mockito.mock(Card.class);
        try {
            // arrange preciseATR
            ATR preciseATR = new ATR(
                    new byte[] { (byte) 0x3B, (byte) 0xDD, (byte) 0x00, (byte) 0xFF, (byte) 0x81, (byte) 0x50, (byte) 0xFE, (byte) 0xF0, (byte) 0x03,
                            (byte) 0x01, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00, (byte) 0x00
                    });
            // configure preciseCard with ATR and channel
            Mockito.when(preciseCard.getATR()).thenReturn(preciseATR);
            Mockito.when(preciseCard.getBasicChannel()).thenReturn(preciseChannel);

            // arrange tactivoCard
            tactivoCard = new TactivoCard(preciseCard);

            // arrange tactivoCardChannel
            tactivoCardChannel = new TactivoCardChannel(tactivoCard, preciseChannel);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testGetCard() {
        Assert.assertThat(tactivoCardChannel.getCard(), Is.is(tactivoCard));
    }

    @Test
    public void testGetChannelNumber() {
        Assert.assertThat(tactivoCardChannel.getChannelNumber(), Is.is(0));
    }

    @Test
    public void testTransmitCommandAPDU() {
        CommandApdu commandApdu = new CommandApdu(0x00, 0x22, 0x81, 0xA4);
        try {
            IResponseApdu resp = tactivoCardChannel.transmit(commandApdu);
            Assert.assertThat(resp.getBytes(), IsEqual.equalTo(new byte[] { (byte) 0x90, (byte) 0x00 }));
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testClose() {
        try {
            tactivoCardChannel.close();
            ExpectedException.none();
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void shouldSucceedExtendedLengthSupported() {
        LOG.info("------ Start " + testName.getMethodName());
        TactivoCard cardMock = Mockito.mock(TactivoCard.class);
        CardChannel cardChannel = Mockito.mock(CardChannel.class);
        TactivoCardChannel tactivoCardChannelLocal = new TactivoCardChannel(cardMock, cardChannel);
        boolean extendedLengthSupported = tactivoCardChannelLocal.isExtendedLengthSupported();
        Assert.assertThat(extendedLengthSupported, Is.is(true));
    }

    @Test
    public void testGetMaxMessageLength() {
        LOG.info("------ Start " + testName.getMethodName());
        TactivoCard cardMock = Mockito.mock(TactivoCard.class);
        CardChannel cardChannel = Mockito.mock(CardChannel.class);
        TactivoCardChannel tactivoCardChannelLocal = new TactivoCardChannel(cardMock, cardChannel);
        int maxMessageLength = tactivoCardChannelLocal.getMaxMessageLength();
        Assert.assertThat(maxMessageLength, Is.is(1033));
    }

    @Test
    public void testGetMaxResponseLength() {
        LOG.info("------ Start " + testName.getMethodName());
        TactivoCard cardMock = Mockito.mock(TactivoCard.class);
        CardChannel cardChannel = Mockito.mock(CardChannel.class);
        TactivoCardChannel tactivoCardChannelLocal = new TactivoCardChannel(cardMock, cardChannel);
        int maxResponseLength = tactivoCardChannelLocal.getMaxResponseLength();
        Assert.assertThat(maxResponseLength, Is.is(4096));
    }

}
