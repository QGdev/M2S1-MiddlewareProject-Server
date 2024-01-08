# Markdown Collaborative Editor

## M2S1 - Middleware Project - Server

Ce projet fait partie du cours Middleware de la deuxième année du Master ALMA de l'Université de Nantes.

### Membres du groupe
- [Matthéo Lécrivain](https://github.com/MattheoLec)
- [Nathan Deshayes](https://github.com/nathan-art)
- [Quentin Gomes Dos Reis](https://github.com/QGdev)
- [Rodrigue Meunier](https://github.com/Rod4401)

## Description du projet

Le but du projet est de construire une application, nous avons choisi de faire un éditeur collaboratif Markdown.
Ce projet est réparti dans deux dépôts github, celui-ci contient la partie serveur du projet, et un autre dépôt contient la partie client (voir [M2S1-MiddlewareProject-Client](https://github.com/QGdev/M2S1-MiddlewareProject-Client)).

## Index

- [**Fonctionnement**](#fonctionnement)
  - [Notre API REST](#notre-api-rest)
  - [Websockets](#websockets)
    - [Comment utilisons-nous les websockets ?](#comment-utilisons-nous-les-websockets-)
    - [Comment s'authentifier ?](#comment-sauthentifier-)
    - [Comment transmettre les modifications apportées aux documents ?](#comment-transmettre-les-modifications-apportées-aux-documents-)
    - [Gestion de la déconnexion des utilisateurs](#gestion-de-la-déconnexion-des-utilisateurs)
    - [Représentation du document en mémoire](#représentation-du-document-en-mémoire)
    - [Gestion de la concurrence](#gestion-de-la-concurrence)
    - [La représentation côté client](#la-représentation-côté-client)
- [**Limites et améliorations**](#limites-et-améliorations)
  - [Limites](#limites)
  - [Améliorations](#améliorations)
- [**Configuration et exécution du projet**](#configuration-et-exécution-du-projet)
    - [Docker](#docker)
        - [Cloner le dépôt du serveur](#cloner-le-dépôt-du-serveur)
        - [Build de l'image Docker](#build-de-limage-docker)
        - [Exécution du projet](#exécution-du-projet)
    - [Compilation et exécution sans Docker](#compilation-et-exécution-sans-docker)
        - [Prérequis](#prérequis)
        - [Cloner les dépôts](#cloner-les-dépôts)
        - [Compilation du projet](#compilation-du-projet)
        - [Exécution du projet](#exécution-du-projet)

## Fonctionnement

Notre projet utilise une structure client-serveur basique. Nous utilisons une API REST simple couplée à des websockets pour gérer la communication bidirectionnelle entre le serveur et les clients.

### Notre API REST

Une documentation swagger est disponible à l'adresse [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) mais est accessible que lorsque le serveur est lancé.\
Nous allons donc faire une présentation basique de notre API REST, mais nous vous encourageons à utiliser la documentation swagger pour plus de détails.

| Méthode  | Chemin d'accès | Description                                                                                  |
|----------|----------------|----------------------------------------------------------------------------------------------|
| **POST** | /api/create    | Création d'un nouveau document à partir d'un nom d'utilisateur et d'un nom de document.      |
| **POST** | /api/join      | Ajout d'un utilisateur à un document à partir d'un nom d'utilisateur et d'un id de document. |

Si l'appel à l'API réussi, vous obtiendrez une réponse JSON contenant des informations sur le document et l'utilisateur, dont notamment l'identifiant du document (UUID) et l'identifiant de l'utilisateur (UUID) que vous devrez utiliser pour vous authentifier auprès du websocket.

### Websockets

#### Comment utilisons-nous les websockets ?

Notre websocket est un canal de communication bidirectionnel entre le serveur et le client.
Chaque client en ouvre un avec le serveur et ne communiquera qu'à travers lui. Il n'y a pas de communication entre les clients, tout se fait par l'intermédiaire du serveur.
Avant d'utiliser le websocket, vous devrez vous authentifier, sinon vous serez expulsé.

#### Comment s'authentifier ?

Pour vous authentifier auprès du websocket, vous devez envoyer un message JSON avec la structure suivante :

```json
{
  "type": "CONNECT",
  "docId": "00000000-0000-0000-0000-000000000000",
  "userId": "00000000-0000-0000-0000-000000000000"
}
```

Le serveur a lié votre précédente demande `create` ou `join` à votre session websocket, vous n'avez donc pas besoin d'envoyer à nouveau votre nom d'utilisateur ou votre nom de document.\
Ensuite, vous recevrez un message du serveur de la forme suivante :

```json
{
  "type": "CONNECT",
  "message": "Connected",
  "userId": "00000000-0000-0000-0000-000000000000",
  "docName": "My super document",
  "content": "Hello World !\n This is a test !"
}
```

Et chaque utilisateur connecté au document recevra un message de la forme :

```json
{
  "type": "CONNECT",
  "userId": "00000000-0000-0000-0000-000000000000",
  "userName": "George Abitbol",
  "users": [
    {
      "userId": "00000000-0000-0000-0000-000000000000",
      "userName": "George Abitbol"
    },
    {
      "userId": "00000000-0000-0000-0000-000000000001",
      "userName": "Chuck Norris"
    },
    {
      "userId": "00000000-0000-0000-0000-000000000002",
      "userName": "Bruce Lee"
    }
  ]
}
```

Une fois connecté, vous pourrez envoyer des messages au serveur, qui les diffusera à tous les utilisateurs connectés au document.
Sans cette étape d'authentification, vous ne recevrez aucun message du serveur et vous ne pourrez pas envoyer de message au serveur sans être expulsé.

> ***Note:*** Grâce au lien entre la session et l'identifiant de l'utilisateur que nous avons fait lors de l'étape d'authentification, nous serons en mesure d'avoir une modification de document sécurisée.
> Le serveur vérifie si l'identifiant de l'utilisateur du message est le même que celui de la session websocket.
> Ainsi, si un *utilisateurA* essaie d'effectuer une modification en falsifiant l'identifiant de *utilisateurB*, le serveur l'expulsera.

#### Comment transmettre les modifications apportées aux documents ?

Pour transmettre les modifications apportées au document via le websocket, nous utilisons un ensemble de messages pour différentes actions.
Chacune d'entre elles est envoyée par un client au serveur et sera diffusée à chaque utilisateur connecté au document si l'opération est réussie.

Voici la liste des messages :

| Type de message                                   | Description                                                           |
|---------------------------------------------------|-----------------------------------------------------------------------|
| [INSERT_CHAR](#insertion-de-caractère)            | Insère un caractère à une position spécifique dans le document.       |
| [DELETE_CHAR](#suppression-de-caractère)          | Supprime un caractère à une position spécifique dans le document.     |
| [INSERT_LINE_BRK](#insertion-de-saut-de-ligne)    | Insère un saut de ligne à une position spécifique dans le document.   |
| [DELETE_LINE_BRK](#suppression-de-saut-de-ligne)  | Supprime un saut de ligne à une position spécifique dans le document. |
| [CHANGE_DOC_NAME](#changement-de-nom-de-document) | Change le nom du document.                                            |

Voici les structures des messages :

##### Insertion de caractère

```json
{
  "type": "INSERT_CHAR",
  "lineIdx": 0,
  "columnIdx": 0,
  "char": "a",
  "userId": "00000000-0000-0000-0000-000000000000"
}
```

Ce message insérera le caractère "a" au début du document.
De ```Hello World !``` à ```aHello World !```

##### Suppression de caractère

```json
{
  "type": "DELETE_CHAR",
  "lineIdx": 0,
  "columnIdx": 0,
  "userId": "00000000-0000-0000-0000-000000000000"
}
```

Ce message supprimera le premier caractère du document.
De ```Hello World !``` à ```ello World !```

##### Insertion de saut de ligne

```json
{
  "type": "INSERT_LINE_BRK",
  "lineIdx": 0,
  "columnIdx": 0,
  "userId": "00000000-0000-0000-0000-000000000000"
}
```

Ce message insérera un saut de ligne au début du document.
De ```Hello World !``` à ```\nHello World !```


##### Suppression de saut de ligne

```json
{
    "type": "DELETE_LINE_BRK",
    "lineIdx": 0,
    "userId": "00000000-0000-0000-0000-000000000000"
}
```

Ce message supprimera le premier saut de ligne du document.
De ```Hello World !\nThis is a test !``` à ```Hello World !This is a test !```

##### Changement de nom de document

```json
{
  "type": "CHANGE_DOC_NAME",
  "newName": "My new document name",
  "userId": "00000000-0000-0000-0000-000000000000"
}
```

Ce message changera le nom du document pour "My new document name".

### Gestion de la déconnexion des utilisateurs

Lorsqu'un utilisateur se déconnecte du document, le serveur envoie un message à tous les utilisateurs connectés au document avec la structure suivante :

```json
{
  "type": "DISCONNECT",
  "userId": "00000000-0000-0000-0000-000000000000"
}
```

### Représentation du document en mémoire

Pour représenter le document en mémoire, il suffit de se demander "**Qu'est-ce qui compose la structure d'un document texte ?**".
Nous pouvons décomposer un document texte en lignes, et les lignes en colonnes.
Ainsi, il suffit de représenter un document comme une liste de lignes, et une ligne comme une liste de colonnes.

Nous avons donc une représentation du document en mémoire qui ressemble à ceci :

#### ColumnNode

```java
public class ColumnNode {
    private final AtomicReference<ColumnNode> next;
    private final AtomicReference<ColumnNode> previous;
    private final AtomicReference<LineNode> parent;
    private final AtomicReference<Character> character;

    // Basic insert, delete, getters and setters methods
}
```
Il s'agit de la représentation d'une colonne dans une ligne, qui contient une référence à la colonne suivante et à la colonne précédente, une référence à la ligne parente et au caractère qu'elle contient.
Nous avons en fait créé une liste de colonnes doublement chaînée avec une référence à la ligne parente et au caractère qu'elle contient.

#### LineNode

```java
public class LineNode {
    private final AtomicReference<LineNode> next;
    private final AtomicReference<LineNode> previous;
    private final AtomicReference<ColumnNode> content;
    
    // Basic insert, delete, getters and setters methods
}
```
Notre ligne est une liste de colonnes, elle contient donc une référence à la ligne suivante et à la ligne précédente, ainsi qu'une référence à la première colonne de la ligne.
C'est une liste de lignes doublement chaînée avec une référence à la première colonne de la ligne.

#### Document

```java
public class Document {
    private final UUID uuid;
    private String name;
    private final ConcurrentHashMap<UUID, User> joiningUsers = new ConcurrentHashMap<UUID, User>();
    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<UUID, User>();
    private final AtomicInteger lineCount = new AtomicInteger(1);

    private final LineNode content;
    
    // Basic insert, delete, getters and setters methods
}
```

Le document est une structure de données qui contient :
- **uuid** : L'identifiant du document nécessaire pour joindre le document
- **name** : Le nom du document
- **joiningUsers** : Les utilisateurs en attente d'authentification au websocket.
- **users** : Les utilisateurs authentifiés au websocket.
- **lineCount** : Le nombre de lignes du document.
- **content** : La première ligne du document.

C'est donc avec ces structures de données que nous représentons le document en mémoire.

#### Gestion de la concurrence

Pour gérer la concurrence, nous utilisons la classe ```AtomicReference``` et le mot-clé ```synchronized``` de Java.
Ainsi, chaque section critique est synchronisée, et chaque structure de données qui peut être modifiée par plusieurs threads est une ```AtomicReference```.

#### La représentation côté client

Pour en savoir plus sur le côté client et sur la manière dont tout est géré, vous pouvez regarder le README du dépôt [M2S1-MiddlewareProject-Client](https://github.com/QGdev/M2S1-MiddlewareProject-Client).

## Limites et améliorations

Notre projet n'est pas parfait, c'est un projet universitaire, nous avons donc des limites et des améliorations possibles:

### Limites

- Dans certains cas, en cas de forte concurrence, le document peut être corrompu, ce qui pose des problèmes de synchronisation.
- Il n'y a pas vraiment de sécurité, nous n'utilisons pas de chiffrement (ce n'était pas l'objectif premier de ce projet).
- Il s'agit d'une implémentation basique, il nous manque donc beaucoup de fonctionnalités comme :
  - Le traitement massif des copier/coller
  - Le traitement de la sélection/suppression massive
  - Le traitement des undo/redo
  - etc...

### Améliorations

- Des deux côtés, client et serveur, nous pouvons améliorer la gestion de la concurrence en implémentant des algorithmes tels qu'un algorithme d'horloge ou un algorithme d'horloge vectorielle.
- Nous pouvons améliorer la sécurité en implémentant des algorithmes de chiffrement.
- Nous pouvons améliorer l'implémentation des fonctionnalités manquantes énumérées dans la section "Limites".

## Configuration et exécution du projet

Pour exécuter ce projet, vous avez trois options :

| Option                          | Description                                                                                 | Prérequis                     |
|---------------------------------|---------------------------------------------------------------------------------------------|-------------------------------|
| [**Docker**](#docker)           | Utiliser Docker pour exécuter le projet                                                     | Docker                        |
| [**Compilation**](#compilation) | Compiler le projet en utilisant Maven et NodeJS                                             | Java **21** (JDK), Maven, NPM |
| [**Exécution**](#exécution)     | Exécuter le projet en utilisant le fichier JAR disponible dans la section release de GitHub | Java 21                       |

### Docker

Pour exécuter ce projet avec Docker, vous devez avoir Docker installé sur votre ordinateur.

#### Cloner le dépôt du serveur

Tout d'abord, clonez ce dépôt dans un dossier vide.

Dépôt serveur : [github.com/QGdev/M2S1-MiddlewareProject-Server](https://github.com/QGdev/M2S1-MiddlewareProject-Server)

```sh
> cd your_folder
> git clone https://github.com/QGdev/M2S1-MiddlewareProject-Server.git
> ls
M2S1-MiddlewareProject-Server
```

#### Lancement de l'image Docker

> ***ATTENTION:*** Il est préférable de ne pas se situer sur le réseau informatique de Nantes Université, puisqu'il faudrait insérer la configuration Maven pour le proxy dans le conteneur de build du JAR.
> Sans quoi, le build du JAR échouera, car Maven ne pourra pas télécharger les dépendances.

Ensuite, nous allons build l'image Docker du serveur, pour cela, nous allons utiliser le Dockerfile et un docker-compose.yml est disponible à la racine du dépôt Serveur.
Nous allons utiliser docker-compose pour build l'image et lancer le conteneur Docker.
Il suffit d'exécuter la commande suivante :

```sh
> pwd
your_folder/M2S1-MiddlewareProject-Server
> docker-compose up --build
```

> ***Note:*** Le build de l'image Docker peut prendre un certain temps, car il doit télécharger toutes les dépendances Maven ainsi que NodeJS et NPM.
> ***Note:*** Si vous voulez exécuter le projet en arrière-plan, ajoutez l'option ```-d``` à la commande ```docker-compose up```.

Normalement, tout devrait s'être exécuté sans erreurs, et nous devrions avoir :
```shell
> docker compose up                  
Attaching to middlewareserver-app-1
middlewareserver-app-1  |    ___     _ _      _                  _   _         ___    _ _ _           
middlewareserver-app-1  |   / __|___| | |__ _| |__  ___ _ _ __ _| |_(_)_ _____| __|__| (_) |_ ___ _ _ 
middlewareserver-app-1  |  | (__/ _ \ | / _` | '_ \/ _ \ '_/ _` |  _| \ V / -_) _|/ _` | |  _/ _ \ '_|
middlewareserver-app-1  |   \___\___/_|_\__,_|_.__/\___/_| \__,_|\__|_|\_/\___|___\__,_|_|\__\___/_|                                          
middlewareserver-app-1  | Powered by Spring Boot 3.2.1
middlewareserver-app-1  | 
middlewareserver-app-1  | MIDDLEWARE PROJECT - MASTER 2 ALMA - 2023-2024
middlewareserver-app-1  |     Developped by :
middlewareserver-app-1  |         - Matthéo Lécrivain
middlewareserver-app-1  |         - Nathan Deshayes
middlewareserver-app-1  |         - Quentin Gomes Dos Reis
middlewareserver-app-1  |         - Rodrigue Meunier
middlewareserver-app-1  | 
middlewareserver-app-1  | 2024-01-06T11:04:14.382Z  INFO 7 --- [           main] fr.univnantes.Application                : Starting Application using Java 21.0.1 with PID 7 (/usr/app.jar started by root in /)
middlewareserver-app-1  | 2024-01-06T11:04:14.387Z  INFO 7 --- [           main] fr.univnantes.Application                : No active profile set, falling back to 1 default profile: "default"
middlewareserver-app-1  | 2024-01-06T11:04:15.408Z  INFO 7 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
middlewareserver-app-1  | 2024-01-06T11:04:15.418Z  INFO 7 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
middlewareserver-app-1  | 2024-01-06T11:04:15.419Z  INFO 7 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.17]
middlewareserver-app-1  | 2024-01-06T11:04:15.456Z  INFO 7 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
middlewareserver-app-1  | 2024-01-06T11:04:15.457Z  INFO 7 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1013 ms
middlewareserver-app-1  | 2024-01-06T11:04:15.685Z  INFO 7 --- [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page: class path resource [static/web/index.html]
middlewareserver-app-1  | 2024-01-06T11:04:15.995Z  INFO 7 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
middlewareserver-app-1  | 2024-01-06T11:04:16.021Z  INFO 7 --- [           main] fr.univnantes.Application                : Started Application in 1.949 seconds (process running for 2.379)
```

SI tout s'est bien passé, vous pouvez maintenant tester le projet en ouvrant un navigateur et en tapant :
- [http://localhost:8080](http://localhost:8080)
- ```http://[YOUR_COMPUTER_IP]:8080``` (peut ne pas fonctionner en fonction des paramètres de votre pare-feu)

#### Démontage de l'image Docker

Pour démonter l'image Docker, il suffit d'exécuter la commande suivante :

```sh
> pwd
your_folder/M2S1-MiddlewareProject-Server
> docker-compose down
```

### Compilation et exécution sans Docker

#### Prérequis

Avant de pouvoir compiler et exécuter ce projet, vous devrez avoir ces outils installés sur votre ordinateur :
- Java **21** (JDK)
- Maven
- NodeJS
- NPM

> ***Note:*** Sans ces outils, vous ne pourrez pas compiler et exécuter ce projet.

####  Cloner les dépôts

Tout d'abord, clonez ce dépôt et le dépôt client dans un dossier vide.

Dépôt client : [github.com/QGdev/M2S1-MiddlewareProject-Client](https://github.com/QGdev/M2S1-MiddlewareProject-Client)\
Dépôt serveur : [github.com/QGdev/M2S1-MiddlewareProject-Server](https://github.com/QGdev/M2S1-MiddlewareProject-Server)

```sh
> cd your_folder
> git clone https://github.com/QGdev/M2S1-MiddlewareProject-Client.git
> git clone https://github.com/QGdev/M2S1-MiddlewareProject-Server.git
> ls
M2S1-MiddlewareProject-Client  M2S1-MiddlewareProject-Server
```

Vous obtiendrez deux dossiers :
- M2S1-MiddlewareProject-Client
- M2S1-MiddlewareProject-Server

#### Compilation du projet

Pour compiler ce projet, nous devons commencer par la partie front-end :

##### Partie Front-End

```sh
> pwd
your_folder
> cd M2S1-MiddlewareProject-Client
> npm install --legacy-peer-deps
> npm run build
```

Normalement, tout devrait s'être exécuté sans erreurs, et nous devrions avoir :

**Les assets**
```sh
> pwd
your_folder/M2S1-MiddlewareProject-Client
> ls .svelte-kit/output/client
_app  favicon.png
```

**La page web**
```sh
> pwd
your_folder/M2S1-MiddlewareProject-Client
> ls .svelte-kit/output/prerendered/pages
index.html
```

##### Partie Back-End

Maintenant que la partie front-end est compilée, nous allons devoir l'intégrer dans nos fichiers de ressources :

```sh
> pwd
your_folder
> cp -r M2S1-MiddlewareProject-Client/.svelte-kit/output/prerendered/pages/* M2S1-MiddlewareProject-Server/src/main/resources/static/web/
> cp -r M2S1-MiddlewareProject-Client/.svelte-kit/output/client/* M2S1-MiddlewareProject-Server/src/main/resources/static/web/
> ls M2S1-MiddlewareProject-Server/src/main/resources/static/web/
_app  docs favicon.png  index.html
```

Et il nous reste à build le back-end :

> ***ATTENTION:*** La version de JDK requise est la 21 mais même si elle est définie sur JDK21, Maven peut ne pas la prendre comme il le devrait...
> Si vous avez des problèmes avec Maven et la compilation, un fichier JAR est disponible dans la section release de GitHub.

```sh
> pwd
your_folder
> cd M2S1-MiddlewareProject-Server
> mvn package -f pom.xml
```

### Exécution du projet

Vous pouvez maintenant exécuter le fichier JAR compilé dans ```M2S1-MiddlewareProject-Server/target/MiddlewareServer-1.0-SNAPSHOT.jar```, ou utiliser celui disponible dans la section release de GitHub :
```sh
> java -jar MiddlewareServer-1.0-SNAPSHOT.jar
```

Normalement, tout devrait s'être exécuté sans erreurs, et nous devrions avoir :
```shell
> java -jar MiddlewareServer-1.0-SNAPSHOT.jar
   ___     _ _      _                  _   _         ___    _ _ _           
  / __|___| | |__ _| |__  ___ _ _ __ _| |_(_)_ _____| __|__| (_) |_ ___ _ _ 
 | (__/ _ \ | / _` | '_ \/ _ \ '_/ _` |  _| \ V / -_) _|/ _` | |  _/ _ \ '_|
  \___\___/_|_\__,_|_.__/\___/_| \__,_|\__|_|\_/\___|___\__,_|_|\__\___/_|                                          
Powered by Spring Boot 3.2.1

MIDDLEWARE PROJECT - MASTER 2 ALMA - 2023-2024
    Developped by :
        - Matthéo Lécrivain
        - Nathan Deshayes
        - Quentin Gomes Dos Reis
        - Rodrigue Meunier

2024-01-06T12:13:02.093+01:00  INFO 15998 --- [           main] fr.univnantes.Application                : Starting Application using Java 21.0.1 with PID 15998 (/home/gomes/IdeaProjects/MiddlewareServer/target/MiddlewareServer-1.0-SNAPSHOT.jar started by gomes in /home/gomes/IdeaProjects/MiddlewareServer)
2024-01-06T12:13:02.097+01:00  INFO 15998 --- [           main] fr.univnantes.Application                : No active profile set, falling back to 1 default profile: "default"
2024-01-06T12:13:03.211+01:00  INFO 15998 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-01-06T12:13:03.221+01:00  INFO 15998 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-01-06T12:13:03.221+01:00  INFO 15998 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.17]
2024-01-06T12:13:03.255+01:00  INFO 15998 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-01-06T12:13:03.255+01:00  INFO 15998 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1114 ms
2024-01-06T12:13:03.473+01:00  INFO 15998 --- [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page: class path resource [static/web/index.html]
2024-01-06T12:13:03.778+01:00  INFO 15998 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-01-06T12:13:03.797+01:00  INFO 15998 --- [           main] fr.univnantes.Application                : Started Application in 2.056 seconds (process running for 2.475)
```

Si tout s'est bien passé, vous pouvez maintenant tester le projet en ouvrant un navigateur et en tapant
- [http://localhost:8080](http://localhost:8080)
- Ou ```http://[YOUR_COMPUTER_IP]:8080``` (peut ne pas fonctionner en fonction des paramètres de votre pare-feu)
