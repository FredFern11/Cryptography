import java.util.*;
// TODO: 2023-03-06 change the padding bytes: the sum of the bytes in the last block % fist byte = length of the padding bytes (see goodnotes) 

public class Encoder extends AES{

    /**
     * Using the electronic code book (ECB) mode, encrypts a message with a key, both 1D bytes array.
     * @param message 16 bytes long
     * @param key 16 bytes long
     * @return 16 byte long encrypted message with key
     */
    public byte[] encrypt(byte[] message, byte[] key) {
        byte[] global = new byte[(message.length/16+1)*16];
        for (int i = 0; i < global.length/16; i++) {
            byte[] encrypted = flatten(encrypt(extract(message, i), toMatrix(key)));
            System.arraycopy(encrypted, 0, global, 16*i, 16);
        }
        return global;
    }

    /**
     * Using the cipher block chaining (CBC) mode, encrypts a message with a key, both 1D bytes array. Each 
     * @param message 16 bytes long
     * @param key 16 bytes long
     * @return 16 byte long encrypted message with key
     */
    public byte[] encrypt(byte[] message, byte[] key, byte[] initVector) {
        byte[] global = new byte[(message.length/16+1)*16];
        byte[][] block = encrypt(addMatrix(extract(message, 0), toMatrix(initVector)), toMatrix(key));
        System.arraycopy(flatten(block), 0, global, 0, 16);
        for (int i = 1; i < global.length/16; i++) {
            block = encrypt(addMatrix(extract(message, i), block), toMatrix(key));
            System.arraycopy(flatten(block), 0, global, 16 * i, 16);
        }
        return global;
    }

    /**
     * encypts a state with a key, both under the matrix form
     * @param state 4x4 byte matrix
     * @param key 4x4 byte matrix
     * @return matrix of bytes
     */
    public byte[][] encrypt(byte[][] state, byte[][] key) {
        addMatrix(state, key);

        for (int i = 0; i < 10; i++) {
            subBytes(state);
            shiftRows(state);
            if (i != 9) mixColumns(state);
            key = schedule(key, i);
            addMatrix(state, key);
//            display(state);
//            System.out.println();
        }
        return state.clone();
    }

