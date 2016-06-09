package com.tfg.jose.proteccionpersonas;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
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

    public static PublicKey getPublic(Context mContext, int id) throws InvalidKeySpecException, NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

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
        Log.i("ECDH", "keybytes: " + Crypto.hex(spec.getEncoded()));

        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "SC");
        PublicKey key = keyFactory.generatePublic(spec);

        byte[] hex = key.getEncoded();
        StringBuilder aaa = new StringBuilder();
        for (byte b : hex) {
            aaa.append(String.format("%02X ", b));
        }

        Log.i("ECDH", "Clave pública final: " + aaa.toString());
        Log.i("ECDH", "Clave pública key: " + key);
        Log.i("ECDH", "Clave pública key tipo: " + key.getFormat());

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
        Log.i("ECDH", "keybytes: " + Crypto.hex(spec.getEncoded()));

        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "SC");
        PrivateKey key = keyFactory.generatePrivate(spec);

        Log.i("ECDH", "Clave privada final: " + Crypto.hex(key.getEncoded()));
        Log.i("ECDH", "Clave privada key: " + key.getEncoded());
        Log.i("ECDH", "Clave privada key tipo: " + key.getFormat());

        return key;
    }

    public static String generarClaveCompartida(PrivateKey privada, PublicKey publica) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeyException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "SC");
        keyAgreement.init(privada);
        keyAgreement.doPhase(publica, true);

        byte[] sharedKeyBytes = keyAgreement.generateSecret();

        StringBuilder aaa = new StringBuilder();
        for (byte b : sharedKeyBytes) {
            aaa.append(String.format("%02X ", b));
        }

        Log.i("ECDH", "CLAVE COMPARTIDA: " + aaa.toString());

        return Crypto.hex(sharedKeyBytes);
    }

    public static KeyPair generatePairKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPair keyPair = null;

        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp128r1");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "SC");
        keyPairGenerator.initialize(ecGenParameterSpec);
        keyPair = keyPairGenerator.generateKeyPair();

        return keyPair;
    }

    public static void savePrivKeyServer(PrivateKey pkey) throws IOException {
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cert/KPrivServer.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(pkey);
        oos.close();
        Log.i("ECDH", "Privada Servidor Guardada: " + pkey);
    }

    public static void savePubKeyServer(PublicKey pkey) throws IOException {
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cert/KPubServer.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(pkey);
        oos.close();
        Log.i("ECDH", "Pública Servidor Guardada: " + pkey);
    }

    public static void savePrivKeyClient(PrivateKey pkey) throws IOException {
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cert/KPrivClient.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(pkey);
        oos.close();
        Log.i("ECDH", "Privada Cliente Guardada: " + pkey);
    }

    public static void savePubKeyClient(PublicKey pkey) throws IOException {
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cert/KPubClient.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(pkey);
        oos.close();
        Log.i("ECDH", "Pública Cliente Guardada: " + pkey);
    }

    public static PrivateKey getPrivKeyServer() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cert/KPrivServer.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PrivateKey key = (PrivateKey) ois.readObject();
        ois.close();
        Log.i("ECDH", "Privada Servidor Guardada: " + key);
        return key;
    }

    public static PublicKey getPubKeyServer() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cert/KPubServer.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PublicKey key = (PublicKey) ois.readObject();
        ois.close();
        Log.i("ECDH", "Pública Servidor Guardada: " + key);
        return key;
    }

    public static PrivateKey getPrivKeyClient() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cert/KPrivClient.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PrivateKey key = (PrivateKey) ois.readObject();
        ois.close();
        Log.i("ECDH", "Privada Cliente Guardada: " + key);
        return key;
    }

    public static PublicKey getPubKeyClient() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cert/KPubClient.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        PublicKey key = (PublicKey) ois.readObject();
        ois.close();
        Log.i("ECDH", "Pública Cliente Guardada: " + key);
        return key;
    }
}
