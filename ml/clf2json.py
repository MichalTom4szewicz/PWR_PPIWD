from datetime import datetime
import joblib
import json

def clf2json(data, user_id, id):
    response = {
        "_id": {
            "$oid": id
        },
        "classifications": [],
        "data": "???",
        "processed_at": {
            "$date": datetime.now().strftime("%Y-%m-%dT%H:%M:%SZ")
        },
        "send_at": {
            "$date": datetime.now().strftime("%Y-%m-%dT%H:%M:%SZ")
        },
        "user": {
            "$oid": user_id
        },
    }

    # TODO: Transform data to X_test

    # Data Format

    # Accelerometr (X,Y,Z) + Magnetometr (X,Y,Z) => 6 values x 12 (window - 3s 4Hz) = 72 columns

    X_test = data

    # model = joblib.load('./models/rf_model_300_25.sav')
    # y_pred = model.predict(X_test)

    # TODO: map y_pred to classifications

    return json.dumps([response])

print(clf2json([], 123, 123))
