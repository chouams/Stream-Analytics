from flask import Flask

app = Flask(__name__)

@app.route('/')
def homepage():
    return "Dies ist die Startseite."

@app.route('/user/<name>')
def user(name):
    return '<h1>Hi %s!</h1>' %name


if __name__ == "__main__":
    app.run()
