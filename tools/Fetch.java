import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Minimal resumable-ish HTTPS downloader (Java 24, uses JVM trust store,
 * bypasses Windows schannel which is blocked in this sandbox).
 * Usage: java Fetch <url> <output-file>
 */
public class Fetch {
    static int attempts = 0;

    public static void main(String[] args) throws Exception {
        String url = args[0];
        String out = args[1];
        File f = new File(out);
        f.getParentFile().mkdirs();

        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

        int maxAttempts = 4;
        for (int i = 1; i <= maxAttempts; i++) {
            attempts = i;
            try {
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "RecipeApp-Bootstrap/1.0")
                    .timeout(Duration.ofMinutes(15))
                    .build();
                HttpResponse<InputStream> resp =
                    client.send(req, HttpResponse.BodyHandlers.ofInputStream());
                if (resp.statusCode() / 100 != 2) {
                    throw new RuntimeException("HTTP " + resp.statusCode());
                }
                long total = resp.headers().firstValueAsLong("Content-Length").orElse(-1);
                try (InputStream in = resp.body();
                     OutputStream outStream = new FileOutputStream(f)) {
                    byte[] buf = new byte[1 << 16];
                    long done = 0;
                    int n;
                    long lastLog = 0;
                    while ((n = in.read(buf)) != -1) {
                        outStream.write(buf, 0, n);
                        done += n;
                        if (done - lastLog >= (10L << 20)) {
                            lastLog = done;
                            String size = total > 0 ? (done / (1024 * 1024)) + "/" + (total / (1024 * 1024)) + "MB"
                                : (done / (1024 * 1024)) + "MB";
                            System.out.println("  [" + out + "] " + size);
                        }
                    }
                }
                System.out.println("OK " + f.length() + " bytes -> " + out);
                return;
            } catch (Exception e) {
                System.out.println("  attempt " + i + " failed: " + e);
                if (i == maxAttempts) {
                    System.err.println("FAILED after " + maxAttempts + " attempts: " + url);
                    System.exit(1);
                }
                Thread.sleep(2000L * i);
            }
        }
    }
}
