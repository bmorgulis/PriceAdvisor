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
            //publishNotification("You have successfully subscribed to Price Advisor Notifications great work", userEmail, emailNotificationsFrequency, businessId); //for testing purposes to make sure that publish method works

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


    private String buildBaseTopicArn(String businessName) {
        String formattedBusinessName = businessName.replaceAll("\\s+", "_");
        String topicName = formattedBusinessName + "_";
        String topicArn = baseArn + topicName;
        return topicArn;
    }


// //will erase the old subscriptions from aws sns but not as good with multiple subscriptions
//    public void unsubscribe(String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
//        try {
//            String businessName = businessRepository.findById(businessId)
//                    .orElseThrow(() -> new IllegalArgumentException("Business not found.")).getName();
//
//
//            String topicArn = buildTopicArn(emailNotificationsFrequency, businessName);
//
//
//            // List all subscriptions for the topic and find the subscription to unsubscribe from
//            ListSubscriptionsByTopicRequest listRequest = ListSubscriptionsByTopicRequest.builder()
//                    .topicArn(topicArn)
//                    .build();
//
//            // Get the list of subscriptions
//            ListSubscriptionsByTopicResponse listResponse = snsClient.listSubscriptionsByTopic(listRequest);
//
//            // Unsubscribe from the topic
//            for (Subscription subscription : listResponse.subscriptions()) { // Iterate through the list of subscriptions to find the one to unsubscribe from
//                if (subscription.endpoint().equals(userEmail)) { // Check if the subscription endpoint(email) matches the user's email
//                    UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.builder()
//                            .subscriptionArn(subscription.subscriptionArn()) // Use the subscription ARN to unsubscribe
//                            .build();
//                    snsClient.unsubscribe(unsubscribeRequest);
//                    break;
//                }
//            }
//        } catch (SnsException e) {
//            System.err.println("Error Unsubscribing: " + e.getMessage());
//        }
//
//    }







    // checks for multiple subscriptions as opposed to just one subscription. will not erase the old subscriptions from aws sns but will mark them as deleted
        public void unsubscribe(String userEmail, User.EmailNotificationsFrequency newFrequency, int businessId) {
        try {
            // Fetch the business name from the repository
            String businessName = businessRepository.findById(businessId)
                    .orElseThrow(() -> new IllegalArgumentException("Business not found.")).getName();

            // Build the topic ARN for the new subscription
            String newTopicArn = buildTopicArn(newFrequency, businessName);

            // List all subscriptions for the relevant business topics
            String baseTopicArn = buildBaseTopicArn(businessName); // Common base ARN for all topics related to this business todo right now without this line we are not getting the correct arn for the topic because we are not adding in the "_" in the business name
            ListSubscriptionsByTopicRequest listRequest = ListSubscriptionsByTopicRequest.builder()
                    .topicArn(baseTopicArn)
                    .build();

            // Fetch the list of subscriptions
            ListSubscriptionsByTopicResponse listResponse = snsClient.listSubscriptionsByTopic(listRequest);

            // Iterate through the list of subscriptions to find the one to unsubscribe from
            for (Subscription subscription : listResponse.subscriptions()) {
                if (subscription.endpoint().equals(userEmail)) { // Check if the subscription endpoint(email) matches the user's email
                    if (!subscription.topicArn().equals(newTopicArn)) {  // Check if subscription in the list has the same topic ARN as the new one. If not, unsubscribe.
                        UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.builder()
                                .subscriptionArn(subscription.subscriptionArn())
                                .build();
                        snsClient.unsubscribe(unsubscribeRequest);
                    }
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
