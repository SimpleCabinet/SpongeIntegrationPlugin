package pro.gravit.sponge.launcherintegration.command;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.BiConsumer;

public class CartItemInfoCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = (Player) src;
        ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        if(item == null) {
            return CommandResult.empty();
        }
        BiConsumer<? super DataQuery, ? super Object> consumer = (key, value) -> {
            if(value instanceof List) {
                player.sendMessage(Text.of("P: ", key.toString(), " Class ", value.getClass().getName(), " List: "));
                for(Object o : (List<?>)value) {
                    player.sendMessage(Text.of("E: ", key.toString(), " Class ", o.getClass().getName(), " V: ", o.toString()));
                }
                return;
            }
            player.sendMessage(Text.of("P: ", key.toString(), " Class ", value.getClass().getName(), " Value ", String.valueOf(value)));
        };
        item.toContainer().getValues(true).forEach(consumer);
        return CommandResult.success();
    }
}
