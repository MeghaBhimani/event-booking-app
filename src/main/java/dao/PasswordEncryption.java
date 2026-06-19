package dao;

public class PasswordEncryption {

    private static final int SHIFT = 3;

    private PasswordEncryption() {}

    public static String encrypt(String plainText) {
        StringBuilder encrypted = new StringBuilder(plainText.length());
        for (char c : plainText.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                encrypted.append((char) (base + (c - base + SHIFT) % 26));
            } else if (Character.isDigit(c)) {
                encrypted.append((char) ('0' + (c - '0' + SHIFT) % 10));
            } else {
                encrypted.append(c);
            }
        }
        return encrypted.toString();
    }
}
