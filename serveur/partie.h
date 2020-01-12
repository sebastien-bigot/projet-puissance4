#include <time.h>
typedef struct partie{
    int id;
    int ligne;
    int colonne;
    int pion2Win;
    int suicideMode;
    int j1;
    int j2;
    int** matrice;
    int tour;
    time_t ts;
    
} Partie;

typedef struct joueur{
    char* pseudo;
    int partie_en_cours;
} Joueur;

Partie* init_partie(int colonne, int ligne, int mode, int pion, int j1);
/*
    Entrée : int x int x int x int x int 
    Sortie : Partie*
    Cette fonction retourne un pointeur vers une nouvelle partie ayant les attributs spécifiés en paramètre
    initialisé.
*/

void freePartie(Partie* p);
/*
    Entrée : Partie*
    Sortie : void
    Cette fonction libère l'espace mémoire de la partie donnée en paramètre.
*/

char *convertisPartie(Partie* p,Joueur** liste_des_joueurs);
/*
    Entrée : Partie* x Joueur**
    Sortie : char*
    Cette fonction permet de traduire la partie p passé en paramètre en une chaine de caractère 
    qui est lisible par le client.
*/


int trouvePartie(Partie** liste_des_parties,int joueur);
/*
    Entrée : Partie** x int
    Sortie : int
    Cette fonction renvoie l'id de la partie contenant le joueur passé en paramètre.
*/

int estColonneValide(Partie* p,int col);
/*
    Entrée : Partie* x int
    Sortie : int
    Cette fonction renvoie 0 ou 1 si la colonne passé en paramètre est invalide ou valide respectivement.
*/

int posePion(Partie* p, int colonne,int joueur);
/*
    Entrée : Partie* x colonne x joueur
    Sortie : int
    Cette fonction renvoie 0 ou 1 si le coup dans la colonne donnée en 
    paramètre par le joueur donné en paramètre est invalide ou valide respectivement
*/

int estCoordValide(Partie* p, int ligne, int colonne);
/*
    Entrée : Partie* x int x int
    Sortie : int 
    Cette fonction renvoie 0 ou 1 si les coordonnées passées en paramètre sont invalides ou valides respectivement.
*/

int compteValeur(Partie* p, int ligne, int colonne, int joueur,int inc_lig, int inc_col);
/*
    Entrée : Partie* x int x int x int x int x int 
    Sortie : int 
    Cette fonction permet de renvoyer le nombre de pion aligné selon inc_lig et inc_col
    (1 pour inc_lig indiquant qu'on se déplace sur l'axe vertical et horizontal si inc_col est à 1,
    diagonale si les deux sont à 1)
*/

int estPartieTermine(Partie* p);
/*
    Entrée : Partie*  
    Sortie : int 
    Cette fonction permet de renvoyer 0 ou 1 si la partie n'est pas terminé ou si elle l'est respectivement 
*/

int estPlein(Partie* p);
/*
    Entrée : Partie*  
    Sortie : int 
    Cette fonction permet de renvoyer 0 ou 1 si la partie n'est pas pleine (égalité) ou si elle l'est respectivement 
*/
