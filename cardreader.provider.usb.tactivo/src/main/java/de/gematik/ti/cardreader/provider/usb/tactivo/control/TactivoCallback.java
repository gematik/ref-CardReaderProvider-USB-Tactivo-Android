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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.precisebiometrics.android.mtk.api.PBInitializedCallback;
import com.precisebiometrics.android.mtk.api.mtksmartcardio.MTKTerminals;
import com.precisebiometrics.android.mtk.api.smartcardio.CardException;
import com.precisebiometrics.android.mtk.manager.PBDeviceType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * include::{userguide}/UTACCRP_Overview.adoc[tag=TactivoCallback]
 */
public class TactivoCallback extends BroadcastReceiver implements PBInitializedCallback {

    private final Context context;
    private final TactivoCardReaderController controller;
    private int terminalId = -1;
    int terminalType = -1;

    private static final Logger LOG = LoggerFactory.getLogger(TactivoCallback.class);

    /**
     * Constructor
     * 
     * @param context
     * @param controller
     */
    public TactivoCallback(Context context, TactivoCardReaderController controller) {
        this.context = context;
        this.controller = controller;
    }

    /**
     * Connect/initialize CardReader
     */
    public void connect() {
        MTKTerminals.initialize(context, this);
    }

    /**
     * Displays a message if initialization failed
     */
    @Override
    public void initializationFailed() {
        LOG.debug("Tactivo Callback initialization failed, is Tactivo Manager App installed?");
    }

    /**
     * Sends true on TactivoCardReaderController if the Tactivo cardReader is initialized
     */
    @Override
    public void initialized() {
        controller.setMktInitializeCompleted(true);
        LOG.debug("Tactivo ready to use the smart card API");
        LOG.debug("initialized(): going to read Tactivo cardReader devices");
    }

    /**
     * Displays a message Tactivo Smart card API was uninitialized at some point, and cannot be used any more
     */
    @Override
    public void uninitialized() {
        controller.setMktInitializeCompleted(false);
        MTKTerminals.initialize(context, this);
        LOG.debug("Tactivo Smart card API was uninitialized at some point, and cannot be\n"
                + " used any more, to retry MTKTerminals.initialize() must be called again.");
    }

    /**
     * Handled the received intent actions
     * 
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        terminalId = intent.getIntExtra("id", -1);
        terminalType = intent.getIntExtra("type", -1);

        if (terminalType == PBDeviceType.DRIVER_TYPE_SMARTCARD || terminalType == PBDeviceType.DRIVER_TYPE_SMARTCARD_AND_BIOMETRIC_SENSOR) {
            switch (action) {
                case "com.precisebiometrics.android.mtk.DEVICE_ATTACHED":

                    LOG.debug("Tactivo-Receiver: onDeviceAttached: " + terminalId);
                    try {
                        onDeviceAttached();
                    } catch (CardException e) {
                        LOG.error(e.toString(), e);
                    }
                    break;
                case "com.precisebiometrics.android.mtk.DEVICE_DETACHED":
                    LOG.debug("Tactivo-Receiver: onDeviceDetached: " + terminalId);
                    try {
                        onDeviceDetached();
                    } catch (CardException e) {
                        LOG.error(e.toString(), e);
                    }
                    break;
            }
        }
    }

    private void onDeviceDetached() throws CardException {
        controller.removeAndInform();
    }

    private void onDeviceAttached() throws CardException {
        controller.addNewAndInform();
    }
}
