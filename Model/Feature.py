import numpy as np
import csv
from scipy.stats import variation
from scipy.stats.mstats import mquantiles
from pandas import Series

def Min(vec1, vec2=None, vec3=None, bar=False):
	if(bar):
		return np.nanmin(vec1)
	else:	
		return np.nanmin(vec1), np.nanmin(vec2), np.nanmin(vec3)

def Max(vec1, vec2=None, vec3=None, bar=False):
	if(bar):
		return np.nanmin(vec1)
	else:
		return np.nanmax(vec1), np.nanmax(vec2), np.nanmax(vec3)

def Average(vec1, vec2=None, vec3=None, bar=False):
	if(bar):
		return np.average(vec1)
	else:
		return np.average(vec1), np.average(vec2), np.average(vec3)

def StandardDeviation(np1, np2=None, np3=None, bar=False):
	if(bar):
		return np.std(np1, axis=0)
	else:
		return np.std(np1, axis = 0), np.std(np2, axis = 0), np.std(np3, axis = 0)

def CoefVariation(np1, np2=None, np3=None, featureVec=None, bar=False):
	if(bar):
		if(np.count_nonzero(np1) != 0):
			featureVec = np.append(featureVec, variation(np1, axis=0))
		else:
			featureVec = np.append(featureVec, 0.0)	
	else:
		if(np.count_nonzero(np1) != 0):
			featureVec = np.append(featureVec, variation(np1, axis=0))
		else:
			featureVec = np.append(featureVec, 0.0)	
		if(np.count_nonzero(np2) != 0):
			featureVec = np.append(featureVec, variation(np2, axis=0))
		else:
			featureVec = np.append(featureVec , 0.0)	
		if(np.count_nonzero(np3) != 0):	
			featureVec = np.append(featureVec, variation(np3, axis=0))
		else:
			featureVec = np.append(featureVec, 0.0)	
	return featureVec

def Correlation(vec_A, vec_B, avg_A, avg_B, std_A, std_B):
	up = 0
	res = 0
	bs = 0
	if(vec_A.size > 1 and vec_B.size > 1):
		bs = 1/(vec_A.size - 1)
		for i in range(1, vec_A.size):
			up += ((vec_A[i] - avg_A)*(vec_B[i] - avg_B)) / (std_A * std_B)
		res = bs * up	
	return res

def Quantiles(np1, np2=None, np3=None, bar=False):
	if(bar):
		return Series(np1).quantile(.5)
	else:
		return Series(np1).quantile(.5), Series(np2).quantile(.5), Series(np3).quantile(.5)