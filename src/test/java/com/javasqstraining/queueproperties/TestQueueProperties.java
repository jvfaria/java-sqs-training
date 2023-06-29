package com.javasqstraining.queueproperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("jsqst.queuetraining.queues.test-queue")
@Getter
@Setter
public class TestQueueProperties {
    private String name;
    private String dlqName;
}
