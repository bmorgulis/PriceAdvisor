package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

@Service
public class EmailNotificationService {
    private final SnsClient snsClient;
    private final BusinessRepository businessRepository;

    @Value("${aws.sns.base.arn}") // Inject the topic ARN from the application.properties file.
    private String baseArn;
    private String topicArn;


    public EmailNotificationService(SnsClient snsClient, BusinessRepository businessRepository) {
        this.snsClient = snsClient;
        this.businessRepository = businessRepository;
    }

    public void subscribe(String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
        try {
            String businessName = businessRepository.findById(businessId)
                    .orElseThrow(() -> new IllegalArgumentException("Business not found.")).getName();

            String topicArn = buildTopicArn(emailNotificationsFrequency, businessName);

            // Subscribe to the topic
            SubscribeRequest request = SubscribeRequest.builder()
                    .protocol("email")
                    .endpoint(userEmail)
                    .returnSubscriptionArn(true) // Return the ARN, even if the subscription is not yet confirmed
                    .topicArn(topicArn)
                    .build();

            snsClient.subscribe(request);
            publishNotification("You have successfully subscribed to Price Advisor Notifications great work", userEmail, emailNotificationsFrequency, businessId); //for testing purposes to make sure that publish method works

        } catch (SnsException e) {
            System.err.println("Error Subscribing: " + e.getMessage());
        }

    }

    private String buildTopicArn(User.EmailNotificationsFrequency emailNotificationsFrequency, String businessName) {
        String formattedBusinessName = businessName.replaceAll("\\s+", "_");
        String topicName = formattedBusinessName + "_" + emailNotificationsFrequency.name();
        String topicArn = baseArn + topicName;
        return topicArn;
    }

    public void unsubscribe(String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
        try {
            String businessName = businessRepository.findById(businessId)
                    .orElseThrow(() -> new IllegalArgumentException("Business not found.")).getName();


            String topicArn = buildTopicArn(emailNotificationsFrequency, businessName);


            // List all subscriptions for the topic and find the subscription to unsubscribe from
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

    public void publishNotification(String message, String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
        try {
            String businessName = businessRepository.findById(businessId)
                    .orElseThrow(() -> new IllegalArgumentException("Business not found.")).getName();


            String topicArn = buildTopicArn(emailNotificationsFrequency, businessName);


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
