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

import de.gematik.ti.cardreader.provider.api.card.Atr;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.CardProtocol;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.cardreader.provider.api.card.ICardChannel;

/**
 * include::{userguide}/UTACCRP_Overview.adoc[tag=TactivoCard]
 */
public class TactivoCard implements ICard {

    private final com.precisebiometrics.android.mtk.api.smartcardio.Card preciseCard;
    private final com.precisebiometrics.android.mtk.api.smartcardio.ATR preciseATR;
    private com.precisebiometrics.android.mtk.api.smartcardio.CardChannel channel;
    private final byte[] preciseATRbyte;
    private final Atr atr;

    public TactivoCard(com.precisebiometrics.android.mtk.api.smartcardio.Card preciseCard) {
        this.preciseCard = preciseCard;
        channel = preciseCard.getBasicChannel();
        preciseATR = preciseCard.getATR();
        preciseATRbyte = preciseATR.getBytes();
        atr = new Atr(preciseATRbyte);
    }

    /**
     * Returns the ATR of this card
     *
     * @return the ATR of this card.
     */
    @Override
    public Atr getATR() {
        return atr;
    }

    /**
     * Returns the protocol in use for this card, for example "T=0" or "T=1".
     *
     * @return the protocol in use for this card, for example "T=0" or "T=1".
     */
    @Override
    public CardProtocol getProtocol() {
        CardProtocol cardProtocol = null;
        String protocol = preciseCard.getProtocol();
        switch (protocol) {
            case "T=0":
                cardProtocol = CardProtocol.T0;
                break;
            case "T=1":
                cardProtocol = CardProtocol.T1;
                break;
            default:
                cardProtocol = CardProtocol.T15;

        }
        return cardProtocol;
    }

    @Override
    public ICardChannel openBasicChannel() throws CardException {
        return getBasicChannel();
    }

    /**
     * Returns the CardChannel for the basic logical channel. The basic logical channel has a channel number of 0.
     *
     * @return CardChannel for the basic logical channel. The basic logical channel has a channel number of 0.
     */
    public ICardChannel getBasicChannel() {
        channel = preciseCard.getBasicChannel();
        return new TactivoCardChannel(this, channel);
    }

    /**
     * Opens a new logical channel to the card and returns it.
     *
     * @return a new logical channel to the card
     * @throws CardException
     */
    @Override
    public ICardChannel openLogicalChannel() throws CardException {
        try {
            channel = preciseCard.openLogicalChannel();
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
        return new TactivoCardChannel(this, channel);
    }

    /**
     * Requests exclusive access to this card.
     *
     * Once a thread has invoked `beginExclusive`, only this thread is allowed to communicate with this card until it calls
     * `endExclusive`. Other threads attempting communication will receive a CardException.
     *
     * Applications have to ensure that exclusive access is correctly released. This can be achieved by executing the `beginExclusive()` and
     * `endExclusive` calls in a `try ... finally` block.
     */
    public void beginExclusive() throws CardException {
        try {
            preciseCard.beginExclusive();
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
    }

    /**
     * Releases the exclusive access previously established using `beginExclusive`.
     *
     * @throws CardException
     */
    public void endExclusive() throws CardException {
        try {
            preciseCard.endExclusive();
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException("This thread " + Thread.currentThread().getName() + " has no exclusive access and thus cannot terminate exclusive access");
        }
    }

    /**
     * Control commands not supported on Tactivo USB CardReader
     * @param controlCode
     * @param command
     * @return
     * @throws CardException
     */
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
        try {
            return preciseCard.transmitControlCommand(controlCode, command);
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException("This card reader does not support control commands.");
        }
    }

    /**
     * Disconnects the connection with this card.
     * 
     * @param reset
     * @throws CardException
     */
    @Override
    public void disconnect(boolean reset) throws CardException {
        try {
            preciseCard.disconnect(reset);
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
    }
}
