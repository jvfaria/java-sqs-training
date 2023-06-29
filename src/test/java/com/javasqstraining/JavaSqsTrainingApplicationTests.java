package com.javasqstraining;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javasqstraining.queueproperties.TestQueueProperties;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;

@SpringBootTest
@ActiveProfiles("test")
class JavaSqsTrainingApplicationTests extends AbstractSqsTest {
	@Autowired
	private TestQueueProperties testQueueProperties;

	@Test
	void shouldVerifyIfFirstQueueWasCreatedOnStartup() {
		// Verify if queue was created on startup
		assertThat(getQueueUrl(testQueueProperties.getName())).isNotNull().isNotEmpty();

		// Recriates the queue
		deleteQueue(testQueueProperties.getName());
		Assertions.assertThrows(QueueDoesNotExistException.class, () -> getQueueUrl(testQueueProperties.getName()));

		createQueue(testQueueProperties.getName());
	}

	@Test
	void shouldPublishMessageOnQueueAndVerifyContent() throws JsonProcessingException {
		String messageContent = "NEW message content localstack test";
		publishMessage(buildMessageBody(messageContent), testQueueProperties.getName());

		// Awaitility to receive messages within 5 seconds and execute the callable verifying the predicate
		Callable<List<Message>> receivedMessagesSupplier = () -> receiveMessages(testQueueProperties.getName());
		Predicate<List<Message>> messagesNotEmptyPredicate = messages -> messages != null && !messages.isEmpty();

		List<Message> messages = given()
				.await()
				.atMost(5, SECONDS)
				.until(receivedMessagesSupplier, messagesNotEmptyPredicate);

		MessageDTO messageDTO = new ObjectMapper().readValue(messages.get(0).getBody(), MessageDTO.class);
		assertThat(messageDTO.getMessageContent()).isEqualTo(messageContent);
	}

	public String buildMessageBody(String messageContent) {
		return ("" +
				"{\"id\":\"10\"," +
				"\"messageContent\": \"" + messageContent + "\" ," +
				"\"eventId\":\"ID123\"," +
				"\"timestampUtc\":\"2022-11-11 12:00:00\"," +
				"\"status\":\"PENDING\"}"
		);
	}

	@Component
	@Data
	public static class MessageDTO {
		private String id;
		private String messageContent;
		private String eventId;
		private String timestampUtc;
		private String status;
	}

}
