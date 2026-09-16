package api.poja.app.endpoint.rest.mapper;

import api.poja.app.endpoint.rest.model.SubmissionResponse;
import api.poja.app.repository.model.Submission;
import org.springframework.stereotype.Component;

@Component
public class SubmissionMapper {

  public SubmissionResponse toResponse(Submission submission) {
    return new SubmissionResponse(
        submission.getId(),
        submission.getEmail(),
        submission.getThumbnailKey(),
        submission.getCreatedAt());
  }
}
