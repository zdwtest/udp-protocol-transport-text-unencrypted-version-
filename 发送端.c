#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <ws2tcpip.h>

#define BUFFER_SIZE 1024

void error(const char *message)
{
	perror(message);
	exit(1);
}

int main()
{
	WSADATA wsaData;
	SOCKET sockfd;
	struct sockaddr_in serverAddr;
	char buffer[BUFFER_SIZE];
	int addrLen = sizeof(serverAddr);
	
	// 初始化Winsock库
	if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0)
	{
		error("无法初始化Winsock.");
	}
	
	// 创建套接字
	sockfd = socket(AF_INET, SOCK_DGRAM, 0);
	if (sockfd == INVALID_SOCKET)
	{
		error("无法创建socket.");
	}
	
	memset(&serverAddr, 0, sizeof(serverAddr));
	serverAddr.sin_family = AF_INET;
	
	// 用户输入接收端的域名
	char domainName[100];
	printf("输入你要的发送的域名或者IP: ");
	fgets(domainName, sizeof(domainName), stdin);
	domainName[strcspn(domainName, "\n")] = '\0';
	
	// 用户输入接收端的端口号
	unsigned short port;
	printf("输入接收者的端口: ");
	scanf("%hu", &port);
	getchar(); // 读取换行符
	
	// 解析域名获取IP地址
	struct addrinfo hints, *result;
	memset(&hints, 0, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_DGRAM;
	
	if (getaddrinfo(domainName, NULL, &hints, &result) != 0)
	{
		error("Failed to get address info.");
	}
	
	struct sockaddr_in *addr = (struct sockaddr_in *)(result->ai_addr);
	serverAddr.sin_addr = addr->sin_addr;
	
	freeaddrinfo(result);
	
	serverAddr.sin_port = htons(port);
	
	while (1)
	{
		// 输入消息
		printf("发送你的信息: ");
		fgets(buffer, BUFFER_SIZE, stdin);
		buffer[strcspn(buffer, "\n")] = '\0'; // 去除换行符
		
		// 发送消息
		int bytesSent = sendto(sockfd, buffer, strlen(buffer), 0, (struct sockaddr *)&serverAddr, addrLen);
		if (bytesSent == SOCKET_ERROR)
		{
			error("发送失败.");
		}
		
		// 接收回复消息
		int bytesRead = recvfrom(sockfd, buffer, BUFFER_SIZE, 0, (struct sockaddr *)&serverAddr, &addrLen);
		if (bytesRead == SOCKET_ERROR)
		{
			error("无法收到回应.");
		}
		
		buffer[bytesRead] = '\0'; // 添加字符串结束符
		
		// 打印回复消息
		printf("回复: %s\n", buffer);
	}
	
	// 关闭套接字
	closesocket(sockfd);
	
	// 清理Winsock库
	WSACleanup();
	
	return 0;
}
