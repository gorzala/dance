# Dancer
This is the backend (for frontend) for dancier.

## Working locally

We are working locally with a docker-compose setup, that launches every needed service.

Before you run it the first time, you have to set this up....

### Setting everything up

You have to have maven, docker and docker-compose being installed.

Then in the project root folder create the following directories with the proper rights.

````bash
# for the pg-admin volume
mkdir -p volumes/pg-admin-data

# for the kafka volume

mkdir -p volumes/kafka
````

You can now access the database GUI with your browser:

[PG-Admin](http://localhost:5050)

|User| Pass   |
|----|--------|
|admin@dancier.net| secret |

Here you can configure the connection to the postgres instance:

|Hostname|database|user|pass|
|--------|--------|----|----|
|dancer-db|dancer|dancer|dancer|


### Building and running the dancer

#### Without test and update the running docker environment
(assuming the docker-compose setup is up and running)
````bash
./mvnw clean install -DskipTests; docker-compose up --build
````
#### Building with tests
````bash
./mvnw clean install
````
#### running the dancer not inside docker-compose
(assuming the docker-compose setup is up and running)


````shell
# stopping dancer in docker-compose
docker-compose stop dancer;
# running the boot app with overwriting the needed host
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/dancer
````

#### checking test-coverage
Show test coverage in target/site/jacoco/index.html: 
`.target/site/jacoco/index.html

### Accessing the API-Definition
[OpenApi](https://editor.swagger.io/?url=https%3A%2F%2Fraw.githubusercontent.com%2Fdancier%2Fdancer%2Fmaster%2Fopenapi.yml)

### Local Mailing
When working locally the mailing-system of the backend is configured to _not_ send the mails, but to dump them only to the log.
This is achieved by using spring profiles. The profile here is 'dev'.
So make sure to not change the profile unintentionally.
## About the staging environment
As long as we are not live, this is the only other environment next to working locally.
The corresponding Profile is 'staging'

### Mailing in the Staging Environment
Mail will be send via an SMTP-Server. But the target addresses of all mails are rewritten to always go to a configurable mail-address.
