import socket
from Cryptodome.Cipher import AES
from Cryptodome.Util.Padding import pad, unpad
import base64

# 输入接收端域名
receiver_host = input("请输入接收端域名：")

# 输入密钥
key = input("请输入密钥：").encode()
key = key.ljust(16, b'0')[:16]  # 用0填充至16位

# 创建AES加密器
cipher = AES.new(key, AES.MODE_ECB)

# 第一次握手
receiver_port = 12345
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((receiver_host, receiver_port))
sock.sendall(b'Hello')
response = sock.recv(1024)
if response == b'ACK':
    print("第一次握手成功！")
else:
    print("第一次握手失败！")
    sock.close()
    exit(1)

while True:
    # 加密并发送消息
    message = input("请输入要发送的消息（输入'exit'退出）：")
    if message == 'exit':
        break

    padded_message = pad(message.encode(), AES.block_size)
    encrypted_message = cipher.encrypt(padded_message)
    encoded_message = base64.b64encode(encrypted_message)
    sock.sendall(encoded_message)

    # 接收并解密回复消息
    response = sock.recv(1024)
    encoded_response = base64.b64decode(response)
    decrypted_response = cipher.decrypt(encoded_response)
    unpadded_response = unpad(decrypted_response, AES.block_size)
    print("接收到的回复消息：", unpadded_response.decode())

# 关闭连接
sock.close()