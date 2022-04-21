from datetime import datetime
from typing import Optional
from server.models.Measurement import Measurement, MeasurementClassification
from server.models.User import User


class MeasurementService:

    def create_measurement(self, data: str, user: User):
        measurement = Measurement(data=data, user=user)

        return measurement.save()

    def classify_measurement(self, measurement: Measurement, classifications: list[MeasurementClassification]) -> Measurement:
        measurement.classifications = classifications
        measurement.processed_at = datetime.now()

        return measurement.save()

    def create_classification(self, start: float, end: float, activity_name: str, count: Optional[int] = None):
        return MeasurementClassification(start=start, end=end, activity_name=activity_name, count=count)

    def find_by_user(self, user: User) -> list[Measurement]:
        return Measurement.objects(user=user)

    def get_next_unprocessed(self) -> Optional[Measurement]:
        unprocessed = Measurement.objects(processed_at=None).limit(1)
        return unprocessed[0] if len(unprocessed) else None
