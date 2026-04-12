## Choix de l'architecture :

- Un joueur a acces a l'ensemble des informations le concernant
- Pour l'instant pas de coherence geographique : les lieux de ressources sont entasses les uns a cote des autres
- Au debut du jeu, on cree un objet joueur, constitue de lieux d'extraction de matieres premieres, de quelques ouvriers et aussi des ressources de base pour survivre
- Les usines et les lieux de matieres premieres sont deux implementations de Batiment
- Pour simplifier, organisation des usines en quartiers
- Toutes les usines sont claires : on sait ce qu'on fabrique. Sauf pour l'usine finale, la fusee : une sorte de boite noire dont on remplit les emplacements, qui nous indique en sortie le pourcentage de reussite de la fusee : si pas 100%, toutes les ressources sont perdues

**Point important :**
- Pour eviter un nombre d'appels entre classes qui explose avec l'echelle du jeu, les interactions entre classes passent par l'EventBus : chaque composant publie et ecoute sur des canaux dedies (file d'attente avec ordres de priorite)
- L'affichage est separe du modele : les classes metier (Carte, Case, etc.) ne contiennent aucune methode d'affichage
- Le stockage des ressources utilise `Map<TypeRessource, Integer>` (et non `List<Ressource>`) pour eviter la creation massive d'objets et les boucles de recherche lentes


## UML


### Interface Item :
*Element caracterise par sa position sur la carte.*

Attributs : int x, int y

distance(Item) : double -- Calculer la distance a un autre element
deplacer(int x, int y) : void
deplacer(Direction direction) : void


### Enum Direction :
NORD, SUD, EST, OUEST


### Interface Batiment extends Item :
*Herite de Item, caracterise par une position constante (x, y fixes a la construction). Contient les attributs communs a tout batiment.*

Attributs : int sante, List<Ouvrier> personnelAffecte, int maxPersonnel

isOperationnel() : boolean -- sante > 0
affecterPersonnel(Ouvrier o) : void -- lier un ouvrier a ce batiment
retirerPersonnel(Ouvrier o) : void
subirDegats(int degats) : void -- reduit la sante
reparer(int points) : void


### Classe Joueur implements Item :

Attributs : Direction orientation, Map<TypeRessource, Integer> ressources, List<Ouvrier> ouvriers, List<Batiment> batiments

creerJoueur() : void
deplacer(int x, int y) : void
deplacer(Direction direction) : void

Tableau de bord du personnel -> equipage = moteur de la simulation
Feedback immediat : jauges de couleur pour les besoins urgents des equipes


### Classe Ouvrier implements Item :
*Un ouvrier gagne de l'experience en travaillant, lui permettant d'evoluer de role via evoluer().*

Attributs : int identifiant, Role role, int faim, int soif, int repos, int moral, int experience, EtatOuvrier etat, boolean disponibilite

gagnerExp(int gain) : void
getNiveau() : int
evoluer() : void -- change le Role si l'experience est suffisante
affecterPoste(Batiment b) : void -- lier l'ouvrier a une Usine ou LieuDeRessource
mettreAJourBesoins() : void -- decremente faim/soif/repos a chaque tick, met a jour l'etat
getProductivite() : float -- efficacite = experience * etat ; diviser le temps par l'efficacite pour le temps reel

etat : {STRESSE, FATIGUE, MOTIVE, NORMAL}. Ces etats appliquent un multiplicateur a la vitesse de production du batiment occupe. (Stresse = rapide mais erreurs, Fatigue = lent, Motive = rapide, Normal = etat de base)

2 manieres de faire evoluer le personnel :
- par l'ecole : via des patrons de "cours", l'ouvrier apprend de nouveaux skills (plus rapide)
- par l'experience : au bout d'un certain temps de travail, l'ouvrier renforce ses competences
/!\ il faut un min de connaissances via l'ecole pour devenir ingenieur


### Enum Role :
MINEUR, MACON, TRANSPORTEUR, FERMIER, BUCHERON, TECHNICIEN, SOIGNANT, INGENIEUR, SCIENTIFIQUE, FORMATEUR

