from flask import Blueprint, Response, request, jsonify
from server.services.DatasetService import DatasetService

Training = Blueprint('training', __name__)

dataset_service = DatasetService.getDefaultInstance()


@Training.route('/training/measurement/<activity>', methods=['POST'])
def save_training_measurements(activity):
    if 'measurements' not in request.files:
        res = {'errorMessage': 'No \'measurements\' file part'}
        return res, 400
    csv_file = request.files['measurements']
    if not csv_file.filename.endswith(('.csv', '.tsv')):
        res = {'errorMessage': 'File format should be csv or tsv'}
        return res, 400
    csv_data = csv_file.read().decode("utf-8")
    dataset_service.saveMeasurement(activity, csv_data)
    res = {'message': 'CSV/TSV uploaded'}
    return res, 200
