/*
 * Package : Vue
 * Classe : VueLobby
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de créer le visuel du Lobby du jeu en multi-joueur
 */

package vue;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class VueLobby extends Application {
    
    // Paramètre servant à initialiser les coordonnées de la fenêtre
    // Ils vont permettre de faire que la page ne se reset pas au milieu à chaque changement de page
    private int preceX = 0;
    private int preceY = 0;
    
    // Fonction pour lancer la vue et transferer les données au controleur
    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("FXMLLobby.fxml"));
        Scene scene = new Scene(root);
        stage.setX(preceX);
        stage.setY(preceY);
        stage.setTitle("Puissance 4");
        stage.setScene(scene);
        stage.show();
    }
    
    // Cette fonction est utilisé avec les deux attributs preceX et preceY
    // cela nous permet de faire que la page ne soit pas reset en coordonnée à chaque ouverture
    public void setPrece(int x, int y){
        this.preceX = x;
        this.preceY = y;
    }

}
