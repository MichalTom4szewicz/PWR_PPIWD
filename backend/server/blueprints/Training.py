import logging
import os
from flask import Blueprint, Response, request, jsonify, send_file
from server.services.DatasetService import DatasetService
from server.services.JwtService import JwtService

Training = Blueprint('training', __name__)

dataset_service = DatasetService.getDefaultInstance()
jwt_service = JwtService()


@Training.route('/training/measurement/<activity>', methods=['POST'])
def save_training_measurements(activity):
    if 'measurements' not in request.files:
        res = {'errorMessage': 'No \'measurements\' file part'}
        return res, 400
    csv_file = request.files['measurements']
    if not csv_file.filename.endswith(('.csv', '.tsv')):
        res = {'errorMessage': 'File format should be csv or tsv'}
        return res, 400

    if 'count' not in request.args or not request.args['count'].strip():
        return {'errorMessage': 'You must provide the count argument'}, 400

    csv_data = csv_file.read().decode("utf-8")
    count = request.args['count'].strip()
    dataset_service.saveMeasurement(activity, count, csv_data)
    res = {'message': 'CSV/TSV uploaded'}
    return res, 200


@Training.route('/training/download', methods=['GET'])
def download_training_dataset():
    try:
        path_to_archive = dataset_service.exportTarGZ()
        if not path_to_archive.startswith("/"):
            path_to_archive = os.path.join(os.getcwd(), path_to_archive)
        return send_file(path_to_archive, as_attachment=True)
    except Exception as e:
        logging.error(e)
        return {'errorMessage': 'Error during exporting the dataset'}, 500
