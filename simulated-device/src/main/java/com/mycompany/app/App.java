package com.mycompany.app;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.*;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.net.URISyntaxException;
import java.util.Random;

import static java.lang.Math.pow;
import static java.lang.Math.round;

public class App
{
    private static String connString1 = "HostName=iothub-0f09bdec.azure-devices.net;DeviceId=htw-device;SharedAccessKey=n3i6d/C1tSnnbo0mv8KU1sSlOvvx6AOK5yN8rP4ToDQ=";
   private static String connString2="HostName=iothub-0f09bdec.azure-devices.net;DeviceId=htw-device2;SharedAccessKey=+XXJfI4/L0iTOi4zZNCCjV2u142280F92Akbk/FSRsE=";
    private static IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
    private static String deviceId = "htw-device";
    private static String DeviceId2="htw-device2";
    private static DeviceClient client1;
    private static DeviceClient client2;

    public static class TelemetryDataPoint {
        public String mongoId;
        public String deviceId;
        public double Temperatur;
        public double Geschwindigkeit;
        public int Stückzahl;
        public Date Datum;
        public String Uhrzeit;


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
    public static class MessageSender implements Runnable {


        int minTemperature ;
        int minSpeed;
        int maxTemperature;
        int maxSpeed;
        String dbID;
        int TimeIntevall;

        public MessageSender(int minTemperature, int minSpeed, int maxTemperature, int maxSpeed, String mongoId, int TimeIntervall) {
            this.minTemperature = minTemperature;
            this.minSpeed = minSpeed;
            this.maxTemperature = maxTemperature;
            this.maxSpeed = maxSpeed;
            this.dbID=mongoId;
            this.TimeIntevall=TimeIntervall;
        }
public static double getDoubleRound(double zahl,int Nachkommastelle ){
                    double Zahlx=zahl;
                    zahl=round(zahl*pow(10,Nachkommastelle));
                    Zahlx=zahl/pow(10,Nachkommastelle);

            return Zahlx;
}
        private static double getRandomNumberInRange(int min, int max) {

            if (min > max) {
                int a= min;
                min=max;
                max  =a;

            }

            Random r = new Random();
            return r.nextInt(((6 - 1) + 1) );
        }
        public void run()  {
            try {
                ReadFiles readFiles=new ReadFiles();
                int minPieceNumber=0;
                Random rand = new Random();
                int i=1;
                int j=6;
                double currentTemperature = minTemperature,currentSpeed = minSpeed;
                int currentPieceNumber;


                    while (true)
                    {
                        String msgStr;
                        Message msg;

                        currentSpeed+=getRandomNumberInRange(i,j)/3;
                        currentTemperature+=getRandomNumberInRange(i,j)/9;
                        currentPieceNumber = minPieceNumber++;
                        TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
                        telemetryDataPoint.deviceId =deviceId;
                        telemetryDataPoint.mongoId=dbID;
                        telemetryDataPoint.Temperatur = getDoubleRound(currentTemperature,3);
                        telemetryDataPoint.Geschwindigkeit = getDoubleRound(currentSpeed,3);
                        telemetryDataPoint.Stückzahl = currentPieceNumber;
                        Date date= new Date( System.currentTimeMillis() );
                        telemetryDataPoint.Datum =date;
                        SimpleDateFormat sdf =new SimpleDateFormat("HH:mm:ss");
                        telemetryDataPoint.Uhrzeit =sdf.format(date);


                        msgStr = telemetryDataPoint.serialize();
                        msg = new Message(msgStr);
                        msg.setProperty("temperatureAlert", (currentTemperature > maxTemperature) ? "true" : "false");
                        msg.setMessageId(java.util.UUID.randomUUID().toString());
                        System.out.println("Sending: " + msgStr);

                        Object lockobj = new Object();
                        EventCallback callback = new EventCallback();
                        client1.sendEventAsync(msg, callback, lockobj);
                       // client2.sendEventAsync(msg,callback,lockobj);

                        synchronized (lockobj) {
                            lockobj.wait();
                        }
                        Thread.sleep(TimeIntevall);

                        if (currentSpeed>=maxSpeed |currentTemperature>maxTemperature){
                            if(currentSpeed>maxSpeed){currentSpeed-=getRandomNumberInRange(i,j)/3;}
                            if(currentTemperature>maxTemperature){currentTemperature-=getRandomNumberInRange(i,j)/5;}
                        currentPieceNumber = minPieceNumber++;
                        telemetryDataPoint = new TelemetryDataPoint();
                        telemetryDataPoint.deviceId=deviceId;
                        telemetryDataPoint.mongoId=dbID;
                        telemetryDataPoint.Temperatur = getDoubleRound(currentTemperature,2);
                        telemetryDataPoint.Geschwindigkeit = getDoubleRound(currentSpeed,2);
                        telemetryDataPoint.Stückzahl = currentPieceNumber;
                        date= new Date( System.currentTimeMillis() );
                        telemetryDataPoint.Datum =date;
                        sdf =new SimpleDateFormat("HH:mm:ss");
                        telemetryDataPoint.Uhrzeit =sdf.format(date);


                        msgStr = telemetryDataPoint.serialize();
                        msg = new Message(msgStr);
                        msg.setProperty("temperatureAlert", (currentTemperature > maxTemperature) ? "true" : "false");
                        msg.setMessageId(java.util.UUID.randomUUID().toString());
                        System.out.println("Sending: " + msgStr);

                         lockobj = new Object();
                         callback = new EventCallback();
                        client1.sendEventAsync(msg, callback, lockobj);
                        // client2.sendEventAsync(msg,callback,lockobj);

                        synchronized (lockobj) {
                            lockobj.wait();
                        }
                        Thread.sleep(TimeIntevall);}
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
        client1 = new DeviceClient(connString1, protocol);
        client2= new DeviceClient(connString2,protocol);

      //  String path="C:\\Users\\chouams\\Desktop\\iot-java-get-started12\\simulated-device\\src\\main\\java\\com\\mycompany\\app\\Config_maschine 1.txt";
        String path="C:\\Users\\chouams\\Desktop\\iot-java-get-started12\\simulated-device\\src\\main\\java\\com\\mycompany\\app\\Config_maschine 2.txt";

        Map<String,String> Dictionary= ReadFiles.readfile(path);

        client1.open();
        //client2.open();
        client1.toString();
       // client2.toString();

        MessageSender sender = new MessageSender(Integer.parseInt(Dictionary.get("minTemperature")),Integer.parseInt(Dictionary.get("minSpeed")),Integer.parseInt(Dictionary.get("maxTemperature")),Integer.parseInt(Dictionary.get("maxSpeed")),Dictionary.get("mongoId"),Integer.parseInt(Dictionary.get("TimeIntervall")));
        sender.run();


        client1.closeNow();
      //  client2.closeNow();
    }
}

