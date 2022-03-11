import os
import hashlib
import logging

from server.models.User import User


class UserService:

    def createUser(self, email: str, firstName: str, lastName: str, password: str):
        """
        This method creates a new user account. It will raise an exception when required fields are not present or there's a duplicate email
        """
        password_hash_salt = self.__hash_and_salt(password)
        user = User(email=email, firstName=firstName,
                    lastName=lastName, password=password_hash_salt)
        user.save()

        return user

    def findUserByEmail(self, email: str) -> User:
        """This method returns a user object that has the given email. It returns None when no user is found"""
        return User.objects(email=email).first()

    def authenticate(self, email: str, password: str):
        """This method returns whether the authentication for given credentials was successful"""
        user = self.findUserByEmail(email)

        return self.__compare_passwords(password, user.password)

    def __hash_and_salt(self, password: str):
        salt = os.urandom(16)
        hashed = self.__hash(password, salt)
        hashed_salt = f"{salt.hex()}${hashed.hex()}"

        return hashed_salt

    def __hash(self, password: str,  salt: bytes):
        return hashlib.pbkdf2_hmac(
            "sha512", password.encode('utf8'), salt, iterations=2000)

    def __compare_passwords(self, candidate: str, password: str):
        salt, hash = password.split('$')
        hashed_candidate = self.__hash(candidate, bytes.fromhex(salt))

        return bytes.fromhex(hash) == hashed_candidate
