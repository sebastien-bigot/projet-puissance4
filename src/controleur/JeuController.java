/*
 * Package : Controleur
 * Classe : FXMLJeuController
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de définir le FXML Controleur associé à VueJeu
 */

package controleur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import modele.Modele;
import network.NetworkUtil;
import vue.VueAccueil;
import vue.VueJeu;

public class JeuController implements Initializable {
    /**
     * Controleur de la fenêtre principale du jeu
     **/

    @FXML
    private Canvas mainCanvas;
    @FXML
    private Label lblTour;
    @FXML
    private Pane panFond;
    @FXML
    private Button btnAccueil;
    @FXML
    private Label lblMode;
    @FXML
    private Label lblTaille;
    @FXML
    private Label lblPion;
    @FXML
    private Label lblTimer;
    
    // Le tour pendant lequel le client peut jouer
    private String mon_tour;
    
    // Un booleen permettant de jouer au client
    private boolean peut_jouer;
    
    // L'affichage du tour
    private int etat_tour;
    
    // Le socket
    private Socket socket;
    
    // Le modele
    private Modele jeu;
    
    // La vue
    private VueJeu vueJeu;
    
    // Le canvas
    private GraphicsContext paint;
    
    // La boucle permettant d'attendre le prochain coup
    private Timeline timeline;
    
    // La boucle timant que le coup soit ajouté à temps
    private Timeline timelineCoup;
    
    // Le temps limit pour jouer
    private final int TIME_LIMIT = 10;
    
    // Un booleen indiquant qu'on a dépassé le temps limite
    private boolean fini = false;
    
    // Un booleen permettant de s'assurer qu'on ne passe qu'une fois dans l'écran de défaite par manque de temps
    private boolean passe = false;
    
    // Un int permettant de savoir si le serveur a crash
    private int tpsCrash = 0;
    
