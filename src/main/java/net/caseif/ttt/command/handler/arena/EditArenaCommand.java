package net.caseif.ttt.command.handler.arena;

import net.caseif.ttt.TTTCore;
import net.caseif.ttt.command.handler.CommandHandler;
import net.caseif.ttt.util.constant.Color;
import net.caseif.ttt.util.constant.MetadataKey;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EditArenaCommand extends CommandHandler {

    public EditArenaCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public void handle() {
        if (TTTCore.ARENA_EDITORS.containsKey(((Player) sender).getUniqueId())) {
            TTTCore.locale.getLocalizable("error.arena.already-editing").withPrefix(Color.ALERT)
                    .withReplacements(Color.EM + TTTCore.ARENA_EDITORS.get(((Player) sender).getUniqueId())
                            + Color.INFO).sendTo(sender);
        }

        String arenaId = args[1];
        Optional<Arena> arena = TTTCore.mg.getArena(arenaId);
        if (!arena.isPresent()) {
            TTTCore.locale.getLocalizable("error.arena.dne").withPrefix(Color.ALERT).sendTo(sender);
            return;
        }

        if (arena.get().getMetadata().containsKey(MetadataKey.Arena.EDITOR)) {
            TTTCore.locale.getLocalizable("error.arena.has-editor").withPrefix(Color.ALERT)
                    .withReplacements(Color.EM + Bukkit.getPlayer(arena.get()
                            .getMetadata().<UUID>get(MetadataKey.Arena.EDITOR).get()).getName() + Color.INFO)
                    .sendTo(sender);
            return;
        }

        TTTCore.ARENA_EDITORS.put(((Player) sender).getUniqueId(), arena.get().getId());
        arena.get().getMetadata().set(MetadataKey.Arena.EDITOR, ((Player) sender).getUniqueId());
    }

}
