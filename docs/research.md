# PortCat development reasearch
In diesem Dokument teile ich meine Entscheide und bedenken während der Entwicklung von **PortCat**. Das Dokument wird laufend bis zum Release sowie bei weiterführender Entwicklung aktualisiert.

## Auswahl UI Framework – Swing vs JavaFX
Schon während der Ideenfindung hat sich gezeigt, dass PortCat ein GUI-/Frontend-intensives Programm wird. Schliesslich sollte es auch Personen mit wenig oder gar keiner Erfahrung gelingen, zu sehen, mit welchen anderen Rechnern sich der eigene verbindet.

Bezüglich des UI-Frameworks war ich anfangs skeptisch, was die Verwendung von JavaFX betrifft. Vor der Entscheidung habe ich mich über die besten Frameworks für Java-Benutzeroberflächen informiert. Meistens wurde dabei über JavaFX und Swing gesprochen. JavaFX ist das deutlich neuere und modernere UI-Framework von Java und hat sich seit Java 11 von der Standardbibliothek abgesetzt. Swing ist bis heute in `javax.swing.*` vorhanden.

Eine Swing-UI zu erstellen, ist deutlich einfacher als mit JavaFX. Allerdings kann man mit JavaFX viel mehr machen, beispielsweise moderne Designs erstellen.

Schlussendlich habe ich mir vorgenommen, mich mit JavaFX auseinanderzusetzen, obwohl Swing viel einfacher ist. Das heisst, ich möchte JavaFX lernen und gleich anwenden.

## UI-Mockup mit Figma
Ich habe zwei UI-Mockups erstellt. Das erste habe ich früh, noch während der Projektbeschreibung, erstellt. Mit einem einfachen Tool fürs hinzufügen und verschieben von Formen und Bilder konnte ich das erste Mockup fertigstellen, das mir eine erste Vorstellung davon gibt, wie das Programm etwa aussehen wird.

Jetzt, da das Projekt gestartet ist, habe ich mich sofort an ein verbessertes Mockup gemacht und dazu das weltweit beliebte und bekannte Tool Figma verwendet. Mit Unterstützung der in Figma integrierten AI, konnte ich ein schöneres, ausführlicheres und stru
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTg3MDY4OTQ0MCw5ODcyNjIyNDJdfQ==
-->