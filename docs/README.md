# LessonFlow

LessonFlow ist eine webbasierte Lernplattform für selbstgesteuertes Lernen mit Tutorbegleitung. Tutor:innen erstellen strukturierte Kurse mit Lektionen und Lernkontrollen, während Lernende die Inhalte Schritt für Schritt durcharbeiten. Der Lernfortschritt wird über klare Zustände gesteuert: Eine Lektion wird erst nach Materialbearbeitung, Tutor-Meeting und bestandener Lernkontrolle abgeschlossen.

Diese Dokumentation bildet den Grundstein für die schriftliche Projektdokumentation. Sie wird in Iteration 4 mit finalen Diagrammen, Screenshots, KI-Details und weiteren Abschlussinformationen ergänzt.

# Inhaltsverzeichnis

- [Einleitung](#einleitung)
    - [Explore-Board](#explore-board)
    - [Create-Board](#create-board)
    - [Evaluate-Board](#evaluate-board)
    - [Diskussion Feedback Pitch](#diskussion-feedback-pitch)
- [Anforderungen](#anforderungen)
    - [Use-Case Diagramm](#use-case-diagramm)
    - [Use-Case Beschreibung](#use-case-beschreibung)
    - [Fachliches Datenmodell](#fachliches-datenmodell)
    - [Beschreibung der Zustände](#beschreibung-der-zustände)
    - [UI-Mockup](#ui-mockup)
- [Implementation](#implementation)
    - [Frontend](#frontend)
    - [Backend](#backend)
    - [Core Learning Flow](#core-learning-flow)
    - [KI-Funktionen](#ki-funktionen)
    - [Drittsysteme](#drittsysteme)
    - [Optionale Anforderungen](#optionale-anforderungen)
    - [Testing und CI/CD](#testing-und-cicd)
- [Fazit](#fazit)
    - [Stand der Implementation](#stand-der-implementation)
    - [Nächste Schritte](#nächste-schritte)

# Einleitung

LessonFlow adressiert das Problem, dass viele Online-Kurse zwar begonnen, aber nicht abgeschlossen werden. Ein häufiger Grund dafür ist fehlende Struktur, fehlende Motivation oder fehlendes Feedback. Die Plattform kombiniert deshalb selbstgesteuertes Lernen mit Tutorbegleitung und Lernkontrollen.

Tutor:innen können Kurse mit mehreren Lektionen erstellen. Lernende schreiben sich in Kurse ein und bearbeiten Lektionen in einer festen Reihenfolge. Nach jeder Lektion gibt es einen Fortschrittsprozess, der aus Materialbearbeitung, Tutor-Meeting und Quiz besteht.

## Explore-Board

Im Explore-Board wurden Trends, Nutzergruppen, Bedürfnisse und Potenzialfelder untersucht.

### Trends und Technologien

Relevante Trends und Technologien:

- Digitalisierung der Bildung
- E-Learning
- Self-Paced Learning
- Remote Learning
- Lifelong Learning
- KI-gestützte Lernsysteme
- Personalisierte Lernpfade
- Automatisierte Lernkontrollen
- Videokonferenzen im Bildungsbereich
- EdTech-Plattformen

### Wettbewerb und potenzielle Partner

Vergleichbare Plattformen:

- Udemy
- Coursera
- Udacity
- Skillshare
- Khan Academy
- LinkedIn Learning

Diese Plattformen bieten digitale Kurse und Lerninhalte an. LessonFlow unterscheidet sich durch den stärker geführten Lernprozess mit Tutor-Meetings, Lernfortschritt und geplanten KI-gestützten Lernkontrollen.

Mögliche Partner:

- Bildungsinstitutionen
- Weiterbildungsanbieter
- Online-Bootcamps
- Sprachschulen
- Tutor:innen, Coaches und Trainer:innen
- Videokonferenz-Tools wie Microsoft Teams oder Zoom
- Cloud-Plattformen wie Microsoft Azure
- KI-Dienste für automatische Quizgenerierung

### Nutzergruppen

#### Lernende

Lernende möchten neue Fähigkeiten erwerben und flexibel lernen. Sie benötigen dabei Struktur, Motivation und Feedback, damit sie begonnene Kurse konsequent abschliessen können.

#### Tutor:innen

Tutor:innen möchten ihr Wissen strukturiert weitergeben. Sie benötigen eine einfache Möglichkeit, Kurse digital zu erstellen, Lernfortschritte zu verfolgen und Lernende zu begleiten.

### Bedürfnisse

Bedürfnisse der Lernenden:

- klare Struktur beim Durcharbeiten von Online-Lerninhalten
- Motivation und Orientierung
- Feedback zum Lernfortschritt
- flexible Lernumgebung

Bedürfnisse der Tutor:innen:

- digitale Kurserstellung
- Verwaltung von Lektionen und Lerninhalten
- Nachvollziehbarkeit des Lernfortschritts
- einfache Betreuung der Lernenden

### Wie-können-wir-Frage

Wie können wir Lernenden helfen, Online-Kurse strukturiert und motiviert abzuschliessen, indem wir selbstgesteuertes Lernen mit persönlicher Tutor-Begleitung und Lernkontrollen kombinieren?

## Create-Board

Im Create-Board wurde die konkrete Produktidee entwickelt.

### Ideenbeschreibung

LessonFlow ist eine Lernplattform, auf der Tutor:innen strukturierte Kurse mit Lektionen erstellen können. Lernende bearbeiten diese Lektionen in ihrem eigenen Tempo. Nach einer Lektion folgt ein Tutor-Meeting und danach eine Lernkontrolle, die bestanden werden muss, um zur nächsten Lektion zu gelangen.

### Adressierte Nutzer

Die Idee richtet sich hauptsächlich an:

- Lernende
- Tutor:innen
- Coaches
- Trainer:innen
- Weiterbildungsanbieter

### Adressierte Bedürfnisse

LessonFlow adressiert vor allem diese Bedürfnisse:

- Lernende benötigen eine klare Struktur.
- Lernende benötigen Feedback und Lernkontrollen.
- Lernende benötigen Motivation, um Kurse abzuschliessen.
- Tutor:innen benötigen einfache Werkzeuge zur Kurserstellung.
- Tutor:innen möchten Lernfortschritte nachvollziehen.

### Probleme

- Viele Online-Kurse werden begonnen, aber nicht abgeschlossen.
- Lernende erhalten oft kein persönliches Feedback.
- Tutor:innen haben begrenzte Möglichkeiten, strukturierte Online-Kurse mit Fortschrittslogik anzubieten.

### Ideenpotenzial

Der Mehrwert der Idee wurde als relativ hoch eingeschätzt, weil LessonFlow ein bekanntes Problem im Online-Lernen adressiert: fehlende Struktur und fehlendes Feedback. Die Idee ist auf verschiedene Lernbereiche übertragbar, zum Beispiel Programmierung, Sprachen, akademische Kurse oder berufliche Weiterbildung.

### Das Wow

Das Wow-Feature von LessonFlow ist die Kombination aus selbstgesteuertem Lernen, Tutorbegleitung und Lernkontrollen. Lernende können flexibel lernen, müssen ihren Fortschritt aber aktiv nachweisen.

### Wertversprechen

LessonFlow ermöglicht Lernenden, Online-Kurse strukturierter und motivierter abzuschliessen. Tutor:innen erhalten gleichzeitig eine einfache Plattform, um Kurse zu organisieren und Lernfortschritte sichtbar zu machen.

## Evaluate-Board

Im Evaluate-Board wurden Kanäle, Vorteile, KPIs und mögliche Einnahmequellen betrachtet.

### Kanäle

Mögliche Kanäle:

- Social Media
- Online Communities
- Kooperationen mit Bildungsinstitutionen
- Content Marketing
- Empfehlungen durch Tutor:innen und Lernende
- Online-Werbung

### Unfairer Vorteil

LessonFlow kombiniert mehrere Elemente:

- selbstgesteuertes Lernen
- Tutorbegleitung
- verpflichtende Lernkontrollen
- strukturierter Lernpfad
- geplante KI-Unterstützung für Quizgenerierung

Diese Kombination unterscheidet LessonFlow von klassischen Videokursplattformen.

### KPI

Mögliche Kennzahlen:

- Anzahl registrierter Nutzer:innen
- Anzahl erstellter Kurse
- Anzahl aktiver Lernender pro Kurs
- Kursabschlussrate
- Anzahl durchgeführter Tutor-Meetings
- Anzahl bestandener Quizversuche
- wöchentliche aktive Nutzer:innen

### Einnahmequellen

Langfristig könnte LessonFlow als Plattform funktionieren, über die Tutor:innen kostenpflichtige Kurse anbieten. Die Plattform könnte eine Provision pro Kursverkauf erhalten. Zusätzlich wären Premiumfunktionen für Tutor:innen denkbar, zum Beispiel erweiterte Analysen oder KI-gestützte Tools.

## Diskussion Feedback Pitch

Dieser Abschnitt wird in Iteration 4 mit dem finalen Pitch-Feedback ergänzt.

Aktueller Stand:

- Der Fokus wurde bewusst auf einen klaren MVP gelegt.
- Der wichtigste fachliche Kern ist der geführte Lernprozess.
- KI-Funktionen werden vorbereitet, aber erst später vollständig integriert.
- Tutor- und Learner-Rollen wurden klar getrennt.
- Authentifizierung, CI/CD, Deployment und Tests wurden früh priorisiert, um technische Risiken zu reduzieren.

TODO für Iteration 4:

- erhaltenes Pitch-Feedback zusammenfassen
- beschreiben, welches Feedback umgesetzt wurde
- beschreiben, welches Feedback bewusst nicht umgesetzt wurde
- Bezug zum finalen Projektumfang herstellen

# Anforderungen

## Use-Case Diagramm

Das Use-Case Diagramm wird später als Bild in den Ordner `/doc` gelegt und hier eingebunden.

Geplanter Pfad:

`/doc/use-case-diagram.png`

Platzhalter:

![Use-Case Diagramm](doc/use-case-diagram.png)

## Use-Case Beschreibung

### Use Case 1: In Kurs einschreiben

| Feld | Beschreibung |
|---|---|
| Name | In Kurs einschreiben |
| Primärakteur | Lernende:r |
| Ziel | Lernende:r möchte einem Kurs beitreten |
| Vorbedingung | Lernende:r ist eingeloggt und besitzt die Rolle `learner` |
| Auslöser | Lernende:r klickt auf "Enroll in Course" |
| Standardablauf | System prüft Rolle, prüft Kurs, prüft bestehende Einschreibung, erstellt Enrollment und initialisiert LessonProgress |
| Ergebnis | Lernende:r ist eingeschrieben und die erste Lektion ist freigeschaltet |
| Fehlerfälle | Kurs existiert nicht, User ist kein Learner, User ist bereits eingeschrieben |

Code-Referenzen:

- `EnrollmentController.createEnrollment(...)`
- `EnrollmentService.createEnrollmentWithProgress(...)`

### Use Case 2: Lektion bearbeiten

| Feld | Beschreibung |
|---|---|
| Name | Lektion bearbeiten |
| Primärakteur | Lernende:r |
| Ziel | Lernende:r möchte die aktuelle Lektion bearbeiten |
| Vorbedingung | Lernende:r ist in einem Kurs eingeschrieben und eine Lektion ist `UNLOCKED` |
| Auslöser | Lernende:r öffnet den Learning Flow |
| Standardablauf | System zeigt die aktuelle Lektion an, Lernende:r bearbeitet Material und markiert es als erledigt |
| Ergebnis | Zustand wechselt von `UNLOCKED` zu `MATERIAL_DONE` |
| Fehlerfälle | Keine freigeschaltete Lektion vorhanden |

Code-Referenzen:

- `LessonProgressController.markMaterialDone(...)`
- `LessonProgressService.markMaterialDone(...)`

### Use Case 3: Meeting bestätigen

| Feld | Beschreibung |
|---|---|
| Name | Meeting bestätigen |
| Primärakteur | Tutor:in |
| Ziel | Tutor:in bestätigt, dass das Meeting zur Lektion stattgefunden hat |
| Vorbedingung | Lektion befindet sich im Zustand `MATERIAL_DONE` |
| Auslöser | Tutor:in bestätigt Meeting |
| Standardablauf | System prüft Zustand und setzt `meetingConfirmed` auf `true` |
| Ergebnis | Zustand wechselt zu `MEETING_DONE` |
| Fehlerfälle | Fortschritt existiert nicht oder Zustand ist nicht `MATERIAL_DONE` |

Code-Referenzen:

- `LessonProgressService.confirmMeeting(...)`
- Tutor-UI für diesen Schritt ist noch geplant.

### Use Case 4: Quizversuch einreichen

| Feld | Beschreibung |
|---|---|
| Name | Quizversuch einreichen |
| Primärakteur | Lernende:r |
| Ziel | Lernende:r möchte eine Lernkontrolle abschliessen |
| Vorbedingung | Lektion befindet sich im Zustand `MEETING_DONE` |
| Auslöser | Lernende:r sendet Quizversuch ab |
| Standardablauf | System prüft Quiz, Enrollment und LessonProgress, speichert den Versuch und berechnet, ob bestanden wurde |
| Ergebnis | Bei bestandenem Quiz wird die Lektion `PASSED` und die nächste Lektion wird freigeschaltet |
| Fehlerfälle | Quiz existiert nicht, Progress existiert nicht, Zustand ist nicht `MEETING_DONE`, User darf Enrollment nicht verwenden |

Code-Referenzen:

- `QuizAttemptController.submitQuizAttempt(...)`
- `QuizAttemptService.submitQuizAttempt(...)`
- `LessonProgressService.markPassed(...)`

### Use Case 5: Kurs erstellen

| Feld | Beschreibung |
|---|---|
| Name | Kurs erstellen |
| Primärakteur | Tutor:in |
| Ziel | Tutor:in möchte einen neuen Kurs erstellen |
| Vorbedingung | Tutor:in ist eingeloggt und besitzt die Rolle `tutor` |
| Auslöser | Tutor:in sendet Kursformular ab |
| Standardablauf | System prüft Rolle und erstellt Kurs mit Status `DRAFT` |
| Ergebnis | Kurs ist erstellt und gehört dem aktuellen Tutor |
| Fehlerfälle | User ist kein Tutor |

Code-Referenzen:

- `CourseController.createCourse(...)`
- `CourseRepository.findByTutorUserId(...)`

### Use Case 6: Lektion erstellen

| Feld | Beschreibung |
|---|---|
| Name | Lektion erstellen |
| Primärakteur | Tutor:in |
| Ziel | Tutor:in möchte einem Kurs eine Lektion hinzufügen |
| Vorbedingung | Tutor:in besitzt den Kurs |
| Auslöser | Tutor:in sendet Lektionsformular ab |
| Standardablauf | System prüft Rolle, Kurs, Ownership und eindeutige Lektionsnummer |
| Ergebnis | Lektion wird erstellt |
| Fehlerfälle | User ist kein Tutor, Kurs existiert nicht, Kurs gehört anderem Tutor, Lektionsnummer existiert bereits |

Code-Referenzen:

- `LessonController.createLesson(...)`
- `CourseService.courseBelongsToTutor(...)`

### Use Case 7: Quiz erstellen

| Feld | Beschreibung |
|---|---|
| Name | Quiz erstellen |
| Primärakteur | Tutor:in |
| Ziel | Tutor:in möchte ein Quiz für eine Lektion erstellen |
| Vorbedingung | Tutor:in besitzt den Kurs der Lektion |
| Auslöser | Tutor:in sendet Quizformular ab |
| Standardablauf | System prüft Rolle, Lektion, Ownership und ob bereits ein Quiz existiert |
| Ergebnis | Quiz wird erstellt |
| Fehlerfälle | User ist kein Tutor, Lektion existiert nicht, Kurs gehört anderem Tutor, Quiz existiert bereits |

Code-Referenzen:

- `QuizController.createQuiz(...)`
- `QuizRepository.findByLessonId(...)`

## Fachliches Datenmodell

Das fachliche Datenmodell wird später als ER-Modell im Ordner `/doc` abgelegt und hier eingebunden.

Geplanter Pfad:

`/doc/er-model.png`

Platzhalter:

![Fachliches Datenmodell](doc/er-model.png)

### Fachliche Entitäten

Das fachliche Modell enthält aktuell folgende zentrale Objekte:

- Kurs
- Lektion
- Quiz
- Einschreibung
- Lektionsfortschritt
- Quizversuch
- Benutzerrolle

### Kurs

Ein Kurs wird von einem Tutor erstellt und enthält mehrere Lektionen.

Wichtige fachliche Informationen:

- Titel
- Beschreibung
- Status
- Tutor-Zuordnung

### Lektion

Eine Lektion gehört zu einem Kurs und besitzt eine Reihenfolge.

Wichtige fachliche Informationen:

- Lektionsnummer
- Titel
- Lernmaterial
- Meeting-Link

### Quiz

Ein Quiz gehört zu einer Lektion.

Wichtige fachliche Informationen:

- Bestehensgrenze
- Fragen

### Einschreibung

Eine Einschreibung verbindet eine:n Lernende:n mit einem Kurs.

Wichtige fachliche Informationen:

- Kurs
- Lernende:r
- Status

### Lektionsfortschritt

Der Lektionsfortschritt beschreibt, in welchem Zustand sich eine Lektion für eine bestimmte Einschreibung befindet.

Wichtige fachliche Informationen:

- Zustand
- Meeting bestätigt
- Anzahl Quizversuche

### Quizversuch

Ein Quizversuch speichert das Ergebnis eines Lernenden für ein Quiz.

Wichtige fachliche Informationen:

- Punktzahl in Prozent
- bestanden / nicht bestanden

## Beschreibung der Zustände

### Kursstatus

Aktuell wird ein neuer Kurs immer mit folgendem Status erstellt:

- `DRAFT`

Code-Referenz:

- `CourseController.createCourse(...)`

### Lektionsfortschritt

Der wichtigste Zustandsautomat befindet sich beim Lektionsfortschritt.

Aktuelle Zustände:

- `LOCKED`
- `UNLOCKED`
- `MATERIAL_DONE`
- `MEETING_DONE`
- `PASSED`

Fachlicher Ablauf:

1. `LOCKED`: Die Lektion ist noch gesperrt.
2. `UNLOCKED`: Die Lektion ist freigeschaltet und kann bearbeitet werden.
3. `MATERIAL_DONE`: Das Lernmaterial wurde bearbeitet.
4. `MEETING_DONE`: Das Tutor-Meeting wurde bestätigt.
5. `PASSED`: Das Quiz wurde bestanden und die Lektion ist abgeschlossen.

Code-Referenzen:

- `LessonProgressState`
- `LessonProgressService.markMaterialDone(...)`
- `LessonProgressService.confirmMeeting(...)`
- `LessonProgressService.markPassed(...)`

## UI-Mockup

Das UI-Mockup wird später als Bild im Ordner `/doc` abgelegt und hier eingebunden.

Geplanter Pfad:

`/doc/ui-mockup.png`

Platzhalter:

![UI-Mockup](doc/ui-mockup.png)

Aktuell orientiert sich das UI an folgenden Bereichen:

- öffentliche Startseite
- Kursübersicht
- Kursdetails
- Login / Signup
- Account-Seite
- Learner Dashboard
- Learner Learning Flow
- Tutor Dashboard
- Tutor Course Management

# Implementation

## Frontend

Das Frontend ist mit SvelteKit umgesetzt. Für das Styling wird Bootstrap verwendet.

Aktuell implementierte Frontend-Bereiche:

- Home
- Login
- Signup
- Account
- Course Overview
- Course Detail
- Learner Dashboard
- Learner Learning Flow
- Tutor Dashboard
- Tutor Course Management

### Learner Frontend

Lernende können:

- Kurse ansehen
- Kursdetails öffnen
- sich in Kurse einschreiben
- eingeschriebene Kurse ansehen
- den Learning Flow öffnen
- Lernmaterial als erledigt markieren
- Meeting-Link öffnen
- Quizversuche einreichen, sobald der Fortschritt dies erlaubt

Geplante Screenshots:

- `/doc/frontend-screenshot-course-overview.png`
- `/doc/frontend-screenshot-learner-flow.png`

Platzhalter:

![Course Overview Screenshot](doc/frontend-screenshot-course-overview.png)

![Learner Flow Screenshot](doc/frontend-screenshot-learner-flow.png)

### Tutor Frontend

Tutor:innen können:

- eigene Kurse sehen
- neue Kurse erstellen
- Kurse verwalten
- Lektionen erstellen
- manuelle Quizzes erstellen
- bestehende Quizzes pro Lektion sehen

Geplanter Screenshot:

- `/doc/frontend-screenshot-tutor-flow.png`

Platzhalter:

![Tutor Flow Screenshot](doc/frontend-screenshot-tutor-flow.png)

## Backend

Das Backend ist mit Spring Boot umgesetzt und verwendet MongoDB Atlas als Datenbank.

### Controller

Aktuelle Controller:

- `CourseController`
- `LessonController`
- `EnrollmentController`
- `LessonProgressController`
- `QuizController`
- `QuizAttemptController`

### Services

Aktuelle Services:

- `CourseService`
- `LessonService`
- `EnrollmentService`
- `LessonProgressService`
- `QuizService`
- `QuizAttemptService`
- `UserService`

### Repositories

Aktuelle Repositories:

- `CourseRepository`
- `LessonRepository`
- `EnrollmentRepository`
- `LessonProgressRepository`
- `QuizRepository`
- `QuizAttemptRepository`

### Authentifizierung und Autorisierung

Die Authentifizierung erfolgt über Auth0 und JWT Tokens.

Aktuell umgesetzt:

- Login und Signup im Frontend
- Speicherung des JWT Tokens in Cookies
- Backend-Absicherung über OAuth2 Resource Server
- Rollenprüfung über `UserService`
- Ownership-Prüfungen für Tutor- und Learner-Daten

Beispiele für Rollen- und Ownership-Prüfungen:

- `CourseController.createCourse(...)`
- `LessonController.createLesson(...)`
- `QuizController.createQuiz(...)`
- `QuizAttemptController.submitQuizAttempt(...)`

## Core Learning Flow

Der zentrale Ablauf von LessonFlow ist im Backend umgesetzt.

### Enrollment

Beim Einschreiben in einen Kurs erstellt das System eine Einschreibung und initialisiert den Fortschritt für alle Lektionen.

Code-Referenzen:

- `EnrollmentController.createEnrollment(...)`
- `EnrollmentService.createEnrollmentWithProgress(...)`

### Lesson Progress

Der Lernfortschritt wird über Zustände gesteuert.

Code-Referenzen:

- `LessonProgressService.markMaterialDone(...)`
- `LessonProgressService.confirmMeeting(...)`
- `LessonProgressService.markPassed(...)`

### Quiz Attempt

Beim Quizversuch prüft das System, ob der Versuch bestanden wurde. Wenn ja, wird die Lektion abgeschlossen und die nächste Lektion freigeschaltet.

Code-Referenzen:

- `QuizAttemptController.submitQuizAttempt(...)`
- `QuizAttemptService.submitQuizAttempt(...)`

## KI-Funktionen

Die KI-Funktion ist für Iteration 4 geplant.

Geplante Aufgabe des KI-Modells:

- aus dem Lernmaterial einer Lektion automatisch Quizfragen generieren
- Tutor:in kann die generierten Fragen prüfen und übernehmen
- angenommene Fragen werden als Quiz gespeichert

Aktueller Stand:

- manuelle Quiz-Erstellung ist implementiert
- KI-generierte Quiz-Erstellung ist noch nicht implementiert
- Spring AI / OpenAI Integration wird später ergänzt

Geplante technische Referenz:

- Spring AI
- OpenAI API
- zukünftiger Service für Quizgenerierung

## Drittsysteme

### MongoDB Atlas

MongoDB Atlas wird als Cloud-Datenbank verwendet.

Konfiguration über Environment Variable:

- `MONGODB_URI`

Es werden keine echten Zugangsdaten committed.

### Auth0

Auth0 wird für Authentifizierung und Rollen verwendet.

Benötigte Rollen:

- `learner`
- `tutor`

Konfiguration über Environment Variables:

- `AUTH0_DOMAIN`
- `AUTH0_CLIENT_ID`

### GitHub Actions

GitHub Actions wird für CI/CD verwendet.

Aktuell vorhanden:

- Backend Tests
- Frontend Build
- JaCoCo Coverage
- JaCoCo Badge
- Docker Build
- Deployment nach Azure

Code-Referenzen:

- `.github/workflows/ci.yml`
- `.github/workflows/azure-container-webapp.yml`

### Azure App Service

Die Applikation wird als Docker Container auf Azure App Service deployed.

Aktuelle Azure App:

- `zhaw-lessonflow-spinalui`

URL:

- `https://zhaw-lessonflow-spinalui.azurewebsites.net`

## Optionale Anforderungen

Aktuell umgesetzt oder vorbereitet:

### Auth0 Authentication

Die Applikation verwendet Auth0 für Login, Signup und JWT-basierte Backend-Absicherung.

### Rollenbasierter Zugriff

Learner und Tutor haben unterschiedliche Berechtigungen.

Beispiele:

- Nur Tutor:innen können Kurse, Lektionen und Quizzes erstellen.
- Nur Lernende können sich einschreiben und Quizversuche einreichen.

### Ownership Checks

Das Backend prüft, ob Benutzer:innen auf bestimmte Daten zugreifen dürfen.

Beispiele:

- Tutor:in darf nur eigene Kurse verwalten.
- Lernende:r darf nur eigene Enrollments und Attempts verwenden.

### CI/CD

Das Projekt verfügt über GitHub Actions für Tests, Coverage und Deployment.

### Azure Deployment

Das Projekt wird automatisch als Docker Container nach Azure deployed.

## Testing und CI/CD

Aktueller Teststand:

- 90/90 Tests erfolgreich
- JaCoCo Gesamtcoverage: ca. 72%
- Branch Coverage: ca. 65%

Getestete Bereiche:

- Services
- Controller
- Rollenprüfungen
- Ownership-Prüfungen
- Core Learning Flow
- Quizversuche
- Enrollment-Erstellung
- Lektionsfortschritt

Lokaler Testbefehl:

```powershell
.\mvnw test
```

Coverage Report lokal generieren:

```powershell
.\mvnw verify
```

Coverage Report öffnen:

```text
target/site/jacoco/index.html
```

# Fazit

## Stand der Implementation

Aktuell ist ein funktionsfähiger MVP umgesetzt.

Umgesetzt:

- Backend mit Spring Boot
- MongoDB Atlas Integration
- Auth0 Authentication
- Rollenbasierter Zugriff
- Ownership-Prüfungen
- Kursverwaltung
- Lektionsverwaltung
- Enrollment Flow
- Learning Flow
- Quizverwaltung
- Quizversuche
- SvelteKit Frontend
- GitHub Actions CI
- JaCoCo Coverage
- Azure Deployment
- umfangreiche Backend Tests

Noch nicht final umgesetzt:

- KI-generierte Quizfragen
- Tutor Meeting Confirmation UI im Frontend
- finale Diagramme
- finale Screenshots
- finale Postman Dokumentation
- finale Auswertung des Pitch-Feedbacks

## Nächste Schritte

Die nächsten Schritte beziehen sich auf den Backlog und die geplante Iteration 4.

Geplante nächste Issues:

1. Tutor Meeting Confirmation UI umsetzen
2. KI-gestützte Quizgenerierung mit Spring AI / OpenAI integrieren
3. Testcoverage weiter Richtung 80% erhöhen
4. Use-Case Diagramm erstellen
5. Fachliches Datenmodell / ER-Modell erstellen
6. UI-Mockup oder UI-Skizze ergänzen
7. finale Screenshots der Applikation aufnehmen
8. Postman API Dokumentation verlinken
9. Pitch-Feedback final diskutieren
10. README für finale Abgabe überarbeiten

Diese README ist bewusst noch nicht die finale Dokumentation. Sie bereitet die geforderte Struktur vor und dokumentiert den aktuellen Stand so, dass sie in Iteration 4 effizient erweitert werden kann.