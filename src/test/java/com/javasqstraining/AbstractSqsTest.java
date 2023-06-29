package com.javasqstraining;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.InvalidMessageContentsException;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@ActiveProfiles("sqs-test")
@Slf4j
public abstract class AbstractSqsTest {
    @Autowired
    @Getter
    public AmazonSQSAsync amazonSQS;

    public List<Message> messages = new ArrayList<>();

    public void createQueue(String queueName) { amazonSQS.createQueue(queueName); }

    public void purgeQueue(String queueName) {
        amazonSQS.purgeQueue(new PurgeQueueRequest(getQueueUrl(queueName)));
    }

    public void deleteQueue(String queueName) {
        amazonSQS.deleteQueue(queueName);
    }

    public String getQueueUrl(String queueName) {
        return amazonSQS.getQueueUrl(queueName).getQueueUrl();
    }

    public ListQueuesResult listQueues() { return amazonSQS.listQueues(); }

    public List<String> getQueuesUrls() { return amazonSQS.listQueues().getQueueUrls(); }

    public String getSingleQueueUrl(String queueName) { return amazonSQS.getQueueUrl(queueName).getQueueUrl(); }

    public void publishMessage(String message, String queue) {
        try {
            GetQueueUrlResult queueUrl = amazonSQS.getQueueUrl(queue);
            log.info("Reading SQS Queue done: URL {}", queueUrl.getQueueUrl());
            SendMessageResult sendMessageResult = amazonSQS.sendMessage(queueUrl.getQueueUrl(), message);
            log.info("Message sended to queue {} with id {}", queueUrl.getQueueUrl(), sendMessageResult.getMessageId());
        } catch (QueueDoesNotExistException | InvalidMessageContentsException e) {
            log.error("Queue does not exist {}", e.getMessage());
        }
    }

    public ReceiveMessageResult receiveMessage(ReceiveMessageRequest messageRequest) { return amazonSQS.receiveMessage(messageRequest); }

    public List<Message> receiveMessages(String queue) {
        try {
            String queueUrl = amazonSQS.getQueueUrl(queue).getQueueUrl();
            log.info("Reading SQS Queue done: URL {}", queueUrl);

            ReceiveMessageRequest messageRequest = new ReceiveMessageRequest();
            messageRequest.setQueueUrl(queueUrl);
            messageRequest.setVisibilityTimeout(30);
            messageRequest.setMaxNumberOfMessages(10);

            ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(messageRequest);
            var receivedMessagesResult = receiveMessageResult.getMessages();
            if (!receivedMessagesResult.isEmpty()) {
                receivedMessagesResult.forEach(message -> {
                    log.info("Incoming Message From SQS {}", message.getMessageId());
                    log.info("Message Body {}", message.getBody());
                    messages.add(message);
                });
            }
        } catch (QueueDoesNotExistException e) {
            log.error("Queue does not exist {}", e.getMessage());
        }
        return messages;
    }


    public void buildMessageContentAndPublishMessageOnQueue(String queue, String messageContent) {
        Stream.iterate(0, n -> n + 1)
                .limit(10)
                .forEach(x -> publishMessage(messageContent, queue));
    }
}
