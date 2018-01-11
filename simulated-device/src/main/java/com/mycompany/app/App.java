package com.mycompany.app;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.*;

import java.io.*;
import java.util.Properties;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

public class App
{
    private static String connString = "HostName=iothub-0f09bdec.azure-devices.net;DeviceId=htw-device;SharedAccessKey=n3i6d/C1tSnnbo0mv8KU1sSlOvvx6AOK5yN8rP4ToDQ=";
    private static IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
    private static String deviceId = "htw-device";
    private static DeviceClient client;

    private static class TelemetryDataPoint {
        public String deviceId;
        public double temperature;
        public double speed;
        public int  pieceNumber;

        public String serialize() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }
    //region EventCallback class
    private static class EventCallback implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to message with status: " + status.name());

            if (context != null) {
                synchronized (context) {
                    context.notify();
                }
            }
        }
    }
    //endregion
    private static class MessageSender implements Runnable {
        //region Getter and Setter
        public double getMinTemperature() {
            return minTemperature;
        }

        public void setMinTemperature(double minTemperature) {
            this.minTemperature = minTemperature;
        }

        public double getMinSpeed() {
            return minSpeed;
        }

        public void setMinSpeed(double minSpeed) {
            this.minSpeed = minSpeed;
        }

        public double getMaxTemperature() {
            return maxTemperature;
        }

        public void setMaxTemperature(double maxTemperature) {
            this.maxTemperature = maxTemperature;
        }

        public double getMaxSpeed() {
            return maxSpeed;
        }

        public void setMaxSpeed(double maxSpeed) {
            this.maxSpeed = maxSpeed;
        }
        //endregion

        double minTemperature = 20;
        double minSpeed =0;
        double maxTemperature=80;
        double maxSpeed= 280;

        private static double getRandomNumberInRange(int min, int max) {

            if (min >= max) {
                int a= min;
                min=max;
                max  =a;

            }

            Random r = new Random();
            return r.nextInt(((6 - 1) + 1) );
        }
        public void run()  {
            try {
                int minPieceNumber=0;
                Random rand = new Random();
                int i=1;
                int j=5;
                double currentTemperature=minTemperature,currentSpeed=minSpeed;
                int currentPieceNumber;


                    while (true)
                    {
                        String msgStr;
                        Message msg;



                        if (currentSpeed>=maxSpeed){
                            while (currentSpeed>maxSpeed)
                            {
                                currentSpeed+=getRandomNumberInRange(j,i);
                                currentSpeed=getRandomNumberInRange(i,j)/23;
                                currentTemperature+= getRandomNumberInRange(i, j)/15;
                                currentPieceNumber = minPieceNumber++;
                                TelemetryDataPoint telemetryDataPoint= new TelemetryDataPoint();
                                telemetryDataPoint.deviceId=deviceId;
                                telemetryDataPoint.temperature=currentTemperature;
                                telemetryDataPoint.speed=currentSpeed;
                                telemetryDataPoint.pieceNumber=currentPieceNumber;
                                msgStr=telemetryDataPoint.serialize();
                                msg=new Message(msgStr);
                                msg.setProperty("Level","Critical");
                                // msg.setProperty("temperatureAlert", (currentTemperature > maxTemperature) ? "true" : "false");
                                msg.setMessageId(java.util.UUID.randomUUID().toString());
                                System.out.println("Sending: " + msgStr);

                                Object lockobj = new Object();
                                EventCallback callback = new EventCallback();
                                client.sendEventAsync(msg, callback, lockobj);

                                synchronized (lockobj) {
                                    lockobj.wait();
                                }
                                Thread.sleep(1000);


                            }
                        }


                        if (currentTemperature<=maxTemperature){
                            currentTemperature +=getRandomNumberInRange(i,j)/3;
                            //currentSpeed += getRandomNumberInRange(i,j)/5;
                        }
                        else if (currentTemperature>maxTemperature){
                            while(i*j>0){
                                // msgStr="Temperature Alert";
                                // msg= new Message(msgStr);

                                currentSpeed=getRandomNumberInRange(i,j)/23;
                                currentTemperature+= getRandomNumberInRange(i, j)/15;
                                currentPieceNumber = minPieceNumber++;
                                TelemetryDataPoint telemetryDataPoint= new TelemetryDataPoint();
                                telemetryDataPoint.deviceId=deviceId;
                                telemetryDataPoint.temperature=currentTemperature;
                                telemetryDataPoint.speed=currentSpeed;
                                telemetryDataPoint.pieceNumber=currentPieceNumber;
                                msgStr=telemetryDataPoint.serialize();
                                msg=new Message(msgStr);
                                msg.setProperty("Level","Critical");
                                // msg.setProperty("temperatureAlert", (currentTemperature > maxTemperature) ? "true" : "false");
                                msg.setMessageId(java.util.UUID.randomUUID().toString());
                                System.out.println("Sending: " + msgStr);

                                Object lockobj = new Object();
                                EventCallback callback = new EventCallback();
                                client.sendEventAsync(msg, callback, lockobj);

                                synchronized (lockobj) {
                                    lockobj.wait();
                                }
                                Thread.sleep(1000);

                                i--;
                                j--;}
                        }
                        currentSpeed+=getRandomNumberInRange(i,j);
                        currentSpeed+=getRandomNumberInRange(i,j);
                        currentPieceNumber = minPieceNumber++;
                        TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
                        telemetryDataPoint.deviceId = deviceId;
                        telemetryDataPoint.temperature = currentTemperature;
                        telemetryDataPoint.speed = currentSpeed;
                        telemetryDataPoint.pieceNumber= currentPieceNumber;

                        msgStr = telemetryDataPoint.serialize();
                        msg = new Message(msgStr);
                        msg.setProperty("temperatureAlert", (currentTemperature > maxTemperature) ? "true" : "false");
                        msg.setMessageId(java.util.UUID.randomUUID().toString());
                        System.out.println("Sending: " + msgStr);

                        Object lockobj = new Object();
                        EventCallback callback = new EventCallback();
                        client.sendEventAsync(msg, callback, lockobj);

                        synchronized (lockobj) {
                            lockobj.wait();
                        }
                        Thread.sleep(1000);
                        i++;
                        j=j+2;

                    }

            } catch (InterruptedException e) {
                System.out.println("Finished.");
            }
        }
    }
    private static class AppMessageCallback implements MessageCallback {
        public IotHubMessageResult execute(Message msg, Object context) {
            System.out.println("Received message from hub: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            return IotHubMessageResult.COMPLETE;
        }
    }
    public static void main( String[] args ) throws IOException, URISyntaxException {

        client = new DeviceClient(connString, protocol);
        client.open();
        client.toString();

        MessageSender sender = new MessageSender();
        sender.run();

        System.in.read();
        client.closeNow();
    }
}

