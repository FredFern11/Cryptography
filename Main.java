import java.util.Random;

public class Main {
    public static void main(String[] args) {
//        Matrix a = new Matrix(new byte[][]{{19, 21}, {21, 2}});
//        Matrix b = new Matrix(new byte[][]{{16, 51}, {13, 81}});
//        a.multi(b).display();
//        System.exit(11);

        Random random = new Random();
        String message = "Two One Nine Two";
        Key key = new Key("Thats my Kung Fu".getBytes());
        String IV = "abcdefghijllmnop";
        Encoder encoder = new Encoder();
        Decoder decoder = new Decoder();

        System.out.println(encoder.encrypt(message.getBytes(), key));
//        System.out.println(new String(decoder.decrypt(encoder.encrypt(message.getBytes(), key, IV.getBytes()), key, IV.getBytes())));
    }
}