from datetime import datetime
import joblib
import json
import pandas as pd
import numpy as np
from io import StringIO

# args:
# dataframe - pandas dataframe with columns: 
# [
#   activity_type, repetitions, time(sec),
#   gyroscope_x(deg/sec), gyroscope_y(deg/sec), gyroscope_z(deg/sec),
#   accelerometer_x(g), accelerometer_y(g), accelerometer_z(g),
#   magnetometer_x(T), magnetometer_y(T), magnetometer_z(T)
# ]
# wl - length of time window in ms
# us - add every n-th sample
# ws_freq - window step frequency
def data2features(dataframe, wl, us, ws_freq):

    activities = {
        'boxing': 0,
        'jumping_jacks': 1,
        'inactivity': 2,
        'squats': 3
    }

    dataframe['activity_type'] = dataframe['activity_type'].map(activities)

    win_length = wl
    undersampling = us
    win_step = win_length // ws_freq

    windowed_arr = []
    time_stamps = []
    for win_end in range(win_length, len(dataframe), win_step):
        win_start = win_end - win_length
        activity = int(dataframe.iloc[win_start, 0])

        features = [activity]
        is_consistent = True
        for j in range(win_start, win_end, undersampling):
            if dataframe.iloc[j, 0] != activity:
                is_consistent = False
                break

            if j + undersampling < win_end and abs(dataframe.iloc[j, 0] - dataframe.iloc[j + undersampling, 0]) > undersampling * 0.01 + 0.001:
                is_consistent = False
                break

            features = features + list(dataframe.iloc[j, 3:12])

        if is_consistent:
            windowed_arr.append(features)
            if win_end+win_step <= len(dataframe):
                time_stamps.append(dataframe.iloc[win_end]['time(sec)'])
            else:
                time_stamps.append(dataframe.iloc[-1]['time(sec)'])
       
    windowed_df = pd.DataFrame(windowed_arr)
    windowed_df.dropna(inplace=True)
    features = windowed_df.iloc[:, 1:]
    
    return features, time_stamps

def count_activity(activity_name, count, start=0, end=0):
    return {
        "activity_name": activity_name,
        "count": count,
        "end": end,
        "start": start,
    }

# csv_string - csv content
# clf - "rf" | "dt" | "mlp" | "svm" | "cnn"
# win_length, undersampling, win_step_freq - data2features args

def clf2json(csv_string, clf = "rf"):
    # hardcoded values
    win_length = 100
    undersampling = 10
    win_step_freq = 4

    classifications = []

    csvStringIO = StringIO(csv_string)

    dataframe = pd.read_csv(csvStringIO, delimiter=',', header=0)

    X, time_stamps = data2features(dataframe, win_length, undersampling, win_step_freq)

    model = joblib.load('./models/' + clf + '.sav')

    y_pred = model.predict(X)

    activities = {
        0: 'boxing',
        1: 'jumping_jacks',
        2: 'inactivity',
        3: 'squats'
    }

    for activity in activities:
        try:
            start_idx = y_pred.tolist().index(activity)
            start = time_stamps[start_idx]
            if activity + 1 < len(activities):
                try:
                    end = time_stamps[y_pred.tolist().index(activity+1, start_idx)]
                except ValueError:
                    end = time_stamps[-1]
            else:
                end = time_stamps[-1]
        except ValueError:
            start = 0
            end = 0

        classifications.append(count_activity(activities.get(activity), np.count_nonzero(y_pred == activity), start=start, end=end))

    # return json.dumps(classifications)
    return classifications

# import argparse

# parser = argparse.ArgumentParser()

# parser.add_argument('--path')
# parser.add_argument('--user_id')
# parser.add_argument('--id')
# parser.add_argument('--clf')
# parser.add_argument('--wl')
# parser.add_argument('--us')
# parser.add_argument('--ws_freq')

# args = parser.parse_args()

# python clf2json.py --path='backend_data/squats/10/1.csv' --user_id=123 --id=321 --clf='rf' --wl=100 --us=10 --ws_freq=4