    /**
     * Generate the next round key from the previous one.
     * @param prevKey previous matrix key
     * @param n the round index
     * @return next round key
     */
    private byte[][] schedule(byte[][] prevKey, int n) {
        int[] rcon = {0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36};
        byte[][] nextKey = new byte[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < nextKey.length; j++) {
                if (i == 0) {
                    byte[] rotWord = rotate(prevKey[prevKey.length-1].clone());
                    rotWord[j] = substitute(rotWord[j]);
                    nextKey[i][j] = (byte) (rotWord[j] ^ prevKey[i][j]);
                    if (j == 0) nextKey[i][j] ^= rcon[n];
                } else {
                    nextKey[i][j] = (byte) (nextKey[i-1][j] ^ prevKey[i][j]);
                }
            }
        }
        return nextKey;
    }

    /**
     * Perform a left circular shift
     * @param word
     * @return shifted array
     */
    private byte[] rotate(byte[] word) {
        // 67 20 46 75 -> 20 46 75 67
        byte temp = word[0];
        for (int i = 0; i < 3; i++) {
            word[i] = word[i+1];
        }
        word[3] = temp;
        return word;
    }

    /**
     * Substitute a matrix of integers in order to switch the values with the ones in the table.
     * @param state matrix of integers
     * @return new state with substituted values
     */
    private byte[][] subBytes(byte[][] state) {
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                state[i][j] = substitute(state[i][j]);
            }
        }
        return state;
    }

    /**
     * Substitute an integer
     * @param token integer to substitute
     * @return value from the table
     */
    private byte substitute(byte token) {
        int[][] table = {
                // 0     1     2     3     4     5     6     7     8     9     a     b     c     d     e     f
                {0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76}, // 0
                {0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0}, // 1
                {0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15}, // 2
                {0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75}, // 3
                {0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84}, // 4
                {0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf}, // 5
                {0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8}, // 6
                {0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2}, // 7
                {0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73}, // 8
                {0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb}, // 9
                {0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79}, // a
                {0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08}, // b
                {0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a}, // c
                {0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e}, // d
                {0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf}, // e
                {0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16}  // f
        };
        int num = intValue(token);
        return byteValue(table[num / 16][num % 16]);
    }

    /**
     * Perform a left circular shift on the rows. The magnitude of the shift equals the index of the row.
     * The first row (index 0) is not shifted, the second row (index 1) is shifted once and so on...
     * @param state matrix of integers
     * @return shifted state
     */
    private byte[][] shiftRows(byte[][] state) {
        for (int i = 1; i < state.length; i++) {
            byte[] memory = new byte[4];
            for (int j = 0; j < memory.length; j++) memory[j] = state[j][i];
            for (int j = 0; j < state.length; j++) state[j][i] = memory[(i+j)%4];
        }
        return state;
    }

    /**
     * perform a matrix multiplication between the state and a predefine matrix under GF(256)
     * @param state
     * @return new state
     */
    private byte[][] mixColumns(byte[][] state) {
        byte[][] result = Arrays.stream(state).map(byte[]::clone).toArray(byte[][]::new);
        byte[][] matrix = {{2, 3, 1, 1}, {1, 2, 3, 1}, {1, 1, 2, 3}, {3, 1, 1, 2}};
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                byte value = 0;
                for (int k = 0; k < matrix[0].length; k++) {
                    value ^= multi(result[j][k], matrix[i][k]);
                }
                state[j][i] = value;
            }
        }
        return result;
    }

    private String toBinary(byte[] bytes) {
        StringBuilder binary = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            StringBuilder string = new StringBuilder(Integer.toBinaryString(bytes[i]));
            if (string.length() > 8) binary.append(string.substring(24));
            else {
                int length = string.length();
                for (int j = 0; j < 8-length; j++) {
                    string.insert(0, "0");
                }
                binary.append(string);
            }
            binary.append("|");
        }
        return binary.toString();
    }

    private String toHexString(byte[] array) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            String element = Integer.toHexString(intValue(array[i]));
            if (element.length() < 2) builder.append("0");
            builder.append(element);
        }
        return builder.toString();
    }

    private boolean matrixMatch(int[][] roundKey, int i, int n) {
        int[][][] testVectors = new int[10][4][4];

        testVectors[0] = new int[][]{
                Arrays.stream("54 68 61 74 73 20 6D 79 20 4B 75 6E 67 20 46 75".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("E2 32 FC F1 91 12 91 88 B1 59 E4 E6 D6 79 A2 93".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("56 08 20 07 C7 1A B1 8F 76 43 55 69 A0 3A F7 FA".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("D2 60 0D E7 15 7A BC 68 63 39 E9 01 C3 03 1E FB".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("A1 12 02 C9 B4 68 BE A1 D7 51 57 A0 14 52 49 5B".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("B1 29 3B 33 05 41 85 92 D2 10 D2 32 C6 42 9B 69".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("BD 3D C2 87 B8 7C 47 15 6A 6C 95 27 AC 2E 0E 4E".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("CC 96 ED 16 74 EA AA 03 1E 86 3F 24 B2 A8 31 6A".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("8E 51 EF 21 FA BB 45 22 E4 3D 7A 06 56 95 4B 6C".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("BF E2 BF 90 45 59 FA B2 A1 64 80 B4 F7 F1 CB D8".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("28 FD DE F8 6D A4 24 4A CC C0 A4 FE 3B 31 6F 26".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
        };

        // int[][] keyMatrix = {{00, 00, 00, 00}, {00, 00, 00, 00}, {00, 00, 00, 00}, {00, 00, 00, 00}};
        testVectors[1] = new int[][]{
                Arrays.stream("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("62 63 63 63 62 63 63 63 62 63 63 63 62 63 63 63".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("9b 98 98 c9 f9 fb fb aa 9b 98 98 c9 f9 fb fb aa".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("90 97 34 50 69 6c cf fa f2 f4 57 33 0b 0f ac 99".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("ee 06 da 7b 87 6a 15 81 75 9e 42 b2 7e 91 ee 2b".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("7f 2e 2b 88 f8 44 3e 09 8d da 7c bb f3 4b 92 90".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("ec 61 4b 85 14 25 75 8c 99 ff 09 37 6a b4 9b a7".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("21 75 17 87 35 50 62 0b ac af 6b 3c c6 1b f0 9b".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("0e f9 03 33 3b a9 61 38 97 06 0a 04 51 1d fa 9f".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("b1 d4 d8 e2 8a 7d b9 da 1d 7b b3 de 4c 66 49 41".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("b4 ef 5b cb 3e 92 e2 11 23 e9 51 cf 6f 8f 18 8e".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray()
        };

        // int[][] keyMatrix = {{0x00, 0x01, 0x02, 0x03}, {0x04, 0x05, 0x06, 0x07}, {0x08, 0x09, 0x0a, 0x0b}, {0x0c, 0x0d, 0x0e, 0x0f}};
        testVectors[2] = new int[][]{
                Arrays.stream("00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("d6 aa 74 fd d2 af 72 fa da a6 78 f1 d6 ab 76 fe".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("b6 92 cf 0b 64 3d bd f1 be 9b c5 00 68 30 b3 fe".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("b6 ff 74 4e d2 c2 c9 bf 6c 59 0c bf 04 69 bf 41".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("47 f7 f7 bc 95 35 3e 03 f9 6c 32 bc fd 05 8d fd".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("3c aa a3 e8 a9 9f 9d eb 50 f3 af 57 ad f6 22 aa".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("5e 39 0f 7d f7 a6 92 96 a7 55 3d c1 0a a3 1f 6b".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("14 f9 70 1a e3 5f e2 8c 44 0a df 4d 4e a9 c0 26".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("47 43 87 35 a4 1c 65 b9 e0 16 ba f4 ae bf 7a d2".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("54 99 32 d1 f0 85 57 68 10 93 ed 9c be 2c 97 4e".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("13 11 1d 7f e3 94 4a 17 f3 07 a7 8b 4d 2b 30 c5".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray()
        };

        // int[][] keyMatrix = {{0x49, 0x20, 0xe2, 0x99}, {0xa5, 0x20, 0x52, 0x61}, {0x64, 0x69, 0x6f, 0x47}, {0x61, 0x74, 0x75, 0x6e}};
        testVectors[3] = new int[][]{
                Arrays.stream("49 20 e2 99 a5 20 52 61 64 69 6f 47 61 74 75 6e".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("da bd 7d 76 7f 9d 2f 17 1b f4 40 50 7a 80 35 3e".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("15 2b cf ac 6a b6 e0 bb 71 42 a0 eb 0b c2 95 d5".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("34 01 cc 87 5e b7 2c 3c 2f f5 8c d7 24 37 19 02".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("a6 d5 bb b1 f8 62 97 8d d7 97 1b 5a f3 a0 02 58".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("56 a2 d1 bc ae c0 46 31 79 57 5d 6b 8a f7 5f 33".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("1e 6d 12 c2 b0 ad 54 f3 c9 fa 09 98 43 0d 56 ab".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("89 dc 70 d8 39 71 24 2b f0 8b 2d b3 b3 86 7b 18".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("4d fd dd b5 74 8c f9 9e 84 07 d4 2d 37 81 af 35".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("5a 84 4b 2f 2e 08 b2 b1 aa 0f 66 9c 9d 8e c9 a9".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray(),
                Arrays.stream("75 59 98 71 5b 51 2a c0 f1 5e 4c 5c 6c d0 85 f5".split(" ")).mapToInt(e -> Integer.parseInt(e, 16)).toArray()
        };

        for (int j = 0; j < roundKey.length; j++) {
            if (testVectors[n][i][j] != roundKey[j/4][j%4]) return false;
        }
        return true;
    }

    private boolean testBase64() {
        for (int i = 0; i < 1000000; i++) {
            String cipher = null;
            try {
                byte[] bytesCipher = new byte[26];
                new Random().nextBytes(bytesCipher);
                cipher = new String(bytesCipher);
                byte[] bytes = cipher.getBytes();
                String a = new String(Base64.getEncoder().encode(bytes));
                String b = toBase64(bytes);
                if (!a.equals(b)) {
                    System.out.println("FALSE");
                    return false;
                }
            } catch (Exception e) {
                System.out.println(i + "->" + cipher);
                e.printStackTrace();
                System.exit(67);
            }
        }
        return true;
    }
}
