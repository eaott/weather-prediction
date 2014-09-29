import urllib
import time
import os


def shouldTryAgain(filename):
	try:
		size = os.path.getsize(filename)
		if size < 2048:
			os.remove(filename)
			return False
	except Exception, e:
		return True
	return True


# look 10 minutes prior to ensure that the file is uploaded
startTime = time.time()


for x in xrange(2, 20 + 1):
	curTime = time.gmtime(time.time() - x * 60)

	year = curTime.tm_year
	month = curTime.tm_mon
	day = curTime.tm_mday
	hour = curTime.tm_hour
	minute = curTime.tm_min

	filename = "%04d%02d%02d_%02d%02d" % (year, month, day, hour, minute)

	# look at both radars covering the Austin area for their 1-hour precipitation.
	radarUrlEWX = "http://radar.weather.gov/ridge/RadarImg/N1P/EWX/EWX_%s_N1P.gif" % filename
	radarUrlGRK = "http://radar.weather.gov/ridge/RadarImg/N1P/GRK/GRK_%s_N1P.gif" % filename
	localUrlEWX = "data/EWX_%s.gif" % filename
	localUrlGRK = "data/GRK_%s.gif" % filename
	try:
		os.stat(os.path.dirname(localUrlEWX))
	except:
		os.mkdir(os.path.dirname(localUrlEWX))
	try:
		os.stat(os.path.dirname(localUrlGRK))
	except:
		os.mkdir(os.path.dirname(localUrlGRK))

	if shouldTryAgain(localUrlEWX):
		try:
			urllib.urlretrieve(radarUrlEWX, localUrlEWX)
			shouldTryAgain(localUrlEWX)
		except Exception, e:
			pass
	if shouldTryAgain(localUrlGRK):
		try:
			urllib.urlretrieve(radarUrlGRK, localUrlGRK)
			shouldTryAgain(localUrlGRK)
		except Exception, e:
			pass	

