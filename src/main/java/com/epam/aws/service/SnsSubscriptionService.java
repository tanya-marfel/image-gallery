package com.epam.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.TopicConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.UnsubscribeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SnsSubscriptionService {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private AmazonSNS snsClient;

    @Value("${amazon.sns.topicArn}")
    private String topicArn;

    @Value("${amazon.s3.bucket-name}")
    private String bucketName;

    public SubscribeResult createSubscription(String email) {
        createBucketNotificationRequest(topicArn, bucketName);
        System.out.println(getExistingSubscriptions(topicArn, email));
        if (getExistingSubscriptions(topicArn, email).size() == 0) {
            return snsClient.subscribe(topicArn, "email", email);
        }
        return new SubscribeResult();

    }

    public List<UnsubscribeResult> unsubscribeFromTopic(String email, String bucketName) {
        JsonNode notifConfig = null;
        try {
            notifConfig = new ObjectMapper()
                    .readTree(s3Client.getBucketNotificationConfiguration(bucketName).toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
        String topicARN = notifConfig.get("snsTopicConfig").get("topicARN").asText();

        return getExistingSubscriptions(topicARN, email)
                .stream()
                .peek(System.out::println)
                .map(s -> snsClient.unsubscribe(s.getSubscriptionArn()))
                .collect(Collectors.toList());

    }

    public void createBucketNotificationRequest(String topicArn, String bucketName) {
        BucketNotificationConfiguration notificationConfiguration = new BucketNotificationConfiguration();

        notificationConfiguration.addConfiguration("snsTopicConfig",
                new TopicConfiguration(topicArn, EnumSet.of(S3Event.ObjectCreated)));

        s3Client.setBucketNotificationConfiguration(bucketName, notificationConfiguration);
    }

    private List<Subscription> getExistingSubscriptions(String topicArn, String email) {
        return snsClient.listSubscriptions()
                        .getSubscriptions()
                        .stream()
                        .peek(System.out::println)
                        .filter(s -> s.getTopicArn().contains(topicArn.trim())
                                && s.getEndpoint().contains(email.trim())
                                && !s.getSubscriptionArn().equals("PendingConfirmation"))
                        .collect(Collectors.toList());
    }
}
