#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

struct server{
    int socket;
    struct sockaddr_in servAddr;
    struct sockaddr_in clientAddr;
    socklen_t clientLen;

    ssize_t (*server_receive)(struct server* ,char*,size_t );
    void (*server_send)(struct server* ,char*);
    void (*server_bind)(struct server* ,int );
};

typedef struct server* Server;

Server server_create_udp();

void server_close_and_free(Server);