getPole() : String -- retourne le pole d'activite (Production brute, Logistique, Subsistance, Industriel, Sante/Social, Recherche, Education)

| Metier        | Pole             | Objectif principal                     | Evolution possible                 |
|---------------|------------------|----------------------------------------|------------------------------------|
| Mineur        | Production brute | extraire des ressources mines/carrieres| Technicien (experience ou ecole)   |
| Macon         | Production brute | fabriquer des batiments                | Formateur (experience ++)          |
| Transporteur  | Logistique       | deplacer stocks entre sites            | -                                  |
| Fermier       | Subsistance      | produire la nourriture                 | -                                  |
| Bucheron      | Production brute | fabriquer des batiments (avec macon)   | Formateur (experience ++)          |
| Technicien    | Industriel       | utiliser machines complexes            | Ingenieur (obligatoire par ecole)  |
| Soignant      | Sante/Social     | soigner les gens                       | Scientifique (ecole ou experience) |
| Ingenieur     | Industriel       | assembler modules de la fusee          | Scientifique (experience)          |
| Scientifique  | Recherche        | cree les "patrons" de creation         | Ingenieur ou Formateur (experience)|
| Formateur     | Education        | fait evoluer l'equipage via lecons     | Scientifique (experience)          |


### Enum EtatOuvrier :
NORMAL, FATIGUE, STRESSE, MOTIVE

getMultiplicateur() : float -- retourne le multiplicateur de productivite


### Classe Jeu :
*Point d'entree. Orchestre la boucle de jeu en deleguant a chaque classe sa propre logique.*

Attributs : List<Ouvrier> ouvriers, Joueur joueur, List<Batiment> batiments, Temps temps, Carte carte, EventBus bus, Fusee fusee, Age age

main(args) : void
processTick() : void -- appelle produire()/extraire() sur chaque batiment, mettreAJourBesoins() sur chaque ouvrier, augmenterHeure() sur Temps
mettreAJourProduction() : void -- parcourt tous les batiments du joueur
consommerBesoinsVitaux() : void -- reduit les barres de nourriture/eau des ouvriers a chaque intervalle
recupererCommande() : void
traiterCommande() : void
ajouterOuvrier(Ouvrier) : void
ajouterBatiment(Batiment) : void
gererDate() : void


### Classe Temps :
*Pour simplifier, on compte que les heures (on skip les minutes) et les jours de la semaine.*

Attributs : DayOfWeek jour (Enum Lundi...Dimanche), int heure

Temps() => commence le lundi a 0h
augmenterHeure() : void
augmenterJour() : void

Dans le jeu : 10 min = une demi-journee (sans compter la nuit)


### Classe Age :
*Represente l'age technologique actuel du joueur.*

Attributs : int niveau, NomAge nomAge

Enum NomAge : AGE_PRIMITIF, AGE_INDUSTRIEL, AGE_TECHNOLOGIQUE, AGE_SPATIAL

niveauSuivant() : void -- passage a l'age suivant (temps exponentiel pour avancer)


### Classe Carte :
*Matrice de Cases. Ne contient aucune methode d'affichage. Nommee Carte pour eviter le conflit avec java.util.Map.*

Attributs : Case[][] matrice, int largeur, int hauteur

getTile(int x, int y) : Case
estDansLimites(int x, int y) : boolean
getVoisins(int x, int y) : List<Case>


### Classe Case :
*Liste d'elements Item.*

Attributs : List<Item> contenu

ajouter(Item) : void
supprimer(Item) : void
estOccupee() : boolean


### Enum TypeRessource :
*Matieres premieres et produits transformes.*

Matieres premieres : FER, SILICIUM, PETROLE, EAU, BOIS, ENERGIE
Produits transformes : ACIER, KEROSENE, PLASTIQUE, CARTE_MERE, PROCESSEUR_VOL, ALLIAGE_THERMIQUE, ELECTRICITE, NOURRITURE, EAU_POTABLE


### Classe Ressource :
*Donnee simple : un type et une quantite. Ne connait pas son lieu d'origine. Pour les stocks, privilegier Map<TypeRessource, Integer> plutot que List<Ressource>.*

