# Spring Cloud Netflix Feign vnd.error decoder

A custom error decoder for Netflix Feign that will unmarshall [vnd.error](https://github.com/blongden/vnd.error).

[![Build Status](https://travis-ci.org/jmnarloch/feign-vnderror-spring-cloud-starter.svg?branch=master)](https://travis-ci.org/jmnarloch/feign-vnderror-spring-cloud-starter)

## Features

A custom Feign ErrorDecoder capable of handling JSON vnd.error responses. 

## Setup

## Usage

First on server side make sure that your exception handling logic will returns VndErrors. You can accomplish 
that by registering custom `@ExceptionHandler` for your application

```java
@ExceptionHandler
ResponseEntity<VndErrors> userNotFoundException(SomeException e) {

    String logref = UUID.randomUUID();
    
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.parseMediaType("application/vnd.error+json"));
    return new ResponseEntity<>(new VndErrors(logref, e.getMessage()), httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
}
```

Place them into your `@Controller` or `@ControllerAdvice` annotated beans. You can populate the logref with some 
meaningful identifier that afterwards could be useful during log analysis. Generally it could be a good idea to add 
[HandlerInterceptor](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/servlet/HandlerInterceptor.html) 
to populate request scoped identifier on `preHandle()`.
 
The error decoding happens on content type matching so it is crucial to return `application/vnd.error+json` as the content type.

You use your Feign clients in the same manner as in any other cases, the only difference is that whenever a client call 
will end with vnd.error you should expect `VndErrorException` instead of `FeignException`.

Example:

```java
try {

    feignClient.operation();    
} catch (VndErrorException exc) {
    ...
}
```

The `VndErrorException` makes very convenient to passthrough exceptions, if you call remote service from a web 
application you can register `@ExceptionHandler` and return the ordinal vnd.error retrieved from the exception instance. 

## License

Apache 2.0