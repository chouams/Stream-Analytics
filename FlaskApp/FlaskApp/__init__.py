from __future__ import print_function
from gevent import monkey
#monkey.patch_all()
from flask import Flask, session, render_template, flash, g, request, url_for, redirect
from azure.servicebus import ServiceBusService, Message, Queue
from pymongo import MongoClient
from flask_socketio import SocketIO, emit
from threading import *
import bcrypt
import json
import time
import sys

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret!'
app.debug = True
app.threaded = True

# configuration for database
client = MongoClient('mongodb://Paul2:1234@ds046047.mlab.com:46047/stream')
db = client.stream

#used for live_actualisation
socketio = SocketIO(app)
thread = None
thread2 = None

#connection with IoT-Hub
bus_service = ServiceBusService(
    	service_namespace='iothub-htw', 
    	shared_access_key_name='RootManageSharedAccessKey',
    	shared_access_key_value='iKeDqJ+CBnSfvOwFRLs8xVTF41kx2ZUP8ItK2Xy1NiA=')

#updating database from IoT-Hub
def update_loop():
	while True:
		try:
			msg = bus_service.receive_queue_message('htw-queue', peek_lock=False)
			time.sleep(5)
        		while (msg != None):
    				msg = bus_service.receive_queue_message('htw-queue', peek_lock=False)
            			if msg != None:
					message = json.loads(msg.body)
					Werte = db.Werte
					id = Werte.insert({'Maschine': 1})
					for key, value in message.items():
						Werte.update({'_id': id},{'$set':{key: value}})
						Werte.update({'_id': id},{'$unset':{'Maschine':1}})
 		except:
			time.sleep(5)

#live actualisation of machine/id
def live_actualisation():
	while True:
	    try:
	    	werte = []
		message = []
            	live_werte_cur = db.Werte.find().sort([('_id',-1)]).limit(1)
	    	for x in live_werte_cur:
			werte.append(x)
	    	for wert in werte:
			temp = wert.Temperatur
			number = wert.Stueckzahl
			speed = wert.Geschwindigkeit
		message.append(temp)
		message.append(number)
		message.append(speed)
            	socketio.emit('Newnumber', {'msg':message}, namespace='/live')
             	time.sleep(5)
	    except:
		time.sleep(5)

@socketio.on('connect', namespace='/live')
def test_connect():
    # need visibility of the global thread object
    global thread2
    if thread2 is None:
	#print "Thread2 wird gestartet"
    	thread2 = Thread(target = live_actualisation)
    	thread2.start()
        

@app.route('/login/', methods=["GET","POST"])
def login_page():
    error = ' '
    #start update of database
    global thread
    if thread is None:
	#print "Thread wird gestartet"
	print("Thread gestartet...", file=sys.stderr)
        thread = Thread(target = update_loop)
        thread.start()

    try:
        if request.method == "POST":
	    #login procedure
	    attempted_username = request.form['username']
            attempted_password = request.form['password']
	    users = db.users
	    login_user = users.find_one({'name' : attempted_username})

            if login_user:
		if bcrypt.hashpw(attempted_password.encode('utf-8'), login_user['password'].encode('utf-8')) == login_user['password'].encode('utf-8'):
			session['user'] = attempted_username
			return redirect(url_for('index'))
				
            	else:
                	error = "Ungueltige Kombination. Versuchen Sie es erneut."
		return render_template("login.html", error = error)

	    else:
		error = "Nutzer nicht vorhanden."
		return render_template("login.html", error = error)	

        return render_template("login.html", error = error)

    except Exception as e:
        return render_template("login.html", error = e)

