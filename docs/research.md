
# PortCat development reasearch
In diesem Dokument teile ich meine Entscheide, bedenken und allgemeine Recherche während der Entwicklung von **PortCat**. Das Dokument wird laufend bis zum Release sowie bei weiterführender Entwicklung aktualisiert. 

*Zu unterst befindet sich ein **Quellenverzeichnis**.*


## Auswahl UI Framework – Swing vs JavaFX
Schon während der Ideenfindung hat sich gezeigt, dass PortCat ein GUI-/Frontend-intensives Programm wird. Schliesslich sollte es auch Personen mit wenig oder gar keiner Erfahrung gelingen, zu sehen, mit welchen anderen Rechnern sich der eigene verbindet.

Bezüglich des UI-Frameworks war ich anfangs skeptisch, was die Verwendung von JavaFX betrifft. Vor der Entscheidung habe ich mich über die besten Frameworks für Java-Benutzeroberflächen informiert. Meistens wurde dabei über JavaFX und Swing gesprochen. JavaFX ist das deutlich neuere und modernere UI-Framework von Java und hat sich seit Java 11 von der Standardbibliothek abgesetzt. Swing ist bis heute in `javax.swing.*` vorhanden.

Eine Swing-UI zu erstellen, ist deutlich einfacher als mit JavaFX. Allerdings kann man mit JavaFX viel mehr machen, beispielsweise moderne Designs erstellen.

Schlussendlich habe ich mir vorgenommen, mich mit JavaFX auseinanderzusetzen, obwohl Swing viel einfacher ist. Das heisst, ich möchte JavaFX lernen und gleich anwenden.

## UI-Mockup mit Figma

Ich habe zwei UI-Mockups erstellt. Das erste habe ich bereits zu Beginn des Projekts erstellt. Mit einem einfachen Tool zum Hinzufügen und Verschieben von Formen und Bildern konnte ich das erste Mockup fertigstellen. Es gibt mir eine erste Vorstellung davon, wie das Programm etwa aussehen wird.

Jetzt, da das Projekt gestartet ist, habe ich mich sofort an ein verbessertes Mockup gemacht und dazu das weltweit beliebtpopuläre Tool **Figma** verwendet. Mit Unterstützung der in Figma integrierten KI konnte ich ein ansprechenderes, ausführlicheres und strukturierteres UI entwerfen. Es ist bereits funktionstüchtig in Figma und genau so, wie ich es mir vorgestellt habe. Allerdings bietet Figma keine direkte Konvertierung für UI-Frameworks/Backend an. Daher dient es vielmehr als Inspiration/Entwurf.

##  Windows-Kommando `Get-NetTCPConnection` vs `netstat` 

Nach gründlicher Recherche habe ich eine geeignetere Alternative zu `netstat` gefunden: Das PowerShell-Cmdlet `Get-NetTCPConnection`. Es liefert strukturierte Objekte, die sich direkt per Pipeline mit `ConvertTo-Json` in sauberes JSON umwandeln lassen. Dadurch entfällt das mühsame und fehleranfällige Parsen des reinen Textoutputs von `netstat`. Zwar kann auch `netstat` nach JSON konvertiert werden, dabei entsteht jedoch in der Regel nur ein JSON-Array aus Textzeilen und keine sauber strukturierte Datendarstellung.



## Java RegEx
Für das Parsen mit einem vorgegebenen Muster ist `RegEx` (Regular Expression) die passende Bibliothek.

Ein Beispiel mit einer ArrayList an Strings:
```java
String[] notParsed = {"10Hello", "30Hi", "20Noo"};
```
Die List enthält Wörter in einer wiederkehrender Konstellation. Zahlen zuerst und darauf folgend Buchstaben. Ich muss als allererstes das Pattern bestimmen und es kompilieren damit ich es dann mit einem `Matcher` vergleichen kann. 
```java
// Regex: eine oder mehr Ziffern, gefolgt von einer oder mehr Buchstaben
    Pattern p = Pattern.compile("(\\d+)([A-Za-z]+)");
```
Anschliessend iteriere ich durch die Liste und matche es mit dem Pattern. Ich kann dann einfach die einzelnen Gruppen im String unterteilen und benennen. Ein Handler für einen String der nicht mit dem Pattern ist auch vorhanden.

