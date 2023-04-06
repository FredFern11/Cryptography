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
    public static void main(String[] args) {
        Random random = new Random();
        String message = "!@#$%?&*():'/àçù><«»|δλ";
        String key = "Let me live nowa";
        Encoder encoder = new Encoder(key.getBytes());
        Decoder decoder = encoder.genDecoder();

        System.out.println(encoder.toBase64(encoder.encrypt(message.getBytes())));
        System.out.println(new String(decoder.decrypt(encoder.encrypt(message.getBytes()))));
    }
}