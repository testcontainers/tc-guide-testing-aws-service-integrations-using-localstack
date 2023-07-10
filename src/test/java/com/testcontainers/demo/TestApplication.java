package com.testcontainers.demo;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main).with(TestApplication.class).run(args);
    }

    static {
        try {
            var container = new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.0"));
            container.start();

            String BUCKET_NAME = UUID.randomUUID().toString();
            String QUEUE_NAME = UUID.randomUUID().toString();

            container.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);
            container.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);

            System.setProperty("app.bucket", BUCKET_NAME);
            System.setProperty("app.queue", QUEUE_NAME);
            System.setProperty("spring.cloud.aws.region.static", container.getRegion());
            System.setProperty("spring.cloud.aws.credentials.access-key", container.getAccessKey());
            System.setProperty("spring.cloud.aws.credentials.secret-key", container.getSecretKey());
            System.setProperty(
                    "spring.cloud.aws.s3.endpoint",
                    container.getEndpointOverride(S3).toString());
            System.setProperty(
                    "spring.cloud.aws.sqs.endpoint",
                    container.getEndpointOverride(SQS).toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
