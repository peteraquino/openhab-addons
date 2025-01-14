/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rotel.internal.protocol.ascii;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.binding.rotel.internal.communication.RotelCommand;
import org.openhab.binding.rotel.internal.protocol.RotelProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the Rotel ASCII V1 protocol (build of command messages, decoding of incoming data)
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelAsciiV1ProtocolHandler extends RotelAbstractAsciiProtocolHandler {

    private static final char CHAR_END_RESPONSE = '!';

    private final Logger logger = LoggerFactory.getLogger(RotelAsciiV1ProtocolHandler.class);

    /**
     * Constructor
     *
     * @param model the Rotel model in use
     */
    public RotelAsciiV1ProtocolHandler(RotelModel model) {
        super(model, CHAR_END_RESPONSE);
    }

    @Override
    public RotelProtocol getProtocol() {
        return RotelProtocol.ASCII_V1;
    }

    @Override
    public byte[] buildCommandMessage(RotelCommand cmd, @Nullable Integer value) throws RotelException {
        String messageStr = cmd.getAsciiCommandV1();
        if (messageStr == null) {
            throw new RotelException("Command \"" + cmd.getLabel() + "\" ignored: not available for ASCII V1 protocol");
        }
        if (value != null) {
            switch (cmd) {
                case VOLUME_SET:
                    messageStr += String.format("%d", value);
                    break;
                case BASS_SET:
                case TREBLE_SET:
                    if (value == 0) {
                        messageStr += "000";
                    } else if (value > 0) {
                        messageStr += String.format("+%02d", value);
                    } else {
                        messageStr += String.format("-%02d", -value);
                    }
                    break;
                case BALANCE_SET:
                    if (value == 0) {
                        messageStr += "000";
                    } else if (value > 0) {
                        messageStr += String.format("R%02d", value);
                    } else {
                        messageStr += String.format("L%02d", -value);
                    }
                    break;
                case DIMMER_LEVEL_SET:
                    if (value > 0 && model.getDimmerLevelMin() < 0) {
                        messageStr += String.format("+%d", value);
                    } else {
                        messageStr += String.format("%d", value);
                    }
                    break;
                default:
                    break;
            }
        }
        if (!messageStr.endsWith("?")) {
            messageStr += "!";
        }
        byte[] message = messageStr.getBytes(StandardCharsets.US_ASCII);
        logger.debug("Command \"{}\" => {}", cmd, messageStr);
        return message;
    }
}
