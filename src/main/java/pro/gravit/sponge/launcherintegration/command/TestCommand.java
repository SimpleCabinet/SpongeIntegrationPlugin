package pro.gravit.sponge.launcherintegration.command;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import pro.gravit.utils.helper.LogHelper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TestCommand implements CommandExecutor {
    @Inject
    public Logger logger;
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("Item Lore"));
        lore.add(Text.of("Item Lore 2"));
        List<Enchantment> enchantments = new ArrayList<>();
        enchantments.add(Enchantment.builder()
                .level(2)
                .type(EnchantmentTypes.KNOCKBACK)
                .build());
        enchantments.add(Enchantment.builder()
                .level(3)
                .type(EnchantmentTypes.LOOTING)
                .build());
        ItemStack itemStack = ItemStack.builder()
                .itemType(ItemTypes.DIAMOND_SWORD)
                .add(Keys.DISPLAY_NAME, Text.of("Sword Display Name"))
                .add(Keys.ITEM_LORE, lore)
                .add(Keys.ITEM_DURABILITY, 10)
                .add(Keys.ITEM_ENCHANTMENTS, enchantments)
                .build();
        DataContainer c = itemStack.toContainer();
        c.set(DataQuery.of("a"), 2);
        if(src instanceof Player) {
            ((Player) src).getInventory().poll().ifPresent(stack2 -> stack2.toContainer().getValues(true).forEach((path, value) -> {
                LogHelper.info("key %s class %s",path.toString(), value.getClass().getName());
                if(value instanceof List) {
                    for(Object o : (List)value) {
                        LogHelper.subInfo("O: %s C: %s", o.toString(), o.getClass().getName());
                    }
                }
                else {
                    LogHelper.subInfo("V: %s", value.toString());
                }
            }));
            ((Player) src).getInventory().set(itemStack);
        }
        return CommandResult.success();
    }
}
