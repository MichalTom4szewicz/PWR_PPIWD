import logging
import threading

from server.services.MeasurementService import MeasurementService

logger = logging.getLogger("ClassificationWorker")


class ClassificationWorker(threading.Thread):

    lock = threading.Lock()
    is_running = False

    def __init__(self, measurement_service: MeasurementService):
        self.measurement_service = measurement_service

    def start(self):
        logger.debug("Starting ClassificationWorker...")
        ClassificationWorker.lock.acquire(blocking=True)
        ClassificationWorker.is_running = True
        ClassificationWorker.lock.release()

        for unprocessed in self._get_next_unprocessed():
            logger.debug(f"Processing measurement: {unprocessed.sent_at}")
            classifications = [self.measurement_service.create_classification(
                start=0, end=10, activity_name="activity")]
            self.measurement_service.classify_measurement(
                unprocessed, classifications)

        logger.debug("No more unprocessed measurements found")
        ClassificationWorker.lock.acquire(blocking=True)
        ClassificationWorker.is_running = False
        ClassificationWorker.lock.release()

    def _get_next_unprocessed(self):
        while True:
            unprocessed = self.measurement_service.get_next_unprocessed()

            if unprocessed:
                yield unprocessed
            else:
                return
