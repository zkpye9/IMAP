package com.icegreen.greenmail.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Helper for handling encodings.
 */
public class EncodingUtil {
    /**
     * Constant for 8-Bit encoding, which can be resembled by {@value #EIGHT_BIT_ENCODING}
     */
    public static final String EIGHT_BIT_ENCODING = "ISO-8859-1";
    /**
     * Predefined Charset for 8-Bit encoding.
     */
    public static final Charset CHARSET_EIGHT_BIT_ENCODING = Charset.forName(EIGHT_BIT_ENCODING);

    private EncodingUtil() {
        // No instantiation.
    }

    /**
     * Converts the string of given content to an input stream.
     *
     * @param content the string content.
     * @param charset the charset for conversion.
     * @return the stream (should be closed by invoker).
     */
    public static InputStream toStream(String content, Charset charset) {
        byte[] bytes = content.getBytes(charset);
        return new ByteArrayInputStream(bytes);
    }
}
