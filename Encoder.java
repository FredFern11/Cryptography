// TODO: 2023-03-06 change the padding bytes: the sum of the bytes in the last block % fist byte = length of the padding bytes (see goodnotes)

public class Encoder extends AES {
    private byte[][] mixColumn = new byte[][]{{2, 3, 1, 1}, {1, 2, 3, 1}, {1, 1, 2, 3}, {3, 1, 1, 2}};

    /**
     * Using the electronic code book (ECB) mode, encrypts a message with a key, both 1D bytes array.
     *
     * @param message 16 bytes long
     * @return 16 byte long encrypted message with key
     */
    public byte[] encrypt(byte[] message, Key key) {
        byte[] global = new byte[(message.length / 16 + 1) * 16];
        for (int i = 0; i < global.length / 16; i++) {
            byte[] encrypted = encrypt(extract(message, i), key).flatten();
            System.arraycopy(encrypted, 0, global, 16 * i, 16);
        }
        return global;
    }

    /**
     * Using the cipher block chaining (CBC) mode, encrypts a message with a key, both 1D bytes array. Each
     *
     * @param message 16 bytes long
     * @return 16 byte long encrypted message with key
     */
    public byte[] encrypt(byte[] message, Key key, byte[] IV) {
        byte[] global = new byte[(message.length / 16 + 1) * 16];
        Matrix block = encrypt(extract(message, 0).XOR(new Matrix(IV)), key);
        System.arraycopy(block.flatten(), 0, global, 0, 16);
        for (int i = 1; i < global.length / 16; i++) {
            block = encrypt(extract(message, i).XOR(block), key);
            System.arraycopy(block.flatten(), 0, global, 16 * i, 16);
        }
        return global;
    }

    /**
     * encypts a state with a key, both under the matrix form
     *
     * @param state 4x4 byte matrix
     * @return matrix of bytes
     */
    private Matrix encrypt(Matrix state, Key key) {
        state.XOR(key.getRound(0));
//        state.display();

        for (int i = 0; i < 10; i++) {
            subBytes(state, sbox);
            shiftRows(state, true);
            if (i != 9) state = mixColumns(state, mixColumn);
            state.XOR(key.getRound(i+1));
//            state.display();
        }
        return new Matrix(state);
    }
}