package net.ajmichael.marauder.bittorrent.magnet;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MagnetLinkContentsTest {
  private static final String RESOURCE_PREFIX = "net/ajmichael/marauder/bittorrent/magnet/";

  @Test
  public void testParseMagnetLink() throws Exception {
    URI magnetLink =
        new URI(Resources.toString(getResource("good-magnet-link.txt"), StandardCharsets.UTF_8));
    MagnetLinkContents contents = MagnetLinkContents.parse(magnetLink);
<<<<<<< Updated upstream
    assertThat(contents.exactTopic()).isEqualTo(new AutoValue_MagnetLinkContents_ExactTopic(null));
=======
    assertThat(contents.exactTopic())
        .isEqualTo(
            new AutoValue_MagnetLinkContents_ExactTopic(null));
>>>>>>> Stashed changes
    assertThat(contents.displayName()).isEqualTo("Moana+2016+HDRip.x264-AMIABLE");
    assertThat(contents.trackerAddresses())
        .containsExactly(
            URI.create("udp://tracker.leechers-paradise.org:6969"),
            URI.create("udp://zer0day.ch:1337"),
            URI.create("udp://open.demonii.com:1337"),
            URI.create("udp://tracker.coppersurfer.tk:6969"),
            URI.create("udp://exodus.desync.com:6969"));
  }

  private static URL getResource(String name) {
    return Resources.getResource(RESOURCE_PREFIX + name);
  }
}
