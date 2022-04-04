import os

from flask import Flask
from flask_cors import CORS
from flask_mongoengine import MongoEngine
from flask_restful import Api, Resource, reqparse
from server.services.UserService import UserService

from server.models.User import User
from server.blueprints.Training import Training
from server.blueprints.Auth import Auth

def create_app():
    app = Flask(__name__, instance_relative_config=True)

    CORS(app)
    app.config["MONGODB_SETTINGS"] = {
        'db': 'ppiwd'
    }
    MongoEngine(app)

    app.register_blueprint(Training)
    app.register_blueprint(Auth)

    return app
