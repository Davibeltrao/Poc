import DataParsing as DP
import itertools
import numpy as np
import csv
from sklearn import svm
from sklearn.model_selection import train_test_split
from sklearn.model_selection import cross_val_score
import matplotlib.pyplot as plt
from matplotlib import style
from sklearn.metrics import confusion_matrix
from sklearn.ensemble import RandomForestClassifier
from sklearn.mixture import GMM

def ConfusionMatrix(y_test, y_pred, classes):
	cm = confusion_matrix(y_test, y_pred)
	cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
	np.set_printoptions(precision=2)
	plt.imshow(cm, interpolation='nearest', cmap=plt.cm.Blues)
	plt.colorbar()
	fmt = '.2f'
	thresh = cm.max() / 2.
	tick_marks = np.arange(len(classes))
	plt.xticks(tick_marks, classes, rotation=45)
	plt.yticks(tick_marks, classes)
	for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
		plt.text(j, i, format(cm[i, j], fmt), horizontalalignment="center", color="white" if cm[i, j] > thresh else "black")
	plt.tight_layout()
	plt.ylabel('True label')
	plt.xlabel('Predicted label')
	plt.show()



def SVM(Person_dict):
	davi_vec = Person_dict['davi']
	row_d = len(davi_vec)
	column_d = len(davi_vec[0])

	matheus_vec = Person_dict['antonio']
	row_t = len(matheus_vec)
	column_t = len(matheus_vec[0])	

	X_train = np.array([davi_vec[line][0:column_d-2] for line in range(0, row_d)])
	y_train = np.array([davi_vec[line][column_d-1] for line in range(0, row_d)])
	
	X_test = np.array([matheus_vec[line][0:column_t-2] for line in range(0, row_t)])
	y_test = np.array([matheus_vec[line][column_t-1] for line in range(0, row_t)])

	clf = svm.SVC()
	clf.fit(X_train, y_train)
	print(clf.score(X_test, y_test))
	y_pred = clf.predict(X_test)
	print("Y TEST = ", y_test)
	print("Y PRED= ", y_pred)

	scores = cross_val_score(clf, X_train, y_train, cv = 10)
	print("Accuracy: %0.2f (+/- %0.2f)" % (scores.mean(), scores.std()))
	return y_pred, y_test

def OLOSingle(Person_dict):
	Person = DP.getPerson()		
	for key in Person:
		print("Person = ", key)
		row = len(Person_dict[key])
		column = len(Person_dict[key][0])
		person_vec = Person_dict[key]
		X = person_vec[:, 0:column-2]
		y = person_vec[:, column-1]
		clf = svm.SVC()		
		scores = cross_val_score(clf, X, y, cv = 5)
		print("Accuracy: %0.2f (+/- %0.2f)" % (scores.mean(), scores.std()))
		print()
	return

def OLO(Person_dict):
	row = len(person_vec)
	column = len(person_vec[0])
	indices = np.arange(row)
	for i in range(0, row):
		X_train = person_vec[indices != i , :column-2]
		y_train = person_vec[indices != i, column-1]
		X_test = person_vec[indices == i, :-2]
		y_test = person_vec[indices == i, column-1]
		clf = svm.SVC()
		clf.fit(X_train, y_train)
		y_pred = clf.predict(X_test)
		print("PRED = ", y_pred)
		print("TEST = ", y_test)
		print(clf.score(X_test, y_test))
		print(end="\n\n")
	return 0, 0

def OLOAll(Person_dict):
	Person = DP.getPerson()
	Exercis = 0
	for key in Person:
		if Exercis == 0:
			Exercis = np.array(Person_dict[key])
		else:
			Exercis = np.append(Exercis, Person_dict[key], axis=0)
	row = len(Exercis)
	column = len(Exercis[0])
	indices = np.arange(row)

	clf = svm.SVC()
	scores = cross_val_score(clf, Exercis[:, 0:column-2], Exercis[:, column-1], cv = 10)
	print("Accuracy: %0.2f (+/- %0.2f)" % (scores.mean(), scores.std()))

def OLO2(Person_dict):
	Person = DP.getPerson()
	Exercis = 0
	for key in Person:
		if Exercis == 0:
			Exercis = np.array(Person_dict[key])
		else:
			Exercis = np.append(Exercis, Person_dict[key], axis=0)
	row = len(Exercis)
	column = len(Exercis[0])
	indices = np.arange(row)

	clf = RandomForestClassifier()
	scores = cross_val_score(clf, Exercis[:, 0:column-2], Exercis[:, column-1], cv = 10)
	print("Accuracy: %0.2f (+/- %0.2f)" % (scores.mean(), scores.std()))
	
	for i in range(0, row):
		X_train = Exercis[indices != i , :column-2]
		y_train = Exercis[indices != i, column-1]
		X_test = Exercis[indices == i, :-2]
		y_test = Exercis[indices == i, column-1]
		clf = RandomForestClassifier()
		clf.fit(X_train, y_train)
		y_pred = clf.predict(X_test)
		print("PRED = ", y_pred)
		print("TEST = ", y_test)
		print(clf.score(X_test, y_test))
		print(end="\n\n")
	return 0, 0

