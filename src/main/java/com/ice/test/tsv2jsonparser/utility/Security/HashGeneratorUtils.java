package com.ice.test.tsv2jsonparser.utility.Security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Hash id utility class
 */
public class HashGeneratorUtils {

    private HashGeneratorUtils(){

    }

    /**
     * Genrates Hash using MD5 algorithm
     *
     * @param message
     * @return
     * @throws Exception
     */
    public static String generateMD5(String message) throws Exception {
        return hashString(message, "MD5");
    }

    /**
     * Genrates Hash using SHA-1 algorithm
     *
     * @param message
     * @return
     * @throws Exception
     */
    public static String generateSHA1(String message) throws Exception {
        return hashString(message, "SHA-1");
    }

    /**
     * Genrates Hash using SHA-256 algorithm
     *
     * @param message
     * @return
     * @throws Exception
     */
    public static String generateSHA256(String message) throws Exception {
        return hashString(message, "SHA-256");
    }

    /**
     * Method to generaye Hash String
     *
     * @param message
     * @param algorithm
     * @return
     * @throws Exception
     */
    private static String hashString(String message, String algorithm)
            throws Exception {

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new Exception(
                    "Could not generate hash from String", ex);
        }
    }

    /**
     * Method to convert Byte Array to Hexadecimal String
     *
     * @param arrayBytes
     * @return
     */
    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}
