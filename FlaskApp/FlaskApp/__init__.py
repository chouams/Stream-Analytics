from flask import Flask, render_template, flash, request, url_for, redirect
from content_management import Content, addContent, id
app = Flask(__name__)

id = id
MACHINE_DICT = Content()

@app.route('/login/', methods=["GET","POST"])
def login_page():

    error = ''
    try:
        if request.method == "POST":
		
            attempted_username = request.form['username']
            attempted_password = request.form['password']

            if attempted_username == "admin" and attempted_password == "password":
                return redirect(url_for('homepage'))				
            else:
                error = "Invalid credentials. Try Again."

        return render_template("login.html", error = error)

    except Exception as e:
        return render_template("login.html", error = error)
@app.route('/')
def homepage():
	try:
		return render_template("dashboard.html", MACHINE_DICT = MACHINE_DICT)
	except Exception as e:
		return (str(e))

@app.route('/machine/')
def machine():
	try:
		return render_template("maschine.html", MACHINE_DICT = MACHINE_DICT)
	except Exception as e:
		return (str(e))

@app.route('/create/', methods=["GET","POST"])
def create():
	error = ''
	try:
        	if request.method == "POST":
		
            		machine_name = request.form['machinename']
            		facility = request.form['facility']
			sonstiges = request.form['sonstiges']
			MACHINE_DICT2 = addContent(machine_name, facility, sonstiges, id)
            		MACHINE_DICT.update(MACHINE_DICT2)
			id = id+1			
            	else:
                	error = "some error ocurred"

        	return render_template("create.html", MACHINE_DICT=MACHINE_DICT, error = error)

    	except Exception as e:
        	return render_template("create.html", MACHINE_DICT=MACHINE_DICT, error = error)

@app.errorhandler(404)
def page_not_found(e):
	return("Page not found")
if __name__ == "__main__":
    app.run()
