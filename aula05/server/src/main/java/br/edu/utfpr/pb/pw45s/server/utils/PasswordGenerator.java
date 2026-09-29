package br.edu.utfpr.pb.pw45s.server.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gera senhas aleatórias para os usuários cadastrados via rede social.
 * A senha respeita a mesma regra da entidade User (letra minúscula, letra maiúscula e número)
 * e é gerada com SecureRandom, um gerador de números aleatórios adequado para uso criptográfico.
 */
public final class PasswordGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final int PASSWORD_LENGTH = 32;

    private PasswordGenerator() {
    }

    public static String generate() {
        List<Character> password = new ArrayList<>();
        // garante ao menos um caractere de cada grupo exigido
        password.add(randomChar(LOWER));
        password.add(randomChar(UPPER));
        password.add(randomChar(DIGITS));
        // completa o restante da senha com caracteres de qualquer grupo
        while (password.size() < PASSWORD_LENGTH) {
            password.add(randomChar(LOWER + UPPER + DIGITS));
        }
        // embaralha para que os caracteres obrigatórios não fiquem sempre no início
        Collections.shuffle(password, RANDOM);

        StringBuilder sb = new StringBuilder();
        password.forEach(sb::append);
        return sb.toString();
    }

    private static char randomChar(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }
}
