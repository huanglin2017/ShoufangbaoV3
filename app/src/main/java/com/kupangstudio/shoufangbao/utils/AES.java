/**
 * LICENSE AND TRADEMARK NOTICES
 * <p/>
 * Except where noted, sample source code written by Motorola Mobility Inc. and
 * provided to you is licensed as described below.
 * <p/>
 * Copyright (c) 2012, Motorola, Inc.
 * All  rights reserved except as otherwise explicitly indicated.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p/>
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * - Neither the name of Motorola, Inc. nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * <p/>
 * Other source code displayed may be licensed under Apache License, Version
 * 2.
 * <p/>
 * Copyright ¬© 2012, Android Open Source Project. All rights reserved unless
 * otherwise explicitly indicated.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0.
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

// Please refer to the accompanying article at 
// http://developer.motorola.com/docs/using_the_advanced_encryption_standard_in_android/

package com.kupangstudio.shoufangbao.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private final String KEY_GENERATION_ALG = "PBKDF2WithHmacSHA1";

    private final int HASH_ITERATIONS = 10000;
    private final int KEY_LENGTH = 256;

    private char[] humanPassphrase = { 'P', 'e', 'r', ' ', 'v', 'a', 'l', 'l',
            'u', 'm', ' ', 'd', 'u', 'c', 'e', 's', ' ', 'L', 'a', 'b', 'a',
            'n', 't' };

    // char[] humanPassphrase = { 'v', 't', 'i', 'o', 'n','s','f','o','t', '.',
    // 'c', 'o', 'm',
    // 'p'};
    private byte[] salt = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD,
            0xE, 0xF }; // must save this for next time we want the key

    private PBEKeySpec myKeyspec = new PBEKeySpec(humanPassphrase, salt,
            HASH_ITERATIONS, KEY_LENGTH);
    private final String CIPHERMODEPADDING = "AES/CBC/PKCS7Padding";
    private final String CIPHERMODEPADDING2 = "AES";



    private SecretKeyFactory keyfactory = null;
    private SecretKey sk = null;
    private SecretKeySpec skforAES = null;

    private IvParameterSpec IV;

    public AES(String password) {


        try {
            keyfactory = SecretKeyFactory.getInstance(KEY_GENERATION_ALG);

        } catch (NoSuchAlgorithmException nsae) {
        }
//        try{
//            myKeyspec = new PBEKeySpec(password.toCharArray(), salt,
//                    HASH_ITERATIONS, KEY_LENGTH);
//            sk = keyfactory.generateSecret(myKeyspec);
//
//            // This is our secret key. We could just save this to a file instead of
//            // regenerating it
//            // each time it is needed. But that file cannot be on the device (too
//            // insecure). It could
//            // be secure if we kept it on a server accessible through https.
//            byte[] skAsByteArray = sk.getEncoded();
            // Log.d("",
            // "skAsByteArray=" + skAsByteArray.length + ","
            // + Base64Encoder.encode(skAsByteArray));
            skforAES = new SecretKeySpec(password.getBytes(), "AES");
//        } catch (InvalidKeySpecException ikse) {
//        }
        IV = new IvParameterSpec("0102030405060708".getBytes());

    }

    public String encrypt(String text) {
        if(text == null){
            text = "";
        }
        byte[] plaintext = null;
        try {
            plaintext = text.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] ciphertext = encrypt(CIPHERMODEPADDING, skforAES, IV, plaintext);
        String base64_ciphertext = Base64Encoder.encode(ciphertext);
        return base64_ciphertext;
    }

    public String decrypt(String ciphertext_base64) {
        byte[] s = Base64Decoder.decodeToBytes(ciphertext_base64);
        String decrypted = new String(decrypt(CIPHERMODEPADDING, skforAES, IV,
                s));
        return decrypted;
    }

    // Use this method if you want to add the padding manually
    // AES deals with messages in blocks of 16 bytes.
    // This method looks at the length of the message, and adds bytes at the end
    // so that the entire message is a multiple of 16 bytes.
    // the padding is a series of bytes, each set to the total bytes added (a
    // number in range 1..16).
    private byte[] addPadding(byte[] plain) {
        byte plainpad[] = null;
        int shortage = 16 - (plain.length % 16);
        // if already an exact multiple of 16, need to add another block of 16
        // bytes
        if (shortage == 0)
            shortage = 16;

        // reallocate array bigger to be exact multiple, adding shortage bits.
        plainpad = new byte[plain.length + shortage];
        for (int i = 0; i < plain.length; i++) {
            plainpad[i] = plain[i];
        }
        for (int i = plain.length; i < plain.length + shortage; i++) {
            plainpad[i] = (byte) shortage;
        }
        return plainpad;
    }

    // Use this method if you want to remove the padding manually
    // This method removes the padding bytes
    private byte[] dropPadding(byte[] plainpad) {
        byte plain[] = null;
        int drop = plainpad[plainpad.length - 1]; // last byte gives number of
        // bytes to drop

        // reallocate array smaller, dropping the pad bytes.
        plain = new byte[plainpad.length - drop];
        for (int i = 0; i < plain.length; i++) {
            plain[i] = plainpad[i];
            plainpad[i] = 0; // don't keep a copy of the decrypt
        }
        return plain;
    }

    private byte[] encrypt(String cmp, SecretKey sk, IvParameterSpec IV,
                           byte[] msg) {
        try {
            Cipher c = Cipher.getInstance(cmp);
            c.init(Cipher.ENCRYPT_MODE, sk, IV);
            return c.doFinal(msg);
        } catch (NoSuchAlgorithmException nsae) {
        } catch (NoSuchPaddingException nspe) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        }
        return null;
    }


    private byte[] decrypt(String cmp, SecretKey sk, IvParameterSpec IV,
                           byte[] ciphertext) {
        try {
            Cipher c = Cipher.getInstance(cmp);
            c.init(Cipher.DECRYPT_MODE, sk, IV);
            return c.doFinal(ciphertext);
        } catch (NoSuchAlgorithmException nsae) {
        } catch (NoSuchPaddingException nspe) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

}