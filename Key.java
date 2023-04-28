import java.security.SecureRandom;
import java.util.Arrays;

public class Key extends AES {
    private byte[][] words;

    public Key(byte[] key) {
        if (validLength(key.length)) schedule(key);
        else throw new RuntimeException("The key length isn't valid. It should be 128, 192 or 256 bit long");
    }

    public Key(int length) {
        if (validLength(length)) {
            SecureRandom random = new SecureRandom();
            byte[] init = new byte[length / 8];
            for (int i = 0; i < length / 8; i++) {
                init[i] = (byte) random.nextInt(256);
            }
            schedule(init);
        } else throw new RuntimeException("The key length isn't valid. It should be 128, 192 or 256 bit long");
    }

    private boolean validLength(int length) {
        int[] valid = new int[]{16, 24, 32};
        for (int size : valid) {
            if (length == size) return true;
        }
        return false;
    }

    public int size() {
        return words.length/4;
    }
    
    public void schedule(byte[] initKey) {
        int initLength = initKey.length/4;
        words = new byte[4 * (initLength + 7)][4];
        byte[] rcon = {0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, (byte) 0x80, 0x1b, 0x36};

        for (int i = 0; i < words.length; i++) {
            if (i < initLength) {
                words[i] = Arrays.copyOfRange(initKey, 4*i, 4*(i+1));
            }

            else if (i >= initLength && i % initLength == 0) {
                byte[] subRotWord = substitute(rotate(words[i-1]), sbox);
                for (int j = 0; j < 4; j++) {
                    words[i][j] = (byte) (words[i-initLength][j] ^ subRotWord[j]);
                    if (j == 0) words[i][j] ^= rcon[i/initLength-1];
                }
            }

            else if (i >= initLength && initLength > 6 && i % initLength == 4) {
                for (int j = 0; j < 4; j++) {
                    words[i][j] = (byte) (words[i-initLength][j] ^ substitute(words[i-1], sbox)[j]);
                }
            }

            else {
                for (int j = 0; j < 4; j++) {
                    words[i][j] = (byte) (words[i-initLength][j] ^ words[i-1][j]);
                }
            }
        }
    }

    public Matrix getRound(int n) {
        byte[][] data = new byte[4][4];
        for (int i = 4*n; i < 4*(n+1); i++) {
            data[i%4] = words[i];
        }
        return new Matrix(data);
    }
}