    // Fonction d'initialisation du controleur
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /** 
         * Entrée : URL, ResourceBundle
         * Sortie : Rien
         * Initialise le Contrôleur de la fenêtre principale
         **/
        paint = mainCanvas.getGraphicsContext2D();
        panFond.setPrefHeight((mainCanvas.getHeight())+100);
        panFond.setPrefWidth((mainCanvas.getWidth())+100);
    }    
    
    // Boucle d'animation du chrono
    public void reduireTps(int i){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream out = new PrintStream(socket.getOutputStream());
            if ( i >= 10 ){
                lblTimer.setText("00:"+i);
            } else {
                lblTimer.setText("00:0"+i);
            }
            if ( i==0 ){
                // Si on arrive à 0, on l'envoie au serveur pour indiquer sa défaite
                fini=true;
                out.print("AB");
            }
        } catch (IOException e) {
            // Si probleme, on revient au menu
            Stage prece = (Stage) btnAccueil.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            prece.close();
        }
    }

    // Fonction actualisant l'écran
    public void update(VueJeu v){
        /**
         * Entrée : VueJeu
         * Sortie : Rien
         * Update la vue v passé en paramètre
         **/
        v.dessine(paint);
    }
    
    // Fonction associé au clic permettant de récupérer les coups joués
    @FXML
    private void placePion(MouseEvent event) throws IOException {
        /**
         * Entrée : MouseEvent 
         * Sortie : Rien
         * Action assigné au Canvas
         * Permet de placer un pion dans la colonne correspondant au clic
         **/
        if ( jeu.estVSIA() ){
            
            // Partie Solo
            jeu.posePion((int)event.getX()/50,0);
            vueJeu.dessine(paint);
            if (jeu.estPartieTermine(jeu.getPlateau())){
                if (jeu.getSuicide()){
                    vueJeu.dessineGagnant(jeu.getJoueur(),0);
                    paint.getCanvas().setDisable(true);
                    lblTour.setText("");
                } else {
                    vueJeu.dessineGagnant((jeu.getJoueur()+1)%2,0);
                    paint.getCanvas().setDisable(true);
                    lblTour.setText("");
                }
                return;
            }
            lblTour.setText("Tour du Joueur 2");
            jeu.posePion(jeu.calculeMeilleurCoup(),1);
            lblTour.setText("Tour du Joueur 1");
            
        } else {
            // Partie Multijoueur
            try {
                
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream out = new PrintStream(socket.getOutputStream());
                
                // Si le clic est valide et que le joueur était en position de jouer
                if ((peut_jouer) && jeu.posePion((int)event.getX()/50,Integer.parseInt(mon_tour))){
                    
                    // Alors on checke dans un premier temps qu'il n'est pas cliqué trop tard
                    if(fini && !passe){
                        passe=true;
                        // Si oui, il a reçu un message annonçant sa défaite
                        String msg = in.readLine();
                        
                        // Ce message peut être un message d'abandon si l'autre joueur a quitté avant la fin du timer
                        if ( msg.equals("Abandon") ) {
                            vueJeu.dessineAbandon();
                            peut_jouer = false;
                            return;
                        }
                        
                        // Si c'est pas un abandon, on affiche le message de victoire/défaite
                        vueJeu.dessineGagnant(Character.getNumericValue(msg.charAt(1)),Integer.parseInt(mon_tour));
                        
                    } else if ( !fini ) {
                        // Si il est encore dans les temps, on arrete le timer
                        timelineCoup.stop();
                        
                        // on envoie son coup
                        out.print("PP" + jeu.getDernierCoup()[1]);
                        
                        // PEUT_JOUER = FALSE (pas necessaire mais par sécurité pour les mecs qui spam click)
                        peut_jouer = false;
                        
                        // On attend le message indiquant l'etat de la partie (prochain tour, abandon, victoire etc...)
                        String msg = in.readLine();
                        if (msg.equals("Abandon")) {
                            vueJeu.dessineAbandon();
                            peut_jouer = false;
                            return;
                        } else if (msg.charAt(0) == 'W') {
                            vueJeu.dessineGagnant(Character.getNumericValue(msg.charAt(1)), Integer.parseInt(mon_tour));
                            peut_jouer = false;
                            // Si c'est un message de victoire, on arrete le timer
                            timeline.stop();
                            return;
                        }
                        vueJeu.dessine(paint);
                        
                        // On repart dans la fonction de gestion du multijoueur
                        lanceJeuMulti();
                    }
                }
            } catch (Exception e) {      
                // Si probleme, on revient au menu
                Stage prece = (Stage) btnAccueil.getScene().getWindow();
                NetworkUtil.afficheErreurReseau();
                prece.close();
            }
        }
        if (jeu.estVSIA()){
            // Si partie solo voila la gestion de la partie
            vueJeu.dessine(paint);
            if (jeu.estPartieTermine(jeu.getPlateau())){
                if (jeu.getSuicide()){
                    vueJeu.dessineGagnant(jeu.getJoueur(),0);
                    paint.getCanvas().setDisable(true);
                    lblTour.setText("");
                } else {
                    vueJeu.dessineGagnant((jeu.getJoueur()+1)%2,0);
                    paint.getCanvas().setDisable(true);
                    lblTour.setText("");
                }
            }
            if (jeu.estPlein()){
                vueJeu.dessineFinEgalite();
                paint.getCanvas().setDisable(true);
                lblTour.setText("");
            }
        }
    }

    
    public GraphicsContext getGraphicsContext() {
         /**
         * Entrée : Rien 
         * Sortie : GraphicsContext
         * Renvoie le contexte Graphique du Canvas de la fenếtre de jeu
         **/
        return paint;
    }

    public void setModele(Modele jeu) {
         /**
         * Entrée : Modele
         * Sortie : Rien
         * Place le modèle dans un attribut
         **/
        this.jeu = jeu;
    }
    
    public void setVue(VueJeu vue){
         /**
         * Entrée : VueJeu 
         * Sortie : Rien
         * Place la vue dans un attribut
         **/
        vueJeu = vue;
    }

    @FXML
    private void restart(MouseEvent event) throws Exception {
         /**
         * Entrée : MouseEvent 
         * Sortie : Rien
         * Action assigné au bouton Recommencer
         * Quitte la page actuelle et relance l'écran d'accueil
         **/
         
        Stage s = (Stage)btnAccueil.getScene().getWindow();
        if(!jeu.estVSIA()) {
            // Si en multi, en fermant le socket le serveur va envoyer le message d'abandon à l'autre joueur
            socket.close();
            if ( timeline != null){
                // on stoppe la boucle qui attend les coups
                timeline.stop();
            }
            if ( timelineCoup != null){
                // on stoppe le timer limite pour un coup
                timelineCoup.stop();
            }
        }
        
        // On revient au menu
        VueAccueil v = new VueAccueil();
        v.setPrece((int) s.getX(),(int)s.getY());
        s.close();        
        Stage nouv = new Stage();
        v.start(nouv);     
    }

    // Fonction gérant le jeu Multi avec PlacePion
    public void lanceJeuMulti() throws IOException {
        try {
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream out = new PrintStream(socket.getOutputStream());
            
            // On envoie un message au serveur pour savoir où en est la partie
            out.print("OK");
            
            // On récupere sa réponse
            String getTour= in.readLine();
            
            // Si abandon, on arrete le jeu 
            if(getTour.equals("Abandon")){
                vueJeu.dessineAbandon();
                peut_jouer=false;
            }
            
            Stage prece = (Stage) this.lblTour.getScene().getWindow();
            Popup popup = new Popup();
            Label popupLabel;
            
            // Si message de victoire alors afficher le bon message
            if ( getTour.charAt(0) == 'W' ) { 
                vueJeu.dessineGagnant(Character.getNumericValue(getTour.charAt(1)),Integer.parseInt(mon_tour));
                
            // Si égalité, envoyer le bon message
            } else if ( getTour.charAt(0) == 'D' ){
                vueJeu.dessineFinEgalite();
            
            // Sinon on a reçu le tour de la personne qui joue
            } else if ( getTour.equals(mon_tour) ){
                // Si c'est le mien
                
                // On va lancer le timer limite le temps de reflexion
                List<KeyFrame> tabf = new ArrayList<>();
                for(int i = TIME_LIMIT;i>=0;i--) {
                    int finalI = i;
                    final KeyFrame kf = new KeyFrame(Duration.seconds(TIME_LIMIT - i), e -> reduireTps(finalI));
                    tabf.add(kf);
                }
                final Timeline timelineC = new Timeline();
                timelineC.getKeyFrames().addAll(tabf);
                timelineC.play();
                this.timelineCoup=timelineC;
                
                // On affiche le tour de la personne
                popupLabel = new Label("Votre tour ");
                popupLabel.setStyle(" -fx-background-color: rgba(137, 170, 230, 0.5);-fx-border-color: #061826;-fx-font-weight:bold;-fx-font-size:40;");
                popupLabel.setTextFill(Color.WHITE);
                popup.getContent().add(popupLabel);
                popup.setAutoHide(true);
                popup.show(prece,prece.getX()+100,prece.getY()+200);
                
                // Et on définit la variable peut_jouer à true
                peut_jouer=true;
                
            } else {
                // Si c'est pas mon tour
                
                // On affiche que c'est le tour de l'adversaire
                popupLabel = new Label("Tour adverse");
                popupLabel.setStyle(" -fx-background-color:  rgba(244, 71, 8, 0.5);-fx-border-color: #a10702;-fx-font-weight:bold;-fx-font-size:40;");
                popupLabel.setTextFill(Color.WHITE);
                popup.getContent().add(popupLabel);
                popup.setAutoHide(true);
                popup.show(prece,prece.getX()+100,prece.getY()+200);
                
                // On met peut_jouer à false car c'est pas notre tour
                peut_jouer=false;
                
                // On va lancer une boucle qui toute les secondes attend le coup de l'adversaire
                final KeyFrame kf0 = new KeyFrame(Duration.seconds(1), e->attenteCoup());
                final Timeline timeline = new Timeline(kf0);
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
                this.timeline=timeline;

            }
        } catch (Exception e) { 
            // Si probleme, on revient au menu
            Stage prece = (Stage) btnAccueil.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            prece.close();
        }
    }

    // Fonction qui va être appeler en boucle lors de l'attente du coup adverse
    public void attenteCoup(){
        // On incremente le timer avant potentiel crash à chaque appel
        tpsCrash+=1;
        
        // Si le temps de crash est supérieur au temps limite fixé et qu'on avait rien reçu c'est que le serveur a crashé
        if ( tpsCrash > TIME_LIMIT + 2 ) {
            // Il y a eu probleme on revient au menu
            Stage prece = (Stage) btnAccueil.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            this.timeline.stop();
            prece.close();
        }
        
        try {
            // Sinon on regarde si le serveur a un message pour nous
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if ( in.ready() ) {
                // on reset le timer avant crash
                tpsCrash=0;
                
                // on lit le message
                String msg = in.readLine();
                
                // On gere les différents cas (victoire, abandon, un coup joué)
                if ( msg.equals("Abandon") ) {
                    vueJeu.dessineAbandon();
                    peut_jouer=false;
                    timeline.stop();
                    return;
                } else if ( msg.charAt(0) == 'W' ) {
                    vueJeu.dessineGagnant(Character.getNumericValue(msg.charAt(1)),Integer.parseInt(mon_tour));
                    peut_jouer=false;
                    timeline.stop();
                    return;
                }
                jeu.posePion(Integer.parseInt(msg), ((mon_tour.equals("1")) ? 0 : 1));
                this.timeline.stop();
                vueJeu.dessine(paint);
                
                // On repart dans la boucle du jeu
                lanceJeuMulti();
            }
        } catch (Exception e) {
            // Si probleme, on arrete le timer et on revient au menu
            Stage prece = (Stage) btnAccueil.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            this.timeline.stop();
            prece.close();
        }
    }

    // Cette fonction permet d'affecter une valeur à la variable monTour 
    // elle nous sert pour identifier son tour ou le tour adverse
    public void setMonTour(String i) {
        mon_tour = i;
    }

    // Cette fonction permet d'affecter une valeur à la variable socket 
    // cela nous permettra de faire transiter le socket partout durant le jeu
    public void setSocket(Socket socket){
        this.socket=socket;
    }
    
    // Cette fonction permet d'afficher les infos de la partie à l'écran
    // cette fonction sert car une personne qui a rejoint une partie random ne les connait pas
    public void setInfos(Modele jeu) {
        lblMode.setText("Mode de jeu : "+(jeu.getSuicide()?"Mode suicide":"Mode normal"));
        lblTaille.setText("Taille : "+jeu.getLigne()+"x"+jeu.getColonne());
        lblPion.setText("Nombre de pion pour "+(jeu.getSuicide()?"perdre : ":"gagner : ")+jeu.getPion2Win());
    }

    // Cette fonction définit le tour de la personne qui joue en mode Solo
    public void setLabelTour(String s) {
        this.lblTour.setText(s);
    }

}