```java
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
```
        
**Wichtig**: Ich konnte mir die Arbeit mit `RegEx` ersparen, da ich wie schon oben erwähnt einen besseren Befehl mit sauberem JSON-Output gefunden habe.


## Bottom Pannel statt VBox
Beim Gespräch mit meinem Experten sind wir das Problem angegangen, dass die Tabelle nicht die zur verfügung stehende Höhe nutzen kann. Selbst mit ausdrücken wie setMinHeight()` usw. konnte es nicht gelöst werden 
Daher hat mir mein Expert hat mir Empfohlen anstelle der VBox ein Border Panel Layout zu verwenden.  
Es war einfach zu implementieren und das Problem wurde somit behoben.

## Color Palettes für UX
Die basic JavaFX UI ist schlicht in weiss und sieht nicht wirklich benutzerfreundlich aus und auch nicht wirklich modern. Ich habe mich darum entschieden mit Farben zu arbeiten, um die UI lebendiger zu machen. Für die Farben habe ich ein Tool gefunden was einem eine passende Palette generiert. Ich habe mich für eine entschieden und konnte das mittels kopieren des HEX-Codes dort anwenden, wo ich wollte. Das Tool nennt sich coolors.co. 

Abgesehen davon habe ich Hilfsmethoden geschrieben, die mir auch beim zukünftigen Erstellen und bearbeiten von Button-Styles helfen.

## Cross-Platform
Am Anfang habe ich mich darauf fokussiert das Programm nur Windows kompatibel zu halten. Da ein wichtiger Teil der UX ist, dass man ein Programm möglichst plattformübergreifend nutzen zu können habe ich mich damit befasst, ein Linux äquivalenten Befehl zu suchen. 
Es gibt leider keinen Befehl, wessen Ausgabe 1:1 derselbe ist wie der von Windows, daher musste ich die PortParser-Klasse leicht umbauen.

Entscheidend ist am Anfang der Check auf welcher OS man das Programm läuft. Der Check funktioniert durch `System.getProperty("os.name").` 

Im Gegensatz zum Powershell-Command Get-NetTCPConnection habe ich für die Linux variante `ss -tunH -J` genommen. SS steht für Socket Statistic und liefert viel Informationen rund um das Netwerk. 

## DNS-Auflösung

Auf JavaDoc gibt es eine Dokumentation zu einer Bibliothek die eine native DNS-Abfrage ausführt und somit gleichzeitig Plattformübergreifend ist. Leider ist die Anzahl auflösbare IP-Adressen klein, da nur wenige einen **PTR-Record** haben. Ein gutes Beispiel zur Demonstration ist die DNS von coop:
| Eingabe | Ausgabe |
|---------|---------|
| `nslookup coop.ch` | **Server:** 62.2.17.61<br>**Address:** 62.2.17.61#53<br><br>Non-authoritative answer:<br>**Name:** coop.ch<br>**Address:** 185.170.88.26 |
| `nslookup 185.170.88.26` |  **server can't find 26.88.170.185.in-addr.arpa: NXDOMAIN** |

Die IP gehört sehr wahrscheinlich einem Host-Provider und Coop mietet dort Server, besitzt daher die IP nicht selbst.

Im Programm wird versucht die DNS aufzulösen.


## Java ScheduledExecutorService
Die Klasse Scheduler aktualisiert den Inhalt der Tabelle in der GUI regelmässig, ohne den JavaFX-UI-Thread zu blockieren. Die Daten werden in einem Hintergrund-Thread geladen und anschliessend thread-sicher im UI-Thread angezeigt.

**Aufbau**
`ScheduledExecutorService scheduler` erstellt einen Executor mit genau einem Worker-Thread. Dadurch laufen die periodischen Tasks seriell d.h. es kann nie mehr als ein Aktualisierungslauf gleichzeitig laufen (keine Überlappungen). 

**scheduleWithFixedDelay vs scheduleWithFixedRate**
- `scheduleWithFixedDelay(...)`: Wartet, nachdem ein Durchlauf fertig ist noch die angegebene Zeit und startet dann den nächsten Durchlauf. Wenn ladeDaten() länger dauert, verschiebt sich der nächste Start automatisch nach hinten. Es gibt keine Aufholversuche.
- `scheduleWithFixedRate(...)`: Versucht, die Starts in einem fixen Takt auszuführen (z.B. immer genau jede Sekunde ab Startzeit). Wenn ein Durchlauf zu lange dauert, kann der Scheduler versuchen "aufzuholen" (bei Single-Thread heisst das vor allem: der nächste Lauf startet sofort nach dem vorherigen, um den Takt wieder zu treffen). Das kann zu hoher last führen.

`scheduleWithFixedDelay` berücksichtigt somit die Geschwindigkeit des Rechners, da gewartet wird, bis der vorherige Prozess ladeDaten() beendet ist, bevor der nächste Zyklus (ink. Wartezeit) startet.

## Start & Stop Logik
Ich habe mich mit dem Thema befasst, eine Start&Stop-Logik zu implementieren, damit das Programm erst dann beginnt, Verbindungen zu scannen, wenn man auf "Start" klickt. Dafür habe ich eine Start&Stop-Hilfsmethode erstellt. Allerdings machte meine Konfiguration des Schedulers Probleme: Meine Methode `scheduler.stop()` stoppte den Scheduler nicht nur, sondern beendete den zugrunde liegenden ScheduledExecutorService mit `shutdownNow()`. Dadurch war der Executor danach "Terminated" und nahm beim erneuten Start keine neuen Tasks mehr an (u.a. `RejectedExecutionException`). Ein erneutes Starten war erst nach einem Programmneustart möglich.

Das Problem habe ich behoben, indem ich zwischen "Task stoppen" und "Scheduler endgültig beenden" getrennt habe: Beim Stoppen wird nun nur der geplante Task über ein `ScheduledFuture<?>` gecancelt. Das komplette Beenden des Executors passiert nur noch beim Schliessen der Applikation.

## CSS Style-Sheets für JavaFx
Ein cooles Features, was das JavaFx Framework anbietet, ist die anbindung von CSS Style-Sheets für Designzwecken. 

Damit man sich nicht im Code selbst allzu sehr mit dem Design von Knöpfen, Labels usw. auseinandersetzen muss, kann man für Elemente, wie z.B. ein Button eine CSS Klasse zuweisen. Im separaten CSS-File kann man dann die Erscheinung des Buttons sauber anpassen.

Für meine Buttons habe ich beispielsweise Schatten und eine schöne 3D-Druckanimation erstellt. 

 Zuerst wird dem Button eine CSS-Klasse zugewiesen: `btnClear.getStyleClass().add("btnClear");`. Anschliessend verweise ich in app.css auf die Klasse und füge die CSS-Properties hinzu:

```CSS
.btnClear {
-fx-font-size: 17.5px;
-fx-font-weight: bold;
-fx-text-fill: #322525;
-fx-background-radius: 10;
-fx-padding: 8 18 8 18;
-fx-background-color: #ffffff;
-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 10, 0.3, 0, 3);

}
```
Für den 3D-Effekt musste ich folgendes weiter unten ergänzen:

```CSS
.btnClear:pressed {-fx-translate-y: 2px;
    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 6, 0.0, 0, 1);}
