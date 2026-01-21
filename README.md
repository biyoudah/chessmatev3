# ♟️ ChessMate - Plateforme Interactive d'Échecs

**ChessMate** est une application web complète développée avec **Spring Boot 3**. Elle propose une expérience hybride mêlant résolution de problèmes combinatoires (Placement) et entraînement tactique (Puzzles) basé sur des situations réelles.

---

## 🚀 Fonctionnalités Clés

### 🧩 1. Mode « Placement » (Logique)
* **Défis Classiques :** Résolvez les problèmes des 8 Dames, 8 Tours, 14 Fous et 16 Rois.
* **Mode Personnalisé :** Configurez votre propre défi (ex: placer 4 Cavaliers et 2 Dames).
* **Moteur de Menaces :** Calcul en temps réel. Une pièce ne peut être posée si elle est menacée ou si elle menace une pièce déjà présente.
* **Système "Perfect" :** Bonus de points accordé si le puzzle est résolu sans aucune erreur et sans retirer de pièces.

### 🎯 2. Mode « Puzzle » (Tactique)
* **Données Réelles :** Puzzles chargés via des chaînes FEN issus de parties historiques.
* **IA de Réponse :** Le serveur joue automatiquement le coup adverse (séquence UCI) après chaque bon coup du joueur.
* **Indices (Hints) :** Possibilité de visualiser la case de départ du coup attendu pour se débloquer.
* **Difficulté Dynamique :** Choix entre Facile (Mat en 1-2 coups), Moyen (3-4) et Difficile (5+).

### 🏆 3. Progression & Social
* **Classements Globaux :** Tableaux de bord filtrables par mode (Global, Puzzle, Placement) et par schéma spécifique.
* **Système de Trophées :** 6 trophées à débloquer (ex: *MaitreDesDames*, *CavalierDuTemps* pour 1h de jeu, *ChessMate* pour l'excellence).
* **Dashboard Dynamique :** Intégration d'une API externe pour les articles récents et les tournois d'échecs en direct.

---

## 🛠️ Stack Technique

* **Backend :** Java 17, Spring Boot 3, Spring Security (Gestion des rôles ADMIN/USER).
* **Persistance :** Spring Data JPA, Hibernate, MySQL.
* **Frontend :** Thymeleaf, JavaScript (Fetch API pour le mode sans rechargement), CSS3 (Animations Animate.css).
* **Sécurité :** BCrypt pour les mots de passe, protection CSRF, et `CustomAuthenticationSuccessHandler` pour la redirection après connexion vers le jeu en cours.

---

## ⚙️ Détails de l'Architecture

### 1. Logique Métier (Moteur)
L'application utilise une hiérarchie de classes pour la gestion du jeu :
* `AbstractChessGame` : Gère le plateau, le parsing FEN et le placement des pièces.
* `JeuPlacement` : Implémente la logique de collision géométrique ($dx$ et $dy$).
* `JeuPuzzle` : Gère la séquence de coups UCI et l'alternance Joueur/Ordinateur.

### 2. Communication AJAX
Toutes les actions de jeu (placer, retirer, coup tactique) passent par des requêtes `POST` asynchrones. Le serveur renvoie des fragments de vue ou des états, permettant de mettre à jour l'échiquier sans recharger la page.

### 3. Persistance des scores
Le système utilise une `schemaKey` unique pour chaque configuration de puzzle. Cela permet de :
* Calculer des classements par défi spécifique.
* Empêcher un utilisateur de gagner des points plusieurs fois sur le même problème.

---

## 📦 Installation et Lancement

### Prérequis
* JDK 17+
* Maven 3.8+
* Une base de données MySQL

### Étapes
1.  **Cloner le projet :**
    ```bash
    git clone https://gitlab.univ-lorraine.fr/coll/l-inp/polytech/ia2r-fise-promo-2027/pierre-ludmann-chessmate
    cd pierre-ludmann-chessmate
    ```
2.  **Configurer la base de données :**
    Modifiez `src/main/resources/application.properties` avec vos identifiants.
3.  **Lancer l'application :**
    ```bash
    mvn spring-boot:run
    ```
4.  **Accès :**
    Rendez-vous sur `http://localhost:8080`.

---

## 📖 Comment Jouer ?

1.  **Placement :** Sélectionnez une pièce dans la barre latérale, choisissez sa couleur, et cliquez sur l'échiquier.
2.  **Puzzle :** Effectuez votre coup. Si le message "Mauvais coup" s'affiche, réessayez. Utilisez le bouton "Indice" si vous êtes bloqué.
3.  **Compte :** Inscrivez-vous pour enregistrer vos points, débloquer des trophées et apparaître dans le classement mondial.

---

### 👥 Équipe de Développement
Développé à l'Université de Lorraine par :  
**DI LORETO, DODIN, OUADAH, TULASNE, SIERENS & ZILBERBERG**