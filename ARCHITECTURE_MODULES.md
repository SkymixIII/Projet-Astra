# Decoupage en package java

Ce document explicite le découpage choisit pour le
projet Astra afin de simplifier la visualition des modules.

Pour importer une classe maclasse contenue dans le package monpaquet,
il faut ajouter 'import monpaquet.maclasse;' au début de la classe
concernée.

Pour ajouter une classe à un package il faut la positionner dans le
dossier du même nom et ajouter 'package monpaquet;' au plus haut du
fichier .java de la classe.

### Packages de l'application dans src/main/java/

- package astra :
	- CoordMonde.java
	- EtiquettesOuvriers.java
	- GestionCollisions.java
    - GestionInput.java
	- Launcher.java
    - Main.java
	- RenduBatiments.java
    - RenduCarte.java
	- RenduMonde.java
	- RenduOuvriers.java

- package batiments :
    - Batiment.java
    - CentreLancement.java
	- CentreRecherche.java
	- Ecole.java
	- Entrepot.java
    - Hopital.java
    - LieuDeRessource.java
    - Maison.java
	- Serre.java
	- StationPompage.java
	- TypeBatiment.java
    - Usine.java

- package carte :
    - Carte.java
    - Case.java
    - Direction.java
    - GenerateurAleatoire.java
    - Item.java
	- Sol.java
	- TypeSol.java


- package entites :
	- EtatOuvrier.java
    - Joueur.java
    - Ouvrier.java
    - Role.java


- package evenements :
	- Canal.java
	- Evenement.java
	- EventBus.java
	- EventListener.java
	- Tempete.java
    

- package exceptions :
    - LancementImpossibleException.java
    - RessourceInsuffisanteException.java
    - StockException.java


- package fusee :
    - ChargeUtile.java
    - Fusee.java
    - ModuleFusee.java
    - OrdiDeBord.java
    - Propulseur.java


- package jeu :
    - Difficulte.java
    - Jeu.java
	- Temps.java

    
- package metiers :
    - Bucheron.java
    - Ingenieur.java
    - Macon.java
    - Metier.java
    - Mineur.java
    - Technicien.java

- package ressources:
    - Recette.java
    - RegistreRecettes.java
	- Ressources.java
    - Stock.java
    - TypeRessource.java

### Packages de test dans src/test/java :    

- package batiments :
	- TestCentreLancementComplet.java
    - TestCentreRecherche.java
	- TestEcole.java
	- TestEntrepot.java
    - TestHopital.java
    - TestLieuDeRessource.java
    - TestMaison.java
	- TestSerre.java
	- TestStationPompage.java
    - TestUsine.java
	- TestUsineComplet.java

- package carte
	- TestCase.java
	- TestDirection.java

- package entites
	- TestJoueur.java
	- TestOuvrier.java

- package fusee :
    - TestFuseeComplet.java
    - TestModuleFusee.java

- package metiers
	- TestBucheron.java
	- TestIngenieur.java
	- TestMacon.java
	- TestTechnicien.java

- package ressources :
    - StockTest.java
    - TestRegistreRecettes.java
