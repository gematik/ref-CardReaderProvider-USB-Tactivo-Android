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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.precisebiometrics.android.mtk.api.smartcardio.CardException;
import com.precisebiometrics.android.mtk.api.smartcardio.CardTerminal;
import com.precisebiometrics.android.mtk.api.smartcardio.CardTerminals;
import com.precisebiometrics.android.mtk.api.smartcardio.TerminalFactory;

import android.content.Context;
import android.content.IntentFilter;
import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.listener.InitializationStatus;
import de.gematik.ti.cardreader.provider.usb.tactivo.entities.TactivoCardReader;
import de.gematik.ti.openhealthcard.common.AbstractAndroidCardReaderController;

/**
 * include::{userguide}/UTACCRP_Overview.adoc[tag=TactivoCardReaderController]
 */
@Singleton
public final class TactivoCardReaderController extends AbstractAndroidCardReaderController {

    private volatile static TactivoCardReaderController instance;
    private final boolean initialized = false;
    private static final Logger LOG = LoggerFactory.getLogger(TactivoCardReaderController.class);
    private TactivoCallback callback;
    private boolean mktInitializeCompleted = false;
    private CompletableFuture<Boolean> waitForMktInit;
    private final Collection<ICardReader> cardReaders = new ArrayList<>();
    private List<CardTerminal> preciseTerminals = new ArrayList<>();
    private List<CardTerminal> newPreciseTerminals = new ArrayList<>();
    private final Map<ICardReader, TactivoCardChecker> cardCheckerMap = new HashMap<>();
    private Context context;

    private TactivoCardReaderController() {

    }

