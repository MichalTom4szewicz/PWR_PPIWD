import os

from flask import Flask
from flask_cors import CORS
from flask_mongoengine import MongoEngine
from server.services.UserService import UserService

from server.models.User import User


def create_app():
    app = Flask(__name__, instance_relative_config=True)

    CORS(app)
    app.config["MONGODB_SETTINGS"] = {
        'db': 'ppiwd'
    }
    MongoEngine(app)

    return app