Attributs : TypeRessource type, int quantite

ajouter(int qte) : void
retirer(int qte) : boolean -- lance RessourceInsuffisanteException si qte insuffisante


---


## Batiments


### Classe Usine implements Batiment :

Attributs : TypeUsine type, Map<TypeRessource, Integer> stock, TypeRessource production

produire() : Ressource -- consomme le stock, produit la sortie. Lance RessourceInsuffisanteException si stock insuffisant.
peutProduire() : boolean -- verifie si le stock est suffisant

| Type d'usine          | Ressources necessaires          | Piece produite      | Role technique                       |
|-----------------------|---------------------------------|---------------------|--------------------------------------|
| Fonderie              | Fer(x3) + Petrole(x1) ou Energie(x2) | Poutre d'acier | Structure batiments + squelette fusee|
| Raffinerie (kerosene) | Petrole(x5) + Minerai(x1)      | Kerosene            | Energie pour decollage               |
| Raffinerie (plastique)| Petrole(x2)                     | Plastique           | Cablage                              |
| Usine electronique    | Silicium(x2) + Fer(x1)         | Carte mere          | Systeme de navigation                |
| Usine electronique    | Carte mere(x2) + Acier(x1)     | Processeur de vol   | Necessaire pour fusee                |
| Fonderie avancee      | Acier(x2) + Silicium(x1)       | Alliage thermique   | Plus efficace sur la fusee           |


### Enum TypeUsine :
FONDERIE, RAFFINERIE, USINE_ELECTRONIQUE, FONDERIE_AVANCEE


### Classe LieuDeRessource implements Batiment :

Attributs : TypeRessource typeExtrait, int gisementRestant, float tauxExtraction

extraire() : Ressource -- cree une Ressource du type extrait
estEpuise() : boolean

| Type de ressource    | Lieu de la ressource        |
|----------------------|-----------------------------|
| Minerai de fer       | Mine de fer                 |
| Sable/Terres rares   | Carriere (de mineraux)      |
| Petrole              | Puits de petrole            |
| Eau                  | Station de pompage          |
| Bois                 | Foret / Serre               |
| Energie              | Centrale (Solaire/Fuel)     |


### Classe Maison implements Batiment :

Attributs : int capacite, List<Ouvrier> occupants

ajouterOccupant(Ouvrier) : void
retirerOccupant(Ouvrier) : void
estPleine() : boolean


### Classe Hopital implements Batiment :
*Soigne les malades et reboost le moral.*

Attributs : int capaciteSoin, List<Ouvrier> patients

soigner(Ouvrier o) : void
boosterMoral(Ouvrier o) : void


### Classe Ecole implements Batiment :
*Forme le personnel pour le faire evoluer. Peut debloquer la capacite de construire certains modules.*

Attributs : int capaciteFormation, List<Ouvrier> eleves

former(Ouvrier o) : void -- accelere l'evolution du role


### Classe Entrepot implements Batiment :
*Stocke les ressources.*

Attributs : Map<TypeRessource, Integer> stock, int capaciteMax

stocker(TypeRessource type, int qte) : void
retirer(TypeRessource type, int qte) : boolean
getStockTotal() : int


### Classe StationPompage implements Batiment :
*Extraction d'eau avec possibilite de recyclage.*

Attributs : int debitPompage

pomper() : Ressource


### Classe Serre implements Batiment :
*Production de nourriture et de bois.*

Attributs : int rendement

cultiver() : Ressource


### Classe Centrale implements Batiment :
*Production d'electricite (solaire ou fuel).*

Attributs : String typeEnergie, int puissance

produireElectricite() : Ressource


### Classe CentreRecherche implements Batiment :
*Batiment qui sert de point d'interaction entre l'equipage et l'arbre technologique.*

Attributs : ArbreTechnologique arbre

produireSciences() : int -- calcul un gain de connaissance en fonction des scientifiques affectes


### Classe CentreLancement implements Batiment :
*Pas de tir : assemble les 3 modules de la fusee.*

Attributs : Fusee fusee

assemblerModule(ModuleFusee module) : void


---


## Technologie


### Classe Technologie :
*Unite de recherche des scientifiques.*

