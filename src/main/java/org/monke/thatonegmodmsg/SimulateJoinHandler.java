package org.monke.thatonegmodmsg;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SimulateJoinHandler implements BasicCommand {
    private Thatonegmodmsg plugin;

    public SimulateJoinHandler(Thatonegmodmsg plugin){
        this.plugin = plugin;
        super();
    }

    @Override
    public void execute(CommandSourceStack ctx, String[] args) {
        Entity executor = ctx.getExecutor();
        if(!(executor instanceof Player)){
            ctx.getSender().sendMessage("You can't run this command as a console");
            return;
        }

        if(args.length >= 1){
            String country_code = args[0];
            this.plugin.sendMessage((Player) executor, country_code);
        }
        else{
            this.plugin.sendMessageIP((Player) executor);
        }
    }
}
