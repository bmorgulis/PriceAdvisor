package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.ArrayList;
import java.util.List;

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
        } catch (SnsException e) {
            System.err.println("Error Subscribing: " + e.getMessage());
        }
    }

    //Helper method to build the topic ARN from the base ARN, emailNotificationsFrequency, and businessName.
    private String buildTopicArn(User.EmailNotificationsFrequency emailNotificationsFrequency, String businessName) {
        String formattedBusinessName = businessName.replaceAll("\\s+", "_"); // Replace spaces with underscores
        String topicName = formattedBusinessName + "_" + emailNotificationsFrequency.name();
        String topicArn = baseArn + topicName;
        return topicArn;
    }

    // checks for multiple subscriptions and unsubscribes from all but the new one.
    public void unsubscribe(String userEmail, User.EmailNotificationsFrequency newFrequency, int businessId) {
        try {
            // Get business name from the repository
            String businessName = businessRepository.findById(businessId)
                    .orElseThrow(() -> new IllegalArgumentException("Business not found.")).getName();

            // Build the topic ARN for the new subscription
            String newTopicArn = buildTopicArn(newFrequency, businessName);

            // List all topic Arn's related to the business
            List<String> topicArnsList = getTopicArns(businessName);

            // Iterate through all topics of the given business to list and manage subscriptions(for unsubscribing them)
            for (String topic : topicArnsList) {
                // List all subscriptions for the current topic
                ListSubscriptionsByTopicRequest listRequest = ListSubscriptionsByTopicRequest.builder()
                        .topicArn(topic)
                        .build();

                // Get the list of subscriptions for the topic that we are currently iterating through
                ListSubscriptionsByTopicResponse listResponse = snsClient.listSubscriptionsByTopic(listRequest);

                // Iterate through the list of subscriptions for the current topic to find the ones to unsubscribe from
                for (Subscription subscription : listResponse.subscriptions()) {
                    if (subscription.endpoint().equals(userEmail)) { // Check if the subscription endpoint(email) matches the user's email AND
                        if (!subscription.topicArn().equals(newTopicArn)) {  // Check if subscription in the list has the same topic ARN as the new one. If not, unsubscribe.
                            UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.builder()
                                    .subscriptionArn(subscription.subscriptionArn())
                                    .build();
                            snsClient.unsubscribe(unsubscribeRequest);
                        }
                    }
                }
            }
        } catch (SnsException e) {
            System.err.println("Error Unsubscribing: " + e.getMessage());
        }
    }

    // Helper method to get all the topic Arn's of a business to be used in the unsubscribe method
    private List<String> getTopicArns(String businessName) {
        List<String> topicArnsList = new ArrayList<>(); // List to store all topic Arn's

        for (User.EmailNotificationsFrequency frequency : User.EmailNotificationsFrequency.values()) { // Go through all possible emailNotificationFrequency values
            if (frequency != User.EmailNotificationsFrequency.NONE) { // Skip NONE frequency
                String topicArn = buildTopicArn(frequency, businessName);
                topicArnsList.add(topicArn);
            }
        }
        return topicArnsList;
    }


    public void publishNotification(String message, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
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
