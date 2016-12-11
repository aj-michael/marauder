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
    assertThat(contents.exactTopic())
        .isEqualTo("urn:btih:49636cc8a630f47329074383a7dc11d7c284abe4");
    assertThat(contents.displayName()).isEqualTo("Moana+2016+HDRip.x264-AMIABLE");
    assertThat(contents.trackerAddresses())
        .containsExactly(
            "udp://tracker.leechers-paradise.org:6969",
            "udp://zer0day.ch:1337",
            "udp://open.demonii.com:1337",
            "udp://tracker.coppersurfer.tk:6969",
            "udp://exodus.desync.com:6969");
  }

  private static URL getResource(String name) {
    return Resources.getResource(RESOURCE_PREFIX + name);
  }
}
