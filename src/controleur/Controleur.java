/*
 * Package : Controleur
 * Classe : Controleur
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de définir le FXML Controleur associé à Vue
 */

package controleur;

import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import modele.Modele;
import network.NetworkUtil;
import vue.VueAccueil;
import vue.VueAttente;
import vue.VueJeu;
        
public class Controleur implements Initializable {
    /**
    * Controleur de l'écran d'accueil
    * Il gère toutes les interactions avec l'écran d'accueil
    **/
    
    // Le nom de l'hote de la partie (en mode multijoueur)
    // Cette attribut sert au transfert à la page d'attente
    private String hostname;
    

    // Le modele de la partie entrain d'etre crée
    private Modele jeu;
    
    // Liste des options de la partie qui seront ajouté au Modele jeu
    private boolean estVSIA;
    private boolean suicideMode;
    private int nbLigne;
    private int nbColonne;
    private int nbPion2Win;
    
    // Couleur des deux joueurs qui sont défini par les ColorsPickers
    private Color j1;
    private Color j2;
    
    @FXML
    private TextField ligne;
    @FXML
    private TextField colonne;
    @FXML
    private TextField pion;
    @FXML
    private Slider ligneDrag;
    @FXML
    private Slider colonneDrag;
    @FXML
    private Slider pionDrag;
    @FXML
    private ColorPicker j1CP;
    @FXML
    private ColorPicker j2CP;
    @FXML
    private Button btnLancer;
    @FXML
    private CheckBox suicideCB;
    @FXML
    private Button btnMenu;
    
    // Le socket (pour la partie multijoueur)
    private Socket socket;
    
    // Cette fonction permet d'affecter une valeur à la variable socket 
    // cela nous permettra de faire transiter le socket partout durant le jeu
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    // Cette fonction permet de retourner si deux couleurs sont proches entre elle
    public boolean equalsColor(Color c1, Color c2){
        /**
         *  Entrée : Color , Color
         *  Sortie : boolean
         *  Renvoie true si les deux couleurs données sont très proche (+-0.1 pour chaque pigment)
         **/ 
        return Math.abs(c1.getRed()-c2.getRed())<0.1 && Math.abs(c1.getBlue()-c2.getBlue())<0.1 && Math.abs(c1.getGreen()-c2.getGreen())<0.1;
    }
    
