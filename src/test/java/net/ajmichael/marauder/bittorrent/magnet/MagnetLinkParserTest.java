package net.ajmichael.marauder.bittorrent.magnet;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Resources;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MagnetLinkParserTest {
    @Test
    public void testParseMagnetLink() throws Exception {
        URI magnetLink =
            new URI(
                Resources.toString(
                    Resources.getResource("good-magnet-link.txt"),
                    StandardCharsets.UTF_8));
        MagnetLinkContents contents = MagnetLinkParser.parse(magnetLink);
        assertThat(contents).isNotNull();
    }
}
