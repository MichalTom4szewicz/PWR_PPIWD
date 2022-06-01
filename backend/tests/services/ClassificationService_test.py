import logging
import time
import unittest

from datetime import datetime
from typing import Optional
from server.workers.ClassificationWorker import ClassificationWorker
from server.services.ClassificationService import ClassificationService
from server.models.Measurement import Measurement, MeasurementClassification
from server.models.User import User


class MeasurementServiceMock:

    def __init__(self, measurement_count=5):
        mock_user = User(
            email="jan.kowalski@gmail.com",
            password="password",
            firstName="Jan",
            lastName="Kowalski"
        )
        self.measurements = [self.create_measurement(
            data="123", user=mock_user) for _ in range(measurement_count)]
        self.last_measurement = 0

    def create_measurement(self, data: str, user: User):
        measurement = Measurement(data=data, user=user)

        return measurement

    def classify_measurement(self, measurement: Measurement, classifications: list[MeasurementClassification]) -> Measurement:
        measurement.classifications = classifications
        measurement.processed_at = datetime.now()

        return measurement

    def create_classification(self, start: float, end: float, activity_name: str, count: Optional[int] = None):
        return MeasurementClassification(start=start, end=end, activity_name=activity_name, count=count)

    def create_classification_from_dict(self, c_dict: dict) -> MeasurementClassification:
        return MeasurementClassification(start=c_dict['start'], end=c_dict['end'], activity_name=c_dict['activity_name'], count=c_dict['count'])

    def get_next_unprocessed(self) -> Optional[Measurement]:
        if self.last_measurement < len(self.measurements):
            unprocessed = self.measurements[self.last_measurement]
            self.last_measurement += 1
            return unprocessed


class MLServiceMock:
    def get_measurement_classifications(self, measurement: Measurement):
        return [{
            'activity_name': 'activity1',
            'count': 0,
            'start': 0,
            'end': 10
        }]


class ClassificationServiceTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        logger = logging.getLogger('ClassificationWorker')
        handler = logging.StreamHandler()
        logger.addHandler(handler)
        logger.setLevel(logging.DEBUG)

    def test_does_process_measurements(self):
        mock_measurement_service = MeasurementServiceMock()
        mock_ml_service = MLServiceMock()
        classification_service = ClassificationService(
            measurement_service=mock_measurement_service, ml_service=mock_ml_service)

        classification_service.trigger_processing()

        while ClassificationWorker.is_running:
            time.sleep(0.25)

        for m in mock_measurement_service.measurements:
            self.assertIsNotNone(m.processed_at)
            self.assertEqual(len(m.classifications), 1)

    def test_can_run_only_single_instance(self):
        mock_service = MeasurementServiceMock()
        mock_ml_service = MLServiceMock()
        classification_service = ClassificationService(
            measurement_service=mock_service, ml_service=mock_ml_service)

        ClassificationWorker.is_running = True
        self.assertFalse(classification_service.trigger_processing())
        ClassificationWorker.is_running = False


if __name__ == "__main__":
    unittest.main()
