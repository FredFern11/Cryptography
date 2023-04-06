import java.util.*;

public abstract class AES extends Utils {
    /**
     * Perform a left circular shift
     *
     * @param word
     * @return shifted array
     */
    public byte[] rotate(byte[] word) {
        // 67 20 46 75 -> 20 46 75 67
        byte temp = word[0];
        for (int i = 0; i < 3; i++) {
            word[i] = word[i + 1];
        }
        word[3] = temp;
        return word;
    }

    /**
     * Substitute a matrix of integers in order to switch the values with the ones in the table.
     *
     * @param state matrix of integers
     * @return new state with substituted values
     */
    public byte[][] subBytes(byte[][] state, int[][] table) {
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                state[i][j] = substitute(state[i][j], table);
            }
        }
        return state;
    }

    /**
     * Substitute an integer
     *
     * @param token integer to substitute
     * @return value from the table
     */
    public byte substitute(byte token, int[][] table) {
        int num = intValue(token);
        return byteValue(table[num / 16][num % 16]);
    }

    /**
     * Perform a left circular shift on the rows. The magnitude of the shift equals the index of the row.
     * The first row (index 0) is not shifted, the second row (index 1) is shifted once and so on...
     *
     * @param state matrix of integers
     * @return shifted state
     */
    public byte[][] shiftRows(byte[][] state, boolean enc) {
        for (int i = 1; i < state.length; i++) {
            byte[] memory = new byte[4];
            for (int j = 0; j < memory.length; j++) memory[j] = state[j][i];
            for (int j = 0; j < state.length; j++) state[j][i] = memory[(j + (enc? 1 : -1 ) * i + 4) % 4];
        }
        return state;
    }

    /**
     * perform a matrix multiplication between the state and a predefine matrix under GF(256)
     *
     * @param state
     * @return new state
     */
    public byte[][] mixColumns(byte[][] state, byte[][] matrix) {
        byte[][] result = Arrays.stream(state).map(byte[]::clone).toArray(byte[][]::new);
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
}