    // Fonction d'initialisation du controleur
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /**
         * Entrée : URL, ResourceBundle
         * Sortie : Rien
         * Initialise le contrôleur et ses attributs
         **/
        this.estVSIA=false;
        this.suicideMode = false;
        this.nbLigne = 6;
        ligne.setText("6");
        this.nbColonne = 7;
        colonne.setText("7");
        this.nbPion2Win = 4;
        pion.setText("4");
        this.j1 = new Color(1,0,0,1.0);
        this.j2 = new Color(1,1,0,1.0);
    }    

    @FXML
    private void setNbColonneDrag(MouseEvent event) {
        /**
         * Entrée : MouseEvent 
         * Sortie : Rien
         * Action assigné au slide horizontal
         * Permet de choisir le nombre de colonne du Puissance 4
         **/
        colonne.setText(Integer.toString((int)colonneDrag.getValue()));
    }

    @FXML
    private void setNbPionDrag(MouseEvent event) {
        /**
         * Entrée : MouseEvent 
         * Sortie : Rien
         * Action assigné au slide horizontal
         * Permet de choisir le nombre de pion pour gagner du Puissance 4
         **/
        pion.setText(Integer.toString((int)pionDrag.getValue()));
    }


    @FXML
    private void setColorJ1(ActionEvent event) {
        /**
         * Entrée : ActionEvent 
         * Sortie : Rien
         * Action assigné au Color Picker
         * Permet de choisir la couleur du Joueur 1
         **/
        this.j1 = j1CP.getValue();
        if (equalsColor(this.j1,this.j2)||equalsColor(this.j1,new Color(1,1,1,1))){
            j1CP.setValue(new Color(Math.random(),Math.random(),Math.random(),1));
            this.j1 = j1CP.getValue();
        }
    }

    @FXML
    private void setColorJ2(ActionEvent event) {
        /**
         * Entrée : ActionEvent 
         * Sortie : Rien
         * Action assigné au Color Picker
         * Permet de choisir la couleur du Joueur 2
         **/
        this.j2 = j2CP.getValue();
        if (equalsColor(this.j1,this.j2)||equalsColor(this.j2,new Color(1,1,1,1))){
            j2CP.setValue(new Color(Math.random(),Math.random(),Math.random(),1));
            this.j2 = j2CP.getValue();
        }
    }

    @FXML
    private void play(ActionEvent event) throws Exception {
        /**
         * Entrée : ActionEvent 
         * Sortie : Rien
         * Action assigné au Bouton lancer
         * Permet de choisir lancer le jeu avec les options données par le reste de la page
         **/
        
        if (estVSIA){
            
            // La partie joué est en solo, on crée le modèle et on joue
            this.jeu = new Modele(Integer.parseInt(ligne.getText()),Integer.parseInt(colonne.getText()),suicideCB.isSelected(),this.estVSIA,Integer.parseInt(pion.getText()));
            VueJeu vue = new VueJeu();
            Stage mainFen = new Stage();
            mainFen.setTitle("Puissance 4");
            mainFen.setHeight(Integer.parseInt(ligne.getText())*50+120);
            mainFen.setWidth(Integer.parseInt(colonne.getText())*50+70);
            vue.setC1(j1);
            vue.setC2(j2);
            vue.setModele(jeu);
            Stage prece = (Stage) btnLancer.getScene().getWindow();
            vue.setPrece((int)prece.getX(),(int)prece.getY());
            prece.close();
            vue.start(mainFen);
            /////////////////////////////////////////////////////////////
            
        } else {
            
            // La partie joué est en multi-joueur
            this.jeu = new Modele(Integer.parseInt(ligne.getText()),Integer.parseInt(colonne.getText()),suicideCB.isSelected(),this.estVSIA,Integer.parseInt(pion.getText()));
            // On doit demander au serveur d'ajouter la partie dans son tableau de partie
            // Ici on posséde toutes les infos dont le serveur a besoin (infos de partie + hostname)
            try {
                
                // On envoie le message indiquant qu'on va envoyer une partie
                PrintStream out = new PrintStream(socket.getOutputStream());
                out.print("CP");
                
                // Puis on envoie la partie sous forme d'une chaine de caractère que le serveur peut comprendre
                String partieData = hostname + " " + (jeu.getSuicide()?1:0) + " " + jeu.getLigne() + " " + jeu.getColonne() + " " + jeu.getPion2Win()+" ";
                out.print(partieData);
                
                // Enfin on met la personne qui vient de créer la partie en attente (sur la VueAttente)
                VueAttente vue = new VueAttente();
                vue.setSocket(socket);
                Stage mainFen = new Stage();
                mainFen.setTitle("Puissance 4");
                VueJeu vueJ = new VueJeu();
                vueJ.setC1(j1);
                vueJ.setC2(j2);
                vueJ.setModele(jeu);
                vue.setVueJeu(vueJ);
                vue.setHostname(this.hostname);
                Stage prece = (Stage) btnLancer.getScene().getWindow();
                vue.setPrece((int)prece.getX(),(int)prece.getY());
                prece.close();
                vue.start(mainFen);
                
            } catch (Exception e) {
                // Si problème réseau, alors revenir au menu
                Stage prece = (Stage) btnLancer.getScene().getWindow();
                NetworkUtil.afficheErreurReseau();
                prece.close();
            }
        }
    }

    @FXML
    private void setNbLigneDrag(MouseEvent event) {
        /**
         * Entrée : MouseEvent 
         * Sortie : Rien
         * Action assigné au slide horizontal
         * Permet de choisir le nombre de ligne du Puissance 4
         **/
        ligne.setText(Integer.toString((int)ligneDrag.getValue()));
    }
    
    // Cette fonction permet de définir la variable vsIA 
    // cela nous permet de récuperer cette information dans le contrôleur
    public void setVsIA(boolean t){
        estVSIA = t;
    }

    @FXML
    private void revenirAuMenu(ActionEvent event)throws Exception {
        /**
         * Entrée : ActionEvent 
         * Sortie : Rien
         * Action assigné au bouton quitter
         * Permet de revenir au menu
         **/
        Stage s = (Stage)btnMenu.getScene().getWindow();
        if(!estVSIA) {
            // si on est en multi on ferme le socket avant de revenir au menu
            socket.close();
        }
        VueAccueil v = new VueAccueil();
        v.setPrece((int)s.getX(),(int)s.getY());
        s.close();
        Stage nouv = new Stage();
        
        v.start(nouv);
    }

    // Cette fonction permet d'affecter une valeur à la variable hostname 
    // cela nous permettra d'afficher le nom de l'hote dans l'écran d'attente
    public void setHostname(String text) {
        this.hostname = text;
    }
}