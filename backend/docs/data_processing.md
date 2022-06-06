# Przetwarzanie danych
Zalogowani użytkownicy będą mogli wysłać pomiary do analizy, czyli klasyfikacji
swojego treningu.

## Propozycja
Niech proces przetwarzania zaczyna się po otrzymaniu zapytania od użytkownika.
Ponieważ w trakcie przetwarzania tego zapytania, inni użytkownicy mogą przesłać swoje pomiary,
te pomiary muszą być przechowywane w bazie danych i oznaczone jako nieprzetworzone. Klasyfikacja może być wymagająca sprzętowo, dlatego nie będzie przetwarzania równoległego.

Proces przetwarzania nie zakończy się dopóki w bazie danych nie będzie już więcej rekordów oznaczonych
jako "nieprzetworzone". Po skończeniu przetwarzania jednego pomiaru, z bazy danych wyciągnie kolejny jeszcze nieprzetworzony.

### Proces odbierania zapytania
```mermaid
flowchart
  a[Użytkownik kończy pomiar i wysyła zapytanie]
  b[Dodanie pomiaru do bazy danych jako nieprzetworzone]
  c[Wywołanie przetwarzania pomiarów]
  d[Odesłanie wiadomości o poprawnym odebraniu pomiaru]

  a --> b
  b --> c
  c --> d
```

### Proces przetwarzania pomiarów
```mermaid
flowchart
  g{{Czy jest blokada przetwarzania?}}
  h[Utwórz blokadę]
  a[/Wywołanie przetwarzania pomiarów/]
  b[Pobranie najstarszego nieprzetworzonego pomiaru]
  c[Klasyfikacja pomiaru]
  d[Zapisanie klasyfikacji do BD]
  e{{Czy są nieprzetworzne pomiary?}}
  i[Usuń blokadę]
  f[/Koniec/]

  a --> g
  g -->|nie| h
  g -->|tak| f
  h --> b
  e -->|tak| b
  e -->|nie| i
  i --> f
  b --> c
  c --> d
  d --> e

```

```mermaid
sequenceDiagram
  participant U as User
  participant HC as HTTPController
  participant CS as ClassificationService
  participant CW as ClassificationWorker
  participant ML as MLService
  participant m as MeasurementService

  U->>+HC: send measurement
  HC->>m: save unprocessed measurement
  par
    HC->>+CS: trigger processing
  and
    HC->>U: measurement saved
  end
  opt thread is not running yet
    CS->>+CW: start thread
    loop there are unprocessed measurements
      CW->>m: fetch oldest unprocessed measurement
      m-->>CW: return measurement
      CW->>ML: classify measurement
      CW->>-m: save classification
    end
  end
```

# ERD
```mermaid
erDiagram
  User {
    string id
    string firstName
    string lastName
    string email
    string password
    date created_at
    date updated_at
  }

  Measurement {
    string id
    string user
    date sent_at
    date processed_at
    string data
  }

  MeasurementClassification {
    string activity_name
    float start
    float end
    int count
  }

  User ||--o{ Measurement : has
  Measurement ||--o{ MeasurementClassification : has
```
