import requests
import time
import datetime

numdays = 2
base = datetime.date(2013, 8, 1)
products = ["EAL3N1P"]

url = "http://www.ncdc.noaa.gov/nexradinv/ordercomplete.jsp"
data = {"emailadd":"evan.ott@utexas.edu",
		"startHour": "00",
		"endHour":"23",
		"dsi" : "7000"}
ids = ["KGRK", "KEWX"]

date_list = [base - datetime.timedelta(days=x) for x in range(0, numdays)]

for date in date_list:
	year = "%04d" % date.year
	month = "%02d" % date.month
	day = "%02d" % date.day
	data["yyyy"] = year
	data["mm"] = month
	data["dd"] = day
	for product in products:
		data["product"] = product
		for id in ids:
			data["id"] = id
			requests.post(url, data=data)
	print("done with %s" % date)

print("done!")