import java.util.Arrays;
import java.util.Random;

public class Utils {

    public Matrix extract(byte[] message, int block) {
        byte[][] state = new byte[4][4];
        int i = 16 * block - 1;
        byte pad = (byte) (16 - message.length % 16);

        while (++i < 16 * (block+1)) {
            if (i < message.length) {
                state[(i % 16) / 4][(i % 16) % 4] = message[i];
            } else {
                state[(i % 16) / 4][(i % 16) % 4] = pad;
            }
        }
        return new Matrix(state);
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



    public static int intValue(byte num) {
        return (num < 0 ? num + 256 : num);
    }

    public static byte byteValue(int num) {
        return (byte) (num > 128 ? num - 256 : num);
    }

    private String toBinary(byte[] bytes) {
        StringBuilder binary = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            StringBuilder string = new StringBuilder(Integer.toBinaryString(bytes[i]));
            if (string.length() > 8) binary.append(string.substring(24));
            else {
                int length = string.length();
                for (int j = 0; j < 8 - length; j++) {
                    string.insert(0, "0");
                }
                binary.append(string);
            }
            binary.append("|");
        }
        return binary.toString();
    }

    /**
     * Convert a binary input into base 64
     * @param bytes
     * @return string base 64
     */
    public String toBase64(byte[] bytes) {
        char[] table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        if (bytes.length == 0) return null;
        double size = bytes.length*8.0/6;
        final int rawLength = size % 1 == 0 ? (int) size : (int) size + 1;
        int extra = (rawLength % 4 == 0 ? 0 : 4 - rawLength % 4);
        char[] encoded = new char[rawLength + extra];
        int value = 0;
        for (int i = 0; i < bytes.length*8; i++) {
            if ((i % 6 == 0 && i != 0)) {
                encoded[i/6-1] = table[value];
                value = 0;
            }
            // TODO: 2023-03-24 change this to match toAscii method
            boolean bit = bitValue(bytes[i/8], 7-(i%8));
            value += bit ? Math.pow(2, 5-(i%6)) : 0;
        }
        encoded[rawLength-1] = table[value];
        if (extra > 0) Arrays.fill(encoded, rawLength, encoded.length, '=');

        return new String(encoded);
    }

    public String toAscii(String base64) {
        if (base64 == null) return "";
        char[] alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        StringBuilder original = new StringBuilder();
        int sum = 0;
        int padCount = 0;

        for (int i = 0; i < base64.length(); i++) {
            char ch = base64.charAt(i);
//            if (i > base64.length() - 5) value = value << 2 * (padCount-i%4);
            if (ch != '=') sum += (int) (search(alpha, ch) * Math.pow(64, 3-i%4));
            else padCount++;

            if (i%4 == 3) {
                for (int j = 2; j >= (i > base64.length()-5 ? padCount : 0); j--) {
                    original.append((char) ((sum >> 8*j) % 256));
                }
                sum = 0;
            }
        }
        return original.toString();
    }

    public int search(char[] array, char target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    /**
     * Convert an hexadecimal string to bytes. The array may contain negative value because the ASCII encoding
     * ranges from 0 to 255 while a byte ranges from -128 to 127
     * @param hexString string containing only hexadecimal digits
     * @return array of bytes
     */
    public byte[] getBytes(String hexString) {
        byte[] bytes = new byte[hexString.length()/2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexString.substring(i*2, (i+1)*2), 16);
        }
        return bytes;
    }

    public static String randStr(int length) {
        Random random = new Random();
        char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = (char) random.nextInt(0, 128);
        }
        return new String(array);
    }

    private byte[] hexToBytes(String string) {
        byte[] array = new byte[string.length()/2];
        for (int i = 0; i < array.length; i++) {
            String element = string.substring(2*i, 2*(i+1));
            array[i] = (byte) Integer.parseInt(element, 16);
        }
        return array;
    }

    private String bytesToHex(byte[] array) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            String element = Integer.toHexString(array[i]);
            if (element.length() < 2) builder.append("0");
            if (element.length() > 2) element = element.substring(element.length()-2);
            builder.append(element).append(" ");
        }
        return builder.toString();
    }
}
