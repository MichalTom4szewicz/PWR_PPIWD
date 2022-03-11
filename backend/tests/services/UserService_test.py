import unittest
import mongoengine
from mongoengine import connect, disconnect

from server.services.UserService import UserService
from server.models.User import User


class UserService_test(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        connect('mongoenginetest', host='mongomock://localhost')

    @classmethod
    def tearDownClass(cls):
        disconnect()

    def setUp(self):
        self.userService = UserService()

    def tearDown(self):
        User.drop_collection()

    def test_can_create_user(self):
        user = self.userService.createUser(
            email="test@test.com", firstName="Jan", lastName="Kowalski", password="test")

        self.assertIsNotNone(user)

    def test_cannot_create_with_duplicate_email(self):
        self.userService.createUser(
            email="test@test.com", firstName="Jan", lastName="Kowalski", password="test")

        self.assertRaises(mongoengine.errors.NotUniqueError, lambda: self.userService.createUser(
            email="test@test.com", firstName="Jan", lastName="Kowalski", password="test"))

    def test_can_find_existing_user(self):
        self.userService.createUser(
            email="test@test.com", firstName="Jan", lastName="Kowalski", password="test")

        user = self.userService.findUserByEmail("test@test.com")

        self.assertIsNotNone(user)

    def test_fail_when_not_existing(self):
        self.assertIsNone(self.userService.findUserByEmail("test@test.com"))

    def test_authenticate_user_with_correct_credentials(self):
        self.userService.createUser(
            email="test@test.com", firstName="Jan", lastName="Kowalski", password="test")

        self.assertTrue(self.userService.authenticate("test@test.com", "test"))

    def test_dont_authenticate_user_with_correct_credentials(self):
        self.userService.createUser(
            email="test@test.com", firstName="Jan", lastName="Kowalski", password="test")

        self.assertFalse(self.userService.authenticate(
            "test@test.com", "test2"))


if __name__ == "__main__":
    unittest.main()
