# Backend
W tym katalogu znajduje się część backendowa projektu

## Założenia
Serwer REST zrealizowany przy użyciu:
* Python 3.10
* flask (nie narzucam, ale tylko flaska w Pythonie znam, więc jak ktoś ma inną propozycję, to jestem zainteresowany)

Zależności zarządzane przy użyciu [pipenv](https://pipenv.pypa.io/en/latest/)

## Instalacja
``` bash
# Do korzystania
pipenv install

# Do developmentu (dodaje np. autopep8)
pipenv install --dev
```

## Uruchamianie
W zależności od środowiska (bash, cmd, powershell) musicie ustawić:
* zmienne środowiskowe:
  * FLASK_APP=server
* wywołać: ``flask run``
* serwer powinien nasłuchiwać na porcie HTTP 5000

### Linux
Dla Linuxa będę tworzył też skrypty i Makefile

### Docker
W przyszłości może powstanie Dockerfile, żeby uruchamiać całość w kontenerze

## Struktura (COMING SOON)

## API (COMING SOON)