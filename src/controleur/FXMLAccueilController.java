/*
 * Package : Controleur
 * Classe : FXMLAccueilController
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de définir le FXML Controleur associé à VueAccueil
 */

package controleur;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import vue.Vue;
import vue.VueLobby;

public class FXMLAccueilController implements Initializable {

    @FXML
    private Button btnMulti;
    @FXML
    private Button btnVsIA;

    // Fonction d'initialisation du controleur
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    

    // Fonction associé au bouton "Mode 1 joueur"
    // Elle permet de lancer la partie contre une IA
    @FXML
    private void lanceVsIa(ActionEvent event) throws Exception {
        Stage prece = (Stage) btnVsIA.getScene().getWindow();
        Vue vue = new Vue();
        vue.setPrece((int)prece.getX(),(int)prece.getY());
        prece.close();
        
        Stage mainFen = new Stage();
        vue.setPrece((int)prece.getX(),(int)prece.getY());
        mainFen.setTitle("Puissance 4");
        vue.start(mainFen);
        vue.setVsIA(true);
    }
    
    // Fonction associé au bouton "Mode multijoueur"
    // Elle permet de lancer le lobby des parties multijoueurs
    @FXML
    private void lanceVsJoueur(ActionEvent event) throws Exception {
        Stage prece = (Stage) btnMulti.getScene().getWindow();
        VueLobby vue = new VueLobby();
        vue.setPrece((int)prece.getX(),(int)prece.getY());
        prece.close();
        Stage mainFen = new Stage();
        mainFen.setTitle("Puissance 4");
        vue.start(mainFen);
    }

    // Fonction associé au bouton "Quitter"
    // Elle permet de quitter l'application
    @FXML
    private void quitte(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }
}
