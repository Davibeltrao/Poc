import os
import glob
import numpy as np
import csv
import Feature as fe

path = 'C:/Users/flabe/Desktop/Dados-POC/Data2/'
Person = ['davi', 'antonio', 'luiz']
exercises_list = ['crucifixo', 'extensao', 'rosca-martelo', 'rosca-direta', 'supino']
sensors = ['others_acc']

def dataToNumpy(data, bar=False):
	if(not bar):
		return  np.array([float(row[1]) for row in data]), 	np.array([float(row[2]) for row in data]),	np.array([float(row[3]) for row in data])
	else:
		return np.array([float(row[1]) for row in data])

def dataToNumpyCut(data, cut_interval, bar=False):
	value = 100 * cut_interval
	index_i = value
	index_f = len(data) - value
	if(not bar):
		return np.array([float(data[row][1]) for row in range(index_i, index_f)]), np.array([float(data[row][2]) for row in range(index_i, index_f)]), np.array([float(data[row][3]) for row in range(index_i, index_f)])
	else:
		return np.array([float(data[row][1]) for row in range(index_i, index_f)])

def appendValues(value1, value2=None, value3=None, featureVec=None, bar=False):
	if(bar):
		featureVec = np.append(featureVec, value1)
	else:	
		featureVec = np.append(featureVec, value1)
		featureVec = np.append(featureVec, value2)
		featureVec = np.append(featureVec, value3)
	return featureVec

def getFeatureVec(name, totalFeat, sensor, sensor_used):
	featureVec = []
	if name.find(sensor) != -1 and name.find("smartphone") == -1:
		sensor_used = sensor_used + 1
		with open(name, 'r') as f:
			data = np.array(list(csv.reader(f, delimiter=";"))) #data parsing
			if(name.find("bar") == -1): #acc, gyr, mag, linear_acc feature extracting
				np_x, np_y, np_z = dataToNumpyCut(data, 6)
				
				#Max
				max1, max2, max3 = fe.Max(np_x, np_y, np_z)
				featureVec = np.array([max1, max2, max3])
				
				#Min
				min1, min2, min3 = fe.Min(np_x, np_y, np_z)
				featureVec = appendValues(min1, min2, min3, featureVec)
				
				#Average
				avg1, avg2, avg3 = fe.Average(np_x, np_y, np_z)
				featureVec = appendValues(avg1, avg2, avg3, featureVec)
				
				#Standard Deviation
				std1, std2, std3 = fe.StandardDeviation(np_x, np_y, np_z)
				featureVec = appendValues(std1, std2, std3, featureVec)
				
				#Coeficient Variation
				featureVec = fe.CoefVariation(np_x, np_y, np_z, featureVec)

				cor1 = fe.Correlation(np_x, np_y, avg1, avg2, std1, std2)
				cor2 = fe.Correlation(np_x, np_z, avg1, avg3, std1, std3)
				cor3 = fe.Correlation(np_y, np_z, avg2, avg3, std2, std3)
				featureVec = appendValues(cor1, cor2, cor3, featureVec)	

				q1, q2, q3 = fe.Quantiles(np_x, np_y, np_z)
				featureVec = appendValues(q1, q2, q3, featureVec)
			else: #bar feature extracting
				np_x = dataToNumpy(data, True)

				max1 = fe.Max(np_x, bar=True)
				featureVec = np.array([max1])
				
				min1 = fe.Min(np_x, bar=True)
				featureVec = appendValues(min1, featureVec=featureVec, bar=True)

				avg1 = fe.Average(np_x, bar=True)
				featureVec = appendValues(avg1, featureVec=featureVec, bar=True)

				std1 = fe.StandardDeviation(np_x, bar=True)
				featureVec = appendValues(std1, featureVec=featureVec, bar=True)	

				featureVec = fe.CoefVariation(np_x, featureVec=featureVec, bar=True)

				q1 = fe.Quantiles(np_x, bar=True)
				featureVec = appendValues(q1, featureVec=featureVec, bar=True)
			
			#Adiciona no vetor de features, as features criadas
			try:
				totalFeat = np.append(totalFeat, featureVec, axis=0)
			except:
				totalFeat = np.array(featureVec)			
			#print("SENSOR = ", sensor_used)	
			return totalFeat, sensor_used
	return totalFeat, sensor_used		

def createFeatureMap(exercises_list):
	Person_dict = {}
	for p in Person:
		sensor_used = 0
		totalFeat = []
		for exercise in exercises_list:#exercises
			print(exercise)
			for name in glob.glob(path + p + "/" + "*" + exercise + "*"):#iterate over all files in path with exercise name
				if sensor_used >= len(sensors):
					totalFeat = np.append(totalFeat, [exercise], axis=0)#adicionar qual exercicio ao final do featureVec
					try:
						Person_dict[p] = np.append(Person_dict[p], [totalFeat], axis=0)
					except:
						Person_dict[p] = np.array([totalFeat])
					finally:
						totalFeat = []	
						sensor_used = 0
				for sensor in sensors: #rodar todos sensores de cada arquivo
					totalFeat, sensor_used = getFeatureVec(name, totalFeat, sensor, sensor_used)				
		print("Person: ", Person_dict[p], " Nome: ", p, end="\n\n\n")							
	return Person_dict

def getPerson():
	return Person

def getExercises():
	return exercises_list