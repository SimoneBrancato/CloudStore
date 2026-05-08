package com.cloudstore.server.model.domain;

/**
 * Domain record representing the outcome of an asynchronous order submission.
 * Replaces the fragile Map&lt;String, Object&gt; with a typed contract.
 *
 * @param status The status of the order submission (e.g., "accepted").
 * @param message A descriptive message indicating the result of the submission.
 **/
public record OrderSubmissionResult(String status, String message) {}