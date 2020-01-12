# Puissance 4 en réseau

*Projet Reseau par Bigot Sebastien, Pouilly Christopher et Thelliez Flavien*

## Les fonctionnalités

Liste des fonctionnalités présentes :

*  Possibilité de jouer en local (contre un ordinateur) ou en réseau (en multijoueur)
*  Lobby de serveur qui permet de rejoindre une partie de son choix (avec un double clic) ou de rejoindre une partie rapide (bouton partie rapide)
*  Ecran d'attente pour la personne qui host une partie qui rappelle les règles de sa partie et une possibilité de quitter
*  Le puissance 4 en lui même fonctionnel qui possède un timer de 10 secondes entre chaque coup (pour des raisons d'équité entre tous les joueurs)
*  La possibilité de moduler le nombre de lignse, colonnes et même le nombre de pions pour gagner
*  Un mode de jeu suicide où les règles du jeu changent et où il faut faire aligner un certain nombre de pions à son adversaire pour gagner
*  Une robustesse au crash client et serveur pour toutes les pages accessibles par le client (hors local)

    
Correctifs apportés depuis l'oral :

*  Correction d'un bug qui laissait l'hôte qui attendait un adversaire dans l'écran d'attente alors que le serveur avait planté
*  Correction d'un bug qui fait qu'utiliser partie rapide alors qu'il n'y a pas de partie provoquait une exception non gérée


## Comment lancer notre serveur ?

Il y a deux manières :

*  En lançant le fichier server présent à la racine de ce projet (c'est la version déjà compilée)
*  En buildant le server pour cela, un makefile est disponible (make main pour créer server) ou la liste des commandes permettant de le compiler 
se trouvent à l'intérieur de celui-ci (avec comme ordre la directive partie.o puis main) si make n'est pas disponible. 
Il suffit ensuite de lancer le fichier résultant. 
Le makefile et les fichiers sources se trouve dans src/serveur.


## Comment lancer notre client ?

Notre client nécessite une version de java 11 ou plus.

Pour lancer le client, il faut utiliser la commande ci dessous :

```
java --module-path [PATH_TO_JAVA_FX] --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media -jar Puissance4.jar
```

avec [PATH_TO_JAVA_FX] qui doit contenir une référence vers le dossier lib de javafx fourni dans le projet.

Voici la commande pour lancer le client si celui ci n'a pas été déplacé :

```
java --module-path ./javafx-sdk-11.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media -jar Puissance4.jar
```
