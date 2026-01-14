# PortCat development reasearch
In diesem Dokument teile ich meine Entscheide, bedenken und allgemeine Recherche während der Entwicklung von **PortCat**. Das Dokument wird laufend bis zum Release sowie bei weiterführender Entwicklung aktualisiert.


## Auswahl UI Framework – Swing vs JavaFX (Woche 1)
Schon während der Ideenfindung hat sich gezeigt, dass PortCat ein GUI-/Frontend-intensives Programm wird. Schliesslich sollte es auch Personen mit wenig oder gar keiner Erfahrung gelingen, zu sehen, mit welchen anderen Rechnern sich der eigene verbindet.

Bezüglich des UI-Frameworks war ich anfangs skeptisch, was die Verwendung von JavaFX betrifft. Vor der Entscheidung habe ich mich über die besten Frameworks für Java-Benutzeroberflächen informiert. Meistens wurde dabei über JavaFX und Swing gesprochen. JavaFX ist das deutlich neuere und modernere UI-Framework von Java und hat sich seit Java 11 von der Standardbibliothek abgesetzt. Swing ist bis heute in `javax.swing.*` vorhanden.

Eine Swing-UI zu erstellen, ist deutlich einfacher als mit JavaFX. Allerdings kann man mit JavaFX viel mehr machen, beispielsweise moderne Designs erstellen.

Schlussendlich habe ich mir vorgenommen, mich mit JavaFX auseinanderzusetzen, obwohl Swing viel einfacher ist. Das heisst, ich möchte JavaFX lernen und gleich anwenden.

## UI-Mockup mit Figma (Woche 1)

Ich habe zwei UI-Mockups erstellt. Das erste habe ich bereits zu Beginn des Projekts erstellt. Mit einem einfachen Tool zum Hinzufügen und Verschieben von Formen und Bildern konnte ich das erste Mockup fertigstellen. Es gibt mir eine erste Vorstellung davon, wie das Programm etwa aussehen wird.

Jetzt, da das Projekt gestartet ist, habe ich mich sofort an ein verbessertes Mockup gemacht und dazu das weltweit beliebte Tool **Figma** verwendet. Mit Unterstützung der in Figma integrierten KI konnte ich ein ansprechenderes, ausführlicheres und strukturierteres UI entwerfen. Es ist bereits funktionstüchtig in Figma und genau so, wie ich es mir vorgestellt habe. Allerdings bietet Figma keine direkte Konvertierung für UI-Frameworks/Backend an.

## Der Windows Netstat command (Woche 1)

Das Kommando `netstat`

## Java RegEx  
Für das Parsen mit einem vorgegebenen Muster ist `RegEx` (Regular Expression) die passende Bibliothek.

Ein Beispiel mit einer ArrayList an Strings:

    String[] notParsed = {"10Hello", "30Hi", "20Noo"};

Die List enthält Wörter in einer wiederkehrender Konstellation. Zahlen zuerst und darauf folgend Buchstaben. Ich muss als allererstes das Pattern bestimmen und es kompilieren damit ich es dann mit einem `Matcher` vergleichen kann. 

    // Regex: eine oder mehr Ziffern, gefolgt von einer oder mehr Buchstaben
    Pattern p = Pattern.compile("(\\d+)([A-Za-z]+)");

Anschliessend iteriere ich durch die Liste 

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

<!--stackedit_data:
eyJoaXN0b3J5IjpbMTMzODE4MzA4OSwtMzU1NjYxMDQxLDk4Nz
I2MjI0Ml19
-->