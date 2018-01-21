# Stream-Analytics
Server-IP: 46.101.144.109
server is running Ubuntu 16.04
website implemented with Flask with Apache 2 Gateway on WSGI-Framework
https://www.digitalocean.com/community/tutorials/how-to-deploy-a-flask-application-on-an-ubuntu-vps

Database running is Mongodb on mLab

# Simulated-device 

Simulated-device is an application written in Java that simulates the operation of a machine.  This applicaion sends telemetry data from the simulated device to the Cloud in real time. This telemetry data is sent directly to the cloud in the form of Gson data. 
This Gson has the following structure:

        string deviceId --> device id of the device.
        double Temperature --> Current temperature.
        double Geschwindigkeit --> current device speed.
        int Stückzahl --> The current number of parts manufactured by the device.
        Date Datum --> Current date of data collection.
        String Uhrzeit --> Current time of data collection. 
	
All these data are sent in real time, following a delay that can be changed in the Config file.

# Config-files 

this application is configured thanks to a config file containing the following structure:

	minTemperature: Minimum temperature value
	minSpeed: Minimum speed value
	maxTemperature: value of the maximum temperature
	maxSpeed: maximum speed value
	mongoId: Id for the database
	TimeIntervall: Intervall of time where data will be sent to the cloud (given in milliseconds). 

The config file is read by the application and its extension is "txt".
for the settings to be made in the config file,
the order of the rows in the config file is not important, but the structure of each row must remain unchanged. The changes that can be made only concern the values of each parameter, which is to the right of the two points (:) in the line.

you can create as many configuration files as you like, the structure of the lines and the spelling of the names are the same. The Path of the configuration file must be indicated and saved.
in the Main block of the application where the content will be read and collected for the device simulation.
