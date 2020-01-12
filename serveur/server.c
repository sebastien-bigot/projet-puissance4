#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/types.h>
#include <arpa/inet.h>  
#include <sys/socket.h>  
#include <netinet/in.h>  
#include "server.h"
#include "error.h"
#include "error.c"
#include "partie.h"
#define MAX 40
#define MAX_JOUEUR 30
#define TIME_LIMIT 10

ssize_t server_receive_udp(Server this, char* buf, size_t size){
    return recvfrom(this->socket, buf, size, 0, (struct sockaddr *) &this->clientAddr, &this->clientLen);
}

void server_send_udp(Server this, char* msg){
    if (sendto(this->socket, msg, strlen(msg), 0, (struct sockaddr *) &this->clientAddr, this->clientLen) == ERR){
        neterror(SEND_ERROR);
    }
}

void server_bind(Server this, int port){
    this->servAddr.sin_family = AF_INET;
    this->servAddr.sin_addr.s_addr = INADDR_ANY;
    this->servAddr.sin_port = htons((uint16_t) port);
    if(bind(this->socket, (struct sockaddr *) &this->servAddr, sizeof(this->servAddr))<0){
        neterror(BIND_ERROR);
    }
}

Server server_create_udp(){
    Server srv = malloc(sizeof(struct server));

    int sfd;
    if((sfd=socket(AF_INET, SOCK_DGRAM, 0))==ERR){
        free(srv);
        neterror(SOCKET_ERROR);
    }
    srv->socket = sfd;
    memset(&srv->servAddr, 0, sizeof(struct sockaddr_in));
    memset(&srv->clientAddr,0, sizeof(struct sockaddr_in));
    srv->clientLen = sizeof(struct sockaddr_in);
    srv->server_bind=&server_bind;
    srv->server_receive=&server_receive_udp;
    srv->server_send=&server_send_udp;
      
    return srv;
}

void server_close_and_free(Server this){
    close(this->socket);
    free(this);
}


void server_listen(Server this){
    if((listen(this->socket,1)) !=0){
        neterror(LISTEN_ERROR);
    }
}

int server_accept(Server this){
    int connfd = accept(this->socket,(struct sockaddr *) &this->clientAddr, &this->clientLen);
    if(connfd < 0 ) {
        neterror(ACC_ERROR);
    }
    return connfd;
}

ssize_t server_receive_tcp(Server this, char* buf, size_t size){
    return recvfrom(this->socket, buf, size, 0, (struct sockaddr *) &this->clientAddr, &this->clientLen);
}

void server_send_tcp(Server this, char* msg){
    if (sendto(this->socket, msg, strlen(msg), 0, (const struct sockaddr *) &this->clientAddr, this->clientLen) == ERR){
        neterror(SEND_ERROR);
    }
}


Server server_create_tcp(){
    Server srv = malloc(sizeof(struct server));

    int sfd;
    if((sfd=socket(AF_INET, SOCK_STREAM, 0))==ERR){
        free(srv);
        neterror(SOCKET_ERROR);
    }
    srv->socket = sfd;
    memset(&srv->servAddr, 0, sizeof(struct sockaddr_in));
    srv->server_bind=&server_bind;
    srv->server_receive=&server_receive_tcp;
    srv->server_send=&server_send_tcp;

    return srv;
}

int peut_creer_partie(Partie** liste_parties){
    // On regarde si il y a un emplacement de disponible dans le tableau des parties
    for (int i = 0 ; i < MAX ; i++){
        if (!liste_parties[i]){
            return 1;
        }
    }
    return 0;
}

void ajouter_joueur(Joueur** liste_joueurs,int  indice){
    // On ajoute un pointeur vers un nouveau joueur ainsi que ces variables d'initialisations à l'indice
    // donnée en paramètre
    liste_joueurs[indice] = malloc(sizeof(Joueur));
    liste_joueurs[indice]->partie_en_cours = -1;
    liste_joueurs[indice]->pseudo = NULL;
}

int startsWith(char* string, char* prefix ) {
    // On regarde si la chaine de caractère démarre par le préfixe donnée en paramètre
    return !strncmp(string, prefix, strlen(prefix));
}

