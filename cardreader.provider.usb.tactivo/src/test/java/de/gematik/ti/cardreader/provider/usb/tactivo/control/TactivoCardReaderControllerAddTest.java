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

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.precisebiometrics.android.mtk.api.mtksmartcardio.MTKTerminals;
import com.precisebiometrics.android.mtk.api.smartcardio.ATR;
import com.precisebiometrics.android.mtk.api.smartcardio.CardException;
import com.precisebiometrics.android.mtk.api.smartcardio.CardTerminal;
import com.precisebiometrics.android.mtk.api.smartcardio.TerminalFactory;

import android.content.Context;
import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.cardreader.provider.api.listener.InitializationStatus;
import de.gematik.ti.cardreader.provider.usb.tactivo.Whitebox;
import de.gematik.ti.cardreader.provider.usb.tactivo.entities.TactivoCard;

public class TactivoCardReaderControllerAddTest {
    private static TactivoCardReaderController controller;
    private static Context context;
    private static ICardReader cardReader;
    private static ICard card;

    @BeforeClass
    public static void setUpBeforeClass() {
        context = new MockContext();
        controller = TactivoCardReaderController.getInstance();
        controller.setContext(context);
        cardReader = Mockito.mock(ICardReader.class);
        com.precisebiometrics.android.mtk.api.smartcardio.Card preciseCard = Mockito.mock(com.precisebiometrics.android.mtk.api.smartcardio.Card.class);
        Mockito.when(preciseCard.getATR()).thenReturn(
                new ATR(new byte[] { (byte) 0x3B, (byte) 0xDD, (byte) 0x00, (byte) 0xFF, (byte) 0x81, (byte) 0x50, (byte) 0xFE, (byte) 0xF0, (byte) 0x03,
                        (byte) 0x01, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00,
                        (byte) 0x00, (byte) 0x00 }));
        card = new TactivoCard(preciseCard);
        // Mockito.when(cardReader.connect()).thenReturn(card);
        int size = controller.getCardReaders().size();
        Assert.assertThat(size, Is.is(0));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        // try {
        // Mockito.doAnswer((a) -> {
        // return null;
        // }).when(card).disconnect(false);
        // } catch (de.gematik.ti.cardreader.provider.api.card.CardException e) {
        // Assert.fail(e.toString());
        // }
    }

    /**
     * TODO: PowerMock is until now required. After Removing PowerMock is this Test ignored
     */
    @Ignore
    @Test
    public void testAddNewAndInform() {
        try {
            // mock mtkTerminals
            MTKTerminals mtkTerminals = Mockito.mock(MTKTerminals.class);
            // mock factory
            TerminalFactory factory = new TerminalFactory();
            Mockito.when(factory.terminals()).thenReturn(mtkTerminals);
            List<CardTerminal> mtkTerminalList = new ArrayList() {
                {
                    CardTerminal aTerminal = Mockito.mock(CardTerminal.class);
                    add(aTerminal);
                }
            };
            Mockito.when(mtkTerminals.list()).thenReturn(mtkTerminalList);
            // mock TerminalFactory
            Whitebox.setStaticFinalInsternalState(TerminalFactory.class, "defaultFactory", factory);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
        try {
            controller.addNewAndInform();
            int size = controller.getCardReaders().size();
            Assert.assertThat(size, Is.is(1));
        } catch (CardException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testInformAboutReaderConnection() {
        InitializationStatus initStatus = InitializationStatus.INIT_SUCCESS;
        controller.informAboutReaderConnection(
                cardReader, initStatus);
        ExpectedException.none();
    }

    @Test
    public void testSetMktInitializeCompleted() {
        controller.setMktInitializeCompleted(true);
        ExpectedException.none();
    }

}
