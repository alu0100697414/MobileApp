package com.tfg.jose.proteccionpersonas;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyAgreement;

/**
 * Created by jose on 4/06/16.
 */
public class KeysReader {

    public static PublicKey getPublic(Context mContext, int id) throws InvalidKeySpecException, NoSuchProviderException, NoSuchAlgorithmException {

        InputStream is = mContext.getResources().openRawResource(id);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<String>();
        String line = null;
        try {
            while ((line = br.readLine()) != null)
                lines.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // removes the first and last lines of the file (comments)
        if (lines.size() > 1 && lines.get(0).startsWith("-----") && lines.get(lines.size()-1).startsWith("-----")) {
            lines.remove(0);
            lines.remove(lines.size()-1);
        }

        // concats the remaining lines to a single String
        StringBuilder sb = new StringBuilder();
        for (String aLine: lines)
            sb.append(aLine);
        String keyString = sb.toString();
        Log.i("ECDH", "Clave pública leida: " + keyString);

        // converts the String to a PublicKey instance
        byte[] keyBytes = Crypto.base64Decode(keyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "SC");
        PublicKey key = keyFactory.generatePublic(spec);

        Log.i("ECDH", "Clave publica GENERADA: " + key);

        return key;
    }

    public static PrivateKey getPrivate(Context mContext, int id) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {

        InputStream is = mContext.getResources().openRawResource(id);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<String>();
        String line = null;
        try {
            while ((line = br.readLine()) != null)
                lines.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // removes the first and last lines of the file (comments)
        if (lines.size() > 1 && lines.get(0).startsWith("-----") && lines.get(lines.size()-1).startsWith("-----")) {
            lines.remove(0);
            lines.remove(lines.size()-1);
        }

        // concats the remaining lines to a single String
        StringBuilder sb = new StringBuilder();
        for (String aLine: lines)
            sb.append(aLine);
        String keyString = sb.toString();
        Log.i("ECDH", "Clave privada leida: " + keyString);

        // converts the String to a PrivateKey instance
        byte[] keyBytes = Crypto.base64Decode(keyString);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "SC");
        PrivateKey key = keyFactory.generatePrivate(spec);

        Log.i("ECDH", "Clave privada GENERADA: " + key);

        return key;
    }

    public static String generarClaveCompartida(PrivateKey privada, PublicKey publica) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeyException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "SC");
        keyAgreement.init(privada);
        keyAgreement.doPhase(publica, true);

        byte[] sharedKeyBytes = keyAgreement.generateSecret();
        Crypto.base64Encode(sharedKeyBytes);

        return Crypto.base64Encode(sharedKeyBytes);
    }
}
