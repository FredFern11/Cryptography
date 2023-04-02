import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args)  {
        Encoder encoder = new Encoder();
        Decoder decoder = new Decoder();

//        String string;
//        for (int i = 5; i < 10000; i++) {
//            string = decoder.randStr(i);
//            String base64 = encoder.toBase64(string.getBytes());
//            String ascii = encoder.toAscii(base64);
//            if (!string.equals(ascii)) {
//                System.out.println(string + "\n" + base64 + "\n" + ascii);
//                System.exit(i);
//            }
//        }
//        System.out.println("---- Test #10000 passed ----");

        byte[] message = "Two One Nine Two".getBytes();
        byte[] password = "Thats my Kung Fu".getBytes();

        byte[] cipherText = encoder.encrypt(message, password);
//        byte[] plainTxt = decoder.decrypt(cipherText, password);
//        System.out.println(encoder.toBase64(message) + "\n" + encoder.toBase64(plainTxt));

    }

    public static void test(int n) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        for (int j = 0; j < n; j++) {
            Random random = new Random();
            SecretKey secretKey = generateKey(128);
            IvParameterSpec IV = generateIv();

//            char[] charArr = new char[random.nextInt(100)];
            char[] charArr = new char[random.nextInt(100)];
            for (int i = 0; i < charArr.length; i++) {
                charArr[i] = (char) random.nextInt(-128, 128);
            }
            String text = new String(charArr);
            Encoder encoder = new Encoder();
            String myAes = encoder.toBase64(encoder.encrypt(text.getBytes(), secretKey.getEncoded(), IV.getIV()));
            String provided = encrypt(new String(text), secretKey, IV);
            System.out.println(myAes + "\n" + provided + "\n");
            if (!myAes.equals(provided)) return;
        }
        System.out.println("Passed");
    }

    public static byte search() {
        try {
            Random random = new Random();
            SecretKey secretKey = generateKey(128);
            IvParameterSpec IV = generateIv();
            char[] charArr = new char[16];
            charArr[15] = 0;
            for (int i = 0; i < charArr.length-1; i++) {
                charArr[i] = (char) random.nextInt(-128, 128);
            }
            String text = new String(charArr);
            Encoder encoder = new Encoder();
            String provided = encrypt(text, secretKey, IV);
            String aes = "";
            while (!aes.equals(provided)) {
                System.out.println((int) charArr[15]);
                aes = encoder.toBase64(encoder.encrypt(new String(charArr).getBytes(), secretKey.getEncoded(), IV.getIV()));
                charArr[15] += 1;
                System.out.println(aes);
            }
            return (byte) charArr[15];

        } catch (Exception e) {}
        return -1;
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static SecretKey getKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
        return secret;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static String encrypt(String input, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }
}