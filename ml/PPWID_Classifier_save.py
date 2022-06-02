import pandas as pd

from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

from sklearn.neural_network import MLPClassifier
from sklearn.metrics import confusion_matrix, accuracy_score, precision_score, recall_score

import joblib

RANDOM_STATE = 331

dataframe = pd.read_csv('./backend_data/PPWID_100_10.csv')

X = dataframe.drop('0', axis=1)
y = dataframe['0']

hidden_layer_sizes = [64, 32, 16]

X_train, X_test, y_train, y_test = train_test_split(X, y, random_state=RANDOM_STATE, test_size=0.25)

scaler = StandardScaler()

X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)

clf = MLPClassifier(hidden_layer_sizes=hidden_layer_sizes, random_state=RANDOM_STATE)
clf.fit(X_train, y_train)

y_pred = clf.predict(X_test)

acc = accuracy_score(y_test, y_pred)
p = precision_score(y_test, y_pred,average=None).mean()
r = recall_score(y_test, y_pred, average=None).mean()

print("Accuracy: %.3f \nPrecision: %.3f \nRecall: %.3f" % (acc, p, r) )

cm = confusion_matrix(y_test, y_pred, normalize = 'true')

print(cm)

model_filename = './models/PPWID_classifier.sav'
joblib.dump(clf, model_filename)
print("Model saved to:", model_filename)

scaler_filename = './preprocessing/PPWID_scaler.sav'
joblib.dump(scaler, scaler_filename)
print("Scaler saved to:", scaler_filename)

print("\n-----Loading test-----\n")

clf = joblib.load(model_filename)
scaler = joblib.load(scaler_filename)

X_train, X_test, y_train, y_test = train_test_split(X, y, random_state=RANDOM_STATE, test_size=0.25)
X_test = scaler.transform(X_test)

y_pred = clf.predict(X_test)

acc = accuracy_score(y_test, y_pred)
p = precision_score(y_test, y_pred,average=None).mean()
r = recall_score(y_test, y_pred, average=None).mean()

print("Accuracy: %.3f \nPrecision: %.3f \nRecall: %.3f" % (acc, p, r) )

cm = confusion_matrix(y_test, y_pred, normalize = 'true')

print(cm)