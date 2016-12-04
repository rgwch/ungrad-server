---
date: 2016-12-04T15:08:39+02:00
title: Backup
menu: main
weight: 0
---
## Die Backup Komponente

### Zweck

Diese Kompomnente verwaltet Datensicherungen, welche von anderen Tools erstellt wurden.

Beispiel: Ein cron-job erstellt nächtliche Dumps der Elexis-Datenbank. Ungrad-Server-Backup kann nun beispielsweise dafür sorgen, dass

* Ein Teil der Backups, zum Beispiel eines pro Woche, in verschlüsselter Form per SCP auf einen externen Server gebracht werden.
* Ein Teil der Backups, zum Beispiel eines pro Quartal, in eine Lanzeitspeicherung bei Amazon Glacier überführt werden.
* Dazwischenliegende Backups vom lokalen System gelöscht werden.
* Doppelt extern gesicherte Backups ebenfalls vom lokalen System gelöscht werden.

### SCP

"Secure Copy" ist eine von SSH abgeleitete Technik, mit der Dateien zwischen verschiedenen Rechnern über eine verschlüsselte Verbindung
 kopiert werden können.

### Glacier

[Amazon Glacier](https://aws.amazon.com/de/glacier/) ist eine Dienstleistung zum kostengünstigen Speichern grosser Datenmengen. Derzeit
liegen die Kosten bei 0.4-0.45 US-Cent pro Gigabyte und Monat, also rund 50 Dollar pro Terabyte und Jahr. Damit ist man in ähnlichen
Preisregionen, als wenn man externe Festplatten zur Datensicherung verwenden würde, man spart sich aber das Handling und die 
periodische Prüfung solcher Platten.

Die Kehrseite der Medaille ist, dass Amazon dafür eine Technologie verwendet, die nicht auf Zugriffsgeschwindigkeit optimiert ist. Glacier
ist nicht für Daten geeignet, auf die man häufig unf schnell Zugriff haben muss. Das Zurückholen von Daten ist je nachdem sogar kostenpflichtig 
(Derzeit ist der Download von 5% des gesamten Datenvolumens pro Monat kostenlos, darüber kostet es um 0.1 Cent pro GB), und es dauert
mindestens 3 Stunden, bevor der Download auch nur beginnen kann.

Glacier ist somit nur für die Langzeitspeicherung von Backups geeignet, auf die man normalerweise "nie" Zugriff benötigt.

Und: Man kann sich zwar aussuchen, ob die Daten in den USA, Europa oder Asien gespeichert werden, aber da Amazon eine amerikanische
Firma ist, darf man wohl getrost davon ausgehen, dass zumindest die US-Regierung vollen Zugriff auf sämtliche in Glacier gespeicherte
Daten hat. Man sollte sämtliche Daten daher vor dem Upload verschlüsseln.
