package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.entity.User;
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
    private final ItemService itemService;
    private final InventoryService inventoryService;
    private final BusinessService businessService;


    @Autowired
    public EmailNotificationsService(SnsClient snsClient, ItemService itemService, InventoryService inventoryService, BusinessService businessService) {
        this.snsClient = snsClient;
        this.itemService = itemService;
        this.inventoryService = inventoryService;
        this.businessService = businessService;
    }

    public void subscribeUserToTopic(String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency, int businessId) {
        try {
            String businessName = businessService.getBusinessName(businessId);
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
            String businessName = businessService.getBusinessName(businessId);
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
                            .subscriptionArn(subscription.subscriptionArn())  // Set the subscription ARN for the unsubscribe request
                            .build();

                    snsClient.unsubscribe(unsubscribeRequest);
                    break;
                }
            }

        } catch (SnsException e) {
            e.printStackTrace();
        }
    }

    private String buildTopicArn(String businessName, User.EmailNotificationsFrequency emailNotificationsFrequency) {
        String formattedBusinessName = businessName.replaceAll("\\s+", "_");
        String topicName = formattedBusinessName + "_" + emailNotificationsFrequency.name();
        return baseArn + topicName;
    }

    public void publishEmailNotifications(User.EmailNotificationsFrequency emailNotificationsFrequency) {
        try {
            List<Integer> inventoryIds = itemService.getAllDistinctInventoryIds();

            for (Integer inventoryId : inventoryIds) {

                Integer businessId = inventoryService.findBusinessIdByInventoryId(inventoryId);
                String businessName = businessService.getBusinessName(businessId);

                List<String> priceSuggestionRaiseItemNames = itemService.getItemsWithPriceSuggestionRaise(inventoryId, Item.PriceSuggestion.RAISE);
                List<String> priceSuggestionLowerItemNames = itemService.getItemsWithPriceSuggestionRaise(inventoryId, Item.PriceSuggestion.LOWER);

                StringBuilder textContent = buildTextContent(priceSuggestionRaiseItemNames, priceSuggestionLowerItemNames);

                String emailSubject = emailNotificationsFrequency.name().substring(0, 1).toUpperCase() + emailNotificationsFrequency.name().substring(1).toLowerCase() + " Price Advisor Notification";

                String topicArn = buildTopicArn(businessName, emailNotificationsFrequency);

                PublishRequest request = PublishRequest.builder()
                        .message(textContent.toString())
                        .subject(emailSubject)
                        .topicArn(topicArn)
                        .messageStructure("text")
                        .build();
                snsClient.publish(request);
            }
        } catch (SnsException e) {
            e.printStackTrace();
        }
    }

    private static StringBuilder buildTextContent(List<String> priceSuggestionRaiseItemNames, List<String> priceSuggestionLowerItemNames) {
        StringBuilder textContent = new StringBuilder();

        textContent.append("Consider raising the price for the following items:\n\n");
        for (String itemName : priceSuggestionRaiseItemNames) {
            textContent.append(itemName);
        }

        textContent.append("\n\n");

        textContent.append("Consider lowering the price for the following items:\n\n");
        for (String itemName : priceSuggestionLowerItemNames) {
            textContent.append(itemName);
        }
        return textContent;
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
