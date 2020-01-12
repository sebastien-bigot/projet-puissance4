/*
 * Package : Modele
 * Classe : Modele
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de définir le modéle du puissance 4 
 */

package modele;

import java.util.Arrays;

public class Modele {
    /**
     * Classe representant le Modele
     * Determine la grille du jeu
     **/
    private int ligne;
    private int colonne;
    private int joueur;
    private int[][] plateau;
    private int[] dernierCoup;
    private boolean suicideMode;
    private int nbPion2Win;
    private boolean vsIA;

    public Modele(int ligne,int colonne,boolean suicideMode,boolean estVSIA, int nbPion2Win) {
        /** 
         * Entrée : int, int , boolean, int
         * Sortie : Modele
         * Constructeur de la classe Modele
         **/
            this.suicideMode = suicideMode;
            this.nbPion2Win = nbPion2Win;
            this.vsIA = estVSIA;
            this.ligne = ligne;
            this.colonne = colonne;
            this.plateau = new int[ligne][colonne];
            for(int l = 0; l<ligne;l++) {
                    int ligne_plateau[] = new int[colonne];
                    for (int c = 0; c<colonne;c++ ) {
                            ligne_plateau[c] = -1;
                    }
                    this.plateau[l] = ligne_plateau;
            }
            this.dernierCoup = new int[2];
    }

    public Modele(){
        /** 
         * Entrée : Rien
         * Sortie : Modele
         * Constructeur de la classe Modele par défaut
         **/
            this(6,7,false,false,4);
    }

    public boolean estColonneValide(int nCol) {
        /** 
         * Entrée : int
         * Sortie : boolean
         * Renvoie true si la colonne passé en paramètre est un emplacement disponible pour un pion
         **/
            for (int l=0;l<getLigne();l++) {
                    if (this.plateau[l][nCol]==-1) {
                            return true;
                    }
            }
            return false;
    }

    public boolean posePion(int colonne, int joueur) {
        /** 
         * Entrée : int, int
         * Sortie : boolean
         * Pose le pion dans la colonne passé en paramètre et renvoie true ou false en fonction de si il a réussi
         **/
            if (estColonneValide(colonne)) {
                    int l = 0;
                    while (l < getLigne() && this.plateau[l][colonne]==-1) {
                            l+=1;
                    }
                    this.plateau[l-1][colonne] = joueur;
                    this.dernierCoup[0] = l-1 ;
                    this.dernierCoup[1] = colonne ;
                    this.joueur = (joueur+1)%2;
                    return true;
            } else {
                    return false;
            }
    }

    public int calculeMeilleurCoup(){
        /** 
         * Entrée : Rien
         * Sortie : int
         * Renvoie la colonne du meilleur coup possible pour l'IA
         **/
        int[] c = coupPossible();
        if (getSuicide()){
            return choisitMeilleurCoupSuicide(c);
        }
        return choisitMeilleurCoup(c);
    }

    public int[] coupPossible(){
        /** 
         * Entrée : Rien
         * Sortie : int[]
         * Renvoie la liste des colonnes dans lequel il est possible de jouer
         **/
        int res[] = new int[getColonne()];
        int iRes = 0;
        for (int i=0;i<getColonne();i++){
            if (estColonneValide(i)){
                res[iRes++]=i;
            }
        }
        return Arrays.copyOf(res, iRes);
    }

    public int choisitMeilleurCoup(int[] coupPossible){
        /** 
         * Entrée : int[]
         * Sortie : int
         * Renvoie la colonne du meilleur coup possible parmi une liste de coup possible
         **/
        int tour = 0;
        boolean aCoupPossible = false;
        int maxScore = -1;
        for (int i=0;i<coupPossible.length;i++){
            if (peutTerminer(coupPossible[i])){
                return coupPossible[i];
            } 
            if (empecheFin(coupPossible[i])){
                tour = coupPossible[i];
                aCoupPossible=true;
            }               
        }
        if (aCoupPossible){
            return tour;
        } else {
            int alea = (int)(Math.random()*coupPossible.length);
            return coupPossible[alea];
        }
    }

    public boolean estCoordValide(int ligne,int colonne) {
        /** 
         * Entrée : int, int
         * Sortie : boolean
         * Renvoie true si la colonne et la ligne passé en paramètre est un emplacement disponible pour un pion
         **/
            return (ligne>=0 && ligne<= getLigne()-1 && colonne>=0 && colonne<=getColonne()-1);
    }

