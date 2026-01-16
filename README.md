


#  PortCat – Visualizer für Netzwerkverbindungen

**PortCat** ist eine Java-/JavaFX-Anwendung, die dir zeigt, welche Netzwerkverbindungen dein PC gerade hat – **verständlich, visuell und in Echtzeit**.


<p align="center">
  <img src="https://i.imgur.com/lSglvpp.png" alt="PortCat logo" width="500">
</p>

## 🚀 Beschreibung

PortCat liest regelmäßig die Ausgabe des **`netstat`-Befehls** aus und zeigt zu jeder aktiven Verbindung wichtige Informationen an:

- 🕒 **Zeitstempel** – Wann wurde die Verbindung erkannt
- 💻 **Lokale & entfernte IP-Adresse**
- 🔌 **Ports**
- 🌐 **Protokoll (TCP/UDP)**
- 🧭 **Richtung** (eingehend/ausgehend)
- 📡 **DNS-Name** (falls auflösbar)

Die Verbindungen werden mit **Icons, Pfeilen und Farben** visualisiert. So siehst du auf einen Blick, wie dein Rechner mit der Außenwelt kommuniziert.

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
- Welche Programme mit welchen Servern sprechen
- Wie Netzwerkaktivität in Echtzeit aussieht

Für mich persönlich ist PortCat außerdem ein Lernprojekt, um meine **Kenntnisse in Java, JavaFX und Netzwerktechnik** zu vertiefen.

---

## ⚙️ Funktionsweise

1. PortCat führt regelmäßig `netstat` aus (z. B. alle paar Sekunden).
2. Die Ergebnisse werden mit dem vorherigen Stand verglichen.
3. Neue, beendete oder geänderte Verbindungen werden erkannt und **live visualisiert**.

So entsteht ein „lebendes“ Bild deines Netzwerkverkehrs – ohne erweiterte Berechtigungen oder komplizierte Filter.

---

## 🧰 Voraussetzungen

- Java 17 oder neuer
- JavaFX SDK
- Betriebssystem mit `netstat` (Windows, macOS, Linux)

---

## 🔮 Geplante Features

- 📊 Historienansicht (Verlauf der Verbindungen)
- 🔍 Filter nach Prozessname oder IP
- 🎨 Anpassbare Themes
- 📁 Exportfunktion (CSV oder JSON)

---

## 📘 Quellen & Hinweise

- `netstat` liefert nur Momentaufnahmen der Verbindungen.  
  PortCat ruft diesen Befehl regelmäßig auf, um Aktivitäten **nahezu in Echtzeit** anzuzeigen.
- Weitere Infos zu `netstat`:  
  [Microsoft Docs](https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/netstat) · [Linux man page](https://man7.org/linux/man-pages/man8/netstat.8.html)

---

## 💬 Autor

Entwickelt mit ❤️ von Melih Can Abay 
📫 **Kontakt:** <melihcan05@protonmail.com>

*Dieses Readme wurde mit Hilfe von KI erstellt*
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTIwMDc2MjI3NjddfQ==
-->