```

Den normalen Zustand des Buttons wird durch `.KlassenName{...}` bearbeitet und wenn es um andere States geht dann mit z.B. `.KlassenName:pressed{...}`.

## Unit-Tests in Java

Unit-Tests testen einzelne Methoden automatisiert. Mit JUnit 5 können wir Funktionen mit verschiedenen Eingaben prüfen und überprüfen, ob sie das erwartete Ergebnis zurückgeben.

### Ordnerstruktur

Test-Code folgt der gleichen Package-Struktur wie Production-Code, befindet sich aber in `src/test/java/`:

### pom.xml Dependencies

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
```

### AAA-Pattern

Jeder Test folgt: **Arrange** (Setup) → **Act** (Ausführen) → **Assert** (Überprüfen)

```Java

@Test
void testConnectionCreation() {
    Connection conn = new Connection("192.168.1.1", 8080, "10.0.0.1", "443", "ESTABLISHED");
    assertEquals("192.168.1.1", conn.getLocalAddress());
}
```

### Fehler: Source Roots

Ein Fehler den ich gemacht habe und der mir einige Zeit gekostet hat, war, dass ich src sowie java als sources-root markiert habe. Somit konnte die JUnit Dependency nicht korrekt bereitgestellt werden.

Lösung:

    src/main/java/ → Mark as Sources Root (blau)
    src/test/java/ → Mark as Test Sources Root (grün)

