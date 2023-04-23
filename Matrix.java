import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Matrix {
    private byte[][] data;

    public Matrix(byte[][] matrix) {
        this.data = matrix.clone();
    }

    public Matrix(Matrix matrix) {
        this.data = matrix.data;
    }

    /**
     * Convert an integer array into a matrix.
     * @param array integer array of size 16
     * @return 4x4 matrix
     */
    public Matrix(byte[] array) {
        data = new byte[4][4];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = array[4*i+j];
            }
        }
    }

    public Matrix(int rows, int columns) {
        data = new byte[rows][columns];
    }

    public int getLength() {
        return data.length;
    }

    public int getLength(int i) {
        return data[i].length;
    }

    public byte[] get(int i) {
        return data[i];
    }

    public byte get(int i, int j) {
        return data[i][j];
    }

    public void set(int i, int j, byte element) {
        data[i][j] = element;
    }

    public void set(byte[] array, int i) {
        data[i] = array.clone();
    }

    public void morph(int i, int j, Function<Byte, Byte> function) {
        data[i][j] = function.apply(data[i][j]);
    }

    public void morph(int i, int j, byte element, BiFunction<Byte, Byte, Byte> function) {
        data[i][j] = function.apply(data[i][j], element);
    }

    /**
     * Print a matrix in hexadecimal
     */
    public void display() {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                String hex = Integer.toHexString(Utils.intValue(data[i][j])).toUpperCase();
                if (hex.length() < 2) System.out.print("0");
                System.out.print(hex + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Convert a matrix of integers into an array of integers.
     * @return 1D array traverse in a word wise direction
     */
    public byte[] flatten() {
        byte[] flat = new byte[this.data.length * this.data.length];
        for (int i = 0; i < flat.length; i++) {
            flat[i] = this.data[i / this.data.length][i % this.data.length];
        }
        return flat;
    }

    /**
     * Perform XOR operation symetrically between entries in state and roundKey.
     * @return new state
     */
    public Matrix XOR(Matrix matrix) {
        for (int i = 0; i < matrix.data.length; i++) {
            for (int j = 0; j < matrix.data[0].length; j++) {
                this.data[i][j] ^= matrix.data[i][j];
            }
        }
        return new Matrix(this.data);
    }

    public Matrix multi(Matrix matrix) {
        Matrix result = new Matrix(matrix.getLength(), matrix.getLength(0));
        for (int i = 0; i < matrix.getLength(); i++) {
            for (int j = 0; j < matrix.getLength(0); j++) {
                byte value = 0;
                for (int k = 0; k < matrix.data[0].length; k++) {
                    value ^= multiGF(matrix.data[j][k], this.data[i][k]);
                }
                result.data[i][j] = value;
            }
        }

        return result;
    }

    /**
     * Perform multiplication under GF(256)
     * @param a
     * @param b
     * @return product of a and b under GF(256)
     */
    private int multiGF(int a, int b) {
        if (a == 0 || b == 0) return 0;
        int[] p = toPoly(a);
        int[] q = toPoly(b);
        // TODO: 2023-04-07 review this methos should onl assess od or even
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
