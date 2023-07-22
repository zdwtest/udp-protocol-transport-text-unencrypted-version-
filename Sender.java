import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class Sender {
    private static final int RECEIVER_PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            // ������ն�����
            System.out.print("��������ն�������");
            String receiverHost = System.console().readLine();

            // ������Կ
            System.out.print("��������Կ��������ʱ�Զ���ȥ�������ݣ�");
            char[] keyChars = System.console().readPassword();
            byte[] key = new String(keyChars).getBytes(StandardCharsets.UTF_8);
            key = padKey(key);

            // ����AES������
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // ��һ������
            Socket socket = new Socket(receiverHost, RECEIVER_PORT);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            outputStream.write("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = inputStream.read(buffer);
            String response = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            if (response.equals("ACK")) {
                System.out.println("��һ�����ֳɹ���");
            } else {
                System.out.println("��һ������ʧ�ܣ�");
                socket.close();
                System.exit(1);
            }

            while (true) {
                // ���ܲ�������Ϣ
                System.out.print("������Ҫ���͵���Ϣ������'exit'�˳�����");
                String message = System.console().readLine();
                if (message.equals("exit")) {
                    break;
                }

                byte[] paddedMessage = padMessage(message.getBytes(StandardCharsets.UTF_8));
                byte[] encryptedMessage = cipher.doFinal(paddedMessage);
                byte[] encodedMessage = Base64.getEncoder().encode(encryptedMessage);
                outputStream.write(encodedMessage);

                // ���ղ����ܻظ���Ϣ
                bytesRead = inputStream.read(buffer);
                byte[] partialBuffer = Arrays.copyOfRange(buffer, 0, bytesRead);
                byte[] encodedResponse = Base64.getDecoder().decode(partialBuffer);
                byte[] decryptedResponse = cipher.doFinal(encodedResponse);
                String unpaddedResponse = new String(decryptedResponse, StandardCharsets.UTF_8).trim();
                System.out.println("���յ��Ļظ���Ϣ��" + unpaddedResponse);
            }

            // �ر�����
            socket.close();
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
