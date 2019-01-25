# Campsite Booking Application

Little web application for booking dates on the campsite at Pacific Ocean.

## Technologies
 - Java 8
 - Spring boot as application server;
 - Redis for cache availability bookings;
 - Mysql to store the reservations
 - Docker and Docker compose as container application;
 - swagger as REST api documentation and testing;
 
## Requirements 
 - maven
 - java 8
 - docker 
 - docker-compose

## Before Usage

 1) In the root of the project please run `mvn clean install` 
 2) In the root of the project please run `docker-compose up`

## Running

1) Hit the browser `http://localhost:8080/swagger-ui.html`
2) The endpoints are:
  - Create reservation: `(POST) /v1/reservation`;
  - Delete reservation: `(DELETE) /v1/reservation/{transactionid}`;
  - Modify reservation: `(PUT) /v1/reservation/{transactionid}`;
  - List Availability Dates: `(GET) /v1/reservation/list-availability`;

## What is not implemented at this project ?

- Small validations like:
  - Validate name, email and date params regarding required and type validation.
  - CheckIn must be greater than CheckOut;
  - Validation of email syntax;

## Cache

In order to handle large volume of requests for getting the avaliability of the campsite, the reservation dates are stored in redis.

The cache has default expiration date as 1 day, in order to discard old reservation dates.

## Treat Concurrent Requests

A integration test as written to validate concurrent request of booking operation.

By the Resource Lock Singleton class I can lock the write operations on the database and the cache.

## Tests

All required business validations are completed coverage by integration tests and junit tests.



   
 