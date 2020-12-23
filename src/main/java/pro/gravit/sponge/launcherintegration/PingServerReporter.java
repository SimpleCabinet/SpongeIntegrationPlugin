package pro.gravit.sponge.launcherintegration;

import org.spongepowered.api.Sponge;
import pro.gravit.launcher.request.Request;
import pro.gravit.launcher.request.management.PingServerReportRequest;
import pro.gravit.launcher.server.ServerWrapper;
import pro.gravit.utils.helper.LogHelper;

import java.io.IOException;

public class PingServerReporter implements Runnable {
    private final ServerWrapper serverWrapper;

    public PingServerReporter(ServerWrapper serverWrapper) {
        this.serverWrapper = serverWrapper;
    }

    @Override
    public void run() {
        int maxPlayers = Sponge.getServer().getMaxPlayers();
        int onlinePlayers = Sponge.getServer().getOnlinePlayers().size();
        PingServerReportRequest.PingServerReport report = new PingServerReportRequest.PingServerReport(serverWrapper.config.serverName, maxPlayers, onlinePlayers);
        PingServerReportRequest request = new PingServerReportRequest(serverWrapper.config.serverName, report);
        try {
            Request.service.request(request).thenAccept((e) -> {
            }).exceptionally((e) -> {
                return null;
            });
        } catch (IOException ignored) {
            //
        }
    }
}
