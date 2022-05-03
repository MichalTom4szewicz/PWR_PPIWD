from flask import Blueprint, Response, request, jsonify, send_file
from server.models.User import User
from server.services.MeasurementService import MeasurementService
from server.services.JwtService import JwtService

Measurement = Blueprint('measurements', __name__)

measurement_service = MeasurementService()
jwt_service = JwtService()


@Measurement.route('/measurements', methods=['POST'])
@jwt_service.token_required
def save_unprocessed_measurement(user: User):
    if 'measurements' not in request.files:
        res = {'errorMessage': 'No \'measurements\' file part'}
        return res, 400
    csv_file = request.files['measurements']
    if not csv_file.filename.endswith(('.csv', '.tsv')):
        res = {'errorMessage': 'File format should be csv or tsv'}
        return res, 400

    csv_data = csv_file.read().decode("utf-8")
    measurement_service.create_measurement(csv_data, user)
    res = {'message': 'Unprocessed measurement has been saved'}
    return res, 200


@Measurement.route('/measurements', methods=['GET'])
@jwt_service.token_required
def get_all_user_measurements(user: User):
    user_measurements = measurement_service.find_by_user(user)
    return jsonify(user_measurements), 200


@Measurement.route('/measurements/summary', methods=['GET'])
@jwt_service.token_required
def get_processed_user_measurements(user: User):
    user_measurements = measurement_service.find_processed_by_user(user)
    return jsonify(user_measurements), 200
