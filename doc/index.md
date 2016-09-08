---
date: 2016-06-16T15:21:22+02:00
title: Zusammenfassung
menu: main
weight: 0
---

## Der Elexis Ungrad Server

### Ausgangslage

Die Entwicklung von Komponenten wie Lucinda und Webelexis, sowie das Redesign des Artikelkonzepts führten zur 
 Erkenntnis, dass sich manche Aufgaben besser mit einer Serverkomponente lösen lassen. Es zeigte sich dann auch, dass es 
 mühsam ist, bei jedem Neustart des Systems eine Anzahl von Serverkomponenten zu starten und zu überwachen. Selbst
 wenn dies automatisch z.B. per Startskript geschieht, kann doch jedesmal etwas schiefgehen, und man muss auch immer an alle 
 Komponenten denken. Dasselbe gilt, wenn man das System auf neue Hardware migriert.
 
### Konzept 

Aus dieser Problemstellung entwickelte sich die Idee, serverseitig ein ähnliches Plugin-Konzept einzusetzen, wie clientseitig:
Der Anwender kann wählen, welche Komponenten er nutzen möchte, und das System sorgt dafür, dass die Komponenten kooperieren können.

Anstatt nun einfach ein weiteres Eclipse RCP zu bauen, wurde aber ein weniger eng gekoppeltes System gewählt. Die Vorteile sind:

* Man kann auch im laufenden Betrieb Komponenten zu- oder abschalten.
* Die Komponenten können in verschiedenen, zum Problem passenden Sprachen geschrieben sein.
* Die Komponenten müssen nicht in derselben JVM, ja nicht einmal auf derselben physikalischen Maschine laufen.
* Das System kann, entsprechende Sicherheitsvorkehrungen vorausgesetzt, auch aus dem Internet benutzt werden.

### Koppelungslogik

Gegen aussen kommuniziert das System über ein REST API. Die Koppelung der Komponenten untereinander läuft aber über
ein Messaging-System (Das natürlich "unter der Haube" ebenfalls über Netzwerk-Protokolle geht).

* Eine zentrale Komponente, der "Dispatcher" stellt das REST-Api zur Verfügung und nimmt Registrierungsanfragen von 
 Komponenten entgegen. 
* Jede Komponente kann unabhängig, oder (konfigurierbar) mit dem Dispatcher gestartet zu werden,
* Nach erfolgreichem Start sendet die Komponente dem Dispatcher eine oder mehrere Nachrichten, in denen sie sich für
bestimmte API-Adressen registriert.
* Der Dispatcher stellt eine gemeinsame Admin-Oberfläche zur Verfügung, über die der Anwender alle Komponenten und den
Zustand der Hardware kontrollieren kann.

### Aufbau

Das Projekt "Ungrad Server" enthält einige Standardkomponenten, die auch als Anschauungsmaterial dienen können.

* article: Ein Programm, das Listen Medizinischer Artikel (SL, Swissmedic, Kompendium) aus öffentlich zugänglichen
Quellen zusammensucht, aufbereitet und über das REST API zur Verfügung stellt. 
* backup: Zentrales Daten Backup
* lucinda: Dokumentenverwaltungssystem
* tester: Systeminformationen und Test des Message-Systems
* webelexis: REST-Schnittstelle für die Elexis Daten
