


#  PortCat – Visualizer für Netzwerkverbindungen

**PortCat** ist eine Java-/JavaFX-Anwendung, die dir zeigt, welche Netzwerkverbindungen dein PC gerade hat – **verständlich, visuell und in Echtzeit**.


<p align="center">
  <img src="https://i.imgur.com/lSglvpp.png" alt="PortCat logo" width="500">
</p>

## 🚀 Beschreibung

PortCat liest regelmässig die Ausgabe eines nativen Befehls auf entweder Windows oder Linux aus und zeigt zu jeder aktiven Verbindung wichtige Informationen an:

- 💻 **Lokale & entfernte IP-Adresse**
- 🔌 **Ports inkl. Name**
- 🌐 **TCP-Protokoll**
- 🧭 **Richtung** (eingehend/ausgehend)
- 📡 **DNS-Name** (falls auflösbar)

Die Verbindungen werden mit **Icons, Pfeilen und Farben** visualisiert. So siehst du auf einen Blick, wie dein Rechner mit der Aussenwelt kommuniziert.

Dank einer internen, erweiterbaren **Ports-Liste** werden viele bekannte Dienste automatisch erkannt und benannt – beispielsweise:

- „🌍 Web-Anfrage“
- „📧 Mail-Verbindung“
- „🔒 SSH-Session“
- „🎮 Minecraft-Server“

---

## 💡 Motivation

Wenn man sich erstmals mit Netzwerken beschäftigt, sind komplexe Tools wie **Wireshark** oft zu überfordernd.  
PortCat bietet eine **einfache und anschauliche Einstiegslösung**, um zu lernen:

- Was IP-Adressen und Ports bedeuten
- Wie Netzwerkaktivität in Echtzeit aussieht
- Wie sich **sein eigener Rechner** mit dem Internet verbindet

Für mich persönlich ist PortCat ausserdem ein Lernprojekt, um meine **Kenntnisse in Java, JavaFX und Netzwerktechnik** zu vertiefen.

---

## ⚙️ Funktionsweise

1. PortCat führt regelmässig einen Netzwerk-Befehl aus.
2. Die Ergebnisse werden korrekt verarbeitet (parsing).
3. Neue, beendete oder geänderte Verbindungen werden erkannt und **live visualisiert**.

So entsteht ein „lebendes“ Bild deines Netzwerkverkehrs – ohne erweiterte Berechtigungen oder komplizierte Filter.

---

## 🧰 Voraussetzungen

- Java 17 oder neuer
- JavaFX SDK
- Windows oder Linux

---

## 🔮 Geplante Features

- 🔍 Filter nach Prozessname oder IP
- 🎨 Anpassbare Themes

---

## 💬 Autor

Entwickelt mit ❤️ von Melih Can Abay 
📫 **Kontakt:** <melihcan05@protonmail.com>

*Dieses Readme wurde mit Hilfe von KI erstellt*
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTU2MjAzODkxOCwtNzQzMTE0NDkyXX0=
-->
