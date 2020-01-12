#include "partie.h"
#include <stdlib.h>
#include <time.h> 
#include <string.h>
#include <stdio.h>

Partie* init_partie(int colonne, int ligne, int mode, int pion, int j1){
    
    // On initialise le random du c avec comme seed l'heure actuelle 
    srand(time(NULL));
    
    // On crée la partie et on lui affecte les valeurs passé en paramètre ainsi que les valeurs par défaut
    Partie* p = malloc(sizeof(Partie));
    p->colonne = colonne;
    p->ligne = ligne;
    p->j1 = j1;
    p->j2 = -1;
    p->suicideMode = mode;
    p->pion2Win = pion;
    p->ts = 0;
    int** matrice = malloc(sizeof(int*)*ligne);
    for (int i=0;i<ligne;i++){
        matrice[i] = malloc(sizeof(int)*colonne);
        for(int j = 0;j<colonne;j++){
			matrice[i][j] = -1;
		}
    }
    p->matrice = matrice;
    p->tour = rand()%2;

    // On renvoie cette partie
    return p;
}

void freePartie(Partie* p){

    // On libére toutes les lignes de la matrice
    for (int i=0;i<p->ligne;i++){
        free(p->matrice[i]);
    }

    // On libére la matrice
    free(p->matrice);

    // On libère la partie
    free(p);
}

char* convertisPartie(Partie* p,Joueur** liste_des_joueurs){
    // On crée la partie
    char* message = malloc(sizeof(char)*512);
    char buffer[4];

    // On ajoute dans le message l'id 
    sprintf(buffer,"%d",p->id);
    strcat(message,buffer);
    strcat(message," ");

    // On ajoute dans le message le pseudo
    strcat(message,liste_des_joueurs[p->j1]->pseudo);

    // On ajoute dans le mode de jeu
    strcat(message,(p->suicideMode?" true ":" false "));

    // On ajoute dans le message le nombre de ligne
    sprintf(buffer,"%d",p->ligne);
    strcat(message,buffer);
    strcat(message," ");

    // On ajoute dans le message le nombre de colonne
    sprintf(buffer,"%d",p->colonne);
    strcat(message,buffer);
    strcat(message," ");

    // On ajoute dans le message le nombre de pion pour gagner
    sprintf(buffer,"%d",p->pion2Win);
    strcat(message,buffer);

    // On retourne ce message
    return message;
}

int trouvePartie(Partie** liste_des_parties, int joueur){
    // On se balade dans toutes les parties  
    for (int i = 0 ; i < sizeof(liste_des_parties) / sizeof(Partie*) ; i++){
        // Si la partie n'est pas défini, on la passe
        if (liste_des_parties[i]==NULL){
            continue;
        }
        // Si la partie est défini, on regarde si le joueur passé en paramètre est dans la partie
        if (liste_des_parties[i]->j1 == joueur || liste_des_parties[i]->j2 == joueur){
            return i;
        }
    }
    // On renvoie -1 si le joueur n'a été trouvé dans aucune des parties
    return -1;
}

int estColonneValide(Partie* p, int col){
    // On se balade dans toutes les lignes
    for (int l = 0 ; l < p->ligne ; l++){
        // On regarde si la colonne à une case vide si oui retourne 1
        if (p->matrice[l][col]==-1){
            return 1;
        }
    }
    // Sinon retourne 0 
    return 0;
}

int posePion(Partie* p, int colonne, int joueur){
    //On regarde si la colonne est valide
    if (estColonneValide(p, colonne)) {
        int l = 0;
        
        // On descend jusqu'à arriver tout en bas ou si on rencontre un pion
        while (l < p->ligne && p->matrice[l][colonne]==-1) {
            l+=1;
        }
        // On met le pion dans la ligne juste au dessus de celle où on a rencontrer le pion
        p->matrice[l-1][colonne] = joueur;
        
        // On met à jour le tour
        p->tour = (p->tour+1)%2;

        // Le coup est valide on retourne 1
        return 1;
    } else {
        // Si elle ne l'est pas le coup n'est pas valide et on retourne 0
        return 0;
    }
}

int estCoordValide(Partie* p, int ligne, int colonne){
    //Si le coup n'est pas en dehors des limites retourne 1 sinon 0
    return (ligne>=0 && ligne<=p->ligne-1 && colonne >=0 && colonne<=p->colonne-1);
}

int compteValeur(Partie* p, int ligne, int colonne, int joueur,int inc_lig, int inc_col){
    int inc = 0;
    int maxi = 0;
    while (estCoordValide(p,ligne,colonne)) {
        
        //On cherche la plus grosse ligne de pion consécutif du joueur passé en paramètre 
        if (p->matrice[ligne][colonne]==joueur) {
            inc+=1;
        } else {
            if (inc>maxi) {
                maxi=inc;
            }
            inc=0;
        }
        ligne += inc_lig;
        colonne += inc_col;
    }
    if (maxi==0) {
        maxi=inc;
    }
    return maxi;
}

int estPartieTermine(Partie* p){
    int nbPion2Win = p->pion2Win;
    for (int l = 0;l<p->ligne;l++) {
        // On regarde si il y a une victoire (nb de pion dans une ligne >= au nombre de point pour win)
        // dans toutes les lignes possibles
        if (compteValeur(p,l,0,0,0,1)>=nbPion2Win
            || compteValeur(p,l,0,1,0,1)>=nbPion2Win
            || compteValeur(p,l,0,0,1,1)>=nbPion2Win
            || compteValeur(p,l,0,1,1,1)>=nbPion2Win
            || compteValeur(p,l,p->colonne-1,0,1,-1)>=nbPion2Win
            || compteValeur(p,l,p->colonne-1,1,1,-1)>=nbPion2Win){
            return 1;
        }
    }
    for (int c=0;c<p->colonne;c++) {
        // On regarde si il y a une victoire (nb de pion dans une ligne >= au nombre de point pour win)
        // dans toutes les colonnes possibles
        if (compteValeur(p,0,c,0,1,0)>=nbPion2Win
            || compteValeur(p,0,c,1,1,0)>=nbPion2Win
            || compteValeur(p,0,c,0,1,1)>=nbPion2Win
            || compteValeur(p,0,c,0,1,-1)>=nbPion2Win
            || compteValeur(p,0,c,1,1,1)>=nbPion2Win
            || compteValeur(p,0,c,1,1,-1)>=nbPion2Win){
            return 1;
        }
    }
    return 0;
}

int estPlein(Partie* p){
    //On regarde si toutes les cases sont occupées
    for (int i=0;i<p->ligne;i++){
        for (int j=0;j<p->colonne;j++){
            if (p->matrice[i][j]==-1){
                return 0;
            }
        }
    }
    return 1;
}

