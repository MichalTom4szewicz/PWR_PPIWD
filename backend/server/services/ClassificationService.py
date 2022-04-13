from server.services.MeasurementService import MeasurementService
from server.models.Measurement import Measurement
from server.workers.ClassificationWorker import ClassificationWorker


class ClassificationService:

    def __init__(self, measurement_service: MeasurementService):
        self.measurement_service = measurement_service

    def trigger_processing(self) -> bool:
        if not ClassificationWorker.is_running:
            worker = ClassificationWorker(self.measurement_service)
            worker.start()
            return True
        return False
