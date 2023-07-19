import socket
from Cryptodome.Cipher import AES
from Cryptodome.Util.Padding import pad, unpad
import base64

# 监听端口
receiver_port = 12345
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind(('', receiver_port))
sock.listen(1)

# 等待第一次握手
conn, addr = sock.accept()
data = conn.recv(1024)
if data == b'Hello':
    print("收到第一次握手请求！")
    conn.sendall(b'ACK')
else:
    print("收到无效请求！")
    conn.close()
    exit(1)

# 输入密钥
key = input("请输入密钥：").encode()
key = key.ljust(16, b'0')[:16]  # 用0填充至16位

# 创建AES解密器
cipher = AES.new(key, AES.MODE_ECB)

while True:
    # 接收并解密消息
    encoded_message = conn.recv(1024)
    encrypted_message = base64.b64decode(encoded_message)
    decrypted_message = cipher.decrypt(encrypted_message)
    unpadded_message = unpad(decrypted_message, AES.block_size)
    print("接收到的消息：", unpadded_message.decode())

    # 发送回复消息
    reply = input("请输入回复消息（输入'exit'退出）：")
    if reply == 'exit':
        break

    padded_reply = pad(reply.encode(), AES.block_size)
    encrypted_reply = cipher.encrypt(padded_reply)
    encoded_reply = base64.b64encode(encrypted_reply)
    conn.sendall(encoded_reply)

# 关闭连接
conn.close()
sock.close()