/*
 * Package : Controleur
 * Classe : FXMLAccueilController
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de définir le FXML Controleur associé à VueAccueil
 */

package controleur;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import modele.Modele;
import vue.VueJeu;
import javafx.util.Duration;
import network.NetworkUtil;
import vue.VueLobby;

public class FXMLAttenteController implements Initializable {

    @FXML
    private Label lblTaille;
    @FXML
    private Label lblSuicide;
    @FXML
    private Label lblPion;
    @FXML
    private Button btnQuitter;
    @FXML
    private Label lblPseudo;
    @FXML
    private Label lblAnimation;
    
    // Le socket
    private Socket socket;
    
    // L'etat des 3 petits points pour l'animation
    private int etat = 0;
    
    // La timeline qui gére l'animation
    private Timeline timeline;
    
    // Compteur indiquant au client que le serveur a été trop long à répondre et donc qu'il a crash
    private int cptCrash = 0;
    
    //Infos de la partie
    private int ligne;
    private int colonne;
    private boolean suicide;
    private int pion2Win;
    
    // Fonction d'initialisation du controleur
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        boucleAttente();
    }    

    // Fonction lancé en boucle toute les secondes qui attend un message du serveur pour lancer la partie (cad une connexion par un autre client à la partie
    public void attenteMsg(VueJeu vue){
        try {
            
            if ( cptCrash == 0 ) {
                // On envoie un message au serveur pour savoir si il est encore la
                PrintStream out = new PrintStream(socket.getOutputStream());
                out.print("TTL");
            }
            // On regarde si le socket possède un message 
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if(in.ready()) {
                
                if (in.readLine().equals("Join")){
                    // C'est le bon message, on arrête la boucle, on crée la partie et on la lance
                    timeline.stop();
                    Modele m = new Modele(ligne, colonne, suicide, false, pion2Win);
                    Stage mainFen = new Stage();
                    mainFen.setTitle("Puissance 4");
                    Stage prece = (Stage) btnQuitter.getScene().getWindow();
                    vue.setPrece((int)prece.getX(),(int)prece.getY());
                    prece.close();
                    vue.setSocket(socket);
                    vue.setMonTour("0");
                    try {

                        vue.start(mainFen);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // On passe à -3 car cela permettra d'attendre 3 secondes entre chaque check de crash
                    cptCrash = -3;
                }
            } else {
                if ( cptCrash == 3 ){
                    // Si le compteur atteint 3 sec sans réponse alors le serv a crash
                    Stage prece = (Stage) btnQuitter.getScene().getWindow();
                    NetworkUtil.afficheErreurReseau();
                    prece.close();
                }
                // Sinon on incrémente le temps sans réponse
                cptCrash += 1;
            }

        } catch (Exception e) {      
            // Si problème, on revient au menu
            Stage prece = (Stage) btnQuitter.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            prece.close();
        }

    }

    // Fonction qui anime les 3 petits points de la page d'attente
    public void boucleAttente(){
        final KeyFrame kf0 = new KeyFrame(Duration.ZERO, e->animate());
        final KeyFrame kf1 = new KeyFrame(Duration.seconds(1),e->animate());
        final KeyFrame kf2 = new KeyFrame(Duration.seconds(2),e->animate());
        final Timeline timelinePoints = new Timeline(kf0,kf1,kf2);
        timelinePoints.setCycleCount(Timeline.INDEFINITE);
        timelinePoints.setAutoReverse(true);
        timelinePoints.play();
    }

    // Fonction pour quitter l'écran d'attente, il détruit alors la partie en cours
    @FXML
    private void revenirLobby(ActionEvent event) throws Exception {
        try {
            // On envoie au serveur le message de destruction de partie (il va retrouver avec l'id de l'envoyeur la partie
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.print("DP");
            
            // On revient au Lobby
            VueLobby vue = new VueLobby();
            Stage mainFen = new Stage();
            mainFen.setTitle("Puissance 4");
            Stage prece = (Stage) btnQuitter.getScene().getWindow();
            vue.setPrece((int)prece.getX(),(int)prece.getY());
            prece.close();         
            vue.start(mainFen);
            
        } catch (Exception e) {           
            // Si problème, revenir au menu
            Stage prece = (Stage) btnQuitter.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            prece.close();
        }
    }

    // Fonction permettant de définir les infos à afficher à l'écran pendant l'attente
    public void setInfos(String hostname, int colonne, int ligne, boolean suicide, int pion2Win) {
        lblPseudo.setText(hostname);
        this.ligne=ligne;
        this.colonne=colonne;
        this.suicide = suicide;
        this.pion2Win=pion2Win;
        lblTaille.setText(""+ligne+" lignes x "+colonne+" colonnes");
        lblSuicide.setText((suicide?"Mode suicide":"Mode normal"));
        lblPion.setText(""+pion2Win);
    }

    // Cette fonction permet d'affecter une valeur à la variable socket 
    // cela nous permettra de faire transiter le socket partout durant le jeu
    public void setSocket(Socket s){
        this.socket = s;
    }

    // Fonction qui anime les 3 petits points, elle fait transitionner le label entre 3 etats
    private void animate() {
        String possibilite[] = {"En attente d'un autre joueur","En attente d'un autre joueur .","En attente d'un autre joueur ..", "En attente d'un autre joueur ..."};
        etat+=1;
        etat%=4;
        lblAnimation.setText(possibilite[etat]);
    }

    // Cette fonction permet d'affecter une valeur à la variable timeline 
    // cela permet de faire remonter l'animation d'attente de message
    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }
}
