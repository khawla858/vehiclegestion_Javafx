#############  Système de Vente de Voitures avec Logs, Cartographie et Monitoring    ########

##🚀  Description du projet

Cette application est un système de gestion de vente de voitures permettant
l’interaction entre **administrateurs, vendeurs et clients**.
Elle intègre la gestion des magasins, des véhicules, des ventes, des rendez-vous
ainsi qu’un système de **journalisation (logs)** basé sur **Elasticsearch et Kibana**.
Une **carte interactive OpenStreetMap** permet d’afficher et de gérer la localisation
des magasins.

---

##🚀  Objectifs du projet
- Centraliser la gestion des ventes de véhicules
- Offrir une interface adaptée à chaque rôle utilisateur
- Assurer la traçabilité des actions via les logs
- Visualiser les magasins sur une carte interactive
- Améliorer la supervision et l’analyse du système

---

##🚀  Rôles et fonctionnalités

#### Admin
- Gérer les utilisateurs (activation / blocage)
- Accepter ou refuser les demandes de création de magasins
- Consulter et analyser les logs via Kibana
- Superviser l’activité globale de l’application

#### Vendeur
- Créer et gérer ses magasins
- Localiser les magasins sur la carte OpenStreetMap
- Ajouter, modifier et supprimer des véhicules
- Effectuer des ventes de véhicules
- Gérer les rendez-vous clients (accepter / refuser)
- Consulter l’historique des ventes et rendez-vous
- Communiquer avec les clients via un système de messagerie chat
- Générer automatiquement des logs pour chaque action

#### Client
- Consulter les véhicules disponibles
- Visualiser les magasins sur la carte
- Demander des rendez-vous
- Communiquer avec les vendeurs
- Consulter son historique de rendez-vous

---

##🚀  Cartographie des magasins
- Intégration de **OpenStreetMap**
- Affichage des magasins avec marqueurs géographiques
- Sélection et enregistrement de la position GPS du magasin
- Interaction directe avec la carte

---

##🚀  Système de logs et monitoring
- Génération automatique des logs pour chaque action utilisateur
- Envoi des logs vers **Elasticsearch**
- Visualisation, recherche et analyse des logs via **Kibana**
- Traçabilité des actions (vente, ajout véhicule, rendez-vous, connexion…)

---

##🚀  Technologies utilisées

### 🔹 Langages & Frameworks
- Java
- JavaFX

### 🔹 Base de données
- PostgreSQL

### 🔹 Logs & Monitoring
- Elasticsearch
- Kibana

### 🔹 Cartographie
- OpenStreetMap

### 🔹 Outils
- IntelliJ IDEA / Eclipse
- Maven
- Git

---

##🚀 Structure du projet
C:.
├───.idea
│   └───libraries
├───images
│   ├───articles
│   └───logos
├───logs
├───src
│   └───main
│       ├───java
│       │   └───com
│       │       └───example
│       │           └───vehiclegestion
│       │               ├───admin
│       │               │   ├───controller
│       │               │   ├───dao
│       │               │   └───service
│       │               ├───auth
│       │               │   ├───controller
│       │               │   ├───dao
│       │               │   ├───model
│       │               │   ├───service
│       │               │   └───utils
│       │               ├───client
│       │               │   ├───controller
│       │               │   ├───doa
│       │               │   └───model
│       │               ├───common
│       │               │   ├───controller
│       │               │   ├───dao
│       │               │   ├───model
│       │               │   ├───service
│       │               │   └───utils
│       │               ├───elastic
│       │               ├───exception
│       │               ├───logging
│       │               │   ├───config
│       │               │   ├───model
│       │               │   ├───service
│       │               │   └───util
│       │               ├───utils
│       │               └───vendeur
│       │                   ├───controller
│       │                   │   └───layout
│       │                   ├───dao
│       │                   ├───model
│       │                   ├───services
│       │                   └───util
│       └───resources
│           ├───com
│           │   └───example
│           │       └───vehiclegestion
│           │           ├───css
│           │           └───images
│           │               ├───icons
│           │               └───vehicules
│           └───view
│               ├───admin
│               ├───auth
│               ├───client
│               ├───common
│               ├───layout
│               └───vendeur
│                   └───layout
├───target
│   ├───classes
│   │   ├───com
│   │   │   └───example
│   │   │       └───vehiclegestion
│   │   │           ├───admin
│   │   │           │   ├───controller
│   │   │           │   ├───dao
│   │   │           │   └───service
│   │   │           ├───auth
│   │   │           │   ├───controller
│   │   │           │   ├───dao
│   │   │           │   ├───model
│   │   │           │   ├───service
│   │   │           │   └───utils
│   │   │           ├───client
│   │   │           │   ├───controller
│   │   │           │   ├───doa
│   │   │           │   └───model
│   │   │           ├───common
│   │   │           │   ├───controller
│   │   │           │   ├───dao
│   │   │           │   ├───model
│   │   │           │   ├───service
│   │   │           │   └───utils
│   │   │           ├───css
│   │   │           ├───images
│   │   │           │   └───icons
│   │   │           ├───logging
│   │   │           │   ├───model
│   │   │           │   ├───service
│   │   │           │   └───util
│   │   │           ├───utils
│   │   │           └───vendeur
│   │   │               ├───controller
│   │   │               │   └───layout
│   │   │               ├───dao
│   │   │               ├───model
│   │   │               └───util
│   │   └───view
│   │       ├───admin
│   │       ├───auth
│   │       ├───client
│   │       ├───common
│   │       └───vendeur
│   │           └───layout
│   └───generated-sources
│       └───annotations
└───uploads
└───vehicles

---

##🚀  Installation et exécution

## Dépendances et outils requis

- Java JDK 11 ou supérieur
- JavaFX
- PostgreSQL
- Elasticsearch
- Kibana
- Connexion Internet (OpenStreetMap)

---

##  Installation rapide des dépendances

###  Java & JavaFX
- Installer le JDK 11+
- JavaFX est utilisé pour l’interface graphique

###  PostgreSQL
- Installer PostgreSQL
- Créer une base de données
- Exécuter le script SQL fourni (`schema_base.sql`)

###  Elasticsearch et Kibana
- Télécharger Elasticsearch
- Lancer Elasticsearch
- Télécharger et lancer Kibana
- Accéder à Kibana via le navigateur

---

##  Lancement du projet

1. Importer le projet dans IntelliJ IDEA ou Eclipse
2. Vérifier la configuration de la base de données
3. Vérifier la configuration des logs
4. Lancer la classe principale Java
5. Se connecter avec un compte de test

---
---

##  Comptes de test
🔹Admin
email : nada@gmail.com
mot de passe : 12345678
🔹Vendeur
email : aamer@gmail.com
mot de passe :12345678
🔹Client
email : client@test.com
mot de passe : 1234568

---

##🚀  Remarques importantes
- Les logs sont générés automatiquement en temps réel
- Les actions utilisateurs sont consultables dans Kibana
- La carte OpenStreetMap nécessite une connexion Internet
- Le système est basé sur une gestion de rôles sécurisée

---

##🚀  Perspectives d’évolution
- Développement d’une application mobile
- Carte map affiché pour les magasin recherchés
- Paiement en ligne
- Tableaux de bord statistiques avancés
- Système de recommandation de véhicules

---

