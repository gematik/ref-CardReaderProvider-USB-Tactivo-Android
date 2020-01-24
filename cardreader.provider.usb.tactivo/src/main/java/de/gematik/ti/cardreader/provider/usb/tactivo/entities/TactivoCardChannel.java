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

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.precisebiometrics.android.mtk.api.smartcardio.CardChannel;
import com.precisebiometrics.android.mtk.api.smartcardio.CommandAPDU;
import com.precisebiometrics.android.mtk.api.smartcardio.ResponseAPDU;

import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.cardreader.provider.api.card.ICardChannel;
import de.gematik.ti.cardreader.provider.api.command.ICommandApdu;
import de.gematik.ti.cardreader.provider.api.command.IResponseApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;

/**
 * include::{userguide}/UTACCRP_Overview.adoc[tag=TactivoCardChannel]
 */
public class TactivoCardChannel implements ICardChannel {

    private static final Logger LOG = LoggerFactory.getLogger(TactivoCardChannel.class);
    private static final String CARDREADER_BUFFER = "cardReader_Buffer";
    private static final String CARD_MAXAPDUBUFFERSIZE = "card_maxApduBufferSize";
    private static final String CARD_MAXRESPAPDUBUFFERSIZE = "card_maxRespApduBufferSize";
    // SecureMessaging-Values not required for the time being, but should stay here for future
    // private static final String CARD_MAXAPDUBUFFERSIZESM = "card_maxApduBufferSizeSM";
    // private static final String CARD_MAXRESPAPDUBUFFERSIZESM = "card_maxRespApduBufferSizeSM";

    private final TactivoCard tactivoCard;
    private final CardChannel preciseChannel;
    private static final Map<String, Integer> bufferSizeConfig = new LinkedHashMap() {
        {
            // If required configure following data in the android.App.Config
            put(CARDREADER_BUFFER, 4096);
            put(CARD_MAXAPDUBUFFERSIZE, 1033);
            put(CARD_MAXRESPAPDUBUFFERSIZE, 65535);
            // SecureMessaging-Values not required for the time being, but should stay here for future
            // put(CARD_MAXAPDUBUFFERSIZESM, 1033);
            // put(CARD_MAXRESPAPDUBUFFERSIZESM, 1033);
        }
    };

    TactivoCardChannel(TactivoCard tactivoCard, CardChannel preciseChannel) {
        this.tactivoCard = tactivoCard;
        this.preciseChannel = preciseChannel;
    }

    /**
     * Returns the connected card object
     *
     * @return TactivoCard
     */
    @Override
    public ICard getCard() {
        return tactivoCard;
    }

    /**
     * Returns the number of channel
     *
     * @return channelNo
     */
    @Override
    public int getChannelNumber() {
        return preciseChannel.getChannelNumber();
    }

    /**
     * @return
     */
    @Override
    public boolean isExtendedLengthSupported() {
        return getMaxMessageLength() > 255 && getMaxResponseLength() > 255;
    }

    /**
     *TODO: secureMessaging: Do only if it it required, channel must be know before if secureMessaging used.
     * @return
     */
    @Override
    public int getMaxMessageLength() {
        int maxMessageLengthCardReader = bufferSizeConfig.get(CARDREADER_BUFFER);
        int maxMessageLengthCard = bufferSizeConfig.get(CARD_MAXAPDUBUFFERSIZE);
        return Math.min(maxMessageLengthCard, maxMessageLengthCardReader);
    }

    /**
     *secureMessaging: Do only if it it required, channel must be know before if secureMessaging used.
     * @return
     */
    @Override
    public int getMaxResponseLength() {
        int maxResponseLengthCardReader = bufferSizeConfig.get(CARDREADER_BUFFER);
        int maxResponseLengthCard = bufferSizeConfig.get(CARD_MAXRESPAPDUBUFFERSIZE);
        return Math.min(maxResponseLengthCard, maxResponseLengthCardReader);
    }

    /**
     * Returns the responseAPDU after transmitting a commandAPDU
     *
     * @param command
     * @return responseAPDU
     * @throws CardException
     */
    @Override
    public IResponseApdu transmit(ICommandApdu command) throws CardException {
        byte[] tactivoCommand = command.getBytes();
        byte[] tactivoResponseBytes;
        IResponseApdu tactivoResponseADPU;
        CommandAPDU preciseCommand = new CommandAPDU(
                tactivoCommand);
        try {
            ResponseAPDU preciseResponseAPDU = preciseChannel.transmit(preciseCommand);
            tactivoResponseBytes = preciseResponseAPDU.getBytes();
            tactivoResponseADPU = new ResponseApdu(tactivoResponseBytes);
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
        return tactivoResponseADPU;
    }

    /**
     * Returns the length of the response byte array after sending an command APDU
     *
     * @param command
     * @param response
     * @return length of the response byte array
     * @throws CardException
     */
    public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
        try {
            return preciseChannel.transmit(command, response);
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
    }

    /**
     * Closes the logical channel
     * 
     * @throws CardException
     */
    @Override
    public void close() throws CardException {
        try {
            preciseChannel.close();
        } catch (com.precisebiometrics.android.mtk.api.smartcardio.CardException e) {
            throw new CardException(e.toString());
        }
    }

}