## Liste speichern als CSV
Ein feature, dass ich bereits zu Beginn geplant habe, war, dass man die Liste mit den Connection-Einträgen speichern kann.
Ich habe dafür die Klasse `SaveAsCSV` mithilfe von KI geschrieben.

Die Utility-Klasse `SaveAsCSV` kapselt den gesamten Exportvorgang, damit die GUI nur die Methode `saveAsCsv` aufrufen muss. Diese Methode prüft zuerst, ob überhaupt Daten vorhanden sind, öffnet danach einen FileChoser und legt den vom Benutzer gewählten Pfad als Ziel fest. Anschliessend wird mit `Files.newBufferedWriter` ein UTF-8-Writer, um die Datei speichersicher zu schreiben. Die Kernlogik steckt in `writeCsv`: Hier wird zuerst die Kopfziele mit allen Spaltennamen inkl. der neuen Spalte `Direction` ausgegeben, danach werden sämtliche Connection-Objekte zeilenweise in CSV-Format übertragen. Damit Felder mit KOmmas, Anführungszeichen oder Zeilenumbrücken korrekt sind, sorgt die Hilfsmethode `csvEscape`. Sollte der Benutzer den Dialog abbrechen oder ein IO-Fehler auftreten, wird dies durch Rückgabe von `null` bzw. eine Exception-Ausgabe abgefangen.

## Quellenverzeichnis
| Thema | Beschreibung | Link |
|---|---|---|
| JavaFX | Einstieg in GUI-Entwicklung mit JavaFX | [Getting Started with JavaFX](https://openjfx.io/openjfx-docs/) |
| Java allgemein | Offizielle Java-Dokumentation und Getting Started | [Java Documentation – Get Started](https://docs.oracle.com/en/java/) |
| UI-Mockups (Figma) | Tool zum Erstellen von UI-Designs und Prototypen | [Figma](https://www.figma.com/) |
| `netstat`-Befehl (Windows) | Dokumentation des `netstat`-Befehls unter Windows | [netstat – Microsoft Learn](https://learn.microsoft.com/de-de/windows-server/administration/windows-commands/netstat) |
| `Get-NetTCPConnection`-Befehl (Windows PowerShell) | Dokumentation des `Get-NetTCPConnection`-Cmdlets (Modul **NetTCPIP**) | [Get-NetTCPConnection – Microsoft Learn](https://learn.microsoft.com/en-us/powershell/module/nettcpip/get-nettcpconnection?view=windowsserver2025-ps) |
| RegEx & Java-Grundlagen | Tutorial zu Java inkl. RegEx und weiteren Themen | [Java Tutorial (W3Schools)](https://www.w3schools.com/java/default.asp) |
| KI-Unterstuetzung (ChatGPT, Perplexity) | KI-Tools fuer Fragen, Recherche und Code-Snippets | [ChatGPT](https://chat.openai.com/) · [Perplexity](https://www.perplexity.ai/) |
| DNS-Auflösung (Java) | JavaDoc: `InetAddress` (z.B. Namensauflösung, Hostname/IP) | [InetAddress (Java SE 25)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/net/InetAddress.html) |
