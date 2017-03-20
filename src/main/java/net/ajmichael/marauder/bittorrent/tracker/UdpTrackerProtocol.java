package net.ajmichael.marauder.bittorrent.tracker;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import net.ajmichael.marauder.bittorrent.magnet.MagnetLinkContents;

class UdpTrackerProtocol {
  private static final Logger LOGGER = Logger.getLogger(UdpTrackerProtocol.class.getName());
  private static final long PROTOCOL_ID = 0x41727101980L;
  private static final byte[] PEER_ID = new byte[20];

  static {
    ThreadLocalRandom.current().nextBytes(PEER_ID);
  }

  private final Selector trackerSelector;
  private final DatagramChannel datagramChannel;

  private UdpTrackerProtocol(Selector trackerSelector, DatagramChannel datagramChannel) {
    this.trackerSelector = trackerSelector;
    this.datagramChannel = datagramChannel;
  }

  static UdpTrackerProtocol create(
      Selector trackerSelector, SocketAddress localSocket, SocketAddress trackerSocket)
      throws IOException {
    DatagramChannel datagramChannel =
        DatagramChannel.open().bind(localSocket).connect(trackerSocket);
    datagramChannel.configureBlocking(false);
    return new UdpTrackerProtocol(trackerSelector, datagramChannel);
  }

  private void waitForReadableDatagramChannel() throws IOException {
    while (trackerSelector.select() > 0) {
      for (SelectionKey key : ImmutableSet.copyOf(trackerSelector.selectedKeys())) {
        trackerSelector.selectedKeys().remove(key);
        if (key.isReadable()) {
          return;
        }
      }
    }
    throw new IOException("Did not receive response from tracker before selector closed.");
  }

