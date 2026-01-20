
# PortCat development reasearch
In diesem Dokument teile ich meine Entscheide, bedenken und allgemeine Recherche während der Entwicklung von **PortCat**. Das Dokument wird laufend bis zum Release sowie bei weiterführender Entwicklung aktualisiert. 

*Zu unterst befindet sich ein **Quellenverzeichnis**.*


## Auswahl UI Framework – Swing vs JavaFX (Woche 1)
Schon während der Ideenfindung hat sich gezeigt, dass PortCat ein GUI-/Frontend-intensives Programm wird. Schliesslich sollte es auch Personen mit wenig oder gar keiner Erfahrung gelingen, zu sehen, mit welchen anderen Rechnern sich der eigene verbindet.

Bezüglich des UI-Frameworks war ich anfangs skeptisch, was die Verwendung von JavaFX betrifft. Vor der Entscheidung habe ich mich über die besten Frameworks für Java-Benutzeroberflächen informiert. Meistens wurde dabei über JavaFX und Swing gesprochen. JavaFX ist das deutlich neuere und modernere UI-Framework von Java und hat sich seit Java 11 von der Standardbibliothek abgesetzt. Swing ist bis heute in `javax.swing.*` vorhanden.

Eine Swing-UI zu erstellen, ist deutlich einfacher als mit JavaFX. Allerdings kann man mit JavaFX viel mehr machen, beispielsweise moderne Designs erstellen.

Schlussendlich habe ich mir vorgenommen, mich mit JavaFX auseinanderzusetzen, obwohl Swing viel einfacher ist. Das heisst, ich möchte JavaFX lernen und gleich anwenden.

## UI-Mockup mit Figma

Ich habe zwei UI-Mockups erstellt. Das erste habe ich bereits zu Beginn des Projekts erstellt. Mit einem einfachen Tool zum Hinzufügen und Verschieben von Formen und Bildern konnte ich das erste Mockup fertigstellen. Es gibt mir eine erste Vorstellung davon, wie das Programm etwa aussehen wird.

Jetzt, da das Projekt gestartet ist, habe ich mich sofort an ein verbessertes Mockup gemacht und dazu das weltweit beliebtpopuläre Tool **Figma** verwendet. Mit Unterstützung der in Figma integrierten KI konnte ich ein ansprechenderes, ausführlicheres und strukturierteres UI entwerfen. Es ist bereits funktionstüchtig in Figma und genau so, wie ich es mir vorgestellt habe. Allerdings bietet Figma keine direkte Konvertierung für UI-Frameworks/Backend an. Daher dient es vielmehr als Inspiration/Entwurf.

##  Windows-Kommando `Get-NetTCPConnection` vs `netstat`

Nach gründlicher Recherche habe ich tatsächlich ein besseres Kommando als `netstat` gefunden. Das PowerShell-Kommando `Get-NetTCPConnection` kann dank dem `ConvertTo-Json` Pipe-Parameter den Output sauber als JSON ausgeben somit entfällt das mühsame Parsen des rohen String-Outputs aus `netstat`.  Der `netstat` Befehl kann zwar auch als JSON ausgegeben werden, allerdings Das vereinfacht das Parsen erheblich. 


Nach gründlicher Recherche habe ich tatsächlich ein besseres Kommando als `netstat` gefunden. 

## Java RegEx  (Woche 1)
Für das Parsen mit einem vorgegebenen Muster ist `RegEx` (Regular Expression) die passende Bibliothek.

Ein Beispiel mit einer ArrayList an Strings:

    String[] notParsed = {"10Hello", "30Hi", "20Noo"};

Die List enthält Wörter in einer wiederkehrender Konstellation. Zahlen zuerst und darauf folgend Buchstaben. Ich muss als allererstes das Pattern bestimmen und es kompilieren damit ich es dann mit einem `Matcher` vergleichen kann. 

    // Regex: eine oder mehr Ziffern, gefolgt von einer oder mehr Buchstaben
    Pattern p = Pattern.compile("(\\d+)([A-Za-z]+)");

Anschliessend iteriere ich durch die Liste und matche es mit dem Pattern. Ich kann dann einfach die einzelnen Gruppen im String unterteilen und benennen. Ein Handler für einen String der nicht mit dem Pattern ist auch vorhanden.

    for (int i = 0; i < notParsed.length; i++) {
            String s = notParsed[i];
            Matcher m = p.matcher(s); // Matcher für den aktuellen String

            if (m.matches()) {
                String number = m.group(1); // z.B. "10"
                String text   = m.group(2); // z.B. "Hello"
                System.out.println("Original: " + s);
                System.out.println("Zahl: " + number);
                System.out.println("Text: " + text);
                System.out.println();
                
            } else {
                System.out.println(
                    "String \"" + s + "\" auf Index " + i + " passt nicht zum Pattern."
                );
            }
        }


## Bottom Pannel statt VBox (Woche 2)

## Timers in Java (Multithreading) (Woche 2)
## Cross-Platform (Woche 2)

## DNS-Auflösung (Woche 2)

Auf JavaDoc gibt es eine Dokumentation zu einer Bibliothek die eine native DNS-Abfrage ausführt und somit gleichzeitig Plattformübergreifend ist.


## Quellenverzeichnis

| Thema                         | Beschreibung                                           | Link                                                                                                  |
|-------------------------------|--------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| JavaFX                        | Einstieg in GUI-Entwicklung mit JavaFX                 | [Getting Started with JavaFX](https://openjfx.io/openjfx-docs/)                                      |
| Java allgemein                | Offizielle Java-Dokumentation und Getting Started      | [Java Documentation – Get Started](https://docs.oracle.com/en/java/)                                 |
| UI-Mockups (Figma)           | Tool zum Erstellen von UI-Designs und Prototypen       | https://www.figma.com/                                                                                |
| `netstat`-Befehl (Windows)   | Dokumentation des `netstat`-Befehls unter Windows      | [netstat – Microsoft Learn](https://learn.microsoft.com/de-de/windows-server/administration/windows-commands/netstat) |
| RegEx & Java-Grundlagen      | Tutorial zu Java, inkl. RegEx und weiteren Themen      | [Java Tutorial (W3Schools)](https://www.w3schools.com/java/default.asp)                              |
| KI-Unterstützung (ChatGPT etc.) | KI-Tools zur Beantwortung von Fragen, Recherche und Code-Snippets | https://chat.openai.com/  https://www.perplexity.ai/                                   |
| DNS-Auflösung Java            | Dokumentation auf JavaDoc. Die benötigten Methoden usw. | https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/InetAddress.html |


<!--stackedit_data:
eyJoaXN0b3J5IjpbLTEzNzc2MDE2NDVdfQ==
-->

<!--stackedit_data:
eyJoaXN0b3J5IjpbMTc3NjEwMTI0NywtMzM1MzA1NTA5LC0xMz
c3NjAxNjQ1XX0=
-->