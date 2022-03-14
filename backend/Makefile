start_docker:
	docker-compose up -d

start_flask_dev:
	-FLASK_ENVIRONMENT=development ./start.sh

start_dev : start_docker start_flask_dev

stop_docker:
	docker-compose stop

clean_docker:
	docker-compose down

clean:
	find . -name "__pycache__" -type d -prune -exec rm -rf {} \;

clean_all: clean clean_docker

test:
	python3 -m unittest discover -p "*_test.py"