int chercheJoueur(Partie** p, int joueur){
    // On cherche dans le tableau de partie le joueur passé en paramètre
    for (int i=0;i<MAX;i++){
        if (p[i]->j1 == joueur || p[i]->j2 == joueur){
            return i;
        }
    }
    return -1;
}

int analyse(char* chaine){
    // On attribut à chaque message le comportement associé dans le switch du main
    if (startsWith(chaine,"CB")){

        // Correspond au message qui demande le nb de parties
        return 0;

    } else if (startsWith(chaine,"EP")){
        
        // Correspond au message qui demande d'envoyer toutes les parties
        return 1;

    } else if (startsWith(chaine, "CP")){
        
        // Correspond au message qui demande la création d'une partie 
        return 2;

    } else if (startsWith(chaine,"RP")){

        // Correspond au message qui demande l'accès pour rejoindre une partie
        return 3;

    } else if (startsWith(chaine,"PP")){

        // Correspond au message qui demande l'ajout d'un pion dans une colonne d'une partie
        return 4;

    } else if (startsWith(chaine,"DP")){

        // Correspond au message qui demande la destruction de la partie (uniquement envoyé lorsque l'hote attend un adversaire)
        return 5;

    } else if (startsWith(chaine,"VP")){

        // Correspond au message qui demande si il est possible de créer une partie (en fonction du nombre déjà crée)
		return 6;

	} else if (startsWith(chaine,"OK")){

        // Correspond au message qui demande d'envoyer les tours
		return 7;

	} else if (startsWith(chaine,"AB")){

        // Correspond au message qui indique un tour qui a été abandonné à cause du temps
		return 8;
	} else if (startsWith(chaine,"TTL")){

        // Correspond au message qui indique que le serveur est encore actif
		return 9;
	}
    return -1;
}

void detruit_partie(Partie** p, int id){
    free(p[id]);
    p[id] = NULL;
}