@app.route('/register/', methods=["GET","POST"])
def register():
	error = ''
	try:
		if request.method == "POST":
			#register procedure
			attempted_username = request.form['username']
            		attempted_password = request.form['password']
			attempted_password2 = request.form['password2']
			users = db.users
			existing_user = users.find_one({'name' : attempted_username})
			
			if existing_user is None:
				if attempted_password == attempted_password2:
					hashpass = bcrypt.hashpw(attempted_password.encode('utf-8'), bcrypt.gensalt())
					users.insert({'name' : attempted_username, 'password' : hashpass})
					session['user'] = attempted_username
					return redirect(url_for('index'))
				else:
					error = "Passwoerter stimmen nicht ueberein!"

			else:
				error = "Benutzer existiert bereits!"

		return render_template('register.html', error = error)
            		
	except Exception as e:
        	return render_template("register.html", error = str(e))
	
@app.route('/logout/')
def logout():
    # remove the user from the session if it's there
    session.pop('user', None)
    return redirect(url_for('login_page'))

@app.before_request
def before_request():
	g.user =None
	if 'user' in session:
		g.user =session['user']

@app.route('/')
def index():
	if g.user:
		try:
			machines = []
			machines_cur = db.machines.find({'user':g.user})
			for x in machines_cur:
				machines.append(x)	
			return render_template('dashboard.html', user_machines = machines)
		except Exception as e:
			return (str(e))
	else:
		return redirect(url_for('login_page'))

@app.route('/machine/<_id>')
def machine(_id):
	if g.user:
		try:
			werte = []
			time = []
			temp = []
			speed = []
			number = []
			maxTemp = db.machines.find_one({'_id': _id}, {'_id':0, 'MaxTemp':1})
			minTemp = db.machines.find_one({'_id': _id}, {'_id':0, 'MinTemp':1})
			maxV = db.machines.find_one({'_id': _id}, {'_id':0, 'MaxV':1})
			minV = db.machines.find_one({'_id': _id}, {'_id':0, 'MinV':1})
			Name = db.machines.find_one({'_id': _id}, {'Maschine':1, '_id':0})
			
			werte_cur = db.Werte.find({'mongoId':_id},{'_id':0, 'deviceId':0, 'mongoId':0})
			for x in werte_cur:
				werte.append(x)
			time_cur = db.Werte.find({'mongoId':_id},{'_id':0, 'Uhrzeit':1}).sort([('_id',1)]).limit(500)
			for x in time_cur:
				time.append(x)
			temperature_cur = db.Werte.find({'mongoId':_id},{'_id':0, 'Temperatur':1}).sort([('_id',1)]).limit(500)
			for x in temperature_cur:
				temp.append(x)
			speed_cur = db.Werte.find({'mongoId':_id},{'_id':0, 'Geschwindigkeit':1}).sort([('_id',1)]).limit(500)
			for x in speed_cur:
				speed.append(x)
			number_cur = db.Werte.find({'mongoId':_id},{'_id':0, 'Stueckzahl':1}).sort([('_id',1)]).limit(500)
			for x in number_cur:
				number.append(x)
			return render_template("maschine.html", werte = werte, Name = Name,numbers = number, speed = speed, times = time, temperature = temp)
		except Exception as e:
			return (str(e))
	else:
		return redirect(url_for('login_page'))

@app.route('/create/', methods=["GET","POST"])
def create():
	if g.user:
		error = ''
		try:
        		if request.method == "POST":
				machinename = request.form['machinename']
            			facility = request.form['facility']
				sonstiges = request.form['sonstiges']
				min_temp = request.form['mintemp']
				max_temp = request.form['maxtemp']
				min_v = request.form['minv']
				max_v = request.form['maxv']
				machines = db.machines
				users = db.users
				_id = machines.insert({'Standort': facility, 'Maschine' : machinename, 'Sonstiges':sonstiges, 'MaxTemp': max_temp, 'MinTemp': min_temp, 'MaxV':max_v, 'MinV':min_v, 'user' : g.user})
				error = 'Maschine hinzugefuegt.'
				return redirect(url_for('index'))

        		return render_template("create.html", error = error)

    		except Exception as e:
        		return render_template("create.html", error = error)
	else:
		return redirect(url_for('login_page'))

@app.errorhandler(404)
def page_not_found(e):
	return("Page not found")

if __name__ == "__main__":
   	socketio.run(app)
	