    public int compteValeur(int[][] plateau, int ligne, int colonne, int joueur, int inc_lig, int inc_col) {
        /** 
         * Entrée : int[][], int, int, int, int, int 
         * Sortie : int
         * Renvoie la valeur maximum de pion aligné en partant d'une ligne et d'une colonne donnée et en se déplacant de inc_lig ligne et de inc_col colonne
         **/
            int inc = 0;
            int maxi = 0;
            while (estCoordValide(ligne,colonne)) {
                    if (plateau[ligne][colonne]==joueur) {
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

    public boolean estPartieTermine(int[][] plateau) {
        /** 
         * Entrée : int[][]
         * Sortie : boolean
         * Renvoie true si l'un des joueurs a gagné
         **/
            for (int l = 0;l<getLigne();l++) {
                    if (compteValeur(plateau,l,0,0,0,1)>=this.nbPion2Win
                     || compteValeur(plateau,l,0,1,0,1)>=this.nbPion2Win
                     || compteValeur(plateau,l,0,0,1,1)>=this.nbPion2Win	
                     || compteValeur(plateau,l,0,1,1,1)>=this.nbPion2Win
                     || compteValeur(plateau,l,getColonne()-1,0,1,-1)>=this.nbPion2Win
                     || compteValeur(plateau,l,getColonne()-1,1,1,-1)>=this.nbPion2Win){
                            return true;
                    }
            }
            for (int c=0;c<getColonne();c++) {
                    if (compteValeur(plateau,0,c,0,1,0)>=this.nbPion2Win
                     || compteValeur(plateau,0,c,1,1,0)>=this.nbPion2Win
                     || compteValeur(plateau,0,c,0,1,1)>=this.nbPion2Win
                     || compteValeur(plateau,0,c,0,1,-1)>=this.nbPion2Win
                     || compteValeur(plateau,0,c,1,1,1)>=this.nbPion2Win
                     || compteValeur(plateau,0,c,1,1,-1)>=this.nbPion2Win){
                            return true;
                    }
            }
            return false;
    }

    public int getLigne() {
        /** 
         * Entrée : Rien
         * Sortie : int
         * Renvoie le nombre de ligne du plateau
         **/
            return ligne;
    }

    public void setLigne(int ligne) {
        /** 
         * Entrée : int
         * Sortie : void
         * Set le nombre de ligne du plateau
         **/
            this.ligne = ligne;
    }

    public int getColonne() {
        /** 
         * Entrée : Rien
         * Sortie : int
         * Renvoie le nombre de colonne du plateau
         **/
            return colonne;
    }
        
    public int getValeur(int x, int y){
        /** 
        * Entrée : int, int
        * Sortie : int
        * Renvoie la valeur à la ligne x et à la colonne y du plateau
        **/
        return plateau[x][y];
    }

    public void setColonne(int colonne) {
        /** 
        * Entrée : int
        * Sortie : Rien
        * Set le nombre de colonne du plateau
        **/
        this.colonne = colonne;
    }

    public int getJoueur() {
        /** 
         * Entrée : Rien
         * Sortie : int
         * Renvoie le joueur en cours (0 ou 1)
         **/
        return joueur;
    }

    public void setJoueur(int joueur) {
        /** 
         * Entrée : int
         * Sortie : Rien
         * Set le joueur en cours (0 ou 1)
         **/
        this.joueur = joueur;
    }

    public int[][] getPlateau() {
        /** 
         * Entrée : Rien
         * Sortie : int[][]
         * Renvoie le plateau
         **/
        return plateau;
    }

    public void setPlateau(int[][] plateau) {
        /** 
         * Entrée : int[][]
         * Sortie : Rien
         * Set le plateau
         **/
        this.plateau = plateau;
    }

    public int[] getDernierCoup() {
        /** 
         * Entrée : Rien
         * Sortie : int[]
         * Renvoie le dernier coup sous la forme [x,y]
         **/
        return dernierCoup;
    }

    public void setDernierCoup(int[] dernierCoup) {
        /** 
         * Entrée : int[]
         * Sortie : Rien
         * Set le dernier coup
         **/
        this.dernierCoup = dernierCoup;
    }

    public int getPion2Win() {
        /** 
	     * Entrée : Rien
	     * Sortie : int
	     * Renvoie l'attribut Pion2Win du modele
	     **/
        return this.nbPion2Win;
    }
        
    public boolean getSuicide() {
        /** 
	     * Entrée : Rien
	     * Sortie : boolean
	     * Renvoie l'attribut suicideMode du modele
	     **/
        return this.suicideMode;
    }

    public boolean estPlein() {
        /** 
	     * Entrée : Rien
	     * Sortie : boolean
	     * Renvoie true si le puissance 4 est plein
	     **/
        for (int i=0;i<getLigne();i++){
            for (int j=0;j<getColonne();j++){
                if (this.plateau[i][j]==-1){
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean estVSIA(){
        /** 
	     * Entrée : Rien
	     * Sortie : boolean
	     * Renvoie l'attribut vsIA du modele
	     **/
        return this.vsIA;
    }
    
    private boolean peutTerminer(int coupPossible) {
        /** 
	     * Entrée : int
	     * Sortie : boolean
	     * Renvoie true si l'ordinateur peut gagner la partie
	     **/
        int[][] clone = creeClone(this.plateau);
        int x = -1;
        for (int i=getLigne()-1;i>=0;i--){
            if (clone[i][coupPossible]==-1){
                x = i;
                break;
            }
        }
        clone[x][coupPossible] = 1;
        if (estPartieTermine(clone)){
            return true;
        }
        return false;
    }

    private boolean empecheFin(int coupPossible) {
        /** 
	     * Entrée : int
	     * Sortie : boolean
	     * Renvoie true si le joueur peut gagner la partie
	     **/
        int[][] clone = creeClone(this.plateau);
        int x = -1;
        for (int i=getLigne()-1;i>=0;i--){
            if (clone[i][coupPossible]==-1){
                x = i;
                break;
            }
        }
        clone[x][coupPossible] = 0;
        if (estPartieTermine(clone)){
            return true;
        }
        return false;
    }
    
    private int[][] creeClone(int[][] plateau){
        /** 
	     * Entrée : int[][]
	     * Sortie : int[][]
	     * Renvoie un clone du plateau passé en paramètre
	     **/
        int[][] copy = new int[plateau.length][plateau[0].length];
        for (int i=0;i<plateau.length;i++){
            for (int j=0;j<plateau[0].length;j++){
                copy[i][j] = plateau[i][j];
            }
        }
        return copy;
    }

    private int choisitMeilleurCoupSuicide(int[] c) {
        /** 
	     * Entrée : int[]
	     * Sortie : int
	     * Renvoie le meilleur coup possible de l'IA en SuicideMode
	     **/
        int choix[] = new int[c.length];
        int iChoix = 0;
        for (int i=0;i<c.length;i++){
            if (!peutTerminer(c[i])){
                choix[iChoix++] = c[i];
            }
        }
	int alea = (int)(Math.random()*c.length);
        return choix[alea];
    }
}