/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.util;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class Obfuscator {

    private static final byte OFFSET = -17;


    /**
     * Obfuscates the input into an unidentifiable text.
     *
     * @param input The string to hide the content of.
     * @return The obfuscated string.
     */
    public static String obfuscate(String input) {
        // create bytes from the string
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        // offset
        byte[] offsetted = new byte[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            byte current = bytes[i];
            if (current + OFFSET < 0) {
                offsetted[i] = (byte)(0xff + (current + OFFSET));
            } else {
                offsetted[i] = (byte)(current + OFFSET);
            }
        }
        // byte value and order invert
        byte[] unordered = new byte[offsetted.length];
        for (int i = 0; i < offsetted.length; ++i) {
            unordered[unordered.length - i - 1] = (byte)(~offsetted[i] & 0xff);
        }
        // base64 encode
        byte[] result = Base64.encode(unordered, Base64.DEFAULT);
        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * Deobfuscates the string using our own methods
     *
     * @param input The string to deobfuscate.
     * @return The result, which should equal with the input string of the obfuscation method.
     */
    public static String deObfuscate(String input) {
        // Input should be first Base64 decoded.
        byte[] base64Decoded = Base64.decode(input, Base64.DEFAULT);
        // Bytes are inverted in value and also order
        byte[] ordered = new byte[base64Decoded.length];
        for (int i = 0; i < base64Decoded.length; ++i) {
            ordered[ordered.length - i - 1] = (byte)(~base64Decoded[i] & 0xff);
        }
        // they also have an offset
        byte[] result = new byte[ordered.length];
        for (int i = 0; i < ordered.length; ++i) {
            byte current = ordered[i];
            if (current - OFFSET > 0xff) {
                result[i] = (byte)(current - OFFSET - 0xff);
            } else {
                result[i] = (byte)(current - OFFSET);
            }
        }
        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * With this method you can test if the obfuscator truly works on any string.
     *
     * @param input The string to test.
     * @return True if the obfuscator works.
     */
    public static boolean test(String input) {
        String probe = deObfuscate(obfuscate(input));
        return input.equals(probe);
    }
}