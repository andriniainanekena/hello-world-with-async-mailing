package api.poja.app.service;

import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.ThumbnailProcessingRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.file.zip.FileTyper;
import api.poja.app.repository.SubmissionRepository;
import api.poja.app.repository.model.Submission;
import api.poja.app.service.exception.InvalidEmailException;
import api.poja.app.service.exception.UnsupportedImageTypeException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class SubmissionService {

  private static final Set<MediaType> SUPPORTED_TYPES =
      Set.of(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG);
  private static final String RAW_KEY_PREFIX = "submissions/raw/";

  private final SubmissionRepository submissionRepository;
  private final BucketComponent bucketComponent;
  private final FileTyper fileTyper;
  private final EventProducer<ThumbnailProcessingRequested> eventProducer;

  public Submission create(MultipartFile file, String email) {
    validateEmail(email);

    File tempFile = toTempFile(file);
    MediaType detectedType = fileTyper.apply(tempFile);
    validateImageType(detectedType);

    String id = UUID.randomUUID().toString();
    String rawBucketKey = RAW_KEY_PREFIX + id + extensionFor(detectedType);
    bucketComponent.upload(tempFile, rawBucketKey);

    Submission submission =
        Submission.builder()
            .id(id)
            .email(email)
            .thumbnailKey(null)
            .createdAt(Instant.now())
            .build();
    submissionRepository.save(submission);

    eventProducer.accept(
        List.of(
            ThumbnailProcessingRequested.builder()
                .submissionId(id)
                .rawBucketKey(rawBucketKey)
                .email(email)
                .build()));

    return submission;
  }

  public List<Submission> findAll() {
    return submissionRepository.findAll();
  }

  private void validateEmail(String email) {
    try {
      new InternetAddress(email, true);
    } catch (AddressException e) {
      throw new InvalidEmailException(email);
    }
  }

  private void validateImageType(MediaType type) {
    if (!SUPPORTED_TYPES.contains(type)) {
      throw new UnsupportedImageTypeException(type.toString());
    }
  }

  private String extensionFor(MediaType type) {
    return MediaType.IMAGE_PNG.equals(type) ? ".png" : ".jpg";
  }

  @SneakyThrows
  private File toTempFile(MultipartFile file) {
    File tempFile = File.createTempFile("submission-", extensionFromFilename(file));
    try (var inputStream = file.getInputStream()) {
      Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    return tempFile;
  }

  private String extensionFromFilename(MultipartFile file) {
    String original = file.getOriginalFilename();
    if (original == null || !original.contains(".")) {
      return ".tmp";
    }
    return original.substring(original.lastIndexOf('.'));
  }
}
