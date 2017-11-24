package com.mycompany.app;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.*;

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
        public double humidity;

        public String serialize() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }
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
    private static class MessageSender implements Runnable {

        public void run()  {
            try {
                double minTemperature = 20;
                double minHumidity = 60;
                Random rand = new Random();

                while (true) {
                    String msgStr;
                    Message msg;
                    if (new Random().nextDouble() > 0.7) {
                        msgStr = "This is a critical message.";
                        msg = new Message(msgStr);
                        msg.setProperty("level", "critical");
                    } else {
                        double currentTemperature = minTemperature + rand.nextDouble() * 15;
                        double currentHumidity = minHumidity + rand.nextDouble() * 20;
                        TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
                        telemetryDataPoint.deviceId = deviceId;
                        telemetryDataPoint.temperature = currentTemperature;
                        telemetryDataPoint.humidity = currentHumidity;

                        msgStr = telemetryDataPoint.serialize();
                        msg = new Message(msgStr);
                    }

                    System.out.println("Sending: " + msgStr);

                    Object lockobj = new Object();
                    EventCallback callback = new EventCallback();
                    client.sendEventAsync(msg, callback, lockobj);

                    synchronized (lockobj) {
                        lockobj.wait();
                    }
                    Thread.sleep(1000);
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

        MessageCallback callback = new AppMessageCallback();
        client.setMessageCallback(callback, null);
        client.open();
    }
}

