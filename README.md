# ZeroWasteCook (Beta Version 1.3)

ZeroWasteCook ist eine Android-Anwendung, die darauf abzielt, die Verwaltung von Lebensmitteln, Rezepten und Routinen zu optimieren. Die App bietet Funktionen zum Hinzufügen, Verwalten und Überwachen von Lebensmitteln im Vorrat, zur Erstellung und Verwaltung von Rezepten sowie zur Automatisierung von Routinen.

## Technologien und Dienste

- **Kotlin**: Die Hauptprogrammiersprache für die Entwicklung der Android-App.
- **Room**: Eine SQLite-Objektrelational-Mapping-Bibliothek, die für die lokale Datenbankverwaltung verwendet wird.
- **Espresso**: Ein Framework für UI-Tests, das zur Implementierung von End-to-End-Tests verwendet wird.
- **JUnit**: Ein Framework für Unit-Tests, das zur Überprüfung der Geschäftslogik der App verwendet wird.
- **AWS S3**: Wird für das Hochladen und Verwalten von Bildern in der Cloud verwendet.
- **Supabase**: Cloud Datenbank, die die Rezepte enthählt.

## Funktionen

- **Lebensmittelverwaltung**: Hinzufügen, Aktualisieren und Löschen von Lebensmitteln im Vorrat.
- **Rezeptverwaltung**: Erstellen, Bearbeiten und Löschen von Rezepten mit detaillierten Anweisungen und Zutaten.
- **Routinen**: Automatisierung von Aufgaben basierend auf benutzerdefinierten Zeitplänen.
- **Bild-Upload**: Hochladen von Bildern zu AWS S3 für die Verwendung in Rezepten.

## Tests

Das Projekt enthält umfassende Tests, um die Funktionalität und Zuverlässigkeit der App sicherzustellen:

- **Unit Tests**: Überprüfen die Logik der Controller, einschließlich der Verwaltung von Lebensmitteln, Rezepten und Routinen.
- **Integration Tests**: Stellen sicher, dass die verschiedenen Komponenten der App korrekt zusammenarbeiten.
- **End-to-End Tests**: Simulieren Benutzerinteraktionen, um die Benutzeroberfläche und die Benutzererfahrung zu testen.

## Installation

1. Klone das Repository:
   ```bash
   git clone https://github.com/username/ProjektMBun.git
   ```
2. Öffne das Projekt in Android Studio.
3. Stelle sicher, dass alle Abhängigkeiten installiert sind.
4. Führe die App auf einem Emulator oder einem physischen Gerät aus.

## Mitwirken

Beiträge sind willkommen! Bitte erstelle einen Fork des Repositories und sende einen Pull-Request mit deinen Änderungen.
