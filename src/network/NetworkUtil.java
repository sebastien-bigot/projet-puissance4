/*
 * Package : Network
 * Classe : NetworkUtil
 * Auteur : Pouilly Christopher, Bigot Sébastien, Thelliez Flavien
 * Description : Cette classe permet de définir tout un tas de fonctions utilitaires lié au réseau 
 */

package network;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import vue.VueAccueil;

public class NetworkUtil {
    
    // Fonction statique permettant à partir d'un flux d'entrée et de sortie de récupérer toutes les parties visibles du serveur 
    public static List<String> getParties(BufferedReader in, PrintStream out) throws IOException {
        
        List<String> liste_parties= new ArrayList<>();
        
        // Envoi du message indiquant au serveur qu'on veut récupérer le nombre de parties
        out.print("CB");
        
        // Attente d'une réponse (cad le nombre de partie visible) 
        String reponse = in.readLine();
        int nbParties = Integer.parseInt(reponse);
        
        // Envoi du message indiquant au serveur qu'on veut récupérer les parties visibles
        out.print("EP");
        
        // Récupération des n parties
        for(int i=0;i<nbParties;i++){
            liste_parties.add(in.readLine());
        }
        
        return liste_parties;
        
    }
    
    // Fonction statique permettant de récuperer tous les serveurs de jeu disponibles
    public static List<InetAddress> getServeurs(InetAddress address) throws IOException {
        
        // Création du socket UDP
        DatagramSocket socket = new DatagramSocket();
        List<InetAddress> tabaddr = new ArrayList<>();
        String verif;
        int idx=0;
        socket.setBroadcast(true);
        
        // Création du datagramme UDP
        String msg = "CheckservA";
        byte[] buffer = msg.getBytes();
        DatagramPacket dp = new DatagramPacket(buffer,buffer.length,address,3615);
        
        // Envoi du message (en broadcast l'option est mise à true au dessus
        socket.send(dp);
        
        // On désactive le broadcast
        socket.setBroadcast(false);
        
        // On définit un temps arbitraire ici 1s 
        // Ce temps définit le délai pendant lequel on va recevoir des réponses de serveurs disponibles
        long t = System.currentTimeMillis();
        long end = t+1000;
        while ( System.currentTimeMillis() < end ){
            socket.setSoTimeout((int)(end-System.currentTimeMillis()));
            try {
                //Si on recoit une réponse qui a été choisi lors de la conception, on ajoute l'adresse du serveur
                socket.receive(dp);
                verif = new String(dp.getData());
                if ( verif.equals("CheckservB") ) {
                    tabaddr.add(dp.getAddress());
                }
            } catch( SocketTimeoutException e ) {
                System.out.println("Fin recherche serveurs");
            }
        }
        return tabaddr;
    }

    // Fonction permettant de récuperer l'adresse de broadcast
    public static InetAddress getBroadcast() throws SocketException {
        // Pour chaque interface réseau, on va regarder si elle posséde une adresse de broadcas
        // Si oui, on l'a renvoie
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress broadcastAdress;
        while ( interfaces.hasMoreElements() ){
            NetworkInterface netInterface = interfaces.nextElement();
            if( netInterface.isLoopback() ){
                continue;
            }

            for(InterfaceAddress intAddr : netInterface.getInterfaceAddresses()){
                broadcastAdress = intAddr.getBroadcast();
                if(broadcastAdress!=null){
                    return broadcastAdress;
                }
            }
        }
        return null;
    }

    // Fonction affichant renvoyant un message d'erreur et renvoyant au menu
    // Dans le projet cette fonction est utilisé lors d'un crash serveur
    public static void afficheErreurReseau(){
        try {
            VueAccueil v = new VueAccueil();
            Stage nouv = new Stage();
            v.start(nouv);
            Popup popup = new Popup();
            Label popupLabel = new Label("Erreur au niveau du serveur !");
            popupLabel.setStyle(" -fx-background-color: white;-fx-border-color: red;-fx-font-weight:bold;-fx-font-size:24;");
            popup.setAutoHide(true);
            popup.getContent().add(popupLabel);
            popup.show(nouv,nouv.getX(),nouv.getY());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
