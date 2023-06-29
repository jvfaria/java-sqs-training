package com.javasqstraining;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.QueueNameExistsException;
import com.javasqstraining.queueproperties.TestQueueProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Slf4j
@Profile("sqs-test")
@Configuration
public class SqsTestConfiguration implements InitializingBean {
    public static final LocalStackContainer SQS_CONTAINER;

    static {
        SQS_CONTAINER = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.1.0")).withServices(SQS);
        SQS_CONTAINER.start();
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Preparing localstack container and AWS beans");
    }

    @Bean
    @Primary
    public AmazonSQSAsync amazonSQSAsync(Set<TestQueueProperties> queueProperties) {
        AmazonSQSAsync amazonSQSAsync = AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(SQS_CONTAINER.getEndpointConfiguration(SQS))
                .withCredentials(SQS_CONTAINER.getDefaultCredentialsProvider())
                .build();

        queueProperties.forEach(queue -> {
            log.info("Creating message queue on AWS SQS");
            try {
                CreateQueueResult queueResult = amazonSQSAsync.createQueue(queue.getName());
                CreateQueueResult queueDlqResult = amazonSQSAsync.createQueue(queue.getDlqName());
                log.info("Create Queue Response {}", queueResult.getQueueUrl());
                log.info("Create DLQ Queue Response {}", queueDlqResult.getQueueUrl());
            } catch (QueueNameExistsException e) {
                log.error("Queue Name Exists {}", e.getErrorMessage());
            }
        });

        return amazonSQSAsync;
    }
}
