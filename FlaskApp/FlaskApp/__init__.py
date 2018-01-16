from flask import Flask, session, render_template, flash, g, request, url_for, redirect
from azure.servicebus import ServiceBusService, Message, Queue
from pymongo import MongoClient
from multiprocessing import Process
import bcrypt
import json
import time


app = Flask(__name__)


client = MongoClient('mongodb://Paul2:1234@ds046047.mlab.com:46047/stream')
db = client.stream

bus_service = ServiceBusService(
    	service_namespace='iothub-htw', 
    	shared_access_key_name='RootManageSharedAccessKey',
    	shared_access_key_value='iKeDqJ+CBnSfvOwFRLs8xVTF41kx2ZUP8ItK2Xy1NiA=')

@app.route('/update/')
def update_loop():
	while True:
		msg = bus_service.receive_queue_message('htw-queue', peek_lock=False)
		time.sleep(5)
        	while (msg != None):
    			msg = bus_service.receive_queue_message('htw-queue', peek_lock=False)
            		if msg != None:
				message = json.loads(msg.body)
				Werte = db.Werte
				id = Werte.insert({'Maschine': 1})
				for key, value in message.items():
					Werte.update({'_id': id},{'$push':{key: value}})
				Werte.update({'_id': id},{'$unset':{'Maschine':1}})


@app.route('/login/', methods=["GET","POST"])
def login_page():
    error = ' '
    try:
        if request.method == "POST":
	    attempted_username = request.form['username']
            attempted_password = request.form['password']
	    users = db.users
	    login_user = users.find_one({'name' : attempted_username})

            if login_user:
		if bcrypt.hashpw(attempted_password.encode('utf-8'), login_user['password'].encode('utf-8')) == login_user['password'].encode('utf-8'):
			session['user'] = attempted_username
			return redirect(url_for('homepage'))
				
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
			attempted_username = request.form['username']
            		attempted_password = request.form['password']
			users = db.users
			existing_user = users.find_one({'name' : attempted_username})
			
			if existing_user is None:
				hashpass = bcrypt.hashpw(attempted_password.encode('utf-8'), bcrypt.gensalt())
				users.insert({'name' : attempted_username, 'password' : hashpass})
				session['user'] = attempted_username
				return redirect(url_for('homepage'))

			else:
				error = "Username already exists!"

		return render_template('register.html', error = error)
            		
	except Exception as e:
        	return render_template("register.html", error = str(e))
	

@app.before_request
def before_request():
	g.user =None
	if 'user' in session:
		g.user =session['user']

@app.route('/')
def homepage():
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
			Name = db.machines.find_one({'_id': _id}, {'Maschine':1, '_id':0})
			werte_cur = db.Werte.find({'mongoId':_id},{'_id':0, 'deviceId':0, 'mongoId':0})
			for x in werte_cur:
				werte.append(x)
			return render_template("maschine.html", werte = werte, Name = Name)
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
				machines = db.machines
				users = db.users
				_id = machines.insert({'Standort': facility, 'Maschine' : machinename, 'Sonstiges':sonstiges, 'user' : g.user})
				users.update({'name':g.user},{'$push':{'Maschinen': [_id]}})
				error = 'Maschine hinzugefuegt.'
				return redirect(url_for('homepage'))

        		return render_template("create.html", error = error)

    		except Exception as e:
        		return render_template("create.html", error = error)
	else:
		return redirect(url_for('login_page'))

@app.route('/test/')
def test():
	if g.user:
		error = ''
		try:
        		labels = []
    			values = []
			
    			return render_template('test.html', values=values, labels=labels, e =error)


    		except Exception as e:
        		return render_template("test.html", e = error)
	else:
		return redirect(url_for('login_page'))

@app.errorhandler(404)
def page_not_found(e):
	return("Page not found")

if __name__ == "__main__":
	p = Process(target = update_loop, args = ())
	p.start()
	app.secret.key = 'secretkey'
   	app.run(debug=True, threaded = true)
	
