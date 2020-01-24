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

package de.gematik.ti.cardreader.provider.usb.tactivo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.precisebiometrics.android.mtk.api.smartcardio.TerminalFactory;

public class Whitebox {

    private static final Logger LOG = LoggerFactory.getLogger(Whitebox.class);

    public static void setStaticFinalInsternalState(Object target, String fieldName, Object object) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            LOG.debug("modifyInt befroe: " + modifiersField.getInt(field));
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            LOG.debug("modifyInt after: " + modifiersField.getInt(field));

            field.set(target, object);
        } catch (Exception e) {
            throw new RuntimeException(e + " occurred on fieldName " + fieldName);
        }

    }

    public static Object getInternalState(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e + " occurred on fieldName " + fieldName);
        }
    }

    public static void main(String[] args) {
        TerminalFactory factory = new TerminalFactory();
        LOG.debug("before: " + TerminalFactory.getDefault());
        setStaticFinalInsternalState(TerminalFactory.class, "defaultFactory", factory);
        LOG.debug("after: " + TerminalFactory.getDefault());

    }
}
