import java.security.SecureRandom;

public class Key extends AES {
    private Matrix[] keys;

    public Key(byte[] key) {
        schedule(key);
    }

    public Key(int n) {
        SecureRandom random = new SecureRandom();
        byte[] init = new byte[n/8];
        for (int i = 0; i < n/8; i++) {
            init[i] = (byte) random.nextInt(256);
        }
        schedule(init);
    }

    public int size() {
        return keys.length;
    }


    /**
     * Generate the next round key from the previous one.
     *
     * @param initKey previous matrix key
     * @return next round keY
     */
    public void schedule(byte[] initKey) {
        keys = new Matrix[4 * initKey.length - 53];
        keys[0] = new Matrix(initKey);

        byte[] rcon = {0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, (byte) 0x80, 0x1b, 0x36};

        for (int k = 1; k < keys.length; k++) {
            keys[k] = new Matrix(4, 4);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (i == 0) {
                        byte[] rotWord = rotate(keys[k-1].get(3).clone());
                        rotWord[j] = substitute(rotWord[j], sbox);
                        keys[k].set(i, j, (byte) (rotWord[j] ^ keys[k-1].get(i,j)));
                        if (j == 0) keys[k].morph(i, j, rcon[k-1], (x, y) -> (byte) (x^y));
                    } else {
                        keys[k].set(i, j, (byte) (keys[k].get(i-1, j) ^ keys[k-1].get(i,j)));
                    }
                }
            }
        }
    }

    public Matrix getRound(int n) {
        return keys[n];
    }
}
