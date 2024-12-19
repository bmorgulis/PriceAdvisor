package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Business;
import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

@Service
public class EmailNotificationsService {

    private final String baseArn = "arn:aws:sns:us-east-1:471112717872:";
    private final SnsClient snsClient;
    private final BusinessRepository businessRepository;


    @Autowired
    public EmailNotificationsService(SnsClient snsClient, BusinessRepository businessRepository) {
        this.snsClient = snsClient;
        this.businessRepository = businessRepository;
    }

    public void subscribeUserToTopic(String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
        try {
            String businessName = getBusinessName(businessId);
            String topicArn = buildTopicArn(businessName, emailNotificationsFrequency);

            SubscribeRequest request = SubscribeRequest.builder()
                    .protocol("email")
                    .endpoint(userEmail)
                    .topicArn(topicArn)
                    .build();

            snsClient.subscribe(request);
        } catch (SnsException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeUserFromTopic(String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
        try {
            String businessName = getBusinessName(businessId);
            String topicArn = buildTopicArn(businessName, emailNotificationsFrequency);

            // Create a request to list all subscriptions for the given topic ARN
            ListSubscriptionsByTopicRequest listSubscriptionsByTopicRequest = ListSubscriptionsByTopicRequest.builder()
                    .topicArn(topicArn)
                    .build();

            // Call SNS to retrieve the list of subscriptions for the specified topic
            ListSubscriptionsByTopicResponse response = snsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest);

            // Iterate over all subscriptions and check if the userEmail matches any subscription endpoint
            for (Subscription subscription : response.subscriptions()) {
                if (subscription.endpoint().equals(userEmail)) {  // If a match is found, unsubscribe the user
                    UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.builder()
                            .subscriptionArn(subscription.subscriptionArn())  // Set the Subscription ARN for the unsubscribe request
                            .build();

                    snsClient.unsubscribe(unsubscribeRequest);
                    break;
                }
            }

        } catch (SnsException e) {
            e.printStackTrace();
        }
    }

    private String getBusinessName(int businessId) {
        return businessRepository.findById(businessId)
                .map(Business::getName)
                .orElseThrow(() -> new IllegalArgumentException("Business not found."));
    }

    private String buildTopicArn(String businessName, User.EmailNotificationsFrequency emailNotificationsFrequency) {
        String formattedBusinessName = businessName.replaceAll("\\s+", "_");
        String topicName = formattedBusinessName + "_" + emailNotificationsFrequency.name();
        return baseArn + topicName;
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