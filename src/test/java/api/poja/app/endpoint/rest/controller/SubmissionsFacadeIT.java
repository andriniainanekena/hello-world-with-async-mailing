package api.poja.app.endpoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import api.poja.app.conf.FacadeIT;
import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.file.hash.FileHash;
import api.poja.app.file.hash.FileHashAlgorithm;
import api.poja.app.repository.SubmissionRepository;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class SubmissionsFacadeIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private SubmissionRepository submissionRepository;

  @MockBean private BucketComponent bucketComponent;
  @MockBean private EventProducer eventProducer;

  @Test
  void post_submission_returns_201_with_null_thumbnail_key_and_is_then_listed() {
    when(bucketComponent.upload(any(File.class), any(String.class)))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "dummy-checksum"));

    var response = postSubmission("test-image.png", "student@hei.school");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).containsKeys("id", "email", "createdAt");
    assertThat(response.getBody().get("thumbnailKey")).isNull();

    String id = (String) response.getBody().get("id");
    assertThat(submissionRepository.findById(id)).isPresent();
    assertThat(submissionRepository.findById(id).get().getThumbnailKey()).isNull();

    var listResponse = restTemplate.getForEntity("/submissions", List.class);
    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).isNotEmpty();
  }

  @Test
  void rejects_non_image_file_with_400() {
    var response = postSubmission("not-an-image.txt", "student@hei.school");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void rejects_malformed_email_with_400() {
    var response = postSubmission("test-image.png", "not-an-email");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  private org.springframework.http.ResponseEntity<Map> postSubmission(
      String classpathResource, String email) {
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ClassPathResource(classpathResource));
    body.add("email", email);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    return restTemplate.postForEntity("/submissions", request, Map.class);
  }
}
