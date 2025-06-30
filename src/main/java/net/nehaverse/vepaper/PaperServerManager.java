package net.nehaverse.vepaper;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaperServerManager {

    private final Logger logger;
    private final Path   baseDir;
    private final int    instances;
    private final int    startingPort;
    private final int    maxMemMb;

    private final AtomicBoolean relay = new AtomicBoolean(true); // default ON
    private final List<PaperInstance> running = new ArrayList<>();

    public PaperServerManager(Logger lg, Path dataDir, Properties cfg) {
        logger       = lg;
        baseDir      = dataDir.resolve("servers");
        instances    = Integer.parseInt(cfg.getProperty("instances"   , "1"));
        startingPort = Integer.parseInt(cfg.getProperty("startingPort", "25563"));
        maxMemMb     = Integer.parseInt(cfg.getProperty("maxMemoryMb" , "4096"));
    }

    public void startAll() {
        for(int i=0;i<instances;i++) {
            int port = startingPort + i;
            PaperInstance p = new PaperInstance(
                    logger, baseDir, port, maxMemMb, relay);
            if (p.start()) running.add(p);
        }
    }
    public void stopAll() { running.forEach(PaperInstance::stop); }

    /* util */
    public int  getRunningCount() { return running.size(); }
    public void setRelay(boolean on) { relay.set(on); }
    public boolean sendCommand(String t,String c){
        try{ int idx=Integer.parseInt(t);
            if(0<=idx&&idx<running.size()) return running.get(idx).send(c);
        }catch(NumberFormatException ignore){}
        return running.stream()
                .filter(p->String.valueOf(p.getPort()).equals(t))
                .findFirst().map(p->p.send(c)).orElse(false);
    }
}