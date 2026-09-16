package api.poja.app.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.poja.app.conf.FacadeIT;
import api.poja.app.endpoint.event.model.ThumbnailProcessingRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import api.poja.app.repository.SubmissionRepository;
import api.poja.app.repository.model.Submission;
import java.io.File;
import java.net.URL;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;

class ThumbnailProcessingRequestedFacadeIT extends FacadeIT {

  @Autowired private ThumbnailProcessingRequestedService thumbnailProcessingRequestedService;
  @Autowired private SubmissionRepository submissionRepository;

  @MockBean private BucketComponent bucketComponent;
  @MockBean private Mailer mailer;

  @Test
  void processes_event_resizes_image_updates_db_and_sends_email() throws Exception {
    String id = UUID.randomUUID().toString();
    submissionRepository.save(
        Submission.builder()
            .id(id)
            .email("student@hei.school")
            .thumbnailKey(null)
            .createdAt(Instant.now())
            .build());

    File sourceImage = new ClassPathResource("test-image.png").getFile();
    when(bucketComponent.download(any())).thenReturn(sourceImage);
    when(bucketComponent.presign(any(), any()))
        .thenReturn(new URL("https://example.com/thumb.png"));

    var event =
        ThumbnailProcessingRequested.builder()
            .submissionId(id)
            .rawBucketKey("submissions/raw/" + id + ".png")
            .email("student@hei.school")
            .build();

    thumbnailProcessingRequestedService.accept(event);

    var updated = submissionRepository.findById(id).orElseThrow();
    assertThat(updated.getThumbnailKey()).isEqualTo("submissions/thumbnails/" + id + ".png");
    verify(mailer).accept(any(Email.class));
  }
}
