from flask import Blueprint, request, jsonify
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
    body = request.get_json()
    if not("email" in body and "password" in body and "firstName" in body and "lastName" in body):
        return {"errorMessage": "Request body must include email, password, firstName and lastName"}, 400

    str_body = request.data.decode('UTF-8')
    try:
        created_user = user_service.createUserFromJSON(str_body)
        if created_user:
            return jsonify(created_user), 201
        else:
            return {"errorMessage": 'Error occurred during register process'}, 500
    except Exception as e:
        return {"errorMessage": 'Couldn\'t register user. ' + str(e)}, 400


@Auth.route('/auth/me', methods=['GET'])
@jwt_service.token_required
def verify_token(user):
    return jsonify(user), 200