int main(void){

    // On crée un buffer qui contiendra notre message UDP
    char bufchk[11];
    char msgchk[11];
    pid_t pid;
    
    // On fork afin d'avoir un autre processus qui s'occupe des demandes UDP (le discover des serveurs)
    if ( (pid = fork() ) == ERR){
        perror("Fork");
        exit(1);
    }

    if ( !pid ) {
        
        // On est dans le fils, on va créer le socket UDP qu'on va bind sur le port 3615
        Server serv = server_create_udp();
        server_bind(serv, 3615);

        for( ; ; ){
            
            // Boucle infini, dès la réception d'un message sur le port, on répond au client pour
            // indiquer notre présence
            server_receive_udp(serv,bufchk,11);
            snprintf(msgchk,11,"%s","CheckservB");
            server_send_udp(serv,msgchk);
        }
    } else {
        // On est dans le père, on va gérer les connexions tcp pour les parties

        //On initialise toutes les variables
        //  liste des parties
        Partie* liste_parties[MAX];
        for (int i = 0 ; i < MAX ; i++) liste_parties[i] = NULL;
        //  liste des joueurs
        Joueur* liste_joueurs[MAX_JOUEUR];
        //  nombre de parties visibles et nombre de caractere
        int cptParties=0, nb_caractere;
        //  le tableau des clients, leur valeurs, le nombre de connexion max, l'activité des clients etc...
        int clientskt[MAX_JOUEUR],value,maxsd,activity,nv_skt,sd;
        for(int i = 0 ; i < 30 ; i++){
            clientskt[i]=0;
        }

        //  le buffer des messages
        char buffer[1025];
        
        //  l'ensemble des files descriptors lus 
        fd_set fdslus;

        //  le socket principal en tcp associé au port 3616
        Server srvacc = server_create_tcp();
        server_bind(srvacc, 3616);
        server_listen(srvacc);

        while( 1 ){
            // boucle infini 

            // On init tous les file descriptors
            FD_ZERO(&fdslus);
            FD_SET(srvacc->socket, &fdslus);
            maxsd=srvacc->socket;

            // On gère les diffèrentes réceptions de messages pour tous les clients
            for(int i = 0 ; i < MAX_JOUEUR ; i++){
                sd = clientskt[i];
                if(sd>0)
                    FD_SET(sd,&fdslus);
                if(sd>maxsd)
                    maxsd=sd;
            }

            // On effectue le select fonction principal qui gère la réception de message en simultanée
            activity = select(maxsd+1, &fdslus, NULL, NULL, NULL);
            if ( (activity < 0) && (errno!=EINTR) ) {   
                neterror(SELECT_ERROR);   
            }

            // Si on a reçu un message, on établi la connexion et on l'ajoute au client à écouter
            if ( FD_ISSET(srvacc->socket, &fdslus) ){
                nv_skt=server_accept(srvacc);
                for( int i=0 ; i < MAX_JOUEUR ; i++){

                    // On met le nouveau client dans une case vide du tableau et on crée 
                    // sa structure joueur associé
                    if( clientskt[i] == 0 ){
                        clientskt[i] = nv_skt;
                        ajouter_joueur(liste_joueurs, i);
                        break;
                    }
                }
            }

            // Boucle for principal qui va interprété les messages reçu pour tous les clients connectés
            for(int i=0 ; i < MAX_JOUEUR ; i++){
                sd = clientskt[i];
                // Si on a reçu un message, on le lit
                if ( FD_ISSET(sd, &fdslus) ){
                    if (( value = read(sd, buffer, 1024) ) == 0) {
                        // Le message reçu est vide (cad une déco du client)
                        // On va chercher à supprimer toutes les parties dans laquelle il était et informer
                        // le deuxieme joueur si il était dans une partie
						int id = liste_joueurs[i]->partie_en_cours;
						if (id != -1){
							if ( liste_parties[id]->j1 == i ){
								if ( liste_parties[id]->j2 != -1 ){
									strcpy(buffer,"Abandon");
									nb_caractere = strlen(buffer);
									buffer[nb_caractere]='\n';
									buffer[nb_caractere+1]='\0';
									send(clientskt[liste_parties[id]->j2], buffer, strlen(buffer), 0);
									liste_joueurs[liste_parties[id]->j2]->partie_en_cours=-1;
								}
							} else {
								if ( liste_parties[id]->j1 != -1 ){
									strcpy(buffer,"Abandon");
									nb_caractere = strlen(buffer);
									buffer[nb_caractere]='\n';
									buffer[nb_caractere+1]='\0';
									send(clientskt[liste_parties[id]->j1],buffer,strlen(buffer),0);
									liste_joueurs[liste_parties[id]->j1]->partie_en_cours=-1;
								}
							}
							detruit_partie(liste_parties,id);
						}
                        // On ferme le file descriptor (le socket)
                        close(sd);

                        // On libère l'espace
                        clientskt[i]=0;
                        
                        // On libère l'espace réservé au joueur qui vient de partir
                        if (liste_joueurs[i]!=NULL){
                            free(liste_joueurs[i]->pseudo);
                            liste_joueurs[i]->pseudo = NULL;
                        }
                        if (liste_joueurs[i]!=NULL){
                            free(liste_joueurs[i]);
                            liste_joueurs[i] = NULL;
                        }

                    }else{
                        // Sinon le message n'est pas vide
                        buffer[value]='\0';

                        // On analyse le début afin de savoir quelle type de message a été recu
                        // On initialise aussi toute une série de variable qui nous servirons dans le switch    
                        int cas = analyse(buffer),cptTok=0,findHole=0,addligne,addcolonne,addMode,addP2W;
                        int id;
                        int colonne;
                        char* tmp;

                        // Switch principal qui effectue les actions en fonction des messages reçu
                        switch ( cas ){

                            // Cas numéro 0 : CB, on veut connaitre le nombre de partie visible (pas en cours)
                            case 0:

                                // Pour cela, on a cptPartie qui nous indique le nombre de partie visible
                                snprintf(buffer,strlen(buffer),"%d",cptParties);
                                nb_caractere = strlen(buffer);
                                buffer[nb_caractere]='\n';
                                buffer[nb_caractere+1]='\0';

                                // On lui renvoie cptPartie
                                send(sd,buffer,strlen(buffer),0);
                                buffer[0]='\0';
                                break;

                            // Cas numéro 1 : EP, on veut avoir les parties visibles (pas en cours)
                            case 1:

                                // Pour chaque partie dans la liste des parties
                                for ( int i = 0 ; i < MAX ; i++ ){

                                    // Si la partie n'est pas vide, et qu'il n'y a pas de deuxième joueur
                                    // (cad une partie que l'on peut rejoindre)
                                    if (( liste_parties[i] != NULL ) && ( liste_parties[i]->j2 == -1 )){

                                        // On convertis la partie en un message que l'on peut envoyer
                                        tmp = convertisPartie(liste_parties[i],liste_joueurs);
                                        strcpy(buffer,tmp);
                                        nb_caractere = strlen(buffer);
										buffer[nb_caractere]='\n';
										buffer[nb_caractere+1]='\0';

                                        // On envoie la partie
                                        send(sd,buffer,strlen(buffer),0);
										buffer[0]='\0';
                                    }
                                }
                                break;

                            // Cas numéro 2 : CP, on veut créer une partie
                            case 2:

                                // On recup les infos supplémentaire en plus de CP
								read(sd,buffer,1024);

                                // On va les traiter (les couper à chaque espace)
								char *token = strtok(buffer," ");

								while(token != NULL){
									switch (cptTok){

                                        // Découpage 0 : On recupere le pseudo
										case 0:
											liste_joueurs[i]->pseudo = malloc(sizeof(char)*21);
											strcpy(liste_joueurs[i]->pseudo,token);
											break;

                                        // Découpage 1 : On récupere le mode de jeu
										case 1:
											addMode=atoi(token);
											break;

                                        // Découpage 2 : On récupere le nombre de ligne
										case 2:
											addligne=atoi(token);
											break;
                                        
                                        // Découpage 3 : On récupere le nombre de colonne
										case 3:
											addcolonne=atoi(token);
											break;
                                        
                                        // Découpage 4 : On récupere le nombre de pion pour gagner
										case 4:
											addP2W=atoi(token);
											break;

										default:
											break;
									}
									cptTok++;
									token=strtok(NULL," ");
								}

                                // On crée une partie avec les infos récupérées
								Partie *addp=init_partie(addcolonne,addligne,addMode,addP2W,i);

                                // On cherche un endroit dans le tableau où la mettre
								while(liste_parties[findHole]!=NULL){
									findHole++;
								}

                                // On l'ajoute
								addp->id = findHole;
								liste_parties[findHole]=addp;
								liste_joueurs[i]->partie_en_cours=addp->id;
								cptParties++;
                                break;

                            // Cas numéro 3 : RP, on veut rejoindre une partie  
                            case 3:

                                // On recup l'id de la partie que l'on souhaite rejoindre
                                id = atoi(buffer+2);

                                // Si la partie n'est pas valide (n'existe pas ou n'est pas dispo)
                                if (liste_parties[id]==NULL || liste_parties[id]->j2!=-1){

                                    // Envoi d'un message de refus de connexion à la partie
									strcpy(buffer,"NoJoin");
									nb_caractere = strlen(buffer);
									buffer[nb_caractere]='\n';
									buffer[nb_caractere+1]='\0';
									send(sd,buffer,strlen(buffer),0);
									buffer[0]='\0';

                                } else {
                                    // Sinon on rejoint la partie
                                    liste_parties[id]->j2 = i;
                                    liste_joueurs[i]->partie_en_cours = id;
                                    if (liste_joueurs[i]->pseudo != NULL){
                                        free(liste_joueurs[i]->pseudo);
                                        liste_joueurs[i]->pseudo = NULL;
                                    }
                                    liste_joueurs[i]->pseudo = malloc(sizeof(char)*21);
                                    strcpy(liste_joueurs[i]->pseudo,"Anonyme");

                                    // Et on envoie le signal de départ pour les deux joueurs de la partie
                                    strcpy(buffer,"Join");
									nb_caractere = strlen(buffer);
									buffer[nb_caractere]='\n';
									buffer[nb_caractere+1]='\0';
									cptParties--;
									liste_parties[id]->ts = time(NULL);
									send(clientskt[liste_parties[id]->j2],buffer,strlen(buffer),0);
									send(clientskt[liste_parties[id]->j1],buffer,strlen(buffer),0);
									buffer[0]='\0';
                                }
                                break;
                            
                            // Cas numéro 4 : PP, on veut poser un pion dans une partie
                            case 4:	
                                // On recup la colonne dans lequel on veut jouer
                                colonne = atoi(buffer+2);

                                // On recup l'id de la partie
                                id = liste_joueurs[i]->partie_en_cours;
                                
                                // Si c'est la bonne personne qui joue
                                if ((liste_parties[id]->tour == 0 && liste_parties[id]->j1 == i )|| (liste_parties[id]->tour == 1 && liste_parties[id]->j2 == i)){
                                    
                                    // Alors on pose le pion dans la matrice qui gére la partie
                                    posePion(liste_parties[id],colonne,liste_parties[id]->tour);

                                    // Et on envoie aux deux joueurs le message de validation du coup
									snprintf(buffer,strlen(buffer),"%d",colonne);
									nb_caractere = strlen(buffer);
									buffer[nb_caractere]='\n';
									buffer[nb_caractere+1]='\0';
									send(clientskt[liste_parties[id]->j2],buffer,strlen(buffer),0);
									send(clientskt[liste_parties[id]->j1],buffer,strlen(buffer),0);
									buffer[0]='\0';
                                }
                                break;

                            // Cas numéro 5 : DP, on veut détruire une partie   
                            case 5:
                                // On récup l'id de la partie 
                                id = liste_joueurs[i]->partie_en_cours;

                                // Puis on l'enleve de notre tableau
                                detruit_partie(liste_parties,id);
                                cptParties--;

                                // On reinitialise la partie en cours du joueur qui détruit la partie
                                liste_joueurs[i]->partie_en_cours=-1;
                                break;

                            // Cas numéro 6 : VP, on veut savoir si c'est possible de créer une partie
                            case 6:
                                // Si le nombre de parties visible est égale aux nombres max de partie possible
								if ( cptParties == MAX_JOUEUR ){

                                    //On envoie un message de refus
									strcpy(buffer,"MaxAtt");
									nb_caractere = strlen(buffer);
									buffer[nb_caractere]='\n';
									buffer[nb_caractere+1]='\0';
									send(sd,buffer,strlen(buffer),0);
									buffer[0]='\0';
								} else {

                                    // Sinon on envoie un message de validation
									strcpy(buffer,"NoMax");
									nb_caractere = strlen(buffer);
									buffer[nb_caractere]='\n';
									buffer[nb_caractere+1]='\0';
									send(sd,buffer,strlen(buffer),0);
									buffer[0]='\0';
								}
                                break;
                            
                            // Cas numéro 7 : OK, on veut savoir où en est la partie
                            case 7:
								
                                // Est ce qu'elle est terminé ?
								if ( estPartieTermine( liste_parties[id] ) ){
                                    // Si oui on affiche le gagnant en fonction du mode et du tour actuel
									int tour_du_gagnant = liste_parties[id]->tour;
									if ( !liste_parties[id]->suicideMode ){
										tour_du_gagnant = (tour_du_gagnant+1)%2;
									}
									if ( tour_du_gagnant ){
										// ENVOI GAGNER AU J2
										// ENVOI PERDU AU J1
										strcpy(buffer,"W1");
										nb_caractere = strlen(buffer);
										buffer[nb_caractere]='\n';
										buffer[nb_caractere+1]='\0';
										send(clientskt[i],buffer,strlen(buffer),0);
										buffer[0]='\0';
									} else {
										// ENVOI GAGNER AU J1
										// ENVOI PERDU AU J2
										strcpy(buffer,"W0");
										nb_caractere = strlen(buffer);
										buffer[nb_caractere]='\n';
										buffer[nb_caractere+1]='\0';
										send(clientskt[i], buffer, strlen(buffer), 0);
										buffer[0]='\0';
									}

                                    // On supprime la partie et on reset les deux joueurs de celle ci
									liste_joueurs[i]->partie_en_cours=-1;
									if ( liste_parties[id]->j1 == i ){
										liste_parties[id]->j1=-1;
									} else{
										liste_parties[id]->j2=-1;
									}
									if (( liste_parties[id]->j1 == -1 ) && ( liste_parties[id]->j2 == -1 )){
										detruit_partie(liste_parties,id);
									}

                                } else if ( estPlein( liste_parties[id] )){
                                    // Si égalité
                                    // ENVOI EGALITE AU 2 JOUEURS
                                    strcpy(buffer,"D");
                                    nb_caractere = strlen(buffer);
                                    buffer[nb_caractere]='\n';
                                    buffer[nb_caractere+1]='\0';
                                    send(clientskt[i],buffer,strlen(buffer),0);
                                    buffer[0]='\0';
                                } else {
                                    // Sinon on envoie le tour du joueur qui doit jouer
                                    // ENVOI 1 ou 0 à J2 ET J1
                                    buffer[0]='0'+liste_parties[id]->tour;
                                    buffer[1]='\n';
                                    buffer[2]='\0';
                                    send(clientskt[i],buffer,strlen(buffer),0);
                                    buffer[0]='\0';
                                }
								break;

                            // Cas numéro 8 : AB, on veut signaler qu'un joueur a été trop long
							case 8:

                                // On récup la partie du joueur
								id = liste_joueurs[i]->partie_en_cours;

                                // Si la partie existe
								if( id != -1 ){

                                    // On vérifie que le temps pour jouer à bien été dépassé
									if (( time(NULL) - liste_parties[id]->ts ) >= TIME_LIMIT ){

                                        // Si oui, on envoie les bons messages aux bons joueurs
										if ( liste_parties[id]->tour == 0 ){
											strcpy(buffer,"W1");
											nb_caractere = strlen(buffer);
											buffer[nb_caractere]='\n';
											buffer[nb_caractere+1]='\0';
											send(clientskt[liste_parties[id]->j2],buffer,strlen(buffer),0);
											send(clientskt[liste_parties[id]->j1],buffer,strlen(buffer),0);
											buffer[0]='\0';
										} else {
											strcpy(buffer,"W0");
											nb_caractere = strlen(buffer);
											buffer[nb_caractere]='\n';
											buffer[nb_caractere+1]='\0';
											send(clientskt[liste_parties[id]->j2],buffer,strlen(buffer),0);
											send(clientskt[liste_parties[id]->j1],buffer,strlen(buffer),0);
											buffer[0]='\0';
										}
									} else {
                                        // Si le temps n'a pas été dépassé, c'est qu'il y a eu triche et on fait gagner l'autre joueur
										if ( liste_parties[id]->tour == 0 ){
											strcpy(buffer,"W0");
											nb_caractere = strlen(buffer);
											buffer[nb_caractere]='\n';
											buffer[nb_caractere+1]='\0';
											send(clientskt[liste_parties[id]->j2],buffer,strlen(buffer),0);
											send(clientskt[liste_parties[id]->j1],buffer,strlen(buffer),0);
											buffer[0]='\0';
										} else {
											strcpy(buffer,"W1");
											nb_caractere = strlen(buffer);
											buffer[nb_caractere]='\n';
											buffer[nb_caractere+1]='\0';
											send(clientskt[liste_parties[id]->j2],buffer,strlen(buffer),0);
											send(clientskt[liste_parties[id]->j1],buffer,strlen(buffer),0);
											buffer[0]='\0';
										}
									}
                                    // On reset les deux joueurs après la fin de leur partie
									liste_joueurs[liste_parties[id]->j1]->partie_en_cours=-1;
									liste_joueurs[liste_parties[id]->j2]->partie_en_cours=-1;
                                    // On détruit la partie
									detruit_partie(liste_parties,id);
								}
								break;

                            // Cas numéro 9 : TTL, on veut savoir si le serveur est encore la
                            case 9:
                                buffer[0]='O';
                                buffer[1]='K';
                                buffer[2]='\n';
								buffer[3]='\0';
                                send(sd,buffer,strlen(buffer),0);
                                buffer[0]='\0';
                                break;

							default:
								break;
                        }
                    }
                }
            }
        }
    }
    return 0;
}
