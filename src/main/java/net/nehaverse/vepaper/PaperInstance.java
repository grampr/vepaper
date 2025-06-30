package net.nehaverse.vepaper;

import org.slf4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaperInstance {

    /* Paper API URLs */
    private static final String DL =
            "https://api.papermc.io/v2/projects/paper/versions/%s/builds/%s/downloads/%s";
    private static final String VERS =
            "https://api.papermc.io/v2/projects/paper";
    private static final String BUILDS =
            "https://api.papermc.io/v2/projects/paper/versions/%s";

    private final Logger logger;
    private final Path   dir;
    private final int    port;
    private final int    maxMem;
    private final AtomicBoolean relay;

    private Process        process;
    private BufferedWriter stdin;

    PaperInstance(Logger l, Path baseDir, int port, int memMb,
                  AtomicBoolean relayFlag) {
        logger = l;
        dir    = baseDir.resolve("paper-" + port);
        this.port   = port;
        this.maxMem = memMb;
        this.relay  = relayFlag;
    }

    /* ─── start / stop ─── */
    boolean start() {
        try {
            Files.createDirectories(dir);

            Path jar = dir.resolve("paper.jar");
            if (Files.notExists(jar)) downloadLatestPaper(jar);

            writeBasicFiles();             // ← paper-global.yml には一切触れない

            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-Xmx" + maxMem + "M",
                    "-jar", jar.getFileName().toString(), "--nogui");
            pb.directory(dir.toFile());
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(
                    dir.resolve("latest.log").toFile()));

            process = pb.start();
            stdin   = new BufferedWriter(new OutputStreamWriter(
                    process.getOutputStream(), StandardCharsets.UTF_8));

            startRelayThread();
            logger.info("Paper started on port {}", port);
            return true;
        } catch (Exception e) {
            logger.error("Failed to start Paper on port {}", port, e);
            return false;
        }
    }

    void stop() {
        if (process == null || !process.isAlive()) return;
        try { if (stdin != null) stdin.close(); } catch (IOException ignore) {}
        process.destroy();
        try { process.waitFor(); } catch (InterruptedException ignore) {}
    }

    /* ─── console ─── */
    boolean send(String cmd) {
        if (stdin == null) return false;
        try {
            stdin.write(cmd);
            stdin.newLine();
            stdin.flush();
            return true;
        } catch (IOException e) {
            logger.error("cmd fail {}", port, e); return false;
        }
    }
    int getPort() { return port; }

    /* ─── helpers ─── */
    private void startRelayThread() {
        new Thread(() -> {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (relay.get()) logger.info("[P{}] {}", port, line);
                }
            } catch (IOException ignore) {}
        }, "PaperRelay-" + port).start();
    }

    private void writeBasicFiles() throws IOException {
        /* server.properties */
        Path sp = dir.resolve("server.properties");
        if (Files.notExists(sp)) {
            Files.writeString(sp,
                    "online-mode=false\n" +
                            "server-port=" + port + "\n" +
                            "motd=Paper " + port + "\n");
        }

        /* eula.txt → true */
        Files.writeString(dir.resolve("eula.txt"), "eula=true\n",
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /* ─── Paper jar download ─── */
    private void downloadLatestPaper(Path target) {
        logger.info("Downloading Paper …");
        try {
            String ver   = latestEntry(VERS);
            String build = latestEntry(String.format(BUILDS, ver));
            String file  = "paper-" + ver + "-" + build + ".jar";
            URL url      = new URL(String.format(DL, ver, build, file));

            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(30000);

            try (InputStream in = c.getInputStream();
                 OutputStream out = Files.newOutputStream(target)) {
                in.transferTo(out);
            }
            logger.info("Downloaded {}", file);
        } catch (Exception e) {
            logger.error("Download failed; place paper.jar manually", e);
        }
    }

    private String latestEntry(String url) throws IOException {
        String json = new String(new URL(url).openStream().readAllBytes(),
                StandardCharsets.UTF_8);
        int s = json.indexOf('[') + 1, e = json.indexOf(']', s);
        String[] arr = json.substring(s, e).replace("\"", "").split(",");
        return arr[arr.length - 1].trim();
    }
}