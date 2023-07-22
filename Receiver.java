import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Receiver {
    private static final int RECEIVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            // �����˿�
            ServerSocket serverSocket = new ServerSocket(RECEIVER_PORT);
            System.out.println("�ȴ���һ����������...");

            // �ȴ���һ������
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = inputStream.read(buffer);
            String data = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            if (data.equals("Hello")) {
                System.out.println("�յ���һ����������");
                outputStream.write("ACK".getBytes(StandardCharsets.UTF_8));
            } else {
                System.out.println("�յ���Ч����");
                socket.close();
                System.exit(1);
            }

            // ������Կ
            System.out.print("��������Կ��������ʱ�Զ���ȥ�������ݣ�");
            byte[] key = new String(System.console().readPassword()).getBytes(StandardCharsets.UTF_8);
            key = padKey(key);

            // ����AES������
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            while (true) {
                // ���ղ�������Ϣ
                bytesRead = inputStream.read(buffer);
                byte[] partialBuffer = Arrays.copyOfRange(buffer, 0, bytesRead);
                byte[] encryptedMessage = Base64.getDecoder().decode(partialBuffer);
                byte[] decryptedMessage = cipher.doFinal(encryptedMessage);
                String unpaddedMessage = new String(decryptedMessage, StandardCharsets.UTF_8).trim();
                System.out.println("���յ�����Ϣ��" + unpaddedMessage);

                // ���ͻظ���Ϣ
                System.out.print("������ظ���Ϣ������'exit'�˳�����");
                String reply = System.console().readLine();
                if (reply.equals("exit")) {
                    break;
                }

                byte[] paddedReply = padMessage(reply.getBytes(StandardCharsets.UTF_8));
                byte[] encryptedReply = cipher.doFinal(paddedReply);
                byte[] encodedReply = Base64.getEncoder().encode(encryptedReply);
                outputStream.write(encodedReply);
            }

            // �ر�����
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] padKey(byte[] key) {
        byte[] paddedKey = new byte[16];
        System.arraycopy(key, 0, paddedKey, 0, Math.min(key.length, paddedKey.length));
        return paddedKey;
    }

    private static byte[] padMessage(byte[] message) {
        int paddingLength = 16 - (message.length % 16);
        byte[] paddedMessage = new byte[message.length + paddingLength];
        System.arraycopy(message, 0, paddedMessage, 0, message.length);
        return paddedMessage;
    }
}
