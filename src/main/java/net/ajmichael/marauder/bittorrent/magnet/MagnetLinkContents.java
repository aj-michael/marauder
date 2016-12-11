package net.ajmichael.marauder.bittorrent.magnet;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The relevant information from a magnet link.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Magnet_URI_scheme">Wikipedia</a>
 */
@AutoValue
public abstract class MagnetLinkContents {
  private static final String DISPLAY_NAME = "dn";
  private static final String EXACT_TOPIC = "xt";
  private static final String TRACKER_ADDRESSES = "tr";

  public static MagnetLinkContents parse(URI uri) throws URISyntaxException {
    ListMultimap<String, String> contents =
        Multimaps.transformValues(
            Multimaps.index(
                Splitter.on('&').split(uri.getSchemeSpecificPart().substring(1)),
                s -> s.split("=")[0]),
            s -> s.split("=")[1]);
    return new AutoValue_MagnetLinkContents.Builder()
        .setExactTopic(URI.create(contents.get(EXACT_TOPIC).get(0)))
        .setDisplayName(contents.get(DISPLAY_NAME).get(0))
        .setTrackerAddresses(
            contents.get(TRACKER_ADDRESSES).stream().map(URI::create).collect(Collectors.toList()))
        .build();
  }

  public abstract URI exactTopic();

  public abstract String displayName();

  public abstract List<URI> trackerAddresses();

  @AutoValue.Builder
  abstract static class Builder {
    abstract MagnetLinkContents build();

    abstract Builder setExactTopic(URI value);

    abstract Builder setDisplayName(String value);

    abstract Builder setTrackerAddresses(List<URI> value);
  }
}
