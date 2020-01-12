/*
 * Package : Vue
 * Classe : Vue
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de créer le visuel de la fenêtre de création de partie 
 */

package vue;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controleur.Controleur;
import java.net.Socket;


public class Vue extends Application {
    
    // Pour contrôler un maximum les actions on a besoin de récuperer le controleur afin 
    // de lui transmettre des informations
    public Controleur c;
    
    // On récupére le socket afin de le transferer au controleur
    private Socket socket;
    
    // Paramètre servant à initialiser les coordonnées de la fenêtre
    // Ils vont permettre de faire que la page ne se reset pas au milieu à chaque changement de page
    private int preceX = 0;
    private int preceY = 0;
    
    // Methode permettant de définir le socket qui était ouvert lors de l'arrivée sur cette page
    // Cette fonction nous sera utile pour transférer dans toute les pages le socket
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
    
    // Fonction pour lancer la vue et transferer les données au controleur
    @Override
    public void start(Stage stage) throws Exception {
        /**
         * Entrée : Stage 
         * Sortie : Rien
         * Initialise la vue
         **/
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Puissance 4");
        stage.setScene(scene);
        stage.setX(preceX);
        stage.setY(preceY);
        c = (Controleur) fxmlLoader.getController();
        c.setVsIA(false);
        c.setSocket(socket);
        stage.show();
    }
    
    // Cette fonction permet de définir la variable vsIA du controleur
    // cela nous permet de faire remonter cette information au contrôleur
    public void setVsIA(boolean t){
        c.setVsIA(true);
    }
    
    // Cette fonction est utilisé avec les deux attributs preceX et preceY
    // cela nous permet de faire que la page ne soit pas reset en coordonnée à chaque ouverture
    public void setPrece(int x, int y){
        this.preceX = x;
        this.preceY = y;
    }
    
}