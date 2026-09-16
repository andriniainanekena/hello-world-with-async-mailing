package api.poja.app.service.event;

import api.poja.app.endpoint.event.model.ThumbnailProcessingRequested;
import api.poja.app.service.ThumbnailService;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ThumbnailProcessingRequestedService implements Consumer<ThumbnailProcessingRequested> {

  private final ThumbnailService thumbnailService;

  @Override
  public void accept(ThumbnailProcessingRequested event) {
    thumbnailService.process(event.getSubmissionId(), event.getRawBucketKey(), event.getEmail());
  }
}
