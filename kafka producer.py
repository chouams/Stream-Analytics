from kafka import KafkaProducer
import time
import random

producer = KafkaProducer(bootstrap_servers='localhost:9092')

def Werte():
    u = random.randint(100,1000)
    v = random.randint(0,10)
    t = random.randint(0,100)
    s = 5
    Wertelist = [u,v,t,s]
    return Wertelist

while(True==True):
    Wertelist = Werte()
    for x in range(0,4):
        producer.send('variablentest', b'(%i)' %Wertelist[x])
    
    time.sleep(10)
    

