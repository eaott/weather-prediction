import urllib
import time
import os


def shouldTryAgain(filename):
	try:
		size = os.path.getsize(localUrl)
		if size < 2048:
			os.remove(localUrl)
			return False
	except Exception, e:
		return True
	return True


# look 10 minutes prior to ensure that the file is uploaded
startTime = time.time()


for x in xrange(2, 15 + 1):
	curTime = time.gmtime(time.time() - x * 60)

	year = curTime.tm_year
	month = curTime.tm_mon
	day = curTime.tm_mday
	hour = curTime.tm_hour
	minute = curTime.tm_min

	filename = "%04d%02d%02d_%02d%02d" % (year, month, day, hour, minute)

	# look at Albuquerque's composite reflectivity map. go up two levels
	# to see exactly where the radar is, etc.
	radarUrl = "http://radar.weather.gov/ridge/RadarImg/NCR/ABX/ABX_%s_NCR.gif" % filename
	localUrl = "data/%s.gif" % filename


	if shouldTryAgain(localUrl):
		try:
			urllib.urlretrieve(radarUrl, localUrl)
			shouldTryAgain(localUrl)
		except Exception, e:
			pass	

