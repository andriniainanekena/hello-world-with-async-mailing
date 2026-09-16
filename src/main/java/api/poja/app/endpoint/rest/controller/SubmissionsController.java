package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.mapper.SubmissionMapper;
import api.poja.app.endpoint.rest.model.SubmissionResponse;
import api.poja.app.service.SubmissionService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class SubmissionsController {

  private final SubmissionService submissionService;
  private final SubmissionMapper submissionMapper;

  @PostMapping(value = "/submissions", consumes = "multipart/form-data")
  public ResponseEntity<SubmissionResponse> createSubmission(
      @RequestParam("file") MultipartFile file, @RequestParam("email") String email) {
    var submission = submissionService.create(file, email);
    return ResponseEntity.status(HttpStatus.CREATED).body(submissionMapper.toResponse(submission));
  }

  @GetMapping("/submissions")
  public ResponseEntity<List<SubmissionResponse>> listSubmissions() {
    var submissions =
        submissionService.findAll().stream().map(submissionMapper::toResponse).toList();
    return ResponseEntity.ok(submissions);
  }
}
