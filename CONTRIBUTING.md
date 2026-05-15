# Guide d'installation — JDK 21 et Gradle

> Java 21 · JavaFX 21 · JUnit 4

## Prérequis

### JDK 21

Deux options :

**Option 1 : Installation classique**
Installez normalement via votre gestionnaire de paquets ou l'installateur officiel,
si `java -version` affiche déjà `openjdk 21` alors le tour est joué.

**Option 2 : Installation manuelle** (recommandée)
1. Téléchargez l'archive OpenJDK 21 : https://jdk.java.net/archive/ ou
https://adoptium.net/
1. Décompressez l'archive
2. Placez le dossier dans votre projet, l'arborescence des répertoires
doit ressembler à cela.
   ```
   Projet-Astra/
   └── openjdk/
       └── jdk-21.0.x/
   ```

### Gradle

Gradle est embarqué via le Gradle Wrapper. Rien à installer.

---

## Configuration de JAVA_HOME (pour l'installation manuelle)

### Windows

1. Ouvrez les paramètres système avancés (`Win + X` → "Paramètres système avancés")
2. Cliquez sur "Variables d'environnement"
3. Créez une nouvelle variable utilisateur :
   - Nom : `JAVA_HOME`
   - Valeur : le chemin vers votre JDK ( `C:\chemin\vers\Projet-Astra\openjdk\jdk-21.0.*`)
4. Modifiez la variable `PATH` (existante) et ajoutez en début : `%JAVA_HOME%\bin;`
5. Cliquez OK et redémarrez votre terminal

### macOS / Linux

Éditez votre fichier de shell (`~/.bashrc` ou `~/.zshrc` selon votre config) :

Ajoutez à la fin :

```bash
export JAVA_HOME=/chemin/vers/Projet-Astra/jdk/jdk-21.0.*
export PATH=$JAVA_HOME/bin:${PATH}
```

**Sur les machines de ENSEEIHT :**
```bash
export JAVA_HOME=/lib/jvm/java-21-openjdk-amd64/
export PATH=$JAVA_HOME/bin:${PATH}
```

Sauvegardez puis rechargez :
```bash
source ~/.bashrc    # ou ~/.zshrc
```
---

## Points d'attention courants

**"java : commande introuvable"**
- **Vérifiez le chemin JAVA_HOME, il faut veiller à bien modifier `/chemin/vers/` ou `\chemin\vers\` par votre chemin vers le projet, `pwd` dans le dossier courant du projet peut vous aider.**
- Redémarrez complètement votre terminal
- Sur Windows, redémarrez l'ordinateur après les modifications

**Les modifications ne prennent pas effet**
- Fermez et rouvrez le terminal
- Vérifiez que vous avez bien modifié le bon fichier (`~/.bashrc` ou `~/.zshrc`)
- Assurez-vous d'avoir appelé `source ~/.zshrc` après les modifications

**Plusieurs versions de Java installées**
- `which java` sous Linux/macOs ou `where java` sous Windows vous montrera laquelle est utilisée par défaut
- Si ce n'est pas la bonne, utilisez le chemin explicite dans JAVA_HOME au lieu de `$(which java)`

---

## Vérification complète

Une fois configuré :
```bash
java -version      # Doit afficher "openjdk 21" ou "Java 21"
javac -version     # Doit aussi afficher la version 21
```

Si les deux commandes répondent correctement, tout est bon.

## 3. Préparer (Linux/macOS uniquement)

```bash
chmod +x gradlew
```

# Configurer le projet (version Legacy)

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


