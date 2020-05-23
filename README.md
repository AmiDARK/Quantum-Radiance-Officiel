# Quantum-Radiance-Officiel - A propos de :
Quantum-Radiance est un outil développé pour démontrer qu'il est possible, avec des moyens modestes, de développer un outil similaire aux fermes de devices que l'on trouve un peu partout sur le net.
Il n'a pas été développé dans un but "commercial" mais plutôt dans une recherche singulière et éducationnelle. C'est la première raison pour laquelle la licence Creative Commons BY-NC-ND a été choisie pour l'ouverture de son code source (Pas d'utilisation commerciale).
L'auteur (moi même) ne saurait être tenu pour responsable d'un quelconque problème pourvant survenir lors de son utilisation. Ce produit n'est partagé en code source que pour les objectifs suivants :

# Quantum-Radiance-Officiel - Objectif du projet :
L'objectif de cet outil est principalement de démontrer qu'il est possible, directement en JAVA, d'utiliser un approche différente des approches classiques pour créer un serveur de gestion de devices utilisant Appium pour l'accès à distance, à ceux-ci. Il utilise une base de données mySQl pour gérer les utilisateurs enregistrés, les licences, et les devices disponibles.
Ce système est pleinement fonctionnel. Il permet aussi de développer un serveur Internet pour créer des pages Internet permettant de gérer le système (non développé à l'heure actuelle mais en projet)
De plus, son architecture permet l'intégration facile de nouveaux devices de tests et/ou d'utilisateurs enregistrés avec licences.
Tout comme les fermes de devices, il fonctionne avec un système de clef (UserKey) pour l'accès à un device spécifique, gère les réservations et accès aux devices lorsque plusieurs utilisateurs se connectent au serveur. N'ayant trouvé aucune solution équivalente gratuite ou peu onéreuse, Le choix de licence Creative Commons BY-NC-ND a été choisi en ce sens pour le terme "Attribution". Le terme d'attribution est là pour m'assurer que personne ne s'approprie cette technique et ne tente de la revendre.

# Quantum-Radiance-Officiel - Etat d'avancement du projet :
Ce projet n'est pas terminé mais couvre déjà la gestion des utilisateurs et devices de tests sur base de données mySQL. La connexion au serveur, l'accès au téléphone et l'exécution de tests à distance. Le terme "Pas de modifications" est là pour m'assurer que je puisse mener le projet dans la direction que j'ai prévu initialement sans que d'autres variantes ne viennent perturber l'oeil extérieur. Son utilisation ne devrait se limiter qu'à des tests pour comprendre les principe de fonctionnement (apprentissage du système de réflexions de JAVA).
Pour l'instant l'application doit être localisée dans le dossier du serveur compilé en JAR. Il est prévu d'ajouter le transfert client->Serveur, mais cela n'est pas encore développé.
Le dossier target/web/ contient les pages internet du Serveur WEB.

# Quantum-Radiance-Officiel - Installation :
Il s'agit d'un projet de type MAVEN. Vous devez avoir installé Java, Appium Server et NodeJS. Il a été développé sous l'IDE Eclipse.
Les tests base de donnée mySql ont été réalisés avec l'outil Xampp.
La base de donnée utilisée a été exportée sous le dossier mySql/qRadiance.sql et doit être réinjectée avant tests
Les UDID des deux devices enregistrés ont été supprimés de la base et doivent être mise à jour avec ceux de vos devices de test.
Pour créer le fichier exécutable serveur :
- Clic droit sur le fichier POM.XML, Run As : "Maven Install". LE fichier target/radiance-0.1-jar-with-dependencies.jar sera créé
Pour démarrer le serveur :
- Depuis une ligne de commande dans le dossier target : java -jar radiance-0.1-jar-with-dependencies.jar
Pour exécuter les tests :
- Le fichier APK de test est celui de l'application : https://play.google.com/store/apps/details?id=calculator.innovit.com.calculatrice&gl=FR . Elle doit être extraite en apk avec un outil style ApkExtractor puis placée comme tel dans le projet : target/test.apk
- Pour démarrer le test, exécuter en mode "Java Application" le fichier démonstration : src/test/java/com/alcahest/QRadianceClient/radClient.java

# Quantum-Radiance-Officiel - Licence d'utilisation :
Ce code source est mis à disposition sous les termes de la licence Creative Commons BY-NC-ND
Ce qui signifie que tous les fichiers du projet (à l'exclusion des parties spécifiques aux projets de type MAVEN) sont soumis à la licence Creative Commons BY-NC-ND.
Voici les termes simplifiés :
* Attribution — Vous devez créditer l'Œuvre, intégrer un lien vers la licence et indiquer si des modifications ont été effectuées à l'Oeuvre. Vous devez indiquer ces informations par tous les moyens raisonnables, sans toutefois suggérer que l'Offrant vous soutient ou soutient la façon dont vous avez utilisé son Oeuvre.
* Pas d’Utilisation Commerciale — Vous n'êtes pas autorisé à faire un usage commercial de cette Oeuvre, tout ou partie du matériel la composant.
* Pas de modifications — Dans le cas où vous effectuez un remix, que vous transformez, ou créez à partir du matériel composant l'Oeuvre originale, vous n'êtes pas autorisé à distribuer ou mettre à disposition l'Oeuvre modifiée.
* Pas de restrictions complémentaires — Vous n'êtes pas autorisé à appliquer des conditions légales ou des mesures techniques qui restreindraient légalement autrui à utiliser l'Oeuvre dans les conditions décrites par la licence.
Source : https://creativecommons.org/licenses/by-nc-nd/4.0/deed.fr
La licence complète est présente dans le fichier Licence.txt
Licence Complète disponible ici : https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode.fr
