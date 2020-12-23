package pro.gravit.sponge.launcherintegration;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import pro.gravit.launcher.request.Request;
import pro.gravit.launcher.request.websockets.ClientWebSocketService;
import pro.gravit.launcher.server.ServerWrapper;
import pro.gravit.sponge.launcherintegration.command.CartAllCommand;
import pro.gravit.sponge.launcherintegration.command.CartGetCommand;
import pro.gravit.sponge.launcherintegration.command.CartItemInfoCommand;
import pro.gravit.sponge.launcherintegration.command.TestCommand;
import pro.gravit.sponge.launcherintegration.lk.event.UserItemDeliveryEvent;
import pro.gravit.sponge.launcherintegration.lk.event.request.ChangeOrderStatusRequestEvent;
import pro.gravit.sponge.launcherintegration.lk.event.request.FetchOrdersRequestEvent;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "launcherintegration",
        name = "GravitLauncherIntegration",
        description = "GravitLauncher Integration",
        authors = {
                "Gravita"
        }
)
public class Launcherintegration {
    public static HashSet<Player> listens = new HashSet<>();
    public static boolean registerHandler = false;

    @Inject
    private Logger logger;

    public ServerWrapper serverWrapper;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("GravitLauncher Integration Plugin setup");
        serverWrapper = ServerWrapper.wrapper;
        logger.info("ServerWrapper connected to {}", serverWrapper.config.address);
        CommandSpec execute = CommandSpec.builder().description(Text.of("Execute some command on LaunchServer"))
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("cmd")))
                .permission("gravitlauncher.admin.ttest")
                .executor(new TestCommand()).build();
        CommandSpec cartGet = CommandSpec.builder()
                .permission("gravitlauncher.cart.get")
                .arguments(GenericArguments.integer(Text.of("orderId")))
                .executor(new CartGetCommand())
                .build();
        CommandSpec cartAll = CommandSpec.builder()
                .permission("gravitlauncher.cart.all")
                .executor(new CartAllCommand())
                .build();
        CommandSpec cartItemInfo = CommandSpec.builder()
                .permission("gravitlauncher.cart.iteminfo")
                .executor(new CartItemInfoCommand())
                .build();
        CommandSpec cartBase = CommandSpec.builder()
                .permission("gravitlauncher.cart.base")
                .child(cartGet, "get", "g")
                .child(cartAll, "all", "a")
                .child(cartItemInfo, "iteminfo", "ii")
                .build();
        Sponge.getScheduler().createTaskBuilder().interval(20, TimeUnit.SECONDS).execute(new PingServerReporter(serverWrapper)).submit(this);
        ClientWebSocketService.results.register("lkUserOrderDelivery", UserItemDeliveryEvent.class);
        ClientWebSocketService.results.register("lkChangeOrderStatus", ChangeOrderStatusRequestEvent.class);
        ClientWebSocketService.results.register("lkFetchOrders", FetchOrdersRequestEvent.class);
        Request.service.registerEventHandler(new IntegrationEventHandler());
        //Sponge.getCommandManager().register(this, execute, "ttest");
        Sponge.getCommandManager().register(this, cartBase, "cart");
    }
    @Listener
    public void exitPlayer(ClientConnectionEvent.Disconnect event)
    {
        Player player = event.getTargetEntity();
        listens.remove(player);
    }
}
