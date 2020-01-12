#include <stdio.h>

#define ERR -1
#define NO_ERROR 0
#define SOCKET_ERROR 1
#define BIND_ERROR 2
#define SEND_ERROR 3
#define LISTEN_ERROR 4
#define ACC_ERROR 5
#define CONNECT_ERROR 6
#define SELECT_ERROR 7

#define syserror(m,e) perror(m),exit(e)
#define neterror(n) syserror(msgErr[n],n)

extern char* msgErr[];
