package net.ajmichael.marauder.bittorrent.tracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Selector;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.ajmichael.marauder.bittorrent.magnet.MagnetLinkContents;
import net.ajmichael.marauder.bittorrent.tracker.UdpTrackerProtocol.AnnounceResponse;

public class TrackerDriver {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException,
          URISyntaxException {
    MagnetLinkContents magnetLinkContents =
        MagnetLinkContents.parse(
            new URI(
                "magnet:?xt=urn:btih:49636cc8a630f47329074383a7dc11d7c284abe4&dn=Moana+2016+HDRip.x264-AMIABLE&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Fzer0day.ch%3A1337&tr=udp%3A%2F%2Fopen.demonii.com%3A1337&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Fexodus.desync.com%3A6969"));

    URI trackerUri = magnetLinkContents.trackerAddresses().get(0);

    Selector trackerSelector = Selector.open();
    int transactionId = new Random().nextInt();
    //SocketAddress trackerSocket = new InetSocketAddress("tracker.leechers-paradise.org", 6969);
    SocketAddress trackerSocket = new InetSocketAddress(trackerUri.getHost(), trackerUri.getPort());
    SocketAddress localSocket = new InetSocketAddress(9999);

    UdpTrackerProtocol udpTrackerProtocol =
        UdpTrackerProtocol.create(trackerSelector, localSocket, trackerSocket);

    ExecutorService executorService = Executors.newFixedThreadPool(5);
    Future<UdpTrackerProtocol.ConnectResponse> connectResponseFuture =
        executorService.submit(udpTrackerProtocol.connect(transactionId));
    UdpTrackerProtocol.ConnectResponse connectResponse =
        connectResponseFuture.get(10, TimeUnit.SECONDS);

    Future<AnnounceResponse> announceResponseFuture =
        executorService.submit(
            udpTrackerProtocol.announce(
                transactionId, connectResponse.connectionId(), magnetLinkContents));
    UdpTrackerProtocol.AnnounceResponse announceResponse =
        announceResponseFuture.get(10, TimeUnit.SECONDS);

    executorService.shutdown();
    System.out.println(connectResponse);
    System.out.println(announceResponse);
  }
}
