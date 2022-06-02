from server.models.Measurement import Measurement
from server.services.clf2json import clf2json


class MLService:
    def get_measurement_classifications(self, measurement: Measurement):
        classifications = clf2json(measurement.data)
        return classifications
