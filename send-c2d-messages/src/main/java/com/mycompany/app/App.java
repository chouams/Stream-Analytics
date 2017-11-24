package com.mycompany.app;
import com.microsoft.azure.sdk.iot.service.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.*;


public class App 
{private static final String connectionString = "HostName=iothub-0f09bdec.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=WIyFAoGQDJKye5R6JIaxD2YaD9OQXjQ3f9XlpS5b7o8=";
    private static final String deviceId = "htw-device";
    private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;
    public static void main(String[] args) throws IOException,
            URISyntaxException, Exception {
        ServiceClient serviceClient = ServiceClient.createFromConnectionString(
                connectionString, protocol);

        if  (serviceClient != null) {
            serviceClient.open();
            FeedbackReceiver feedbackReceiver = serviceClient
                    .getFeedbackReceiver();
            if (feedbackReceiver != null) feedbackReceiver.open();
            Message messageToSend = new Message("message From Hub.");
            messageToSend.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);

            serviceClient.send(deviceId, messageToSend);
            System.out.println("Message sent to device");
            FeedbackBatch feedbackBatch = feedbackReceiver.receive(1000);
            if (feedbackBatch != null) {
                System.out.println("Message feedback received, feedback time: "
                        + feedbackBatch.getEnqueuedTimeUtc().toString());
            }

            if (feedbackReceiver != null) feedbackReceiver.close();
            serviceClient.close();
        }
    }
}
