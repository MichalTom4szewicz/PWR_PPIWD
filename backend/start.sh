export FLASK_APP=server
export FLASK_ENV=${FLASK_ENV:-"development"}

flask run --host="0.0.0.0"
