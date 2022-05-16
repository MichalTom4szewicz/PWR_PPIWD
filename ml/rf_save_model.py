import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier
import joblib

dataframe = pd.read_csv('./data/PAMAP2_Extracted_300_25.csv')

X = dataframe.drop('0', axis=1)
y = dataframe['0']

X = dataframe.drop('0', axis=1)
y = dataframe['0']

# Best Random Forest combination - Acceleromter (+6g) + Magnetometr

acc_1_3_cols = []
gyroscope_cols = []

for i in range(1, X.shape[1], 12):
    acc_1_3_cols.append(str(i))
    acc_1_3_cols.append(str(i+1))
    acc_1_3_cols.append(str(i+2))

for i in range(7, X.shape[1], 12):
    gyroscope_cols.append(str(i))
    gyroscope_cols.append(str(i+1))
    gyroscope_cols.append(str(i+2))

X = X.drop(acc_1_3_cols, axis=1)
X = X.drop(gyroscope_cols, axis=1)

print(X.head(5))

X_train, X_test, y_train, y_test = train_test_split(X, y, random_state=123, test_size=0.20)

scaler = StandardScaler()

X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)
    
clf = RandomForestClassifier(max_depth = 50, random_state = 123)
clf.fit(X_train, y_train)

filename = './models/rf_model_300_25.sav'
joblib.dump(clf, filename)
