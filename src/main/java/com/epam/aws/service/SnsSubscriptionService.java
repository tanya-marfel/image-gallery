package com.epam.aws.service;

import com.amazonaws.auth.policy.Condition;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.SNSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.TopicConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SetTopicAttributesRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sns.model.UnsubscribeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class SnsSubscriptionService {

    private static final String DEFAULT_TOPIC_NAME = "S3UploadNotification_%s";

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private AmazonSNS snsClient;

    public SubscribeResult createSubscription(String email, String bucketName) {
        String currentTopic = format(DEFAULT_TOPIC_NAME, bucketName);
        Optional<Topic> topic = snsClient.listTopics().getTopics()
                                         .stream()
                                         .filter(t -> t.getTopicArn().contains(currentTopic))
                                         .findFirst();
        String topicArn = null;
        if (topic.isEmpty()) {
            topicArn = createSNSTopic(currentTopic);
            snsClient.setTopicAttributes(new SetTopicAttributesRequest(topicArn,
                    "Policy", createTopicPolicy(topicArn, bucketName).toJson()));
            createBucketNotificationRequest(topicArn, bucketName);
        } else {
            topicArn = topic.get().getTopicArn();
        }
        if (getExistingSubscriptions(currentTopic, email).size() == 0) {
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
                .map(s -> snsClient.unsubscribe(s.getSubscriptionArn()))
                .collect(Collectors.toList());

    }

    public String createSNSTopic(String topicName) {
        return snsClient.createTopic(topicName).getTopicArn();
    }

    public void createBucketNotificationRequest(String topicArn, String bucketName) {
        BucketNotificationConfiguration notificationConfiguration = new BucketNotificationConfiguration();

        notificationConfiguration.addConfiguration("snsTopicConfig",
                new TopicConfiguration(topicArn, EnumSet.of(S3Event.ObjectCreated)));

        s3Client.setBucketNotificationConfiguration(bucketName, notificationConfiguration);
    }

    public Policy createTopicPolicy(String topicArn, String bucketName) {
        Condition arnCondition = new ArnCondition(ArnCondition.ArnComparisonType.ArnLike, "aws:SourceArn",
                "arn:aws:s3:*:*:".concat(bucketName));
        return new Policy().withStatements(
                new Statement(Statement.Effect.Allow)
                        .withResources(new Resource(topicArn))
                        .withPrincipals(Principal.AllUsers)
                        .withActions(SNSActions.Subscribe, SNSActions.Publish)
                        .withConditions(arnCondition));

    }

    private List<Subscription> getExistingSubscriptions(String topicName, String email) {
        return snsClient.listSubscriptions()
                        .getSubscriptions().stream().filter(s -> s.getTopicArn().contains(topicName)
                        && s.getEndpoint().equals(email)
                        && !s.getSubscriptionArn().equals("PendingConfirmation"))
                        .collect(Collectors.toList());
    }
}
