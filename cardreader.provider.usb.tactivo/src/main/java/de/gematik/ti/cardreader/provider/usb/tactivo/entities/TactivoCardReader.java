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

import com.precisebiometrics.android.mtk.api.smartcardio.Card;

import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.ICard;

/**
 * include::{userguide}/UTACCRP_Overview.adoc[tag=TactivoCardReader]
 */
public class TactivoCardReader implements ICardReader {

    private final com.precisebiometrics.android.mtk.api.smartcardio.CardTerminal preciseTerminal;
    private Card preciseCard = null;

    private static final String PROTOCOL_T1 = "T=1";

    /**
     * Constructor
     * 
     * @param preciseTerminal
     */
    public TactivoCardReader(com.precisebiometrics.android.mtk.api.smartcardio.CardTerminal preciseTerminal) {
        this.preciseTerminal = preciseTerminal;
    }

    /**
     * not implemented
     */
    @Override
    public void initialize() {
    }

    /**
     * Returns the current initialisation status
     *
     * @return
     *          true: if cardReader is initialized
     *          false: cardReader not operational
     */
    @Override
    public boolean isInitialized() {
        return true;
    }

    /**
     * Establishes a connection to the card. If a connection has previously established this method returns a card object with protocol "T=1".
     * 
     * @return
     *          card object
     * @throws CardException
     */
    @Override
    public ICard connect() throws CardException {
        try {
            preciseCard = preciseTerminal.connect(PROTOCOL_T1);
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
        return new TactivoCard(preciseCard);
    }

    /**
     * Returns the unique name of this cardReader.
     *
     * @return this.name
     */
    @Override
    public String getName() {
        return preciseTerminal.getName();
    }

    /**
     * Returns whether a card is present in this cardReader.
     *
     * @return
     *          true if card is present
     *          false if card is not present
     */
    @Override
    public boolean isCardPresent() throws CardException {

        try {
            return preciseTerminal.isCardPresent();
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
    }

    /**
     * Waits until a card is absent in this reader or the timeout expires. If the method returns due to an expired timeout, it returns false. Otherwise it
     * return true.
     *
     * @param timeout
     * @return
     */
    public boolean waitForCardAbsent(long timeout) throws CardException {
        try {
            return preciseTerminal.waitForCardAbsent(timeout);
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
    }

    /**
     * Waits until a card is present in this reader or the timeout expires. If the method returns due to an expired timeout, it returns false. Otherwise it
     * return true.
     *
     * @param timeout
     * @return
     */
    public boolean waitForCardPresent(long timeout) throws CardException {
        try {
            return preciseTerminal.waitForCardPresent(timeout);
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
    }
}
