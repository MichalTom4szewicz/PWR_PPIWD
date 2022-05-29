import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier
import joblib
from sklearn.metrics import ConfusionMatrixDisplay, confusion_matrix, accuracy_score, precision_score, recall_score
import matplotlib.pyplot as plt

dataframe = pd.read_csv('./backend_data/PPWID_100_10.csv')

# Data Format - class - 0, (gyroscope 1-3, acceleromter 4-6, magnetometr 7-9) x 10 [1-90]

X = dataframe.drop('0', axis=1)
y = dataframe['0']

X_train, X_test, y_train, y_test = train_test_split(X, y, random_state=123, test_size=0.20)

scaler = StandardScaler()

X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)
    
clf = RandomForestClassifier(max_depth = 50, random_state = 123)
clf.fit(X_train, y_train)

y_pred = clf.predict(X_test)

acc = accuracy_score(y_test, y_pred)
p = precision_score(y_test, y_pred,average=None).mean()
r = recall_score(y_test, y_pred, average=None).mean()

print("Accuracy: %.3f \nPrecision: %.3f \nRecall: %.3f" % (acc, p, r) )
   
filename = './models/rf.sav'
joblib.dump(clf, filename)

print("Model saved to:", filename)
