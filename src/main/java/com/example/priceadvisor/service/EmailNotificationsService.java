package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Business;
import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.List;

@Service
public class EmailNotificationsService {

    private final String baseArn = "arn:aws:sns:us-east-1:471112717872:";
    private final SnsClient snsClient;
    private final BusinessRepository businessRepository;
    private final SecurityContextService securityContextService;
    private final ItemService itemService;
    private final InventoryService inventoryService;


    @Autowired
    public EmailNotificationsService(SnsClient snsClient, BusinessRepository businessRepository, SecurityContextService securityContextService, ItemService itemService, InventoryService inventoryService) {
        this.snsClient = snsClient;
        this.businessRepository = businessRepository;
        this.securityContextService = securityContextService;
        this.itemService = itemService;
        this.inventoryService = inventoryService;
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

    public void publishEmailNotifications(User.EmailNotificationsFrequency emailNotificationsFrequency) {
        try {
            Integer businessId = securityContextService.getCurrentBusinessId();
            String businessName = businessRepository.findById(businessId)
                    .orElseThrow(() -> new IllegalArgumentException("Business not found.")).getName();

            List<Item> items = itemService.findItemsByInventoryId(inventoryService.getInventoryIdByBusinessId(businessId));

            // For Zerach:
            // Create Excel file
            // Put item details into Excel file using "items" variable
            // Send Excel file to the "topicArn" variable

            String topicArn = buildTopicArn(businessName, emailNotificationsFrequency);
//
//            PublishRequest request = PublishRequest.builder()
//                    .message(excelFile)
//                    .topicArn(topicArn)
//                    .subject(emailNotificationsFrequency.name()) + "Price Advisor Notification")
//                    .build();
//            snsClient.publish(request);
        } catch (SnsException e) {
            System.err.println("Error Sending Notification: " + e.getMessage());
        }
    }


    // Schedule the fetch to run on startup and then every hour
    @Scheduled(fixedRate = 3600000)
    public void publishEmailNotificationsHourly() {
        publishEmailNotifications(User.EmailNotificationsFrequency.HOURLY);
    }

    // Schedule the fetch to run on startup and then every day
    @Scheduled(fixedRate = 86400000)
    public void publishEmailNotificationsDaily() {
        publishEmailNotifications(User.EmailNotificationsFrequency.DAILY);
    }

    // Schedule the fetch to run on startup and then every week
    @Scheduled(fixedRate = 604800000)
    public void publishEmailNotificationsWeekly() {
        publishEmailNotifications(User.EmailNotificationsFrequency.WEEKLY);
    }

    // Schedule the fetch to run on startup and then every month
    @Scheduled(fixedRate = 2592000000L)
    public void fetchDataMonthly() {
        publishEmailNotifications(User.EmailNotificationsFrequency.MONTHLY);
    }
}