Attributs : String nom, int coutScience, boolean debloquee, List<Technologie> prerequis

estDisponible() : boolean -- parcourt les prerequis, retourne false si un prerequis n'est pas debloque
debloquer() : void

| Technologie            | Prerequis                           | Niveau | Type              | Effets                                    |
|------------------------|-------------------------------------|--------|-------------------|-------------------------------------------|
| Extraction optimisee   | -                                   | 1      | Industrie         | +20% production mines                     |
| Traitement de l'eau    | -                                   | 1      | Industrie         | Debloque eau potable dans les industries   |
| Agriculture controlee  | -                                   | 1      | Survie            | Debloque champs                            |
| Stockage               | -                                   | -      | Gestion ressources| +20% capacite stockage ressources          |
| Stockage basique survie| -                                   | -      | Survie            | +20% capacite stockage nourriture + eau    |
| Metallurgie            | Extraction optimisee                | 2      | Industrie         | Debloque acier                             |
| Raffinage petrole      | Extraction optimisee                | 2      | Fusee             | Debloque carburant                         |
| Reseau electrique      | Traitement eau                      | 2      | Industrie         | Alimente batiments                         |
| Architecture fusee     | Metallurgie + Raffinage petrole     | 2      | Fusee             | Debloque assemblage fusee                  |
| Electronique           | Metallurgie + Reseau electrique     | 3      | Industrie         | Automatise une tache                       |
| Chimie avancee         | Raffinage petrole                   | 3      | Fusee             | Ameliore carburant (+10 force decollage)   |
| Formation scientifique | Agri. controlee + Stockage survie   | 3      | Survie            | +1 experience a tous les chercheurs        |
| Alliages thermiques    | Metallurgie + Chimie + Archi. fusee | 4      | Fusee             | Resistance chaleur fusee (+10 sec)         |
| Informatique embarquee | Electronique + Archi. fusee         | 4      | Fusee             | Systeme de navigation fusee                |
| Combustion avancee     | Chimie avancee                      | 4      | Fusee             | +20 force decollage                        |
| Simulation spatiale    | Formation scientifique              | 4      | Fusee             | Simulations sans risque, -20% taux erreur  |
| Optimisation modules   | Architecture fusee                  | 5      | Fusee             | Simulation 10% d'erreurs                   |


### Classe ArbreTechnologique :
*Centralise les technologies disponibles et l'avancement du joueur.*

Attributs : Map<String, Technologie> catalogue, Technologie rechercheActive, int progressionActuelle

lancerRecherche(String nom) : void -- verifie estDisponible(), sinon lance RechercheImpossibleException
ajouterProgression(int points) : void
getRechercheActive() : Technologie


---


## Fusee


### Classe Fusee :
*Classe dediee pour gerer la condition de la victoire.*

Attributs : float etatPropulseur, float etatChargeUtile, float etatCommande (pourcentages de completion)

calculerProbabiliteSucces() : float -- combine la qualite des composants produits et le niveau d'expertise des ouvriers ayant travaille sur l'assemblage
lancer() : boolean -- si le resultat est < 100%, declenche la perte des ressources investies

| Module         | Quantite necessaire                        | Personnel requis               | Risque d'echec                              |
|----------------|--------------------------------------------|--------------------------------|---------------------------------------------|
| Propulseur     | 10 poutres acier, 20 kerosene, 5 alliage   | Technicien, Scientifique       | Pas d'alliage thermique ou kerosene nul     |
| Charge utile   | 5 poutres acier, 10 cablage, 5 carte mere  | Ouvrier, Technicien, Scientifique | Pas d'isolation                          |
| Ordi de bord   | 2 processeurs vol, 5 carte mere, 2 alliage | Ingenieur, Scientifique        | Pas d'alliage thermique                     |


---


## Evenements


### Classe EventBus :
*Distribue les evenements entre les composants du jeu via des canaux avec file d'attente et ordres de priorite.*

3 canaux de diffusion : SYSTEME, SOCIAL, ENVIRONNEMENT

