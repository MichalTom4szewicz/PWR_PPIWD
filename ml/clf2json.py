from datetime import datetime
import joblib
import json

# data - csv file path (?) / csv file (?)
# clf - "rf" | "dt" | "mlp" | "svm" | "cnn"

def clf2json(data, user_id, id, clf):
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

    # Accelerometr (X,Y,Z) + Magnetometr (X,Y,Z) => 6 values x N (based on window size and undersampling frequency)
    # Actually it cant be like that becuase every clf may need different data
    # Maybe just use Accelerometr / Magnetometr / Gyroscope for all of them (?)
    # Then  Accelerometr (X,Y,Z) + Magnetometr (X,Y,Z) + Magnetometr (X,Y,Z)

    X_test = data

    model = joblib.load('./models/' + clf + '.sav')

    # y_pred = model.predict(X_test)

    # TODO: map y_pred to classifications

    return json.dumps([response])

print(clf2json([], 123, 123))
