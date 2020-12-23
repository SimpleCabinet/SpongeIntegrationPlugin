package pro.gravit.sponge.launcherintegration.command;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import pro.gravit.sponge.launcherintegration.IntegrationEventHandler;
import pro.gravit.sponge.launcherintegration.lk.event.request.FetchOrdersRequestEvent;
import pro.gravit.sponge.launcherintegration.lk.request.ChangeOrderStatusRequest;
import pro.gravit.sponge.launcherintegration.lk.request.FetchOrdersRequest;

public class CartAllCommand implements CommandExecutor {
    @Inject
    private Logger logger;
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        FetchOrdersRequest request = new FetchOrdersRequest();
        request.filterByType = ChangeOrderStatusRequest.OrderStatus.DELIVERY;
        request.userUuid = ((Player)src).getUniqueId();
        request.fetchSystemInfo = true;
        request.deliveryUser = true;
        try {
            FetchOrdersRequestEvent result = request.request();
            for(FetchOrdersRequestEvent.PublicOrderInfo i : result.list) {
                if(i.systemInfo == null) {
                    continue;
                }
                if(i.cantDelivery) {
                    continue;
                }
                if(i.status == ChangeOrderStatusRequest.OrderStatus.DELIVERY) {
                    int delivered = IntegrationEventHandler.processDeliveryItemToPlayer(i.orderId, (Player)src, i.systemInfo, i.part);
                    src.sendMessage(Text.of("Вам выдан предмет по заказу номер ", String.valueOf(i.orderId), " в колличестве ", String.valueOf(delivered), " штук. Осталось ", i.part - delivered));
                }
            }
        } catch (Exception e) {
            logger.error("FetchOrdersRequest", e);
            return CommandResult.success();
        }
        return CommandResult.success();
    }
}