  Callable<ConnectResponse> connect(int transactionId) {
    return () -> {
      ConnectRequest connectRequest =
          new AutoValue_UdpTrackerProtocol_ConnectRequest(transactionId);
      LOGGER.info("Sending tracker connect request: " + connectRequest);
      datagramChannel.write(connectRequest.getByteBuffer());
      datagramChannel.register(trackerSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      waitForReadableDatagramChannel();
      return ConnectResponse.readFromChannel(datagramChannel);
    };
  }

  Callable<AnnounceResponse> announce(
      int transactionId, long connectionId, MagnetLinkContents magnetLinkContents) {
    return () -> {
      System.out.println(magnetLinkContents.exactTopic().infoHash().length);
      Preconditions.checkState(magnetLinkContents.exactTopic().infoHash().length == 20);
      AnnounceRequest announceRequest =
          new AutoValue_UdpTrackerProtocol_AnnounceRequest(
              connectionId,
              transactionId,
              magnetLinkContents.exactTopic().infoHash(),
              PEER_ID,
              0,
              0,
              0,
              Event.NONE,
              0,
              -1,
              0,
              (short) 0);
      LOGGER.info("Sending tracker announce request: " + announceRequest);
      datagramChannel.write(announceRequest.getByteBuffer());
      datagramChannel.register(trackerSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
      waitForReadableDatagramChannel();
      return AnnounceResponse.readFromChannel(datagramChannel);
    };
  }

  enum Action {
    CONNECT(0),
    ANNOUNCE(1),
    SCRAPE(2),
    ERROR(3);

    private final int value;

    Action(int value) {
      this.value = value;
    }

    private int getValue() {
      return value;
    }
  }

  enum Event {
    NONE(0),
    COMPLETED(1),
    STARTED(2),
    STOPPED(3);

    private final int value;

    Event(int value) {
      this.value = value;
    }

    private int getValue() {
      return value;
    }
  }

  @AutoValue
  abstract static class ConnectRequest {

    abstract int transactionId();

    ByteBuffer getByteBuffer() {
      ByteBuffer connectRequestBytes =
          ByteBuffer.allocate(16)
              .putLong(PROTOCOL_ID)
              .putInt(Action.CONNECT.getValue())
              .putInt(transactionId());
      connectRequestBytes.flip();
      return connectRequestBytes;
    }
  }

  @AutoValue
  abstract static class ConnectResponse {

    static ConnectResponse readFromChannel(DatagramChannel datagramChannel) throws IOException {
      ByteBuffer connectResponseBytes = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
      datagramChannel.receive(connectResponseBytes);
      connectResponseBytes.rewind();
      ConnectResponse connectResponse =
          new AutoValue_UdpTrackerProtocol_ConnectResponse(
              connectResponseBytes.getInt(),
              connectResponseBytes.getInt(),
              connectResponseBytes.getLong());
      LOGGER.info("Received tracker connect response: " + connectResponse);
      return connectResponse;
    }

    abstract int actionValue();

    abstract int transactionId();

    abstract long connectionId();
  }

  @AutoValue
  abstract static class AnnounceRequest {

    abstract long connectionId();

    abstract int transactionId();

    @SuppressWarnings("mutable")
    abstract byte[] infoHash();

    @SuppressWarnings("mutable")
    abstract byte[] peerId();

    abstract long downloaded();

    abstract long left();

    abstract long uploaded();

    abstract Event event();

    abstract int ipAddress();

    abstract int key();

    abstract int numWant();

    abstract short port();

    ByteBuffer getByteBuffer() {
      ByteBuffer announceRequestBytes =
          ByteBuffer.allocate(98)
              .putLong(connectionId())
              .putInt(Action.ANNOUNCE.getValue())
              .putInt(transactionId())
              .put(infoHash())
              .put(peerId())
              .putLong(downloaded())
              .putLong(left())
              .putLong(uploaded())
              .putInt(event().getValue())
              .putInt(ipAddress())
              .putInt(key())
              .putInt(numWant())
              .putShort(port());
      announceRequestBytes.flip();
      return announceRequestBytes;
    }
  }

  @AutoValue
  abstract static class AnnounceResponse {

    static AnnounceResponse readFromChannel(DatagramChannel datagramChannel) throws IOException {
      ByteBuffer announceResponseBytes = ByteBuffer.allocate(20).order(ByteOrder.BIG_ENDIAN);
      datagramChannel.receive(announceResponseBytes);
      announceResponseBytes.rewind();
      int returnedActionValue = announceResponseBytes.getInt();
      Preconditions.checkState(returnedActionValue == Action.ANNOUNCE.getValue());
      AnnounceResponse.Builder builder =
          builder()
              .setTransactionId(announceResponseBytes.getInt())
              .setInterval(announceResponseBytes.getInt())
              .setLeechers(announceResponseBytes.getInt());
      int seeders = announceResponseBytes.getInt();
      builder.setSeeders(seeders);
      ByteBuffer seedersBytes = ByteBuffer.allocate(6 * seeders);
      datagramChannel.receive(seedersBytes);
      seedersBytes.rewind();
      ImmutableList<Peer> peers =
          IntStream.range(0, seeders)
              .mapToObj(
                  x ->
                      new AutoValue_UdpTrackerProtocol_AnnounceResponse_Peer(
                          seedersBytes.getInt(), seedersBytes.getShort()))
              .collect(ImmutableList.toImmutableList());
      AnnounceResponse announceResponse = builder.setPeers(peers).build();
      LOGGER.info("Received tracker connect response: " + announceResponse);
      return announceResponse;
    }

    static Builder builder() {
      return new AutoValue_UdpTrackerProtocol_AnnounceResponse.Builder();
    }

    abstract int transactionId();

    abstract int interval();

    abstract int leechers();

    abstract int seeders();

    abstract ImmutableList<Peer> peers();

    @AutoValue
    abstract static class Peer {
      abstract int ipAddress();

      abstract short port();
    }

    @AutoValue.Builder
    abstract static class Builder {

      abstract AnnounceResponse build();

      abstract Builder setTransactionId(int value);

      abstract Builder setInterval(int value);

      abstract Builder setLeechers(int value);

      abstract Builder setSeeders(int value);

      abstract Builder setPeers(ImmutableList<Peer> value);
    }
  }
}
