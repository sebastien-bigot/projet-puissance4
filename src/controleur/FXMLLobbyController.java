/*
 * Package : Controleur
 * Classe : FXMLLobbyController
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de définir le FXML Controleur associé à VueLobby
 */

package controleur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import modele.Modele;
import network.NetworkUtil;
import vue.Vue;
import vue.VueJeu;
import vue.VueAccueil;

public class FXMLLobbyController implements Initializable {

    @FXML
    private Button btnHost;
    @FXML
    private ComboBox<String> cbServer;
    @FXML
    private TableView<Partie> tablePartie;
    @FXML
    private TextField tfPseudo;
    @FXML
    private Button btnQuickGame;
    @FXML
    private Button btnQuitter;
    
    // Liste des parties stockées dans le tableau 
    private List<String> listParties;
    
    // Liste des parties au format String (tel qu'elles sont reçu)
    private String[] decoupeRow;
    
    // Liste des serveurs disponibles
    List<InetAddress> servers;
    
    // Le socket en cours
    private Socket socket;

    // Fonction d'initialisation du controleur
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // Initialisation du tableau 
        TableColumn idColumn = new TableColumn("ID de la partie");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn hostColumn = new TableColumn("Nom de l'hôte");
        hostColumn.setPrefWidth(130);
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));

        TableColumn typeColumn = new TableColumn("Type de la partie");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        TableColumn tailleColumn = new TableColumn("Taille de la grille");
        tailleColumn.setCellValueFactory(new PropertyValueFactory<>("taille"));
        
        TableColumn pionColumn = new TableColumn("Pion pour gagner");
        pionColumn.setCellValueFactory(new PropertyValueFactory<>("pion"));
        
        // Detection des doubles clics pour chaque ligne
        tablePartie.setRowFactory(tv -> {
            TableRow<Partie> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    Partie rowData = row.getItem();
                    try {
                        lancePartie(rowData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return row ;
        });
        
        tablePartie.getColumns().addAll(idColumn,hostColumn,typeColumn,tailleColumn,pionColumn);

        try {
            // On effectue la recherche des serveurs dispo 
            servers = rechercheServeur();
            
            // Si il y en a au moins 1
            if (servers.size() > 0){
                
                // On les transforme la liste d'IP en liste de nom d'hôte (pour la lisibilité pour l'utilisateur
                List<String> serv = servers.stream().map((e)->e.getHostName()).collect(Collectors.toList());
                
                // Puis on les ajoute à la combo box de selection de serveur et on choisis le 1 par défaut
                cbServer.getItems().clear();
                cbServer.getItems().addAll(serv);
                cbServer.setValue(serv.get(0));
                
                // On se lie au serveur 1 par défaut, on va donc établir la connexion
                InetAddress a = servers.get(0);
                socket = new Socket(a,3616);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream out = new PrintStream(socket.getOutputStream());

                // On active les boutons de créations de partie et de partie rapide 
                // (on est sur un serveur donc on peut faire ces actions)
                btnHost.setDisable(false);
                btnQuickGame.setDisable(false);
                
                // On veut maintenant récupérer les parties du serveur et les mettre dans le tableau
                listParties=NetworkUtil.getParties(in,out);    
                for (int i = 0; i < listParties.size() ; i++){
                    decoupeRow=listParties.get(i).split(" ");
                    addRowInTable(decoupeRow[0],decoupeRow[1],decoupeRow[2],decoupeRow[3],decoupeRow[4],decoupeRow[5]);
                }
                
            } else {
                // On n'a pas de serveur dispo (si il y en avait un avant on le ferme car il a quitter)
                if (socket!=null){
                    socket=null;
                }
                
                // On désactive les boutons de création de partie et de partie rapide car il n'y a pas de serveur
                btnHost.setDisable(true);
                btnQuickGame.setDisable(true);
                
                // Enfin on affiche qu'il n y a pas de serveur disponible
                cbServer.getItems().clear();
                cbServer.getItems().addAll("Pas de serveur disponible");
                cbServer.setValue("Pas de serveur disponible");
            }
        } catch (Exception e) {
            // Si probleme, revenir au menu
            Stage prece = (Stage) btnQuickGame.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            prece.close();
        }
    }    

    
    // Cette fonction actualise les serveur afin de récupérer ceux qui sont devenus accessible et enlever ceux qui ont quitté
    @FXML
    private void refreshServer(ActionEvent event) {
        try{
            // On effectue la recherche des serveurs dispo
            servers = rechercheServeur();

            // Si il y en a au moins 1
            if (servers.size() > 0){
                
                // On active les boutons de créations de partie et de partie rapide 
                // (on est sur un serveur donc on peut faire ces actions)
                btnHost.setDisable(false);
                btnQuickGame.setDisable(false);
                
                // On les transforme la liste d'IP en liste de nom d'hôte (pour la lisibilité pour l'utilisateur
                List<String> serv = servers.stream().map((e)->e.getHostName()).collect(Collectors.toList());
                
                // Puis on les ajoute à la combo box de selection de serveur et on choisis le 1 par défaut
                cbServer.getItems().clear();
                cbServer.getItems().addAll(serv);
                cbServer.setValue(serv.get(0));
                
            } else {
                // On n'a pas de serveur dispo (si il y en avait un avant on le ferme car il a quitter)
                if (socket != null){
                    socket = null;
                }
                
                // On désactive les boutons de création de partie et de partie rapide car il n'y a pas de serveur
                btnHost.setDisable(true);
                btnQuickGame.setDisable(true);
                
                // Enfin on affiche qu'il n y a pas de serveur disponible
                cbServer.getItems().clear();
                cbServer.getItems().addAll("Pas de serveur disponible");
                cbServer.setValue("Pas de serveur disponible");
            }
        } catch (Exception e) {
            // Si probleme, revenir au menu
            Stage prece = (Stage) tablePartie.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            prece.close();
        }
    }

    // Fonction associé au bouton de création de partie
    @FXML
    private void hostPartie(ActionEvent event)throws Exception {
        // Dans un premier temps, on va vérifier que le pseudo est valide (sans aucun espace car ca poserait des problèmes et inférieur égale à 20 caractères)
        if ( !tfPseudo.getText().replaceAll("\\s", "").equals("") ){
            if ( !tfPseudo.getText().contains(" ") && tfPseudo.getText().length() <= 20 ){
                
                Stage prece = (Stage) btnHost.getScene().getWindow();
                try {
                    // On envoie le message demandant si on peut créer une partie
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    out.print("VP");
                    
                    // On récupère la réponse 
                    if( !( in.readLine().equals("MaxAtt") )) {
                        // Si c'est bon (que le serveur nous dis qu'il n'a pas atteint le max)
                        
                        // On lance l'écran de création de partie
                        Vue vue = new Vue();
                        vue.setPrece((int)prece.getX(), (int)prece.getY());
                        prece.close();
                        Stage mainFen = new Stage();
                        mainFen.setTitle("Puissance 4");
                        vue.setSocket(socket);
                        vue.start(mainFen);
                        vue.c.setHostname(tfPseudo.getText());
                        
                    } else {
                        // Le serveur refuse on affiche un message d'erreur
                        Popup popup = new Popup();
                        Label popupLabel = new Label("Trop de parties créées, rejoignez-en une.");
                        popupLabel.setStyle(" -fx-background-color: white;-fx-border-color: red;");
                        popup.getContent().add(popupLabel);
                        popup.setAutoHide(true);
                        popup.show(prece,prece.getX()+tfPseudo.getLayoutX(),prece.getY()+tfPseudo.getLayoutY());
                    }
                } catch (Exception e){
                    // Si probleme, revenir au menu
                    NetworkUtil.afficheErreurReseau();
                    prece.close();
                }
            } else {
                // Le pseudo n'est pas valide alors envoyez message d'erreur
                Stage prece = (Stage) btnHost.getScene().getWindow();
                Popup popup = new Popup();
                Label popupLabel = new Label("Votre pseudo contient des espaces ou est plus grand que 20 caractères.");
                popupLabel.setStyle(" -fx-background-color: white;-fx-border-color: red;");
                popup.getContent().add(popupLabel);
                popup.setAutoHide(true);
                popup.show(prece,prece.getX()+tfPseudo.getLayoutX(),prece.getY()+tfPseudo.getLayoutY());
            }
        } else {
            // Le pseudo n'est pas valide alors envoyez message d'erreur
            Stage prece = (Stage) btnHost.getScene().getWindow();
            Popup popup = new Popup();
            Label popupLabel = new Label("Votre pseudo est vide.");
            popupLabel.setStyle(" -fx-background-color: white;-fx-border-color: red;");
            popup.getContent().add(popupLabel);
            popup.setAutoHide(true);
            popup.show(prece,prece.getX()+tfPseudo.getLayoutX(),prece.getY()+tfPseudo.getLayoutY());

        }
    }

    // Cette fonction permet de rejoindre une partie quelconque parmi celle disponibles
    @FXML
    private void rejoindreRandom(ActionEvent event) {
        Random r = new Random();
        try {
            // Si il y a au moins une partie, si il essaye de rejoindre une partie qui n'existe plus,
            // qui a déjà été rejoins ou que le serveur a crash, c'est lancePartie qui va s'occuper du problème
            if ( !this.tablePartie.getItems().isEmpty() ) {
                lancePartie(this.tablePartie.getItems().get(r.nextInt(this.tablePartie.getItems().size())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Cette fonction actualise les parties du serveur afin de récupérer celles qui sont accessibles et enlever celles qui ne le sont plus
    @FXML
    private void refreshPartie(ActionEvent event) throws IOException {
        if ( socket != null ) {
            // On vide le tableau des parties
            this.tablePartie.getItems().clear();
            try {
                // On va aller redemander au serveur les parties et les ajouter au tableau 
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream out = new PrintStream(socket.getOutputStream());
                listParties=NetworkUtil.getParties(in,out);
                for ( int i = 0 ; i < listParties.size() ; i++) {
                    decoupeRow = listParties.get(i).split(" ");
                    addRowInTable(decoupeRow[0], decoupeRow[1], decoupeRow[2], decoupeRow[3], decoupeRow[4], decoupeRow[5]);
                }
            } catch ( Exception e ){
                // Si probleme, revenir au menu
                Stage prece = (Stage) tablePartie.getScene().getWindow();
                NetworkUtil.afficheErreurReseau();
                prece.close();
            }
        }
    }

    // Fonction qui recherche les serveurs disponibles
    private List<InetAddress> rechercheServeur() throws Exception {
         return NetworkUtil.getServeurs(NetworkUtil.getBroadcast());
    }
    
    // Fonction qui ajoute une ligne au tableau
    private void addRowInTable(String id, String host, String estSuicide, String ligne, String colonne, String pion2Win){
        Partie p = new Partie(id, host,estSuicide,ligne,colonne,pion2Win);
        tablePartie.getItems().add(p);
    }

    // Fonction qui va lancer une partie dans le tableau de partie si c'est possible
    private void lancePartie(Partie p) throws Exception {
        // Dans un premier temps, on va vérifier que le pseudo est valide (sans aucun espace car ca poserait des problèmes et inférieur égale à 20 caractères)
        if ( !tfPseudo.getText().replaceAll("\\s", "").equals("") ){
            if ( !tfPseudo.getText().contains(" ") && tfPseudo.getText().length() <= 20 ){
                try {
                    // On envoie un message pour savoir si c'est possible de rejoindre la partie d'id ID
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    out.print("RP"+p.getId());
                    
                    // On lit la réponse
                    if(in.readLine().equals("Join")) {
                        // Si c'est ok, on lance la partie
                        int ligne;
                        int colonne;
                        boolean suicide;
                        int pion;

                        ligne = Integer.parseInt(p.getTaille().split(" x ")[0]);
                        colonne = Integer.parseInt(p.getTaille().split(" x ")[1]);
                        suicide = p.getType().equals("Mode suicide");
                        pion = Integer.parseInt(p.getPion());
                        
                        Modele m = new Modele(ligne,colonne,suicide,false,pion);
                        
                        VueJeu vue = new VueJeu();
                        
                        vue.setC1(Color.RED);
                        vue.setC2(Color.YELLOW);

                        vue.setSocket(socket);
                        
                        Stage mainFen = new Stage();
                        mainFen.setTitle("Puissance 4");
                        Stage prece = (Stage) btnQuickGame.getScene().getWindow();
                        vue.setPrece((int)prece.getX(),(int)prece.getY());
                        prece.close();
                        
                        vue.setModele(m);
                        vue.setMonTour("1");
                        
                        vue.start(mainFen);

                    } else {
                        // Sinon on actualise les parties car elles ne devaient pas être à jour
                        listParties=NetworkUtil.getParties(in,out);
                        tablePartie.getItems().clear();
                        
                        for ( int i = 0 ; i < listParties.size() ; i++ ) {
                            decoupeRow=listParties.get(i).split(" ");
                            addRowInTable(decoupeRow[0],decoupeRow[1],decoupeRow[2],decoupeRow[3],decoupeRow[4],decoupeRow[5]);
                        }
                    }
                }catch (Exception e){
                    // Si problème, revenir au menu
                    Stage prece = (Stage) tablePartie.getScene().getWindow();
                    NetworkUtil.afficheErreurReseau();
                    prece.close();
                }
            } else {
                // Le pseudo n'est pas valide alors envoyez message d'erreur
                Stage prece = (Stage) btnHost.getScene().getWindow();
                Popup popup = new Popup();
                Label popupLabel = new Label("Votre pseudo contient des espaces ou est plus grand que 20 caractères.");
                popupLabel.setStyle(" -fx-background-color: white;-fx-border-color: red;");
                popup.getContent().add(popupLabel);
                popup.setAutoHide(true);
                popup.show(prece,prece.getX()+tfPseudo.getLayoutX(),prece.getY()+tfPseudo.getLayoutY());
            }
        } else {
            // Le pseudo n'est pas valide alors envoyez message d'erreur
            Stage prece = (Stage) btnHost.getScene().getWindow();
            Popup popup = new Popup();
            Label popupLabel = new Label("Votre pseudo est vide.");
            popupLabel.setStyle(" -fx-background-color: white;-fx-border-color: red;");
            popup.getContent().add(popupLabel);
            popup.setAutoHide(true);
            popup.show(prece,prece.getX()+tfPseudo.getLayoutX(),prece.getY()+tfPseudo.getLayoutY());
        }
    }

    // Fonction de changement de serveur
    @FXML
    private void changeServer(ActionEvent event) throws IOException {
        int numServer = cbServer.getSelectionModel().getSelectedIndex();
        if ( !( numServer==-1 || servers.isEmpty() )){
            
            InetAddress a = servers.get(numServer);
            
            // On établit la connexion au serveur choisi (en fermant le precedemment ouvert)
            try {
                
                if(socket!=null){
                    socket.close();
                }
                socket = new Socket(a,3616);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream out = new PrintStream(socket.getOutputStream());
                
                // On affiche les parties du serveur selectionné
                listParties=NetworkUtil.getParties(in,out);
                tablePartie.getItems().clear();
                for (int i = 0 ; i < listParties.size() ; i++ ) {
                    decoupeRow=listParties.get(i).split(" ");
                    addRowInTable(decoupeRow[0],decoupeRow[1],decoupeRow[2],decoupeRow[3],decoupeRow[4],decoupeRow[5]);
                }
                
            } catch ( Exception e ){
                // Si problème, revenir au menu
                Stage prece = (Stage) tablePartie.getScene().getWindow();
                NetworkUtil.afficheErreurReseau();
                prece.close();
            }
        }
    }

    // Fonction associé au bouton quitter
    @FXML
    private void quitter(ActionEvent event)throws Exception {
        Stage s = (Stage)btnQuitter.getScene().getWindow();
        try {
            if (socket != null){
                socket.close();
            }
        } catch (Exception e){
            Stage prece = (Stage) tablePartie.getScene().getWindow();
            NetworkUtil.afficheErreurReseau();
            prece.close();
        }
        VueAccueil v = new VueAccueil();
        v.setPrece((int)s.getX(),(int)s.getY());
        s.close();
        Stage nouv = new Stage();
        v.start(nouv);
    }


    // Sous classe partie, elle gère les lignes de la table
    // Une instance = une ligne
    public static class Partie {
        
        String id;
        String host;
        String type;
        String taille;
        String pion;
        
        public Partie(String id, String host, String estSuicide, String ligne, String colonne, String pion2Win) {
            this.id = id;
            this.host = host;
            this.type = (Boolean.parseBoolean(estSuicide)?"Mode suicide":"Mode Normal");
            this.taille = ligne+" x "+colonne;
            this.pion = pion2Win;
        }
        
        public String getId(){
            return id;
        }
        
        public String getHost(){
            return host;
        }
        
        public String getType(){
            return type;
        }
        
        public String getTaille(){
            return taille;
        }
        
        public String getPion(){
            return pion;
        }
    }
}
