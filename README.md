# Contract Testing with PactFlow - Workshop
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/31b8f6f078974887811d865cb5976ac4)](https://app.codacy.com/gh/apenlor/contract-testing-workshop/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

### Table of Contents

- [Purpose of this project](#purpose-of-this-project)
- [Requisites](#requisites)
    - [Software](#software)
    - [Platform](#platform)
    - [Local Environment Setup](#local-environment-setup)
    - [GitHub Actions Setup](#github-actions-setup)
- [Project structure](#project-structure)
    - [Consumer](#consumer)
    - [Student Provider](#student-provider)
    - [Teacher Provider](#teacher-provider)
    - [GitHub Actions Workflows](#github-actions-workflows)
- [PactFlow setup](#pactflow-setup)
    - [Webhook Details](#webhook-details)
    - [Creating the GitHub Token for the Webhook](#creating-the-github-token-for-the-webhook)
- [How to use the Workflows](#how-to-use-the-workflows)
- [Workflows in detail](#workflows-in-detail)
    - [Core Workflows](#core-workflows)
    - [Reusable Composite Actions](#reusable-composite-actions)
    - [Verification Workflow](#verification-workflow)
- [Initial Workflow for Bi-Directional](#initial-workflow-for-bi-directional)
- [Initial Workflow for Consumer Driven](#initial-workflow-for-consumer-driven)
    - [Initial status](#initial-status)
    - [How to start up the automation](#how-to-start-up-the-automation)
        - [Summary of steps needed](#summary-of-steps-needed)
        - [Recommended next steps](#recommended-next-steps)
- [Now, it's yours to play with it...](#now-its-yours-to-play-with-it)

## Purpose of this project

This workshop is designed to provide a hands-on experience in Contract Testing using [PactFlow](https://pactflow.io/), integrating a real-world setup with full automation via **GitHub Actions**.

It requires a free [PactFlow SaaS instance](https://try.platform.smartbear.com/new-organization?product=ApiHub) to simulate realistic scenarios for comprehensive training in coding, automation, and configuration.

For this workshop, we assume participants have a foundational knowledge of contract testing. The focus will be on creating various coding and compatibility scenarios, understanding the basic workflow automation, and enhancing or adapting them to new scenarios. Participants will also learn how to configure and use PactFlow as they would in a real-world project.

## Requisites

**[- IMPORTANT! -] → Create your own fork to use this workshop. You'll need to setup webhooks and tokens for a fully CI/CD experience.**

### Software

- Maven 3.x
- JDK 21+

### Platform

You will need access to a PactFlow instance. If you are using a shared instance for this workshop, please contact the administrator to have access granted.

### Local Environment Setup

Variables required for local builds:

| VARIABLE               | VALUE                                |
|------------------------|--------------------------------------|
| `PACT_BROKER_BASE_URL` | `[https://your-company.pactflow.io]` |
| `PACT_BROKER_TOKEN`    | `[ your own read-only user token ]`  |

These variables are required only for building the provider when doing consumer-driven contract testing, allowing it to download the related contracts from PactFlow.

#### How to get your own user token

Once you have access to PactFlow, navigate to "Settings" and select "Copy your read-only token" to get the value. You should use "read-only" tokens locally. All verifications and contract publishing should occur exclusively from the automation workflows.

### GitHub Actions Setup

To run the automated workflows in this repository, you need to configure two things in your GitHub repository's settings (`Settings > Secrets and variables > Actions`):

1.  **Repository Secret (for sensitive values):**
    *   **Name:** `PACTFLOW_TOKEN`
    *   **Value:** Your PactFlow token with read/write permissions. This is different from the read-only token used for local builds.

2.  **Repository Variable (for non-sensitive configuration):**
    *   **Name:** `PACT_BROKER_BASE_URL`
    *   **Value:** The full URL of your PactFlow instance (e.g., `https://your-company.pactflow.io`).

## Project structure

The project has the following core structure:

```
.
├── .github/
│   ├── actions/
│   └── workflows/
├── consumer/
├── student-provider/
└── teacher-provider/
```

We are using a single repository that contains three components: a consumer and two providers. Each one is an independent REST API based on Maven, Spring Boot, and Java. The `*.yml` files in the `.github/` directory define the CI/CD automation.

### Consumer

The consumer component contains two services that interact with the providers. Its contract tests will generate the pact files.

```consumer/
└── src/
    ├── main/
    │   └── java/
    │       └── com/apenlor/pactflow/consumer/
    │           └── service/
    │                   └── StudentService.java
    │                   └── TeacherService.java
    └── test/
        └── java/
            └── com/apenlor/pactflow/consumer/
                ├── contracts/
                │   ├── StudentProviderTest.java
                │   └── TeacherProviderTest.java
                └── utils/
                    ├── Assertions.java
                    ├── DslBodyFactory.java
                    └── FixtureFactory.java
```

In the consumer, the core classes we will focus on
are [`StudentService.java`](consumer/src/main/java/com/apenlor/pactflow/consumer/service/StudentService.java),
[`TeacherService.java`](consumer/src/main/java/com/apenlor/pactflow/consumer/service/TeacherService.java),
[`StudentProviderTest.java`](consumer/src/test/java/com/apenlor/pactflow/consumer/contracts/StudentProviderTest.java),
and [`TeacherProviderTest.java`](consumer/src/test/java/com/apenlor/pactflow/consumer/contracts/TeacherProviderTest.java).

**Test files naming convention:** We name test classes after their respective covered providers to enhance organization
and clarity. Although this approach might initially seem counterintuitive, it proves beneficial in complex,
realistic scenarios.

[`StudentProviderTest.java`](consumer/src/test/java/com/apenlor/pactflow/consumer/contracts/StudentProviderTest.java)
contains the contract testing implementation for our `student-provider`. On the other
hand, [`TeacherProviderTest.java`](consumer/src/test/java/com/apenlor/pactflow/consumer/contracts/TeacherProviderTest.java)
contains the contract testing implementation for our `teacher-provider`.

As a consumer, our tests will focus on the highest layer available for our contract testing coverage, which is why
[`StudentService.java`](consumer/src/main/java/com/apenlor/pactflow/consumer/service/StudentService.java)
and [`TeacherService.java`](consumer/src/main/java/com/apenlor/pactflow/consumer/service/TeacherService.java) are
utilized in the tests.

It's worth mentioning that, at this level, our approach to the consumer side of contract testing does not differ.
However, we are implementing **Consumer Driven** contract testing with the student-provider and
**Bi-Directional** contract testing with the teacher-provider.

Once the tests run successfully, the contract will be generated in `target/pacts`. This file will be uploaded as a
contract to PactFlow by our automated workflows.

### Student Provider

This provider is driven by the consumer's expectations. Its tests will verify that it fulfills the pact contract.

```
student-provider/
└── src/
    ├── main/
    │   └── java/
    │       └── com/apenlor/pactflow/student/
    │           └── controller/
    │                   └── StudentController.java
    └── test/
        └── java/
            └── com/apenlor/pactflow/student/
                └── contracts/
                    └── StudentProviderVerificationTest.java
```

**For this provider, we are implementing [Consumer Driven](https://docs.pact.io/) contract testing.**

The core classes
are [`StudentController.java`](student-provider/src/main/java/com/apenlor/pactflow/student/controller/StudentController.java)
and [`StudentProviderVerificationTest.java`](student-provider/src/test/java/com/apenlor/pactflow/student/contracts/StudentProviderVerificationTest.java).

[`StudentProviderVerificationTest.java`](student-provider/src/test/java/com/apenlor/pactflow/student/contracts/StudentProviderVerificationTest.java)
contains the contract testing implementation, including `@State` definitions and context actions for verifying contracts
on the provider side. As the provider, our tests will focus on the outermost layer available, which is why
[`StudentController.java`](student-provider/src/main/java/com/apenlor/pactflow/student/controller/StudentController.java)
is utilized in the tests.

**Test files naming convention:** In this case, the naming refers to the verification process for contract
testing, with`student-provider` doing the verification. We do not reference the consumers because one single
verification will provide coverage for all of them.

By running the `build_and_verify` job in our workflow, we verify the compatibility of the provider with all related consumer contracts and publish the results to PactFlow. When running locally, no verification is pushed to the broker; you will receive feedback in your local build, but pushing data requires using the automated workflows.

### Teacher Provider

This provider generates its own contract (an OpenAPI specification) and does not depend on consumer expectations for verification.

```
teacher-provider/
└── src/
    └── main/
        └── java/
            └── com/apenlor/pactflow/teacher/
                └── controller/
                        └── TeacherController.java
```

**For this provider, we are
implementing [Bi-Directional](https://docs.pactflow.io/docs/bi-directional-contract-testing/)
contract testing.**

The core class is [`TeacherController.java`](teacher-provider/src/main/java/com/apenlor/pactflow/teacher/controller/TeacherController.java), and the OpenAPI generation is configured in the [`pom.xml`](teacher-provider/pom.xml).

Here, we use the [SpringDoc OpenAPI Maven plugin](https://springdoc.org/#maven-plugin) to generate the OpenAPI
specification during the build. We chose this method as it is one of the safest and most recommended approaches.
The automated workflow then takes this specification and publishes it to PactFlow as the provider's contract.

### GitHub Actions Workflows
The CI/CD automation is defined in YAML files located in the `.github/` directory. This includes:
-   **`.github/workflows/`**: Contains the main, executable workflows for each component (`consumer.yml`, `verification.yml`, `student-provider.yml`, etc.).
-   **`.github/actions/`**: Contains reusable **Composite Actions** that encapsulate common logic like publishing contracts or checking for deployment readiness.

## PactFlow Setup

Ensure you have permissions to log in to your PactFlow instance.

### Webhook Details

This project uses a webhook to automatically trigger the verification workflow in GitHub Actions whenever a published contract requires verification. **This is specific to Consumer Driven integrations**.

Check how the webhook is configured by navigating to `Settings → Webhooks` in your PactFlow instance.

We are following the **GitHub API recommendations** for triggering workflows. The webhook sends a `POST` request to the following endpoint:
```
https://api.github.com/repos/YOUR_USERNAME/YOUR_REPO/dispatches
```

The request must include:
-   An `Authorization: Bearer ${user.GITHUBTOKEN}` header for authentication. Being GITHUBTOKEN a secret in PactFlow.
-   A JSON body specifying the `event_type` and the `client_payload` with all the necessary variables.
```
{
    "event_type": "contract-requires-verification",
    "client_payload": {
        "provider_name": "${pactbroker.providerName}",
        "provider_version": "${pactbroker.providerVersionNumber}",
        "provider_branch": "${pactbroker.providerVersionBranch}",
        "consumer_branch": "${pactbroker.consumerVersionBranch}"
        }
}
```
<div align="center">
<img src="images/verification-pipeline-webhook.png" alt="Verification pipeline webhook" width="1159"/>
</div>

### Creating the GitHub Token for the Webhook

The webhook needs a **Personal Access Token (PAT)** to get permission to trigger a workflow. It is highly recommended to create this token from a dedicated "bot" or "service" user account.

1.  In GitHub, go to your user `Settings` > `Developer settings` > `Personal access tokens` > **`Fine-grained tokens`**.
2.  Click **`Generate new token`**.
3.  **Token name**: Give it a descriptive name like `PactFlow Webhook Trigger`.
4.  **Expiration**: Set an appropriate expiration date (e.g., 90 days).
5.  **Repository access**: Select **`Only select repositories`** and choose this workshop repository.
6.  **Permissions**: Under `Repository permissions`, find the **`Contents`** permission and select **`Read and Write`** from the dropdown. This is the only permission needed.
7.  Click **`Generate token`** and copy it immediately. You will store this token in PactFlow's secrets, and use it through `${user.SECRETNAME}`.

## How to use the Workflows

This project uses separate, manually-triggered workflows for each component. You can run them directly from the "Actions" tab in your GitHub repository.

## Workflows in detail

This section explains how the automation is configured. The core principle is a set of main workflows that use smaller, reusable **Composite Actions** to perform common tasks.

<div align="center">
<img src="images/pipelines-diagram.png" alt="Verification pipeline webhook" width="951"/>
</div>

### Core Workflows

Located in `.github/workflows/`, these are the workflows you can execute manually:

-   [`consumer.yml`](./.github/workflows/consumer.yml): Builds the consumer, publishes its Pact contract, and handles its deployment lifecycle.
-   [`student-provider.yml`](./.github/workflows/student-provider.yml): Builds the student-provider, verifies contracts against it, and handles its deployment.
-   [`teacher-provider.yml`](./.github/workflows/teacher-provider.yml): Builds the teacher-provider, generates and publishes its OpenAPI specification, and handles its deployment.

### Reusable Composite Actions

Located in subdirectories within `.github/actions/`, these actions encapsulate repeated logic:

-   **`pact-cli-setup`**: Installs and caches the Pact CLI tools. It is run once per job.
-   **`publish-contract`**: Publishes a consumer's Pact contract to PactFlow.
-   **`publish-openapi`**: Publishes a provider's OpenAPI specification to PactFlow.
-   **`can-i-deploy`**: Checks with PactFlow if a component version is safe to deploy to an environment.
-   **`deploy`**: A "dummy" action that simulates a deployment step.
-   **`tagging`**: Tags a deployed version in PactFlow, marking it as present in a specific environment.

### Verification Workflow

The [`verification.yml`](./.github/workflows/verification.yml) workflow is special. It is **not** intended for manual execution. It is automatically triggered by the PactFlow webhook when a new contract requires verification, ensuring a fast and focused CI loop for Consumer-Driven testing.

### Initial Workflow for Bi-Directional

Good news! Bi-Directional contract testing does not require any specific process to initiate the integration. Whether you run the `teacher-provider` workflow or the `consumer` workflow first does not matter.

In PactFlow, two contracts will be managed for this integration: the one provided by the consumer side (the pact file) and the OpenAPI specification provided by the provider side. The comparison and verification will be conducted by PactFlow between these two components. This means there's no prerequisite for one to exist before the other, and the automated `verification.yml` workflow is not needed for this type of testing.

You can inspect both contracts through PactFlow's `View Contracts` feature, where you'll not only see the consumer contract listing the interactions but also the OpenAPI specification presented with Swagger.

### Initial Workflow for Consumer Driven

If you are starting from scratch (which we recommend), with no existing contracts in PactFlow, the first iteration for a Consumer-Driven integration follows a unique "kick-starting" process. This is mainly due to our use of webhooks for verification. In this section, we will detail this initial workflow.

#### Initial status

Initially, PactFlow is empty, with no contracts related to the `consumer`/`student-provider` pair that you are planning to deploy. This scenario mirrors the situation faced by a team that is starting to utilize this framework for their first deployment.

#### How to start up the automation

To "start the wheel" and achieve full automation, the framework requires an initial sequence of deployments.

##### Summary of steps needed

1.  **Consumer Deployment (Expected to Fail)**

    The first consumer deployment will fail, but that's okay! Its purpose is merely to publish the contract to PactFlow for the first time.
    -   Go to the **Actions** tab in GitHub.
    -   Select the **`[Workflow] - Consumer`** workflow.
    -   Click **`Run workflow`**.
    -   Set the `environment` input to **`test`** and run.
    -   The workflow will fail at the "Can I Deploy?" step, which is the correct behavior.

2.  **Provider Deployment (Will Succeed)**

    This initial provider deployment will succeed. Now that the contract exists, the provider's build can verify against it, publish the successful verification results, and deploy.
    -   Go to the **Actions** tab.
    -   Select the **`[Workflow] - Student provider`** workflow.
    -   Click **`Run workflow`**.
    -   Set the `environment` input to **`test`** and run.

3.  **Real Consumer Deployment (Will Succeed)**

    With a verified provider now available in the environment, we can safely deploy our consumer.
    -   Go back to the **Actions** tab.
    -   Run the **`[Workflow] - Consumer`** again.
    -   Set the `environment` input to **`test`** and run.
    -   This time, `can-i-deploy` will pass, and the workflow will complete successfully.

After this initial "ritual," everything is set up to work automatically and independently for all subsequent deployments.

#### Recommended next steps

From this point, you have the freedom to test various scenarios. However, we recommend a couple of executions to clearly understand how the automation behaves:

1.  **Deploy a new provider version in a different environment**:

    Run the `[Workflow] - Student provider` again, but this time deploy to the `production` environment. Remember that the version of a pacticipant is its Git SHA, so you might want to make a small commit first to generate a new version.
    If you check the MATRIX in PactFlow for the `consumer` and `student-provider` integration, you should see two different provider versions reflected, one in `test` and the other in `production`.

2.  **Deploy a new consumer contract version**:

    The focus here is on the **consumer contract version**, which is different from the consumer's application version (the Git SHA). To create a new contract version, you must change the content of the generated pact file. An effective way to do this is to alter the description of an interaction in a `@Pact` method within `StudentProviderTest.java`:

    ```java
    .uponReceiving("get an existing student with ID 1 - updated")
    ```

    Pushing this change and running the `[Workflow] - Consumer` will publish a new contract version. This will, in turn, trigger the PactFlow webhook, automatically launching the **`[Support] - Verification pipeline`** workflow in GitHub Actions. The objective is to see this automated verification run for all deployed provider versions (in our case, for both `test` and `production`).

## Now, it's yours to play with it...

If you've reached this section, **thank you** and congrats for your patience. This is a pretty long README.

Now, you're all set to create your own branch and start experimenting. This project is intended to be a playground that prepares us to handle real-world contract testing projects effectively.

Enjoy