def LOSO(Person_dict, exercises_list):
	Person = DP.getPerson()
	scoreTotal = 0.0
	score = 0.0
	for key in Person:
		Train = 0
		Test = 0
		for key2 in Person:
			if key2 != key:
				if isinstance(Train, int):
					Train = np.array(Person_dict[key2])
				else:
					Train = np.append(Train, Person_dict[key2], axis=0)	
			else:
				Test = Person_dict[key2]
		#Train
		row_train = len(Train)
		column_train = len(Train[0])		
		X_train = np.array([Train[line][0:column_train-2] for line in range(0, row_train)])
		y_train = np.array([Train[line][column_train-1] for line in range(0, row_train)])

		#Test
		row_test = len(Test)
		column_test = len(Test[0])		
		X_test = np.array([Test[line][0:column_test-2] for line in range(0, row_test)])
		y_test = np.array([Test[line][column_test-1] for line in range(0, row_test)])

		print("PESSOA SENDO TESTADA = ", key)
		clf = svm.SVC()
		clf.fit(X_train, y_train)
		y_pred = clf.predict(X_test)
		score = score + clf.score(X_test, y_test)
	print('SCORE =', score/3)	
	return

def LOSORF(Person_dict, exercises_list):
	Person = DP.getPerson()
	
	for key in Person:
		Train = 0
		Test = 0
		print("TEST COM ", key)
		print("TRAIN COM ", end="")
		for key2 in Person:
			if key2 != key:
				if isinstance(Train, int):
					Train = np.array(Person_dict[key2])
				else:
					Train = np.append(Train, Person_dict[key2], axis=0)	
				print(key2, "/", end="")	
			else:
				Test = Person_dict[key2]		
		#Train
		row_train = len(Train)
		column_train = len(Train[0])		
		X_train = np.array([Train[line][0:column_train-2] for line in range(0, row_train)])
		y_train = np.array([Train[line][column_train-1] for line in range(0, row_train)])

		#Test
		row_test = len(Test)
		column_test = len(Test[0])		
		X_test = np.array([Test[line][0:column_test-2] for line in range(0, row_test)])
		y_test = np.array([Test[line][column_test-1] for line in range(0, row_test)])

		clf = RandomForestClassifier()
		clf.fit(X_train, y_train)
		print(clf.score(X_test, y_test))
		y_pred = clf.predict(X_test)
		
		ConfusionMatrix(y_test, y_pred, exercises_list)
	return

def LOSORFPLUS(Person_dict, exercises_list, porcentagem):
	Person = DP.getPerson()
	
	for key in Person:
		Train = 0
		Test = 0
		for key2 in Person:
			if key2 != key:
				if Train == 0:
					Train = np.array(Person_dict[key2])
				else:
					Train = np.append(Train, Person_dict[key2], axis=0)	
			else:
				Test = Person_dict[key2]
		#Train
		row_train = len(Train)
		column_train = len(Train[0])		
		X_train = np.array([Train[line][0:column_train-2] for line in range(0, row_train)])
		y_train = np.array([Train[line][column_train-1] for line in range(0, row_train)])

		#Test
		row_test = len(Test)
		column_test = len(Test[0])		
		X_test = np.array([Test[line][0:column_test-2] for line in range(0, row_test)])
		y_test = np.array([Test[line][column_test-1] for line in range(0, row_test)])

		X1, X_test2, y1, y_test2 = train_test_split(X_test, y_test, test_size=(porcentagem/100))

		X_train = np.append(X_train, X1, axis=0)
		y_train = np.append(y_train, y1, axis=0)
		X_test = X_test2
		y_test = y_test2

		clf = svm.SVC()
		clf.fit(X_train, y_train)
		print("TESTED ONE = ", key)
		print(clf.score(X_test, y_test))
		y_pred = clf.predict(X_test)
	return

def LOSOGMMTeste(Person_dict, exercises_list):
	Person = DP.getPerson()
	print("DAVI = ", Person_dict['davi'])
	print("LUIZ = ", Person_dict['luiz'])
	for key in Person:
		Train = 0
		Test = 0
		for key2 in Person:
			if key2 != key:
				if Train == 0:
					Train = np.array(Person_dict[key2])
				else:
					Train = np.append(Train, Person_dict[key2], axis=0)	
			else:
				Test = Person_dict[key2]
		#Train
		row_train = len(Train)
		column_train = len(Train[0])		
		X_train = np.array([Train[line][0:column_train-2] for line in range(0, row_train)])
		y_train = np.array([Train[line][column_train-1] for line in range(0, row_train)])

		#Test
		row_test = len(Test)
		column_test = len(Test[0])		
		X_test = np.array([Test[line][0:column_test-2] for line in range(0, row_test)])
		y_test = np.array([Test[line][column_test-1] for line in range(0, row_test)])

		clf = RandomForestClassifier()
		clf.fit(X_train, y_train)
		print(clf.score(X_test, y_test))
		y_pred = clf.predict(X_test)
		print("ATUAL TEST = ", y_test)
		print("PREDICTED = ", y_pred)
		ConfusionMatrix(y_test, y_pred, exercises_list)
	return 

exercises_list = DP.getExercises()
Person_dict = DP.createFeatureMap(exercises_list)

LOSORF(Person_dict, exercises_list)

