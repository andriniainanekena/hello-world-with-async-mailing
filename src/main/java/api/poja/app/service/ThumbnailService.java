package api.poja.app.service;

import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.file.image.ImageResizer;
import api.poja.app.mail.Email;
import api.poja.app.mail.Mailer;
import api.poja.app.repository.SubmissionRepository;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ThumbnailService {

  private static final String THUMBNAIL_KEY_PREFIX = "submissions/thumbnails/";
  private static final Duration DOWNLOAD_LINK_EXPIRATION = Duration.ofDays(7);

  private final BucketComponent bucketComponent;
  private final ImageResizer imageResizer;
  private final SubmissionRepository submissionRepository;
  private final Mailer mailer;

  @SneakyThrows
  public void process(String submissionId, String rawBucketKey, String email) {
    File rawFile = bucketComponent.download(rawBucketKey);
    File thumbnailFile = imageResizer.resizeTo256(rawFile);

    String thumbnailKey = THUMBNAIL_KEY_PREFIX + submissionId + ".png";
    bucketComponent.upload(thumbnailFile, thumbnailKey);

    var submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new IllegalStateException("Submission not found: " + submissionId));
    submission.setThumbnailKey(thumbnailKey);
    submissionRepository.save(submission);

    var downloadUrl = bucketComponent.presign(thumbnailKey, DOWNLOAD_LINK_EXPIRATION);
    sendThumbnailReadyEmail(email, downloadUrl.toString());
  }

  private void sendThumbnailReadyEmail(String email, String downloadUrl) throws Exception {
    var to = new InternetAddress(email);
    var htmlBody =
        "<p>Your thumbnail is ready. <a href=\"" + downloadUrl + "\">Download it here</a>.</p>";
    mailer.accept(
        new Email(to, List.of(), List.of(), "Your thumbnail is ready", htmlBody, List.of()));
  }
}