sEnregistrer(Canal, EventListener) : void -- connecte un composant au bus
seDesinscrire(Canal, EventListener) : void -- deconnecte un composant
publier(Canal, Evenement) : void -- diffuse l'evenement aux abonnes du canal
possedeAbonne(Canal) : boolean -- verifie la presence d'un abonne

**Qui publie :**
- Ouvrier publie sur SOCIAL quand son etat change
- GenerateurAleatoire publie sur ENVIRONNEMENT (tempete, decouverte)
- Jeu publie sur SYSTEME (debut/fin de tick)

**Qui ecoute :**
- Jeu ecoute SOCIAL pour detecter les greves
- Usine et LieuDeRessource ecoutent ENVIRONNEMENT pour reagir aux tempetes
- Fusee ecoute SYSTEME


### Enum Canal :
SYSTEME, SOCIAL, ENVIRONNEMENT


### Interface EventListener :
onEvent(Evenement) : void


### Classe abstraite Evenement :
*Chaque type d'evenement est une sous-classe.*

Attributs : int timestamp, String description

getTimestamp() : int
getDescription() : String


### Classe Greve extends Evenement :
*Declenchee si le moral ou le logement est insuffisant (>60% du personnel en dessous du niveau normal dans un batiment).*

Attributs : Batiment batimentConcerne, List<Ouvrier> grevistes

Resolution : loisirs, ameliorer dortoirs/bouffe/eau


### Classe Tempete extends Evenement :
*Aleatoire. Arret temporaire de l'extraction, degats sur les batiments.*

Attributs : int severite

Resolution : reparation batiment


### Classe DecouverteGisement extends Evenement :
*Aleatoire lors d'un sondage de terrain. Apparition nouvelle ressource sur la carte.*

Attributs : TypeRessource type, int x, int y

Resolution : deployer usines et infrastructure


### Classe AccidentTravail extends Evenement :
*Declenche si fatigue ou stress eleve. Baisse temporaire de production.*

Attributs : Ouvrier victime, Batiment lieu

Resolution : soin immediat


### Classe PerceeTechnologique extends Evenement :
*Declenchee par investissement dans la recherche. Reduction du temps de travail/recherche.*

Attributs : Technologie technologie


### Classe GenerateurAleatoire :

genererGisement(Carte carte, TypeRessource type) : void -- calcule des coordonnees aleatoires pour faire apparaitre une ressource
tirageEvenement(Difficulte diff) : Evenement -- tirage ou les evenements positifs ont plus de poids en mode "Apprentissage" et les catastrophes plus de poids en mode "Difficile"
ajusterSeuilCritique(float multiplicateur) : void -- modifier dynamiquement la rarete des ressources (uniquement en mode difficile)


### Enum Difficulte :
APPRENTISSAGE, NORMAL, DIFFICILE


---


## Transport


### Interface CalculateurTrajet :

calculerTempsTrajet(Case depart, Case arrivee) : int -- retourne le nombre de ticks necessaires
estAccessible(Case cible) : boolean -- verifie si le mode de transport actuel permet d'atteindre la case
calculerCoutEnergie(float distance) : float -- determine la consommation de carburant
getVitesseMoyenne() : float -- retourne une vitesse tabulee dependante du moyen de transport

Note : pour les ouvriers, pas de calcul de trajet (les ressources vont directement au stock). Seulement pour le joueur.


---


## Exceptions


### Classe RessourceInsuffisanteException extends Exception :
*Lancee par produire() dans Usine quand le stock est insuffisant. Attrapee par la boucle du jeu.*


### Classe RechercheImpossibleException extends Exception :
*Lancee par lancerRecherche() dans ArbreTechnologique quand les prerequis ne sont pas remplis.*


---


## Affichage


### Classe Affichage :
*Separee du modele. Lit les donnees de Carte, Jeu, etc. pour les afficher. Modifier l'affichage ne touche pas aux classes metier. Supporte des calques/layers (ex: reseaux electriques).*

Attributs : Carte carte, Jeu jeu

afficherCarte() : void
afficherCase(Case c) : void
afficherTableauDeBord(List<Ouvrier>) : void
afficherFusee(Fusee f) : void
rafraichir() : void


---
