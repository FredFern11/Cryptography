import java.util.*;

public abstract class AES {
//    public static void main(String[] args) {
//        AES aes = new AES();
//        byte[] text = ("abcdabcdabcdabcdabcdabcdabcd").getBytes();
//        byte[] key = "1234567891234567".getBytes();
//        byte[] initVector = "1324354657687910".getBytes();
//        byte[] encrypted = aes.encrypt(text, key, initVector);
//        System.out.println(aes.toBase64(encrypted));
//    }

    public static void main(String[] args) {
        StringBuilder string = new StringBuilder();
        String s = "abcdefghijklmnop";
        for (int i = 0; i < 15; i++) {
            string.append(s.charAt(i));
            byte[] message = string.toString().getBytes();
            Encoder encoder = new Encoder();
            byte[] extracted = encoder.flatten(encoder.extract(message, 0));
//            System.out.println(Arrays.toString(extracted));
            int sum = 0;
            for (int j = 0; j < extracted.length; j++) {
                sum += extracted[j];
            }
            System.out.println(sum % extracted[0] == 15-i);
        }
    }

    public byte[][] extract(byte[] message, int block) {
        byte[][] state = new byte[4][4];
        int i = 16 * block;
        int sum = 0;
        int max = ((message.length / 16) + 1) * 16;

        while (i < 16 * (block+1)) {
            if (i < message.length) {
                state[(i%16)/4][(i%16)%4] = message[i];
            }
            else if (i != max-2) {
                state[(i%16)/4][(i%16)%4] = (byte) ((256 * Math.random()) - 128);
            }
            else {
                int k = 0;
                int n = 16 - message.length;
                while (state[0][0] * (k++) + n < sum);
                state[3][3] = (byte) (state[0][0] * k + n - sum);
                return state;
            }
            sum += state[(i%16)/4][(i%16)%4];
            i++;
        }
        return null;
    }

    /**
     * Convert a matrix of integers into an array of integers.
     * @param matrix 2D array of integers
     * @return 1D array traverse in a word wise direction
     */
    public byte[] flatten(byte[][] matrix) {
        byte[] flat = new byte[matrix.length * matrix.length];
        for (int i = 0; i < flat.length; i++) {
            flat[i] = matrix[i / matrix.length][i % matrix.length];
        }
        return flat;
    }

    /**
     * Convert an integer array into a matrix.
     * @param array integer array of size 16
     * @return 4x4 matrix
     */
    public byte[][] toMatrix(byte[] array) {
        byte[][] matrix = new byte[4][4];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = array[4*i+j];
            }
        }
        return matrix;
    }

    /**
     * Convert a binary input into base 64
     * @param bytes
     * @return string base 64
     */
    public static String toBase64(byte[] bytes) {
        char[] table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        // may need to change length of encoded
        double size = bytes.length*8.0/6;
        final int rawLength = size % 1 == 0 ? (int) size : (int) size + 1;
//        System.out.println(rawLength);
        int extra = (rawLength % 4 == 0 ? 0 : 4 - rawLength % 4);
        char[] encoded = new char[rawLength + extra];


//        System.out.println(toBinary(bytes));

//        System.out.println(Integer.toBinaryString(Integer.parseInt("29C", 16)));
        int value = 0;
//        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length*8; i++) {
            if ((i % 6 == 0 && i != 0)) {
//                System.out.println(stringBuilder + "->" + value);
//                stringBuilder = new StringBuilder();
                encoded[i/6-1] = table[value];
                value = 0;
            }

            boolean bit = bitValue(bytes[i/8], 7-(i%8));
//            stringBuilder.append(bit?1:0);
            value += bit ? Math.pow(2, 5-(i%6)) : 0;
        }
//        System.out.println(new String(encoded));
//        System.out.println(rawLength);
        encoded[rawLength-1] = table[value];
        if (extra > 0) Arrays.fill(encoded, rawLength, encoded.length, '=');

        return new String(encoded);
    }

    /**
     * Convert an hexadecimal string to bytes. The array may contain negative value because the ASCII encoding
     * ranges from 0 to 255 while a byte ranges from -128 to 127
     * @param hexString string containing only hexadecimal digits
     * @return array of bytes
     */
    public static byte[] getBytes(String hexString) {
        byte[] bytes = new byte[hexString.length()/2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexString.substring(i*2, (i+1)*2), 16);
        }
        return bytes;
    }

    /**
     * Perform XOR operation symetrically between entries in state and roundKey.
     * @param a
     * @param b
     * @return new state
     */
    public byte[][] addMatrix(byte[][] a, byte[][] b) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                a[i][j] ^= b[i][j];
            }
        }
        return a;
    }

    /**
     * Perform multiplication under GF(256)
     * @param a
     * @param b
     * @return product of a and b under GF(256)
     */
    public int multi(int a, int b) {
        if (a == 0 || b == 0) return 0;
        int[] p = toPoly(a);
        int[] q = toPoly(b);
        HashSet<Integer> hashSet = new HashSet<>(p[p.length-1] + q[q.length-1]);
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < q.length; j++) {
                int c = q[j] + p[i];
                if (!hashSet.contains(c)) hashSet.add(c);
                else hashSet.remove(c);
            }
        }

        int u = hashSet.stream().map(i -> (int) Math.pow(2, i)).reduce(0, Integer::sum);
        int size = hashSet.stream().max(Integer::compareTo).get() + 1;
        for (int i = size-9; i >= 0; i--) {
            if (u > (u ^ (1 << (i+8)))) {
                u ^= 0b100011011 << i;
            }
        }

        return u;
    }

    /**
     * Convert a number base 2 into a polynomial represented by an array. Elements represent exponents of the polynomial.
     * Example given: 11 = 2^0 + 2^1 + 2^3 = {3, 1, 0}
     * @param a number base 2
     * @return polynomial represented as an array
     */
    public int[] toPoly(int a) {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            if (a > (a ^ (1 << i))) list.add(i);
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Extract a bit out of a byte
     * @param octet byte to extract from
     * @param position location of the bit to extract. The position is the same as the exponent.
     * @return binary value of the bit
     */
    public static boolean bitValue(byte octet, int position) {
        return (octet > (octet ^ (1 << position)));
    }

    /**
     * convert the integer matrix into a string matrix containing only hexadecimal values
     * @param matrix integer matrix
     * @return hexadecimal matrix
     */
    public String[][] hexMatrix(byte[][] matrix) {
        String[][] hex = new String[matrix.length][matrix[0].length];
        for (int i = 0; i < hex.length; i++)
            for (int j = 0; j < hex[0].length; j++)
                hex[j][i] = Integer.toHexString(intValue(matrix[j][i])).toUpperCase();
        return hex;
    }

    /**
     * Print the matrix
     * @param matrix
     */
    public void display(byte[][] matrix) {
        String[][] hex = hexMatrix(matrix);
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < hex.length; i++) {
            for (int j = 0; j < hex[0].length; j++) {
                if (hex[j][i].length() < 2) string.append("0").append(hex[j][i]).append(" ");
                else string.append(hex[j][i]).append(" ");
            }
            System.out.println(string);
            string = new StringBuilder();
        }
    }

    public int intValue(byte num) {
        return (num < 0 ? num + 256 : num);
    }

    public byte byteValue(int num) {
        return (byte) (num > 128 ? num - 256 : num);
    }
}
