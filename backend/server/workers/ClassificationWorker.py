import logging
import threading
from server.services.MLService import MLService

from server.services.MeasurementService import MeasurementService

logger = logging.getLogger("ClassificationWorker")


class ClassificationWorker(threading.Thread):

    lock = threading.Lock()
    is_running = False

    def __init__(self, measurement_service: MeasurementService, ml_service: MLService):
        threading.Thread.__init__(self)
        self.measurement_service = measurement_service
        self.ml_service = ml_service

    def run(self):
        logger.debug("Starting ClassificationWorker...")
        ClassificationWorker.lock.acquire(blocking=True)
        ClassificationWorker.is_running = True
        ClassificationWorker.lock.release()

        for unprocessed in self._get_next_unprocessed():
            logger.debug(f"Processing measurement: {unprocessed.id}")
            try:
                classifications_raw = self.ml_service.get_measurement_classifications(
                    unprocessed)
                classifications = [
                    self.measurement_service.create_classification_from_dict(c) for c in classifications_raw
                ]
                self.measurement_service.classify_measurement(
                    unprocessed, classifications)
                logger.debug(
                    f"Processed measurement {unprocessed.id}")
            except Exception as e:
                logger.error(
                    f"Failed to process {unprocessed.id}, marking as invalid: {repr(e)}")
                self.measurement_service.mark_as_invalid(unprocessed)

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
