package com.sigma.negocio.servicios;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class ClaveUsuario {
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 600_000;
    private static final int BYTES_SALT = 16;
    private static final int BYTES_HASH = 32;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private ClaveUsuario() {
    }

    static String proteger(char[] clave) {
        byte[] salt = new byte[BYTES_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = derivar(clave, salt);
        try {
            Base64.Encoder base64 = Base64.getEncoder();
            return ALGORITMO + "$" + ITERACIONES + "$" + base64.encodeToString(salt)
                    + "$" + base64.encodeToString(hash);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

    static boolean verificar(char[] clave, String guardado) {
        String[] partes = guardado.split("\\$", -1);
        if (partes.length != 4 || !ALGORITMO.equals(partes[0])
                || !Integer.toString(ITERACIONES).equals(partes[1])) {
            return false;
        }

        byte[] salt;
        byte[] hash;
        try {
            Base64.Decoder base64 = Base64.getDecoder();
            salt = base64.decode(partes[2]);
            hash = base64.decode(partes[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (salt.length != BYTES_SALT || hash.length != BYTES_HASH) {
            return false;
        }
        byte[] calculado = derivar(clave, salt);
        try {
            return MessageDigest.isEqual(hash, calculado);
        } finally {
            Arrays.fill(calculado, (byte) 0);
        }
    }

    private static byte[] derivar(char[] clave, byte[] salt) {
        PBEKeySpec especificacion = new PBEKeySpec(clave, salt, ITERACIONES, BYTES_HASH * Byte.SIZE);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("El JDK no pudo derivar el hash de contraseña.", e);
        } finally {
            especificacion.clearPassword();
        }
    }
}
