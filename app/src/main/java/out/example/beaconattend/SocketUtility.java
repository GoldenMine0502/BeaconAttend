package out.example.beaconattend;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SocketUtility {

    public static final String ip = "ec2-54-180-100-253.ap-northeast-2.compute.amazonaws.com";
    public static final int port = 12344;

    public static void connect(String command, Lambda<ObjectInputStream, ObjectOutputStream> lambda) {
        connect(command, ip, port, lambda);
    }

    public static void connect(String command, String ip, int port, Lambda<ObjectInputStream, ObjectOutputStream> lambda) {
        new Thread() {
            public void run() {
                try {
                    System.out.println("connecting");
                    Socket socket = new Socket(ip, port);
                    System.out.println("connected");
                    ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    outputStream.writeUTF(command);
                    outputStream.flush();

                    ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

                    lambda.accept(inputStream, outputStream);

                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();
                    socket.close();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    public interface Lambda<E, E2> {
        void accept(E e, E2 e2) throws IOException;
    }
}
