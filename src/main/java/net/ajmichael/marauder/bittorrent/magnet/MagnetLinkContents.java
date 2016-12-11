package net.ajmichael.marauder.bittorrent.magnet;

import com.google.auto.value.AutoValue;
import com.google.common.base.Splitter;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.net.URI;
import java.util.List;

@AutoValue
public abstract class MagnetLinkContents {
  private static final String DISPLAY_NAME = "dn";
  private static final String EXACT_TOPIC = "xt";
  private static final String TRACKER_ADDRESSES = "tr";

  public static MagnetLinkContents parse(URI uri) {
    ListMultimap<String, String> contents =
        Multimaps.transformValues(
            Multimaps.index(
                Splitter.on('&').split(uri.getSchemeSpecificPart().substring(1)),
                s -> s.split("=")[0]),
            s -> s.split("=")[1]);
    return new AutoValue_MagnetLinkContents.Builder()
        .setExactTopic(contents.get(EXACT_TOPIC).get(0))
        .setDisplayName(contents.get(DISPLAY_NAME).get(0))
        .setTrackerAddresses(contents.get(TRACKER_ADDRESSES))
        .build();
  }

  public abstract String exactTopic();

  public abstract String displayName();

  public abstract List<String> trackerAddresses();

  @AutoValue.Builder
  abstract static class Builder {
    abstract MagnetLinkContents build();

    abstract Builder setExactTopic(String value);

    abstract Builder setDisplayName(String value);

    abstract Builder setTrackerAddresses(List<String> value);
  }
}
