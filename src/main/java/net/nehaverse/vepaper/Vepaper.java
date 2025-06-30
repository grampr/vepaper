package net.nehaverse.vepaper;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Plugin(id="vepaper", name="Vepaper", version="1.12.4", authors={"nehaverse"})
public final class Vepaper {

    private static final String CONFIG_FILE = "config.properties";

    @Inject private Logger      logger;
    @Inject private ProxyServer proxy;

    private Path               dataDir;
    private Properties         config;
    private PaperServerManager manager;

    /* ───────── Velocity life-cycle ───────── */
    @Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
        dataDir = Paths.get("plugins").resolve("vepaper");
        try { Files.createDirectories(dataDir); }
        catch (IOException ex) { throw new RuntimeException(ex); }

        loadOrCreateConfig();

        manager = new PaperServerManager(logger, dataDir, config);
        manager.startAll();
        logger.info("Vepaper initialised. {} Paper instance(s) booted.",
                manager.getRunningCount());

        proxy.getCommandManager().register("paper",    new PaperCmd());
        proxy.getCommandManager().register("paperlog", new PaperLogCmd());
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        if (manager != null) manager.stopAll();
        logger.info("Vepaper shutdown complete.");
    }

    /* ───────── config ───────── */
    private void loadOrCreateConfig() {
        config = new Properties();
        Path cfg = dataDir.resolve(CONFIG_FILE);

        if (Files.notExists(cfg)) {
            config.setProperty("instances"   , "1");
            config.setProperty("startingPort", "25563");
            config.setProperty("maxMemoryMb" , "4096");
            try (OutputStream out = Files.newOutputStream(cfg,
                    StandardOpenOption.CREATE_NEW)) {
                config.store(out, "Vepaper configuration");
            } catch (IOException ex) { throw new RuntimeException(ex); }
        }
        try (InputStream in = Files.newInputStream(cfg)) { config.load(in); }
        catch (IOException ex) { throw new RuntimeException(ex); }
    }

    /* ───────── /paper <target> <cmd…> ───────── */
    private final class PaperCmd implements SimpleCommand {
        @Override public void execute(Invocation in) {
            CommandSource src = in.source();
            String[] args = in.arguments();
            if (args.length < 2) {
                src.sendMessage(Component.text("/paper <index|port> <command …>"));
                return;
            }
            String target=args[0];
            String cmd = Arrays.stream(args).skip(1)
                    .collect(Collectors.joining(" "));
            if (!manager.sendCommand(target, cmd))
                src.sendMessage(Component.text("Target not found or not running"));
        }
    }

    /* ───────── /paperlog on|off ───────── */
    private final class PaperLogCmd implements SimpleCommand {
        @Override public void execute(Invocation in) {
            if (in.arguments().length!=1) {
                in.source().sendMessage(Component.text("/paperlog <on|off>")); return;
            }
            boolean on = in.arguments()[0].equalsIgnoreCase("on");
            manager.setRelay(on);
            in.source().sendMessage(Component.text("Paper log relay: "+(on?"ON":"OFF")));
        }
    }
}