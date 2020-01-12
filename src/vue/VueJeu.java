/*
 * Package : Vue
 * Classe : VueJeu
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de créer le visuel de la fenêtre de jeu du puissance 4
 */

package vue;

import controleur.JeuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import modele.Modele;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;


public class VueJeu extends Application {
    
    // Les couleurs pour le décor
    private static final Color BLEU = new Color(0,0.164,0.87,1.0);
    private static final Color BLEUCLAIR = new Color(0.26,0.38,0.90,1.0);
    private static final Color BLANC = new Color(1,1,1,1.0);
    private static final Color GRIS = new Color(0.69,0.76,0.835,1);
    private static final Color CONTOUR = new Color(0.15,1,0,1);
    
    // La couleur des 2 joueurs (propre à chaque client)
    private Color j1;
    private Color j2;
    
    // La taille des cases
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    
    // Le modéle à dessiner dans cette vue
    private Modele jeu;
    
    // Le canvas dans lequel on va dessiner
    private GraphicsContext paint;
    
    // Le numéro de tour du client (sous forme de chaine de caractère
    private String monTour;
    
    // Le socket
    private Socket socket;
    
    // Paramètre servant à initialiser les coordonnées de la fenêtre
    // Ils vont permettre de faire que la page ne se reset pas au milieu à chaque changement de page
    private int preceX = 0;
    private int preceY = 0;
    
    // Le stage
    private Stage stage;
    
    // Cette fonction permet d'affecter une valeur à la variable monTour 
    // cela nous permettra de faire les actions liées au tour lors du jeu 
    public void setMonTour(String tour) {
        this.monTour = tour;
    }
    

    // Fonction uniquement visuel
    public void dessinePionJ1(int x, int y) {
        /**
         * Entrée : int, int
         * Sortie : Rien
         * Dessine le pion selon les couleurs du joueurs 1
         **/
        paint.setFill(BLEU);
        paint.fillRect(x, y, WIDTH, HEIGHT);
        if (x == jeu.getDernierCoup()[1]*WIDTH && y == jeu.getDernierCoup()[0]*HEIGHT) {
            dessineLastPion(x, y);
        }
        paint.setFill(BLEUCLAIR);
        paint.fillOval(x+3, y+3, WIDTH-6, HEIGHT-6);
        paint.setFill(j1);
        paint.fillOval(x+5, y+5, WIDTH-10, HEIGHT-10);
        paint.setFill(GRIS);
        paint.fillOval(x+7, y+7, WIDTH-14, HEIGHT-14);
        paint.setFill(j1);
        paint.fillOval(x+9, y+9, WIDTH-18, HEIGHT-18);
    }

    // Fonction uniquement visuel
    public void dessinePionJ2(int x,int y) {
        /**
         * Entrée : int, int
         * Sortie : Rien
         * Dessine le pion selon les couleurs du joueurs 2
         **/
        paint.setFill(BLEU);
        paint.fillRect(x, y, WIDTH, HEIGHT);
        if (x== jeu.getDernierCoup()[1]*WIDTH && y == jeu.getDernierCoup()[0]*HEIGHT) {
            dessineLastPion(x, y);
        }
        paint.setFill(BLEUCLAIR);
        paint.fillOval(x+3, y+3, WIDTH-6, HEIGHT-6);
        paint.setFill(j2);
        paint.fillOval(x+5, y+5, WIDTH-10, HEIGHT-10);
        paint.setFill(GRIS);
        paint.fillOval(x+7, y+7, WIDTH-14, HEIGHT-14);
        paint.setFill(j2);
        paint.fillOval(x+9, y+9, WIDTH-18, HEIGHT-18);

    }

    // Fonction uniquement visuel
    public void dessineLastPion(int x, int y){
        /**
         * Entrée : int, int
         * Sortie : Rien
         * Dessine un contour autour du dernier pion joué pour plus de lisibilité
         **/
        paint.setFill(CONTOUR);
        paint.fillOval(x+1, y+1, WIDTH-2, HEIGHT-2);
    }

    // Fonction uniquement visuel
    public void dessinePionVide(int x,int y) {
        /**
         * Entrée : int, int
         * Sortie : RienQuitter
         * Dessine le pion vide
         **/
        paint.setFill(BLEU);
        paint.fillRect(x, y, WIDTH, HEIGHT);
        paint.setFill(BLEUCLAIR);
        paint.fillOval(x+3, y+3, WIDTH-6, HEIGHT-6);
        paint.setFill(BLANC);
        paint.fillOval(x+5, y+5, WIDTH-10, HEIGHT-10);
    }
    
    // Fonction pour lancer la vue et transferer les données au controleur
    @Override
    public void start(Stage stage) throws Exception {
        /**
         * Entrée : Stage
         * Sortie : Rien
         * Initialise la vue
         **/
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLJeu.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.stage = stage;
        stage.setX(preceX);
        stage.setY(preceY);
        stage.setScene(scene);
        JeuController c = fxmlLoader.getController();
        c.setModele(jeu);
        if(!jeu.estVSIA()) {
            c.setSocket(socket);
        }
        c.setMonTour(monTour);
        c.setInfos(jeu);
        c.setVue(this);
        paint = c.getGraphicsContext();
        dessine(paint);
        
        if(!jeu.estVSIA()) {
            // Partie concernant le jeu en multi (le tour n'est pas défini de la même facon qu'en solo
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream out = new PrintStream(socket.getOutputStream());
            stage.show();
            c.lanceJeuMulti();
        } else{
            // Partie concernant le jeu en solo 
            c.setLabelTour("Tour du joueur 1");
            stage.show();
        }
        
    }
    
