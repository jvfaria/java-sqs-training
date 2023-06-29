package com.javasqstraining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication(exclude = {
		org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration.class,
		org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration.class,
		org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration.class,
		HypermediaAutoConfiguration.class, RepositoryRestMvcAutoConfiguration.class,
		SpringDataWebAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class,
		ArtemisAutoConfiguration.class
})
@EnableWebMvc
@ConfigurationPropertiesScan
public class JavaSqsTrainingApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaSqsTrainingApplication.class, args);
	}

}
