package org.monke.thatonegmodmsg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Locale;


public final class Thatonegmodmsg extends JavaPlugin implements Listener {
    private HttpClient httpClient; //pray you dont get more than 2 players at once (actually it would just be slow)

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var registrar = event.registrar();

            registrar.register("togm:simulate", new SimulateJoinHandler(this));
            //hacky
            registrar.register(Commands.literal("togm:reload").requires(ctx -> ctx.getSender().isOp())
                .executes(ctx -> {
                reloadConfig();
                ctx.getSource().getSender().sendMessage(String.format("Reloaded config. Default message: \"%s\"",
                        getConfig().getString("default")));
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            }).build());
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendMessageIP(event.getPlayer());
    }

    private void _sendMessage(Player player, String country_code){
        Locale locale = Locale.forLanguageTag(country_code);
        Locale country_locale = Locale.of("", country_code);

        String message = getMessageInLocale(country_code).replace("{player}", player.getName())
                .replace("{country}", country_locale.getDisplayCountry(locale));
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public void sendMessage(Player player, String country_code){
        _sendMessage(player, country_code);
    }

    private String getMessageInLocale(String locale){
        String message = getConfig().getString(locale);
        if(message == null){
            message = getConfig().getString("default");
        }
        return message;
    }

    public void sendMessageIP(Player player){
        HttpRequest req = HttpRequest.newBuilder().GET().uri(
                URI.create("http://ip-api.com/line/" + player.getAddress().getAddress() + "?fields=countryCode")).build();


        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            HttpResponse<String> response = null;
            try {
                response = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            //writing in async is bad if i recall correctly?
            if (response.statusCode() == 200) {
                String country_code = response.body().strip();
                if(country_code.equals("")){
                    //error or localhost connection
                    //just default to us lol (no but really you should fix this)
                    country_code = "us";
                }
                _sendMessage(player, country_code);
            }
        });
    }
}
