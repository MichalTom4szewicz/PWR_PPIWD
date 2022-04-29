docker-compose up -d mongo mongo-express
set FLASK_APP=server
set FLASK_ENV=development
flask run --host="0.0.0.0"
