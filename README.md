# bandi

Mobilna aplikacja na Androida do nauki języka angielskiego z naciskiem na **perfekcyjną wymowę**, **rozumienie ze słuchu** i **pisownię**.

## Cel projektu
Aplikacja ma prowadzić użytkownika przez lekcje od podstaw do poziomu zaawansowanego. Kluczowym celem jest **perfekcyjne mówienie po angielsku** (wymowa, intonacja, płynność). Nauka obejmuje:
- prezentację słówek i ich pisowni,
- odsłuch poprawnej wymowy (text-to-speech),
- pytania zadawane po angielsku,
- odpowiedzi użytkownika przez mikrofon,
- sprawdzanie poprawności wymowy (speech-to-text / ocena wymowy),
- materiały wizualne (obrazki/ilustracje) wspierające zapamiętywanie.

## Główne funkcje (priorytet: mowa)
- Ćwiczenia wymowy z natychmiastową oceną.
- Dialogi głosowe (pytanie → odpowiedź → feedback).
- Nagrywanie i odsłuch własnej mowy.
- Porównanie wymowy użytkownika z wzorcem.
- Rozpoznawanie mowy i wskazówki do poprawy.

## Zakres funkcjonalny (MVP)
- Struktura kursu w modułach/lekcjach (podstawowy → średniozaawansowany → zaawansowany).
- Lekcje słownictwa z obrazkami i odsłuchem wymowy.
- Quizy i pytania głosowe.
- Rozpoznawanie mowy i weryfikacja poprawności wypowiedzi.
- Postęp użytkownika (zaliczone lekcje, wyniki testów).

## Proponowana architektura aplikacji
- **UI**: Kotlin + Jetpack Compose.
- **Logika domenowa**: warstwa use-case / interactor.
- **Dane**: lokalna baza (np. Room) + opcjonalna synchronizacja w chmurze.
- **Audio**: Android TextToSpeech + SpeechRecognizer (lub zewnętrzne API dla lepszej jakości).
- **Multimedia**: katalog zasobów z obrazkami do słownictwa.

## Aktualny stan
- Gotowy szkielet aplikacji Android z Jetpack Compose.
- Ekran główny z listą lekcji i sekcją mówienia.
- Ekran lekcji z realnym **TTS/STT** (odsłuch + nagrywanie).
- Rozbudowany scoring wymowy (dopasowanie słów + przybliżone fonemy).
- Ekran ćwiczeń lekcji (shadowing, Q&A, płynność).
- Podstawowe grafiki (assets) do lekcji.

## Co dalej? (Plan działania)
1. **Projekt UX/UI**
   - Makiety ekranów: wybór poziomu, lista lekcji, ekran lekcji, ekran ćwiczeń głosowych.
2. **Struktura treści**
   - Zdefiniowanie modułów i zakresu materiału (słownictwo, gramatyka, dialogi).
3. **Prototyp funkcji głosowych**
   - Integracja TTS + SpeechRecognizer.
4. **Dodanie zasobów wizualnych**
   - Obrazki dla słownictwa (np. w formacie WebP w katalogu `assets/`).
5. **Budowa podstawowej aplikacji**
   - Nawigacja, lista lekcji, ekran lekcji i pierwsze ćwiczenia.

## Proponowana struktura repozytorium
- `app/` – moduł Android (UI, logika, nawigacja)
- `assets/` – obrazki do słownictwa
- `docs/` – dokumentacja, plan lekcji, makiety UI

## Uruchomienie
1. Otwórz projekt w Android Studio.
2. Poczekaj na synchronizację Gradle.
3. Uruchom konfigurację `app` na emulatorze lub urządzeniu.

## Jak współtworzyć
Jeśli chcesz pomóc w rozwoju, opisz proszę:
- poziom docelowy użytkownika,
- główne funkcje, na których zależy najbardziej,
- preferencje dotyczące technologii.
