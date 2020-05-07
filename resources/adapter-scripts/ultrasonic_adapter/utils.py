import time

from datetime import datetime

def getNowTime():
    return datetime.utcnow().strftime('%D %H:%M:%S')

def createNewId():
    return "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))

