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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.precisebiometrics.android.mtk.api.mtksmartcardio.MTKTerminals;
import com.precisebiometrics.android.mtk.api.smartcardio.CardTerminal;
import com.precisebiometrics.android.mtk.api.smartcardio.TerminalFactory;
import com.precisebiometrics.android.mtk.manager.PBDeviceType;

import android.content.Context;
import android.content.Intent;
import de.gematik.ti.cardreader.provider.usb.tactivo.Whitebox;

public class TactivoCallbackTest {
    private static TactivoCallback tactivoCallback;
    private static TactivoCardReaderController controller;
    private static Context context;
    private static final Logger LOG = LoggerFactory.getLogger(TactivoCallbackTest.class);
    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        context = new MockContext();
        controller = TactivoCardReaderController.getInstance();
        controller.setContext(context);
        tactivoCallback = new TactivoCallback(context, controller);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testOnReceive() {
        LOG.info("--------- " + name.getMethodName() + " ---------");
        // mock intent
        Intent intent = Mockito.mock(Intent.class);
        Mockito.when(intent.getAction()).thenReturn("com.precisebiometrics.android.mtk.DEVICE_ATTACHED");
        Mockito.when(intent.getIntExtra("id", 0)).thenReturn(0);
        Mockito.when(intent.getIntExtra("type", 0)).thenReturn(PBDeviceType.DRIVER_TYPE_SMARTCARD_AND_BIOMETRIC_SENSOR);
        Mockito.when(intent.getIntExtra("type", -1)).thenReturn(PBDeviceType.DRIVER_TYPE_SMARTCARD_AND_BIOMETRIC_SENSOR);
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
            LOG.error(e.toString(), e);
        }

        tactivoCallback.onReceive(context, intent);
        // Assert.assertThat(controller.getCardReaders().size(), Is.is(1));
    }
}
