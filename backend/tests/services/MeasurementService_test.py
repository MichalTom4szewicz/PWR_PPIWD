from datetime import datetime
import textwrap
import unittest

from mongoengine import connect, disconnect

from server.services.MeasurementService import MeasurementService
from server.models.Measurement import Measurement
from server.models.User import User


class MeasurementServiceTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        connect('mongoenginetest', host='mongomock://localhost')

    @classmethod
    def tearDownClass(cls):
        disconnect()

    def setUp(self) -> None:
        self.measurementService = MeasurementService()

    def tearDown(self):
        Measurement.drop_collection()
        User.drop_collection()

    def test_can_create_measurement(self):
        user = User(
            email="jan.kowalski@gmail.com",
            password="password",
            firstName="Jan",
            lastName="Kowalski"
        )
        user = user.save()

        data = textwrap.dedent("""
        123 123 123
        321 321 321
        """)

        self.measurementService.create_measurement(
            data=data, user=user)

    def test_can_query_the_measurements(self):
        user = User(
            email="jan.kowalski@gmail.com",
            password="password",
            firstName="Jan",
            lastName="Kowalski"
        )
        user = user.save()

        data = textwrap.dedent("""
        123 123 123
        321 321 321
        """)

        self.measurementService.create_measurement(
            data=data, user=user)

        user_measurements = self.measurementService.find_by_user(user)

        self.assertEqual(len(user_measurements), 1)
        self.assertEqual(user_measurements[0].data, data)
        self.assertEqual(user_measurements[0].user, user)

    def test_can_classify(self):
        user = User(
            email="jan.kowalski@gmail.com",
            password="password",
            firstName="Jan",
            lastName="Kowalski"
        )
        user = user.save()

        data = textwrap.dedent("""
        123 123 123
        321 321 321
        """)

        measurement = Measurement(user=user, data=data)
        measurement = measurement.save()

        classifications = []

        for i in range(5):
            classification = self.measurementService.create_classification(
                start=i*10, end=(i+1)*10, activity_name=f"activity-{i}")
            classifications.append(classification)

        measurement = self.measurementService.classify_measurement(
            measurement=measurement, classifications=classifications)

        self.assertEqual(measurement.data, data)
        self.assertEqual(measurement.user, user)
        self.assertEqual(len(measurement.classifications), 5)

        for i in range(5):
            self.assertEqual(
                measurement.classifications[i], classifications[i])

    def test_get_next_unprocessed(self):
        user = User(
            email="jan.kowalski@gmail.com",
            password="password",
            firstName="Jan",
            lastName="Kowalski"
        )
        user = user.save()

        data = textwrap.dedent("""
        123 123 123
        321 321 321
        """)

        measurement1 = Measurement(user=user, data=data)
        measurement1.save()
        measurement2 = Measurement(
            user=user, data=data, processed_at=datetime.now())
        measurement2 = measurement2.save()

        unprocessed = self.measurementService.get_next_unprocessed()

        self.assertEqual(unprocessed, measurement1)

        measurement1.processed_at = datetime.now()
        measurement1.save()

        unprocessed = self.measurementService.get_next_unprocessed()

        self.assertIsNone(unprocessed)


if __name__ == "__main__":
    unittest.main()
