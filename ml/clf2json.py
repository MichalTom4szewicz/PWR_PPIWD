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
    win_length = wl
    undersampling = us
    win_step = win_length // ws_freq
    windowed_arr = []
    time_stamps = []
    for win_end in range(win_length, len(dataframe), win_step):
        win_start = win_end - win_length
        features = []
        is_consistent = True
        for j in range(win_start, win_end, undersampling):
            features = features + list(dataframe.iloc[j, 1:10])

        if is_consistent:
            windowed_arr.append(features)
            if win_end+win_step <= len(dataframe):
                time_stamps.append(dataframe.iloc[win_end]['time(sec)'])
            else:
                time_stamps.append(dataframe.iloc[-1]['time(sec)'])
       
    windowed_df = pd.DataFrame(windowed_arr)
    windowed_df.dropna(inplace=True)

    return windowed_df, time_stamps

def get_activity_dict(activity_name, count, start=0, end=0):
    return {
        "activity_name": activity_name,
        "count": count,
        "start": start,
        "end": end,
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

    period = 0

    for (idx, value) in enumerate(y_pred):
        if (idx == 0):
            classifications.append(get_activity_dict(activities.get(value), 1, start=idx, end=idx+1))
        elif (y_pred[idx] == y_pred[idx-1]):
            classifications[period]['count'] = classifications[period]['count'] + 1
            classifications[period]['end'] = classifications[period]['end'] + 1
        else:
            classifications.append(get_activity_dict(activities.get(value), 1, start=idx, end=idx+1))
            period = period + 1

    return classifications
