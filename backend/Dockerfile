FROM python:3.9-slim AS base

ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONFAULTHANDLER 1

FROM base AS python-deps

RUN pip install pipenv

COPY Pipfile .
COPY Pipfile.lock .
RUN PIPENV_VENV_IN_PROJECT=1 pipenv install

FROM base AS runtime
COPY --from=python-deps /.venv /.venv

ENV PATH="/.venv/bin:$PATH"
ENV FLASK_ENV="production"

VOLUME [ "/data" ]

EXPOSE 5000

WORKDIR /app
COPY ./server /app/server
COPY ./start.sh /app

ENTRYPOINT [ "bash", "./start.sh" ]
