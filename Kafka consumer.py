from kafka import KafkaConsumer
import time
consumer = KafkaConsumer('variablentest',auto_offset_reset='earliest', enable_auto_commit=False)
i=0
for message in consumer:
    text = message.value.decode("utf-8")
    text = text.translate('b')
    print (message.topic, text)
    appendFile = open('Data.txt','a')
    if i==0:
        appendFile.write('\n')
        appendFile.write(message.topic)
        appendFile.write('  ')
        appendFile.write('u=')
        i=i+1
    elif i==1:
        appendFile.write('  ')
        appendFile.write('v=')
        i=i+1
    elif i==2:
        appendFile.write('  ')
        appendFile.write('t=')
        i=i+1
    elif i==3:
        appendFile.write('  ')
        appendFile.write('s=')
        i=i-3
    appendFile.write(text)
    appendFile.close()
    time.sleep(0.1)
