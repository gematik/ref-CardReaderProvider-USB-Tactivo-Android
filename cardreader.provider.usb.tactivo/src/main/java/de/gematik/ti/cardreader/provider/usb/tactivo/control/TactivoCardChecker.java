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

import de.gematik.ti.cardreader.provider.api.CardEventTransmitter;
import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.usb.tactivo.entities.TactivoCardReader;

/**
 * include::{userguide}/UTACCRP_Overview.adoc[tag=TactivoCardChecker]
 */
public class TactivoCardChecker extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(TactivoCardChecker.class);

    private final ICardReader cardReader;
    private boolean active = true;
    private static final int TIMEOUT = 5000;
    private final CardEventTransmitter cardEventTransmitter;
    private boolean currentCardState;

    /**
     * Create and Start new checker thread
     * @param cardReader
     */
    public TactivoCardChecker(ICardReader cardReader) {
        this.cardReader = cardReader;
        cardEventTransmitter = TactivoCardReaderController.getInstance().createCardEventTransmitter(cardReader);
        start();
    }

    /**
     * Stop the Checker and could not restarted
     */
    public void shutdown() {
        active = false;
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            LOG.debug("Start Timer error", e);
        }
        currentCardState = isCardPresent();
        if (currentCardState) {
            cardEventTransmitter.informAboutCardPresent();
        }
        while (active) {
            try {
                if (currentCardState) {
                    ((TactivoCardReader) cardReader).waitForCardAbsent(TIMEOUT);
                } else {
                    ((TactivoCardReader) cardReader).waitForCardPresent(TIMEOUT);
                }
                if (active) {
                    checkCardStateAndSendEvent();
                }

            } catch (CardException e) {
                LOG.error("Card State Unknown", e);
                cardEventTransmitter.informAboutCardUnknown();
            }

        }
    }

    private void checkCardStateAndSendEvent() throws CardException {
        if (currentCardState != cardReader.isCardPresent()) {
            if (cardReader.isCardPresent()) {
                cardEventTransmitter.informAboutCardPresent();
            } else {
                cardEventTransmitter.informAboutCardAbsent();
            }
            currentCardState = cardReader.isCardPresent();
        }
    }

    private boolean isCardPresent() {
        try {
            return cardReader.isCardPresent();
        } catch (CardException e) {
            LOG.error("Card State Unknown", e);
            return false;
        }
    }
}
