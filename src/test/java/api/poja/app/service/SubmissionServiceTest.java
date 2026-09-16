package api.poja.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.ThumbnailProcessingRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.file.zip.FileTyper;
import api.poja.app.repository.SubmissionRepository;
import api.poja.app.service.exception.InvalidEmailException;
import api.poja.app.service.exception.UnsupportedImageTypeException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

  @Mock private SubmissionRepository submissionRepository;
  @Mock private BucketComponent bucketComponent;
  @Mock private FileTyper fileTyper;
  @Mock private EventProducer<ThumbnailProcessingRequested> eventProducer;

  private SubmissionService submissionService;

  @BeforeEach
  void setUp() {
    submissionService =
        new SubmissionService(submissionRepository, bucketComponent, fileTyper, eventProducer);
  }

  @Test
  void creates_submission_with_null_thumbnail_key_and_triggers_async_processing() {
    var file = new MockMultipartFile("file", "image.png", "image/png", new byte[] {1, 2, 3});
    when(fileTyper.apply(any())).thenReturn(MediaType.IMAGE_PNG);

    var submission = submissionService.create(file, "someone@example.com");

    assertThat(submission.getId()).isNotBlank();
    assertThat(submission.getEmail()).isEqualTo("someone@example.com");
    assertThat(submission.getThumbnailKey()).isNull();
    assertThat(submission.getCreatedAt()).isNotNull();

    verify(submissionRepository).save(submission);
    verify(bucketComponent).upload(any(), any());

    ArgumentCaptor<List<ThumbnailProcessingRequested>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    assertThat(captor.getValue()).hasSize(1);
    var event = captor.getValue().get(0);
    assertThat(event.getSubmissionId()).isEqualTo(submission.getId());
    assertThat(event.getEmail()).isEqualTo("someone@example.com");
    assertThat(event.getRawBucketKey()).contains(submission.getId());
  }

  @Test
  void rejects_unsupported_file_type_with_400_style_exception() {
    var file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[] {1});
    when(fileTyper.apply(any())).thenReturn(MediaType.APPLICATION_PDF);

    assertThrows(
        UnsupportedImageTypeException.class, () -> submissionService.create(file, "a@b.com"));
  }

  @Test
  void rejects_invalid_email_before_touching_storage() {
    var file = new MockMultipartFile("file", "image.png", "image/png", new byte[] {1, 2, 3});

    assertThrows(InvalidEmailException.class, () -> submissionService.create(file, "not-an-email"));
  }
}
