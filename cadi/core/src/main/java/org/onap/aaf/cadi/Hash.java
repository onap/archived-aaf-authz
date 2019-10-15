/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.cadi;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    private static char hexDigit[] = "0123456789abcdef".toCharArray();

/////////////////////////////////
// MD5
/////////////////////////////////
    /**
     * Encrypt MD5 from Byte Array to Byte Array
     * @param input
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] hashMD5 (byte[] input) throws NoSuchAlgorithmException {
        // Note: Protect against Multi-thread issues with new MessageDigest 
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input); 
        return md.digest();
    }

    /**
     * Encrypt MD5 from Byte Array to Byte Array
     * @param input
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] hashMD5 (byte[] input, int offset, int length) throws NoSuchAlgorithmException {
        // Note: Protect against Multi-thread issues with new MessageDigest 
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input,offset,length); 
        return md.digest();
    }



    /**
     * Convenience Function: Encrypt MD5 from String to String Hex representation 
     * <p>
     * @param input
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String hashMD5asStringHex(String input) throws NoSuchAlgorithmException {
        byte[] output = hashMD5(input.getBytes());
        StringBuilder sb = new StringBuilder("0x");
         for (byte b : output) {
            sb.append(hexDigit[(b >> 4) & 0x0f]);
            sb.append(hexDigit[b & 0x0f]);
         }
         return sb.toString();
    }

/////////////////////////////////
// SHA256
/////////////////////////////////
    /**
     * SHA256 Hashing
     */
    public static byte[] hashSHA256(byte[] input) throws NoSuchAlgorithmException {
        // Note: Protect against Multi-thread issues with new MessageDigest 
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input); 
        return md.digest();
    }

    /**
     * SHA256 Hashing
     */
    public static byte[] hashSHA256(byte[] input, int offset, int length) throws NoSuchAlgorithmException {
        // Note: Protect against Multi-thread issues with new MessageDigest 
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input,offset,length); 
        return md.digest();
    }

    /**
     * Convenience Function: Hash from String to String Hex representation
     * <p>
     * @param input
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String hashSHA256asStringHex(String input) throws NoSuchAlgorithmException {
        return toHex(hashSHA256(input.getBytes()));
    }

    /**
     * Convenience Function: Hash from String to String Hex representation
     * <p>
     * @param input
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String hashSHA256asStringHex(String input, int salt) throws NoSuchAlgorithmException {
        byte[] in = input.getBytes();
        ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE + in.length);
        bb.putInt(salt);
        bb.put(input.getBytes());
        return toHex(Hash.hashSHA256(bb.array()));
    }

    /**
     * Compare two byte arrays for equivalency
     * @param ba1
     * @param ba2
     * @return
     */
    public static boolean isEqual(byte ba1[], byte ba2[]) {
        if (ba1.length!=ba2.length)return false;
        for (int i = 0;i<ba1.length; ++i) {
            if (ba1[i]!=ba2[i])return false;
        }
        return true;
    }

    public static int compareTo(byte[] a, byte[] b) {
        int end = Math.min(a.length, b.length);
        int compare = 0;
        for (int i=0;compare == 0 && i<end;++i) {
            compare = a[i]-b[i];
        }
        if (compare==0)compare=a.length-b.length;
        return compare;
    }

    public static String toHexNo0x(byte[] ba) {
        StringBuilder sb = new StringBuilder();
         for (byte b : ba) {
            sb.append(hexDigit[(b >> 4) & 0x0f]);
            sb.append(hexDigit[b & 0x0f]);
         }
         return sb.toString();
    }

    public static String toHex(byte[] ba) {
        StringBuilder sb = new StringBuilder("0x");
         for (byte b : ba) {
            sb.append(hexDigit[(b >> 4) & 0x0f]);
            sb.append(hexDigit[b & 0x0f]);
         }
         return sb.toString();
    }

    public static String toHex(byte[] ba, int start, int length) {
        StringBuilder sb = new StringBuilder("0x");
         for (int i=start;i<length;++i) {
            sb.append(hexDigit[(ba[i] >> 4) & 0x0f]);
            sb.append(hexDigit[ba[i] & 0x0f]);
         }
         return sb.toString();
    }


    public static byte[] fromHex(String s)  throws CadiException{
        if (!s.startsWith("0x")) {
            throw new CadiException("HexString must start with \"0x\"");
        }
        boolean high = true;
        int c;
        byte b;
        byte[] ba = new byte[(s.length()-2)/2];
        int idx;
        for (int i=2;i<s.length();++i) {
            c = s.charAt(i);
            if (c>=0x30 && c<=0x39) {
                b=(byte)(c-0x30);
            } else if (c>=0x61 && c<=0x66) {
                b=(byte)(c-0x57);  // account for "A"
            } else if (c>=0x41 && c<=0x46) {
                b=(byte)(c-0x37);
            } else {
                throw new CadiException("Invalid char '" + c + "' in HexString");
            }
            idx = (i-2)/2;
            if (high) {
                ba[idx]=(byte)(b<<4);
                high = false;
            } else {
                ba[idx]|=b;
                high = true;
            }
        }
        return ba;
    }

    /**
     * Does not expect to start with "0x"
     * if Any Character doesn't match, it returns null;
     * <p>
     * @param s
     * @return
     */
    public static byte[] fromHexNo0x(String s) {
        int c;
        byte b;
        byte[] ba;
        boolean high;
        int start;
        if (s.length()%2==0) {
            ba = new byte[s.length()/2];
            high=true;
            start=0;
        } else {
            ba = new byte[(s.length()/2)+1];
            high = false;
            start=1;
        }
        int idx;
        for (int i=start;i<s.length();++i) {
            c = s.charAt((i-start));
            if (c>=0x30 && c<=0x39) {
                b=(byte)(c-0x30);
            } else if (c>=0x61 && c<=0x66) {
                b=(byte)(c-0x57);  // account for "A"
            } else if (c>=0x41 && c<=0x46) {
                b=(byte)(c-0x37);
            } else {
                return null;
            }
            idx = i/2;
            if (high) {
                ba[idx]=(byte)(b<<4);
                high = false;
            } else {
                ba[idx]|=b;
                high = true;
            }
        }
        return ba;
    }

}
