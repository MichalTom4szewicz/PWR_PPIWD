import os
import hashlib
import logging
import json
import jwt
import datetime
from functools import wraps
from flask import request

from server.models.User import User
from server.services.UserService import UserService
from server.config import config


class JwtService:
    def __init__(self):
        self.auth_config = config.auth_config
        self.user_service = UserService()

    def generate_jwt_token(self, user_id):
        jwt_payload = {
            "sub": str(user_id),
            'iat': datetime.datetime.utcnow(),
            "exp": datetime.datetime.utcnow() + datetime.timedelta(minutes=45)
        }
        encoded = jwt.encode(
            jwt_payload, self.auth_config.secret_key, algorithm="HS256")
        return encoded

    # Make sure to include the "user" parameter in the wrapped function
    def token_required(self, f):
        @wraps(f)
        def decorator(*args, **kwargs):
            token = None
            if 'Authorization' in request.headers:
                auth_header = request.headers['Authorization']
                if auth_header:
                    token = auth_header.split(" ")[1]
                else:
                    token = ''

            if not token:
                return {'errorMessage': 'a valid token is missing'}, 401
            try:
                data = jwt.decode(
                    token, self.auth_config.secret_key, algorithms=["HS256"])
                current_user = self.user_service.findById(data['sub'])
                kwargs['user'] = current_user
            except Exception as e:
                return {'errorMessage': 'token is invalid'}, 401
            return f(*args, **kwargs)
        return decorator
