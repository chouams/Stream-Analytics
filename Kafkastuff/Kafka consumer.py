from kafka import KafkaConsumer
import time
Topic ='pi_test'
consumer = KafkaConsumer(Topic,auto_offset_reset='earliest', enable_auto_commit=False)
i=0
DATA_DICT = {"u","v","t","s"}
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
        DATA_DICT[0]="%s"%text
        i=i+1
    elif i==1:
        appendFile.write('  ')
        appendFile.write('v=')
        DATA_DICT[1]="%s"%text
        i=i+1
    elif i==2:
        appendFile.write('  ')
        appendFile.write('t=')
        DATA_DICT[2]="%s"%text
        i=i+1
    elif i==3:
        appendFile.write('  ')
        appendFile.write('s=')
        DATA_DICT[3]="%s"%text
        i=i-3
    appendFile.write(text)
    appendFile.close()
    time.sleep(0.1)



            
