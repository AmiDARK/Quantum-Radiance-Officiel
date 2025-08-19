# Quantum Radiance – Personal Device-Farm Server (Java/Appium/MySQL)

> **French below** • CC BY-NC-ND 4.0

Quantum Radiance is a personal, educational project proving you can build a mini device-farm server with modest means: Java/Maven backend, Appium for remote device access, MySQL for users/licenses/devices. It supports user keys, device reservation, and remote test execution. A basic web UI is planned (static pages currently in `target/web/`).  
**Status**: functional core (server + DB + remote test), web console TBD.

## Quick start

**Prereqs**: Java 17+, Node.js, Appium Server, MySQL (XAMPP ok).  
**DB**: import `mySql/qRadiance.sql`, then update your device UDIDs.  
**Build**: `mvn clean install` → `target/radiance-0.1-jar-with-dependencies.jar`  
**Run**: `java -jar target/radiance-0.1-jar-with-dependencies.jar`  
**Demo test**: place test apk at `target/test.apk` (e.g. the Calculator app) and run `src/test/java/com/alcahest/QRadianceClient/radClient.java` as *Java Application*.

> Notes: Pages for the future web console live in `target/web/`. Client→server file transfer is planned.

## Why it matters (portfolio)

- Built a **custom device-farm server** (reservations, user keys) in plain Java.
- **Remote execution** via Appium; **multi-user** device access coordination.
- **MySQL data model** for users/licenses/devices, packaged as importable SQL.
- **Maven fat-jar** packaging → one-line run for demos.

## Roadmap
- Web admin console (devices/users/reservations).
- Optional REST API & auth tokens.
- Appium 2 / Selenium 4 alignment.
- Dockerization (runtime) + compose for DB.

## License
**CC BY-NC-ND 4.0** — attribution, no commercial use, no derivatives. See `Licence.txt`.

---

## 🇫🇷 À propos

Quantum Radiance démontre qu’il est possible de créer **un mini serveur de device-farm** en Java : Appium pour l’accès distant aux devices, **MySQL** pour gérer utilisateurs/licences/devices, **réservations** par *UserKey*, **exécution de tests** à distance. Une **console web** est prévue (maquettes dans `target/web/`).  
**État** : socle fonctionnel OK (serveur + BDD + exécution distante), console web à faire.

### Démarrage rapide

**Prérequis** : Java 17+, Node.js, Appium Server, MySQL.  
**BDD** : import `mySql/qRadiance.sql`, mettez les UDID de vos devices.  
**Build** : `mvn clean install` → JAR exécutable.  
**Run** : `java -jar target/radiance-0.1-jar-with-dependencies.jar`  
**Démo** : placez l’APK de test dans `target/test.apk`, lancez `radClient.java`.

### Points portfolio

- **Serveur type device-farm** développé from scratch (réservations, multi-utilisateur).
- **Intégration Appium** pour exécutions distantes.
- **Schéma MySQL** fourni (import prêt à l’emploi).
- **Packaging Maven** exécutable.

### Licence
**CC BY-NC-ND 4.0** (attribution, pas d’usage commercial, pas d’œuvres dérivées). Voir `Licence.txt`.
