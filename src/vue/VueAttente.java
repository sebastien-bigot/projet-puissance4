/*
 * Package : Vue
 * Classe : VueAttente
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de créer le visuel de la fenêtre d'attente de connexion à la partie
 */

package vue;

import controleur.FXMLAttenteController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import modele.Modele;
import java.net.Socket;

public class VueAttente extends Application {
    // Attribut stockant la fenêtre de jeu a utilisé lorsqu'une personne se sera connecté
    // (il permet aussi de récuperer les infos de la partie en cours pour les infos)
    private VueJeu vue;
    
    // Attribut stockant le pseudo de la personne qui host
    private String hostname;
    
    // Socket en cours 
    private Socket socket;
    
    // Paramètre servant à initialiser les coordonnées de la fenêtre
    // Ils vont permettre de faire que la page ne se reset pas au milieu à chaque changement de page
    private int preceX = 0;
    private int preceY = 0;
    
    // Fonction pour lancer la vue et transferer les données au controleur
    @Override
    public void start(Stage stage)throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLAttente.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setX(preceX);
        stage.setY(preceY);
        stage.setTitle("Puissance 4");
        stage.setScene(scene);
        FXMLAttenteController c = (FXMLAttenteController) fxmlLoader.getController();
        Modele m = vue.getModele();
        c.setSocket(socket);
        c.setInfos(hostname,m.getColonne(),m.getLigne(),m.getSuicide(),m.getPion2Win());
        stage.show();
        
        //Animation permettant d'attendre le message du serveur pour lancer la partie
        final KeyFrame kf0 = new KeyFrame(Duration.seconds(1), e->c.attenteMsg(vue));
        final Timeline timeline = new Timeline(kf0);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        c.setTimeline(timeline);
    }

    // Cette fonction permet d'affecter une valeur à la variable vueJeu 
    // cela nous permettra de faire remonter cette information au contrôleur
    public void setVueJeu(VueJeu vue){
        this.vue = vue;
    }

    // Cette fonction permet d'affecter une valeur à la variable hostname 
    // cela nous permettra d'afficher le nom de l'hote dans l'écran d'attente
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    // Cette fonction permet d'affecter une valeur à la variable socket 
    // cela nous permettra de faire transiter le socket partout durant le jeu
    public void setSocket(Socket s){
        this.socket = s;
    }
    
    // Cette fonction est utilisé avec les deux attributs preceX et preceY
    // cela nous permet de faire que la page ne soit pas reset en coordonnée à chaque ouverture
    public void setPrece(int x, int y){
        this.preceX = x;
        this.preceY = y;
    }
}
