import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 生成软件著作权登记用的《源程序文档》：每页50行，页眉标注软件全称与版本号，右上角页码。 */
public class MakeSourceDoc {
    public static void main(String[] args) throws Exception {
        String srcDir = args[0];
        String outFile = args[1];
        String softName = "温馨食谱软件 V1.51";
        int linesPerPage = 50;

        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(srcDir), "*.java")) {
            for (Path p : ds) files.add(p);
        }
        files.sort(Comparator.comparing(p -> p.getFileName().toString()));

        List<String> all = new ArrayList<>();
        for (Path f : files) {
            List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
            if (lines.isEmpty()) continue;
            all.add("// ========== 文件：" + f.getFileName() + " ==========");
            all.addAll(lines);
            all.add("");
        }

        StringBuilder sb = new StringBuilder();
        int page = 1;
        for (int i = 0; i < all.size(); i += linesPerPage) {
            sb.append(softName);
            int pad = 56 - softName.length();
            for (int s = 0; s < pad; s++) sb.append(' ');
            sb.append("第 ").append(page).append(" 页\n");
            sb.append("--------------------------------------------------------------------------------\n");
            int end = Math.min(i + linesPerPage, all.size());
            for (int j = i; j < end; j++) {
                sb.append(String.format("%4d| %s%n", j - i + 1, all.get(j)));
            }
            sb.append("\n");
            page++;
        }

        Files.write(Paths.get(outFile), sb.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("files=" + files.size() + " totalLines=" + all.size() + " pages=" + (page - 1));
    }
}
