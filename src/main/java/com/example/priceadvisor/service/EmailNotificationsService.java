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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;

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
            Workbook workbook = new XSSFWorkbook();
            // Create a new sheet
            Sheet sheet = workbook.createSheet("Sample Sheet");
            // Create a header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("itemId");
            headerRow.createCell(1).setCellValue("name");
            headerRow.createCell(2).setCellValue("upc");
            headerRow.createCell(3).setCellValue("sku");
            headerRow.createCell(4).setCellValue("description");
            headerRow.createCell(5).setCellValue("smallBusinessPrice");
            headerRow.createCell(6).setCellValue("amazonPrice");
            headerRow.createCell(7).setCellValue("walmartPrice");
            headerRow.createCell(8).setCellValue("ebayPrice");
            headerRow.createCell(9).setCellValue("priceSuggestion");
            headerRow.createCell(10).setCellValue("inventoryId");
            // Write the product data to the sheet
            int rowNum = 1;
            for (Item item : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getItemId());
                row.createCell(1).setCellValue(item.getName());
                row.createCell(2).setCellValue(item.getUpc());
                row.createCell(3).setCellValue(item.getSku());
                row.createCell(4).setCellValue(item.getDescription());
                row.createCell(5).setCellValue(item.getSmallBusinessPrice().toString());
                row.createCell(6).setCellValue(item.getAmazonPrice().toString());
                row.createCell(7).setCellValue(item.getWalmartPrice().toString());
                row.createCell(8).setCellValue(item.getEbayPrice().toString());
                row.createCell(9).setCellValue(item.getPriceSuggestion().toString());
                row.createCell(10).setCellValue(item.getInventoryId());
            }
            // Write the output to a file try
            try (FileOutputStream fileOut = new FileOutputStream("ItemFile.xlsx")){
                workbook.write(fileOut);
                workbook.close();
                System.out.println("Excel file created successfully.");
            } catch(IOException e){
                e.printStackTrace();
            }
            String topicArn = buildTopicArn(businessName, emailNotificationsFrequency, workbook);
//
//            PublishRequest request = PublishRequest.builder()
//                    .message(excelFile)
//                    .topicArn(topicArn)
//                    .subject(emailNotificationsFrequency.name().substring(0, 1).toUpperCase()
//                        + emailNotificationsFrequency.name().substring(1).toLowerCase() + "Price Advisor Notification")
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