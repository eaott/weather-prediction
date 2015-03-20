import requests
import time
import datetime

url = "http://hydromet.lcra.org/chronhist.aspx"
data = {"DropDownList1" : "2992",
		"DropDownList2" : "PC",
		"Date1" : "1/14/2015",
		"Date2" : "2/11/2015",
		"Button1":"Get Historic Data"}

r = requests.post(url, data=data)