    // Cette fonction est utilisé avec les deux attributs preceX et preceY
    // cela nous permet de faire que la page ne soit pas reset en coordonnée à chaque ouverture
    public void setPrece(int x, int y){
        this.preceX = x;
        this.preceY = y;
    }
    
    // Cette fonction permet d'affecter une valeur à la variable j1 
    // cela nous permettra de récupérer la couleur du joueur 1 provenant de la création de partie (Vue)
    public void setC1(Color j1) {
        /**
         * Entrée : Color
         * Sortie : Rien
         * Set la couleur du joueur 1
         **/
        this.j1 = j1;
    }

    // Cette fonction permet d'affecter une valeur à la variable j2 
    // cela nous permettra de récupérer la couleur du joueur 2 provenant de la création de partie (Vue)
    public void setC2(Color j2) {
        /**
         * Entrée : Color
         * Sortie : Rien
         * Set la couleur du joueur 1
         **/
        this.j2 = j2;
    }
    
    // Cette fonction permet d'affecter une valeur à la variable jeu
    // cela nous permettra de récupérer le modéle provenant de la création de partie (Vue)
    public void setModele(Modele m){
        /**
         * Entrée : Modele
         * Sortie : Rien
         * Set le modèle
         **/
        jeu = m;
        
    }
    
    // Cette fonction permet de recuperer la valeur de la variable jeu
    // cela nous permettra de récupérer le modéle 
    public Modele getModele(){
        /**
         * Entrée : Rien
         * Sortie : Modele
         * Get le modèle
         **/
        return jeu;
    }
    
    // Cette fonction permet d'affecter une valeur à la variable paint
    // cela nous permettra de récupérer le canvas pour dessiner
    public void setGraphicsContext(GraphicsContext g){
        /**
         * Entrée : GraphicsContext
         * Sortie : Rien
         * Set le contexte graphique du Canvas
         **/
        paint = g;
    }
    
    // Fonction purement visuelle
    public void dessine(GraphicsContext paint) {
        /**
         * Entrée : GraphicsContext
         * Sortie : Rien
         * Dessine entierement le puissance 4 selon le modele
         **/
        int l = jeu.getLigne();
        int c = jeu.getColonne();
        
        paint.getCanvas().setHeight(l*50);
        paint.getCanvas().setWidth(c*50);
        for (int i=0;i<l;i++){
            for (int j=0;j<c;j++){
                switch (jeu.getValeur(i,j)) {
                    case 0:
                        dessinePionJ1(j*WIDTH,i*HEIGHT);
                        break;
                    case 1:
                        dessinePionJ2(j*WIDTH,i*HEIGHT);
                        break;
                    default:
                        dessinePionVide(j*WIDTH,i*HEIGHT);
                        break;
                }
            }
        }

    }

    // Fonction purement visuelle
    public void dessineGagnant(int joueur,int moi) {
        /**
         * Entrée : int , int
         * Sortie : Rien
         * Affiche le gagnant en fonction de qui a gagné et de qui je suis
         **/
        Stage prece = this.stage;
        Popup popup = new Popup();
        Label popupLabel;
        if (joueur==moi){
            popupLabel = new Label("Victoire");
            popupLabel.setStyle(" -fx-background-color:  #89aae6;-fx-border-color: #061826;-fx-font-weight:bold;-fx-font-size:40;");
            popupLabel.setTextFill(Color.WHITE);
        } else {
            popupLabel = new Label("Défaite");
            popupLabel.setStyle(" -fx-background-color:  #f44708;-fx-border-color: #a10702;-fx-font-weight:bold;-fx-font-size:40;");
            popupLabel.setTextFill(Color.WHITE);
        }
        
        popup.getContent().add(popupLabel);
        popup.setAutoHide(true);
        popup.show(prece,prece.getX()+100,prece.getY()+200);
    }

    // Fonction purement visuelle
    public void dessineFinEgalite() {
        /**
         * Entrée : Rien
         * Sortie : Rien
         * Affiche l'égalité
         **/
        Stage prece = this.stage;
        Popup popup = new Popup();
        Label popupLabel;
        popupLabel = new Label("Egalité !");
        popupLabel.setStyle(" -fx-background-color:  #9acd32;-fx-border-color: #096a09;-fx-font-weight:bold;-fx-font-size:40;");
        popupLabel.setTextFill(Color.WHITE);
        popup.getContent().add(popupLabel);
        popup.setAutoHide(true);
        popup.show(prece,prece.getX()+100,prece.getY()+200);

    }

    // Fonction purement visuelle
    public void dessineAbandon() {
        /**
         * Entrée : Rien
         * Sortie : Rien
         * Affiche l'abandon (dans le cas d'une partie en ligne où l'un des deux quitte
         **/
        Stage prece = this.stage;
        Popup popup = new Popup();
        Label popupLabel;
        popupLabel = new Label("Victoire par abandon !");
        popupLabel.setStyle(" -fx-background-color:  #9acd32;-fx-border-color: #096a09;-fx-font-weight:bold;-fx-font-size:40;");
        popupLabel.setTextFill(Color.WHITE);
        popup.getContent().add(popupLabel);
        popup.setAutoHide(true);
        popup.show(prece,prece.getX()+100,prece.getY()+200);

    }

    // Cette fonction permet d'affecter une valeur à la variable socket 
    // cela nous permettra de faire transiter le socket partout durant le jeu
    public void setSocket(Socket socket) {
        this.socket=socket;
    }

}