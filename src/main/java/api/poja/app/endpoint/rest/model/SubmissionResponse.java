package api.poja.app.endpoint.rest.model;

import java.time.Instant;

public record SubmissionResponse(String id, String email, String thumbnailKey, Instant createdAt) {}
