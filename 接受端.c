#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>

#define BUFFER_SIZE 1024

void error(const char *message)
{
	perror(message);
	exit(1);
}

int main()
{
	int port;
	printf("此应用使用UDP传输信息\n请输入你的端口号:\n");
	scanf("%d", &port);
	WSADATA wsaData;
	SOCKET sockfd;
	struct sockaddr_in serverAddr, clientAddr;
	int addrLen = sizeof(clientAddr);
	char buffer[BUFFER_SIZE];
	
	// 初始化Winsock库
	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0)
	{
		error("无法初始化Winsock库。");
	}
	
	// 创建套接字
	sockfd = socket(AF_INET, SOCK_DGRAM, 0);
	if (sockfd == INVALID_SOCKET)
	{
		error("无法创建套接字。");
	}
	
	memset(&serverAddr, 0, sizeof(serverAddr));
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_addr.s_addr = INADDR_ANY;
	serverAddr.sin_port = htons(port); // 设置端口号
	
	// 绑定套接字
	if (bind(sockfd, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR)
	{
		error("无法绑定套接字。");
	}
	
	printf("等待接收消息...\n");
	
	while (1)
	{
		// 接收消息
		int bytesRead = recvfrom(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&clientAddr, &addrLen);
		if (bytesRead == SOCKET_ERROR)
		{
			error("无法接收消息。");
		}
		
		buffer[bytesRead] = '\0'; // 添加字符串结束符
		
		// 打印接收到的消息
		printf("接收到的消息：%s\n", buffer);
		
		// 发送回复消息
		printf("请输入回复消息：");
		fgets(buffer, BUFFER_SIZE, stdin);
		buffer[strcspn(buffer, "\n")] = '\0'; // 去除换行符
		
		int bytesSent = sendto(sockfd, buffer, strlen(buffer), 0, (struct sockaddr *)&clientAddr, addrLen);
		if (bytesSent == SOCKET_ERROR)
		{
			error("无法发送回复消息。");
		}
		
		printf("回复消息已发送。\n");
	}
	
	// 关闭套接字
	closesocket(sockfd);
	
	// 清理Winsock库
	WSACleanup();
	
	return 0;
}
