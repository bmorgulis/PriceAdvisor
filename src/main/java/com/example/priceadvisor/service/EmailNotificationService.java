package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

@Service
public class EmailNotificationService {
    private final SnsClient snsClient;
    @Value("${aws.sns.topic.arn}")
    private String topicArn;    // Inject the topic ARN from the application.properties file. for now for testing purposes ToDo: change to the actual topic ARN based off of that businesses topic and frequency
//    private String topicArn  = "arn:aws:sns:us-east-1:471112717872:Test_Business_DAILY"; //for testing purposes

    public EmailNotificationService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void subscribe(String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency) {
        try {
            // Subscribe to the topic
            SubscribeRequest request = SubscribeRequest.builder()
                    .protocol("email")
                    .endpoint(userEmail)
                    .returnSubscriptionArn(true)
                    .topicArn(topicArn)
                    .build();

            snsClient.subscribe(request);
//            SubscribeResponse result = snsClient.subscribe(request);
//            System.out.println("Subscription ARN: " + result.subscriptionArn());
        } catch (SnsException e) {
            System.err.println("Error Subscribing: " + e.getMessage());
        }


    }

    public void unsubscribe(String userEmail) {
        publishNotification("You have successfully subscribed to Price Advisor Notifications great work");

        try {
            // List all subscriptions for the topic
            ListSubscriptionsByTopicRequest listRequest = ListSubscriptionsByTopicRequest.builder()
                    .topicArn(topicArn)
                    .build();

            // Get the list of subscriptions
            ListSubscriptionsByTopicResponse listResponse = snsClient.listSubscriptionsByTopic(listRequest);

            // Unsubscribe from the topic
            for (Subscription subscription : listResponse.subscriptions()) { // Iterate through the list of subscriptions to find the one to unsubscribe from
                if (subscription.endpoint().equals(userEmail)) {
                    UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.builder()
                            .subscriptionArn(subscription.subscriptionArn()) // Use the subscription ARN to unsubscribe
                            .build();
                    snsClient.unsubscribe(unsubscribeRequest);
                    break;
                }
            }
        } catch (SnsException e) {
            System.err.println("Error Unsubscribing: " + e.getMessage());
        }

    }

    public void publishNotification(String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .topicArn(topicArn)
                    .subject("Price Advisor Notification")
                    .build();
            snsClient.publish(request);
        } catch (SnsException e) {
            System.err.println("Error Sending Notification: " + e.getMessage());
        }
    }



}
