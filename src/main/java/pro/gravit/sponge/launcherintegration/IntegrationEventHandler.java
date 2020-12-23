package pro.gravit.sponge.launcherintegration;

import ca.momothereal.mojangson.ex.MojangsonParseException;
import ca.momothereal.mojangson.value.*;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import pro.gravit.launcher.request.Request;
import pro.gravit.launcher.request.WebSocketEvent;
import pro.gravit.launcher.request.websockets.ClientWebSocketService;
import pro.gravit.sponge.launcherintegration.lk.event.UserItemDeliveryEvent;
import pro.gravit.sponge.launcherintegration.lk.request.ChangeOrderStatusRequest;
import pro.gravit.utils.helper.LogHelper;

import java.io.IOException;
import java.util.*;

public class IntegrationEventHandler implements ClientWebSocketService.EventHandler {

    @Override
    public <T extends WebSocketEvent> boolean eventHandle(T event) {
        if(event instanceof UserItemDeliveryEvent)
        {
            UserItemDeliveryEvent deliveryEvent = (UserItemDeliveryEvent) event;
            Player player = Sponge.getServer().getPlayer(deliveryEvent.userUuid).orElse(null);
            if(player == null) {
                LogHelper.info("Delivery order %d paused - player %s not online", deliveryEvent.orderId, deliveryEvent.userUsername);
                ChangeOrderStatusRequest request = new ChangeOrderStatusRequest(deliveryEvent.orderId, ChangeOrderStatusRequest.OrderStatus.DELIVERY);
                try {
                    Request.service.request(request);
                } catch (IOException e) {
                    LogHelper.error(e);
                }
            }
            else {
                processDeliveryItemToPlayer(deliveryEvent.orderId, player, deliveryEvent.data, deliveryEvent.part);
            }
        }
        return false;
    }

    public static int processDeliveryItemToPlayer(long orderId, Player player, UserItemDeliveryEvent.OrderSystemInfo orderSystemInfo, int part) {
        int rejectedPart = deliveryItemToPlayer(player, orderSystemInfo, part);
        ChangeOrderStatusRequest request = new ChangeOrderStatusRequest(orderId, ChangeOrderStatusRequest.OrderStatus.DELIVERY);
        if(rejectedPart == 0) {
            request.status = ChangeOrderStatusRequest.OrderStatus.FINISHED;
        }
        else {
            request.isParted = true;
            request.part = rejectedPart;
        }
        try {
            Request.service.request(request).exceptionally((e) -> {
                LogHelper.error(e);
                return null;
            });
        } catch (IOException e) {
            LogHelper.error(e);
        }
        LogHelper.info("Checked part: %d", rejectedPart);
        return part - rejectedPart;
    }

    public static int deliveryItemToPlayer(Player player, UserItemDeliveryEvent.OrderSystemInfo orderSystemInfo, int part)
    {
        ItemStack stack = createItemStackFromInfo(orderSystemInfo, part);
        InventoryTransactionResult result = player.getInventory().offer(stack);
        if(result.getType() == InventoryTransactionResult.Type.SUCCESS) return stack.getQuantity();
        else {
            if(stack.getQuantity() == part) return part;
            int rejectedPart = 0;
            for(ItemStackSnapshot snapshot : result.getRejectedItems()) {
                rejectedPart += snapshot.getQuantity();
            }
            return part - rejectedPart;
        }
    }

    @SuppressWarnings("unchecked")
    public static ItemStack createItemStackFromInfo(UserItemDeliveryEvent.OrderSystemInfo info, int part)
    {
        GameRegistry registry = Sponge.getGame().getRegistry();
        ItemStack.Builder itemStack = ItemStack.builder();
        itemStack.itemType(registry.getType(ItemType.class, info.itemId).orElse(ItemTypes.STICK));
        if(info.enchants != null) {
            List<Enchantment> enchantments = new ArrayList<>();
            for(UserItemDeliveryEvent.OrderSystemInfo.OrderSystemEnchantInfo enchant : info.enchants) {
                Optional<EnchantmentType> optional = registry.getType(EnchantmentType.class, enchant.name);
                optional.ifPresent(enchantmentType -> enchantments.add(Enchantment.of(enchantmentType, enchant.level)));
            }
            itemStack.add(Keys.ITEM_ENCHANTMENTS, enchantments);
        }
        itemStack.quantity(part);
        ItemStack builded = itemStack.build();
        if(info.itemExtra != null) {
            builded.setRawData(builded.toContainer().set(DataQuery.of("UnsafeDamage"), Short.parseShort(info.itemExtra)));
        }
        if(info.itemNbt != null) {
            MojangsonCompound compound = new MojangsonCompound();
            try {
                compound.read(info.itemNbt);
                DataView unsafeData = builded.toContainer().getView(DataQuery.of("UnsafeData")).orElse(DataContainer.createNew());
                Object compoundObj = mojangsonToNbt(compound);
                if(compoundObj instanceof Map)
                ((Map<String, Object>)compoundObj).forEach((k, v) -> {
                    unsafeData.set(DataQuery.of(k), v);
                });
                //builded.setRawData(builded.toContainer().set(DataQuery.of("UnsafeData"), mojangsonToNbt(compound)));
                builded.setRawData(builded.toContainer().set(DataQuery.of("UnsafeData"), unsafeData));
            } catch (MojangsonParseException e) {
                LogHelper.error(e);
            }
        }
        return builded;
    }

    public static Object mojangsonToNbt(MojangsonValue<?> data) {
        if(data instanceof MojangsonCompound) {
            HashMap<String, Object> map = new HashMap<>();
            ((MojangsonCompound)data).getValue().forEach((key, value) -> {
                map.put(key, mojangsonToNbt(value));
            });
            return map;
        }
        if(data instanceof MojangsonArray) {
            List<Object> list = new LinkedList<>();
            ((MojangsonArray<?>)data).getValue().forEach((v) -> {
                list.add(mojangsonToNbt(v));
            });
            return list;
        }
        return data.getValue();
    }
}
