import logging
import os

from flask import Flask
from flask_cors import CORS
from flask_mongoengine import MongoEngine
from flask_restful import Api, Resource, reqparse
from server.services.UserService import UserService

from server.models.User import User
from server.blueprints.Training import Training
from server.blueprints.Auth import Auth
from server.blueprints.Measurement import Measurement
from server.config import config


def create_app():
    logging.basicConfig(level=config.logging_level)
    app = Flask(__name__, instance_relative_config=True)

    CORS(app)
    app.config["MONGODB_SETTINGS"] = {
        'db': config.db_config.db,
        'host': config.db_config.host
    }
    MongoEngine(app)

    app.register_blueprint(Training)
    app.register_blueprint(Auth)
    app.register_blueprint(Measurement)

    return app
