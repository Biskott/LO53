# -*- coding: utf-8 -*-
"""
Created on Thu Jun 28 16:19:39 2018

@author: lucas
"""

hostname = 'localhost'
username = 'admin'
password = 'lo53'
database = 'lo53'

from flask import Flask, request
app = Flask(__name__)

from psycopg2 import psycopg2
import urllib2

@app.route('/ping', methods=['GET', 'POST'])
def index():
    if request.method == 'GET':
        if request.path == '/ping':
            return "pong"
        else:
            return "Calibration OK"


@app.route('/calibrate?x=<int:posx>&y=<int:posy>&mac=<string:mac>')
def calibration(posx, posy, mac):
    myConnection = psycopg2.connect(host=hostname, user=username, password=password, dname=database)    #connect to DB
    cur = myConnection.cursor()#Needed to make request after
    cur.execute("SELECT ipap FROM AP")  #Get all IP from APs
    for ip_ap in cur.fetchall():
        content = urllib2.urlopen("http://"+ip_ap+"/get?mac="+mac)   #Get the RSSI from the APs stored in content
        cur.execute("INSERT INTO AP ('POSX', 'POSY', 'RSSIValue') VALUES ('"+posx+"', '"+posy+"', '"+content+"')") #store into DB
    myConnection.close()
    return "Calibration OK"
    
    
@app.route('/locateme?mac=<string:mac>')
def location(mac):
    myConnection = psycopg2.connect(host=hostname, user=username, password=password, dname=database)
    cur = myConnection.cursor()
    
    cur.execute("SELECT ipap FROM AP")
    for ip_ap in cur.fetchall():
        content = urllib2.urlopen("http://"+ip_ap+"/get?mac"+mac)
        if(content < 200): #RSSI Out Of Range
            rssi_tot += content
            
    #
    #Script de Partoo pour la position
    #Avec retour de pos x et y
        
    return "X = " + posx + " Y = " + posy
        
        

    
if __name__ == '__main__':
    app.run()