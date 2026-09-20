package br.com.songlog.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de senhas com PBKDF2-HMAC-SHA256 (somente JDK, sem dependências).
 * Formato armazenado: {@code pbkdf2$iteracoes$salt$hash} (Base64).
 * O banco nunca recebe a senha em texto puro.
 */
public final class SenhaUtil {

    private static final int ITERACOES = 600_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SenhaUtil() {
    }

    public static String gerarHash(String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = derivar(senha.toCharArray(), salt, ITERACOES);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + ITERACOES + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
    }

    public static boolean verificar(String senha, String armazenado) {
        String[] partes = armazenado.split("\\$");
        if (partes.length != 4 || !partes[0].equals("pbkdf2")) {
            return false;
        }
        int iteracoes = Integer.parseInt(partes[1]);
        byte[] salt = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        byte[] calculado = derivar(senha.toCharArray(), salt, iteracoes);
        return MessageDigest.isEqual(esperado, calculado);
    }

    private static byte[] derivar(char[] senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha, salt, iteracoes, TAMANHO_HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 indisponível neste JDK", e);
        }
    }
}
