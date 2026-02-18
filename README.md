# Bandi Production Suite (MVP)

Desktopowa aplikacja (Python + Tkinter) przygotowana pod dalszą rozbudowę o 8 głównych działów:

- Pozycje
- Szkolenia
- OJT
- Navodki
- Rework
- Odkaz
- Ukoly
- Reporty

## Co działa teraz

- Ekran główny z 8 działami (mega design dark UI).
- Dział **Pozycje** z dwoma kafelkami: **Operatorzy** i **Pozycje**.
- Sekcja **Operatorzy**:
  - Dodawanie operatora (Imię, Nazwisko, ID, Zmiana).
  - Dostępne zmiany: `A1, A2, B1, B2, C1, C2, JIS A, JIS B, JIS C`.
  - Filtrowanie po zmianach + widok **Wszyscy**.
  - Trwały zapis danych do pliku `app_data.json` (w katalogu aplikacji).

## Uruchomienie

```bash
python app.py
```

## Budowa EXE

1. Zainstaluj pyinstaller:

```bash
pip install pyinstaller
```

2. Zbuduj plik EXE:

```bash
pyinstaller --noconfirm --onefile --windowed --name bandi_app app.py
```

Gotowy plik będzie w katalogu `dist/bandi_app.exe`.
