# Organisation du repo
- On veillera à essayer de respecter les répertoires indiqués dans les consignes
    - ​livrables : ne doit contenir que les livrables explicitement demandés en respectant les noms (tout en minuscules) donnés dans les consignes
    - ​src : le code source de votre projet
    - doc : les fichiers sources des documents fournis dans les livrables, etc.
- Pour chaque issue on essayera de faire une branche dédiée.

# Lancer le projet

## Prérequis
- **JDK 17 ou supérieur** installé.
  - macOS : `brew install openjdk@21`
  - Linux : `sudo apt install openjdk-21-jdk` (ou équivalent)
  - Windows : https://adoptium.net/

## Installation du JavaFX SDK
Le SDK JavaFX est volumineux (~50 Mo) et **dépend de l'OS**, donc il n'est **pas versionné** dans le dépôt (voir `.gitignore`). Chacun doit le télécharger localement :

1. Aller sur https://gluonhq.com/products/javafx/
2. Télécharger la version **21.0.x* SDK** correspondant à votre système :
   - macOS Apple Silicon (M1/M2/M3) : `osx-aarch64`
   - macOS Intel : `osx-x64`
   - Windows : `windows-x64`
   - Linux : `linux-x64`
3. Décompresser l'archive et placer le dossier obtenu dans `rsrc/`.
   La structure finale doit être :
   ```
   Projet-Astra/
   └── rsrc/
       └── javafx-sdk-21.0.x/
           └── lib/
   ```

## Lancement
- **macOS / Linux** : `./run.sh`
- **Windows** : double-cliquer sur `run.bat` (ou l'exécuter dans un terminal)

Le script compile automatiquement les fichiers de `src/` vers `out/` puis lance la classe `Main`.