    private void init() {
        context = this.getContext();
        callback = new TactivoCallback(context, this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.precisebiometrics.android.mtk.DEVICE_ATTACHED");
        filter.addAction("com.precisebiometrics.android.mtk.DEVICE_DETACHED");
        context.registerReceiver(callback, filter);

    }

    /**
     * Returns an instance of TactivoCardReaderController
     *
     * @return TactivoCardReaderController
     */
    public static TactivoCardReaderController getInstance() {
        if (instance == null) {
            instance = new TactivoCardReaderController();
        }
        return instance;
    }

    /**
     * Returns a list of connected cardReaders
     *
     * @return list of connected cardReaders
     */
    @Override
    public Collection<ICardReader> getCardReaders() {
        checkContext();
        if (!mktInitializeCompleted) {
            checkIfMktInitializeComplete();
            LOG.debug("MKTInitialize completed");
        }
        return cardReaders;
    }

    private synchronized void findPreciseTerminals() {
        LOG.debug("findPreciseTerminals " + cardReaders.size());
        if (cardReaders.isEmpty()) {
            preciseTerminals.clear();
            preciseTerminals = getPreciseTerminals();
        }
        LOG.debug("findPreciseTerminals end " + cardReaders.size());
    }

    /**
     * Informs about new cardReader connection and adds the reader to cardReaders list
     *
     * @throws CardException
     */
    synchronized void addNewAndInform() throws CardException {
        newPreciseTerminals = getNewPreciseTerminals();
        findNewReaders(newPreciseTerminals);

    }

    /**
     * Informs about cardReader disconnection and removes the reader from cardReaders list
     *
     * @throws CardException
     */
    synchronized void removeAndInform() throws CardException {
        newPreciseTerminals = getNewPreciseTerminals();
        findRemovedReaders(newPreciseTerminals);
    }

    private synchronized void findNewReaders(List<CardTerminal> newPreciseTerminals) {
        for (CardTerminal newPreciseTerminal : newPreciseTerminals) {
            boolean isKnownReader = false;
            if (!cardReaders.isEmpty()) {
                for (ICardReader cardReader : cardReaders) {
                    if (newPreciseTerminal.getName().equals(cardReader.getName())) {
                        isKnownReader = true;
                    }
                }
            }
            if (!isKnownReader) {
                TactivoCardReader tactivoCardReader = new TactivoCardReader(newPreciseTerminal);
                cardReaders.add(tactivoCardReader);
                LOG.debug("Adding new TactivoCardReader: " + tactivoCardReader.getName());
                informAboutReaderConnection(tactivoCardReader, InitializationStatus.INIT_SUCCESS);
                LOG.debug("Tactivo: InformReaderConnection send");
            }
        }
    }

    @Override
    protected void informAboutReaderConnection(ICardReader cardReader, InitializationStatus initStatus) {
        super.informAboutReaderConnection(cardReader, initStatus);
        cardCheckerMap.put(cardReader, new TactivoCardChecker(cardReader));
    }

    private synchronized void findRemovedReaders(List<CardTerminal> newPreciseTerminals) {
        for (ICardReader cardReader : cardReaders) {
            boolean isRemoved = true;
            if (!newPreciseTerminals.isEmpty()) {
                for (CardTerminal newPrecisTerminal : newPreciseTerminals) {
                    if (cardReader.getName().equals(newPrecisTerminal.getName())) {
                        isRemoved = false;
                    }
                }
            }
            if (isRemoved) {
                informAboutReaderDisconnection(cardReader);
                LOG.debug("Tactivo: InformReaderDisconnection send");
                cardReaders.remove(cardReader);
                stopPreciseTerminalCardChecker(cardReader);

                LOG.debug("TactivoCardReader: " + cardReader.getName() + " removed");
            }
        }
    }

    private void stopPreciseTerminalCardChecker(ICardReader cardReader) {
        TactivoCardChecker tactivoCardChecker = cardCheckerMap.remove(cardReader);
        if (tactivoCardChecker != null) {
            tactivoCardChecker.shutdown();
        }
    }

    /**
     * Overrides the method for setting the application context to initialize the callback of the Precise Mobile Toolkit
     *
     * @param context
     */
    @Override
    public void setContext(Context context) {
        super.setContext(context);
        if (!initialized) {
            init();
        }
        intitializeMKT();
    }

    private void intitializeMKT() {
        callback.connect();
    }

    void setMktInitializeCompleted(boolean mktInitializeCompleted) {
        this.mktInitializeCompleted = mktInitializeCompleted;
        if (waitForMktInit != null) {
            waitForMktInit.complete(mktInitializeCompleted);
        }
    }

    private void checkIfMktInitializeComplete() {

        waitForMktInit = new CompletableFuture<>();

        new Thread() {
            @Override
            public void run() {
                try {
                    boolean result = waitForMktInit.get();
                    if (result) {
                        findPreciseTerminals();
                        return;
                    }
                } catch (ExecutionException e) {
                    LOG.error(e.toString(), e);
                } catch (InterruptedException e) {
                    LOG.error(e.toString(), e);
                }
            }
        }.start();
    }

    private synchronized List<CardTerminal> getPreciseTerminals() {
        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals terminals = factory.terminals();
        List<CardTerminal> preciseTerminalList = new ArrayList();

        new Thread() {
            @Override
            public void run() {

                List<CardTerminal> readCardTerminals;
                try {
                    readCardTerminals = terminals.list();
                    for (CardTerminal terminal : readCardTerminals) {
                        TactivoCardReader tactivoCardReader = new TactivoCardReader(terminal);
                        cardReaders.add(tactivoCardReader);
                        preciseTerminalList.add(terminal);
                        LOG.debug("Adding new TactivoCardReader: " + tactivoCardReader.getName());
                        informAboutReaderConnection(tactivoCardReader, InitializationStatus.INIT_SUCCESS);
                        LOG.debug("Tactivo: InformReaderConnection send");

                    }
                } catch (CardException e) {
                    LOG.error(e.toString(), e);
                }
            }
        }.start();

        return preciseTerminalList;
    }

    private synchronized List<CardTerminal> getNewPreciseTerminals() throws CardException {
        TerminalFactory factory = TerminalFactory.getDefault();

        CardTerminals terminals = factory.terminals();
        List<CardTerminal> preciseTerminalList = new ArrayList();
        List<CardTerminal> readCardTerminals;
        readCardTerminals = terminals.list();
        for (CardTerminal terminal : readCardTerminals) {
            preciseTerminalList.add(terminal);
        }

        return preciseTerminalList;
    }

}
