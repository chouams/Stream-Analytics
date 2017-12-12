#!/usr/bin/python
import sys
import logging
logging.basicConfig(stream=sys.stderr)
sys.path.insert(0,"C:/Users/pauls/Desktop/FlaskApp")

from FlaskApp import app as application
application.secret_key = 'trwiwjenjfjdnfhnncdji'
