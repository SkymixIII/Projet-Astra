# Decoupage en package java

Ce document explicite le découpage choisit pour le
projet Astra afin de simplifier la visualition des modules.

Pour importer une classe maclasse contenue dans le package monpaquet,
il faut ajouter 'import monpaquet.maclasse;' au début de la classe
concernée.


- package batiments :
    - Batiment.java
    - CentreLancement.java
    - Hopital.java
    - LieuDeRessource.java
    - Maison.java
    - Usine.java

- package batiments.test_batiments :
    - TestCentreLancement.java
    - TestHopital.java
    - TestLieuDeRessource.java
    - TestMaison.java
    - TestUsine.java


- package carte :
    - Carte.java
    - Case.java
    - Direction.java
    - GenerateurAleatoire.java
    - Item.java


- package entites :
    - Joueur.java
    - Ouvrier.java
    - Role.java
    

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

- package fusee.test_fusee :
    - TestFusee.java
    - TestModuleFusee.java


- package jeu :
    - Difficulte.java
    - Jeu.java


- package ressources:
    
    - Recette.java
    - RegistreRecettes.java
    - Stock.java
    - TypeRessource.java

- package ressources.test_ressources:
    - StockTest.java
