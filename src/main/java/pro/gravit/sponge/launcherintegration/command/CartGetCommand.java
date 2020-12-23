package pro.gravit.sponge.launcherintegration.command;

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
import pro.gravit.utils.helper.LogHelper;

import javax.inject.Inject;

public class CartGetCommand implements CommandExecutor {
    @Inject
    private Logger logger;
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        long orderId = (Integer)args.getOne("orderId").get();
        FetchOrdersRequest request = new FetchOrdersRequest();
        request.orderId = orderId;
        request.fetchSystemInfo = true;
        request.deliveryUser = true;
        try {
            FetchOrdersRequestEvent result = request.request();
            for(FetchOrdersRequestEvent.PublicOrderInfo i : result.list) {
                if(i.cantDelivery) {
                    src.sendMessage(Text.of("Этот заказ не может быть выдан на этом сервере"));
                    continue;
                }
                if(i.systemInfo != null && i.status == ChangeOrderStatusRequest.OrderStatus.DELIVERY) {
                    int delivered = IntegrationEventHandler.processDeliveryItemToPlayer(orderId, (Player)src, i.systemInfo, i.part);
                    src.sendMessage(Text.of("Вам выдан предмет по заказу номер ", String.valueOf(i.orderId), " в колличестве ", String.valueOf(delivered), " штук. Осталось ", i.part - delivered));
                }
                else {
                    src.sendMessage(Text.of("Заказ номер ", String.valueOf(i.orderId), " уже завершен или не является предметом"));
                }
            }
        } catch (Exception e) {
            logger.error("FetchOrdersRequest", e);
            return CommandResult.success();
        }
        return CommandResult.success();
    }
}
