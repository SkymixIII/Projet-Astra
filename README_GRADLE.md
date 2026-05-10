# Mise en place du projet

> Java 21 · JavaFX 21 · JUnit 4

---

## 1. Prérequis

- **JDK 21** installé — `java -version` doit afficher `openjdk 21`
- **Gradle n'a pas besoin d'être installé**, le projet embarque un wrapper

---

## 2. Définir JAVA_HOME

**Linux / macOS** — dans `~/.bashrc` ou `~/.zshrc` :
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
source ~/.bashrc
```

**Windows** — dans les variables d'environnement système :
```
JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-21.x.x.x-hotspot
PATH      = %JAVA_HOME%\bin;%PATH%
```

---

## 3. Préparer (Linux/macOS uniquement)

```bash
chmod +x gradlew
```

---

## 4. Commandes essentielles

> Sur Windows, remplacer `./gradlew` par `gradlew`

| Commande | Description |
|----------|-------------|
| `./gradlew run` | Lance l'application |
| `./gradlew test` | Exécute tous les tests |
| `./gradlew build` | Compile, teste, produit le JAR |
| `./gradlew clean` | Supprime le dossier `build/` |
| `./gradlew clean build` | Recompile tout from scratch |
| `./gradlew installDist` | Produit une application fonctionelle dans `build/install/` |

Les rapports de test sont dans `build/reports/tests/test/index.html`.

---

## 5. Structure du projet

```
src/main/java/   ← code de production
src/test/java/   ← tests (même package que la classe testée)
build.gradle     ← configuration du build
gradlew          ← wrapper Gradle, ne pas supprimer
```

---

## 6. Erreurs fréquentes

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
