import logging
import os
from flask import Blueprint, Response, request, jsonify
from server.services.UserService import UserService
from server.services.JwtService import JwtService

Auth = Blueprint('auth', __name__)

user_service = UserService()
jwt_service = JwtService()


@Auth.route('/auth/login', methods=['POST'])
def login_user():
    token = None
    email = None
    password = None

    body = request.get_json()
    if "email" in body and "password" in body:
        email = body["email"]
        password = body["password"]
    else:
        return {"errorMessage": "Request body must include email and password"}, 400

    user = user_service.authenticate(email, password)
    if user:
        token = jwt_service.generate_jwt_token(user.id)
    else:
        return {"errorMessage": "Couldn't authenticate users with given credentials"}, 401

    if token:
        return {"token": token}, 200
    else:
        return {"errorMessage": 'Error occurred during JWT token generation'}, 500


@Auth.route('/auth/register', methods=['POST'])
def register_user():
    return None
