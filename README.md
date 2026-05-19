# Organisation du repo
- On veillera à essayer de respecter les répertoires indiqués dans les consignes
    - ​livrables : ne doit contenir que les livrables explicitement demandés en respectant les noms (tout en minuscules) donnés dans les consignes
    - ​src : le code source de votre projet
    - doc : les fichiers sources des documents fournis dans les livrables, etc.
- Pour chaque issue on essaiera de faire une branche dédiée.

# Lancer le projet avec gradlew

## Attention : avant d'utiliser Gradle, il faut s'assurer d'avoir installé et configuré JDK 21 correctement pour cela il faut se référer au fichier CONTRIBUTING.md

## 1. Commandes essentielles

> Sur Windows, remplacer `./gradlew` par `gradlew`

| Commande | Description |
|----------|-------------|
| `./gradlew run` | Lance l'application |
| `./gradlew test` | Exécute tous les tests |
| `./gradlew clean` | Supprime le dossier `build/` |
| `./gradlew clean build` | Recompile tout from scratch |
| `./gradlew installDist` | Produit une application fonctionnelle dans `build/install/` |
| `./gradlew jar` | Produit un jar fonctionnel dans `build/libs/` |

## !! Pour lancer **jeu.Jeu** liserer le fichier **build.gradle** et remplacer **astra.Main** par **jeu.Jeu** et inversement en fonction de la classe souhaitée.

Les rapports de test sont dans `build/reports/tests/test/index.html`.

---

## 2. Structure du projet

```
src/main/java/   ← code de production
src/test/java/   ← tests (même package que la classe testée)
build.gradle     ← configuration du build
gradlew          ← wrapper Gradle, ne pas supprimer
```

---

## 3. Erreurs fréquentes

**`JAVA_HOME` n'est pas défini**
```bash
export JAVA_HOME=/chemin/vers/jdk21
```

**`Could not find or load main class Main`**
```bash
./gradlew clean run
```

**`permission denied: ./gradlew`** (Linux/macOS)
```bash
chmod +x gradlew
```

# Lancer le projet avec run.sh

## Attention : avant d'utiliser run.sh, il faut s'assurer d'avoir installé et configuré JDK 17 ou plus correctement pour cela il faut se référer au fichier CONTRIBUTING.md

## Lancement
- **macOS / Linux** : `./run.sh`
- **Windows** : double-cliquer sur `run.bat` (ou l'exécuter dans un terminal)

Le script compile automatiquement les fichiers de `src/` vers `out/` puis lance la classe `Main`.


# Lancer le .jar
Lancer la commande suivante après avoir construit le jar :
`java -jar -Dprism.forceGPU=true build/libs/Projet-Astra.jar`