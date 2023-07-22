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
            // 输入接收端域名
            System.out.print("请输入接收端域名：");
            String receiverHost = System.console().readLine();

            // 输入密钥
            System.out.print("请输入密钥：（输入时自动隐去输入内容）");
            char[] keyChars = System.console().readPassword();
            byte[] key = new String(keyChars).getBytes(StandardCharsets.UTF_8);
            key = padKey(key);

            // 创建AES加密器
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // 第一次握手
            Socket socket = new Socket(receiverHost, RECEIVER_PORT);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            outputStream.write("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = inputStream.read(buffer);
            String response = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            if (response.equals("ACK")) {
                System.out.println("第一次握手成功！");
            } else {
                System.out.println("第一次握手失败！");
                socket.close();
                System.exit(1);
            }

            while (true) {
                // 加密并发送消息
                System.out.print("请输入要发送的消息（输入'exit'退出）：");
                String message = System.console().readLine();
                if (message.equals("exit")) {
                    break;
                }

                byte[] paddedMessage = padMessage(message.getBytes(StandardCharsets.UTF_8));
                byte[] encryptedMessage = cipher.doFinal(paddedMessage);
                byte[] encodedMessage = Base64.getEncoder().encode(encryptedMessage);
                outputStream.write(encodedMessage);

                // 接收并解密回复消息
                bytesRead = inputStream.read(buffer);
                byte[] partialBuffer = Arrays.copyOfRange(buffer, 0, bytesRead);
                byte[] encodedResponse = Base64.getDecoder().decode(partialBuffer);
                byte[] decryptedResponse = cipher.doFinal(encodedResponse);
                String unpaddedResponse = new String(decryptedResponse, StandardCharsets.UTF_8).trim();
                System.out.println("接收到的回复消息：" + unpaddedResponse);
            }

            // 关闭连接
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
