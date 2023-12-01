package com.testcontainers.demo;

import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

public class TestApplication {

  public static void main(String[] args) {
    setup();
    SpringApplication.from(Application::main).run(args);
  }

  static void setup() {
    try {
      var container = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:3.0")
      );
      container.start();

      String BUCKET_NAME = UUID.randomUUID().toString();
      String QUEUE_NAME = UUID.randomUUID().toString();

      container.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);
      container.execInContainer(
        "awslocal",
        "sqs",
        "create-queue",
        "--queue-name",
        QUEUE_NAME
      );

      System.setProperty("app.bucket", BUCKET_NAME);
      System.setProperty("app.queue", QUEUE_NAME);
      System.setProperty(
        "spring.cloud.aws.region.static",
        container.getRegion()
      );
      System.setProperty(
        "spring.cloud.aws.credentials.access-key",
        container.getAccessKey()
      );
      System.setProperty(
        "spring.cloud.aws.credentials.secret-key",
        container.getSecretKey()
      );
      System.setProperty(
        "spring.cloud.aws.endpoint",
        container.getEndpoint().toString()
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
