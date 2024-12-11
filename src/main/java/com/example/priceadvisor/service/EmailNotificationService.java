package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
                    .topicArn(topicArn)
                    .build();

            snsClient.subscribe(request);
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

            // Assuming buildTopicArn returns a single topic ARN
            String topicArn = buildTopicArn(emailNotificationsFrequency, businessName);

            // Create the ListSubscriptionsRequest with the topic ARN
            ListSubscriptionsByTopicRequest listSubscriptionsByTopicRequest = ListSubscriptionsByTopicRequest.builder()
                    .topicArn(topicArn)  // Set the topic ARN directly
                    .build();

            // Call SNS to list subscriptions for the topic
            ListSubscriptionsByTopicResponse response = snsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest);

            // Iterate through subscriptions to find the matching subscription by email
            for (Subscription subscription : response.subscriptions()) {
                if (subscription.endpoint().equals(userEmail)) {
                    UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.builder()
                            .subscriptionArn(subscription.subscriptionArn())  // Get the Subscription ARN
                            .build();

                    snsClient.unsubscribe(unsubscribeRequest);
                    System.out.println("Unsubscribed successfully from topic: " + topicArn);
                    break;  // Successfully unsubscribed, exit the method
                }
            }

            // If no subscription was found for the given email
            System.err.println("No subscription found for email: " + userEmail);

        } catch (SnsException e) {
            System.err.println("Error Unsubscribing: " + e.getMessage());
        }
    }


//    public void publishNotification(String message, String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
//        try {
//            String businessName = businessRepository.findById(businessId)
//                    .orElseThrow(() -> new IllegalArgumentException("Business not found.")).getName();@
//
//
//            String topicArn = buildTopicArn(emailNotificationsFrequency, businessName);
//
//
//            PublishRequest request = PublishRequest.builder()
//                    .message(message)
//                    .topicArn(topicArn)
//                    .subject("Price Advisor Notification")
//                    .build();
//            snsClient.publish(request);
//        } catch (SnsException e) {
//            System.err.println("Error Sending Notification: " + e.getMessage());
//        }
//    }
}
