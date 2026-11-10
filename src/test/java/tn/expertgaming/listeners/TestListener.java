package tn.expertgaming.listeners;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import tn.expertgaming.config.Config;
import tn.expertgaming.driver.DriverFactory;
import tn.expertgaming.utils.ScreenshotUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * - Capture d'ecran PNG + trace TXT pour chaque test echoue (dossiers reports/screenshots et reports/errors)
 * - Rapport HTML reports/execution-report.html (Passed / Failed / Skipped)
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final List<TestRecord> RECORDS = Collections.synchronizedList(new ArrayList<>());
    private static long suiteStart;

    // ------------------------------------------------------------------ suite

    @Override
    public void onStart(ISuite suite) {
        suiteStart = System.currentTimeMillis();
        RECORDS.clear();
        prepareFolders();
    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            writeHtmlReport(suite.getName());
        } catch (IOException e) {
            System.err.println("Impossible d'ecrire le rapport HTML : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ tests

    @Override
    public void onTestSuccess(ITestResult result) {
        RECORDS.add(record(result, "PASSED"));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TestRecord rec = record(result, "SKIPPED");
        Throwable t = result.getThrowable();
        rec.message = t == null || t.getMessage() == null
                ? "Test ignore (test dont il depend en echec, ou precondition non remplie)"
                : firstLine(t.getMessage());
        RECORDS.add(rec);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TestRecord rec = record(result, "FAILED");
        Throwable t = result.getThrowable();
        rec.message = t == null ? "" : firstLine(String.valueOf(t.getMessage()));
        if (rec.message.isBlank() && t != null) {
            rec.message = t.getClass().getSimpleName();
        }
        WebDriver driver = DriverFactory.getDriver();
        String name = rec.tcId + "_" + rec.methodName;
        if (driver != null) {
            rec.screenshot = ScreenshotUtil.capture(driver, name);
        }
        if (rec.screenshot == null) {
            rec.screenshot = ScreenshotUtil.lastCaptured();
        }
        rec.trace = writeTrace(result, rec, driver, t);
        RECORDS.add(rec);
    }

    // ------------------------------------------------------------------ helpers

    private TestRecord record(ITestResult result, String status) {
        TestRecord rec = new TestRecord();
        rec.status = status;
        String description = result.getMethod().getDescription();
        rec.description = description == null ? result.getMethod().getMethodName() : description;
        rec.tcId = rec.description.contains(" ") ? rec.description.substring(0, rec.description.indexOf(' ')) : rec.description;
        rec.className = result.getTestClass().getRealClass().getSimpleName();
        rec.methodName = result.getMethod().getMethodName();
        rec.durationMs = result.getEndMillis() - result.getStartMillis();
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            List<String> parts = new ArrayList<>();
            for (Object p : params) {
                parts.add(String.valueOf(p));
            }
            rec.parameters = String.join(" | ", parts);
        }
        List<String> output = Reporter.getOutput(result);
        rec.logs = output == null ? "" : String.join("\n", output);
        return rec;
    }

    private static String firstLine(String text) {
        if (text == null) {
            return "";
        }
        String line = text.strip().split("\\R")[0];
        return line.length() > 400 ? line.substring(0, 400) + "..." : line;
    }

    private void prepareFolders() {
        try {
            Path root = Paths.get(Config.reportsDir());
            for (String sub : new String[]{"screenshots", "errors"}) {
                Path dir = root.resolve(sub);
                Files.createDirectories(dir);
                try (Stream<Path> files = Files.list(dir)) {
                    files.filter(Files::isRegularFile).forEach(f -> f.toFile().delete());
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible de preparer le dossier des rapports : " + e.getMessage());
        }
    }

    private String writeTrace(ITestResult result, TestRecord rec, WebDriver driver, Throwable t) {
        try {
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = ScreenshotUtil.sanitize(rec.tcId + "_" + rec.methodName) + "_" + stamp + ".txt";
            Path dir = Paths.get(Config.reportsDir(), "errors");
            Files.createDirectories(dir);

            StringBuilder sb = new StringBuilder();
            sb.append("TEST        : ").append(rec.description).append('\n');
            sb.append("CLASSE      : ").append(result.getTestClass().getName()).append('\n');
            sb.append("METHODE     : ").append(rec.methodName).append('\n');
            sb.append("PARAMETRES  : ").append(rec.parameters.isEmpty() ? "-" : rec.parameters).append('\n');
            sb.append("DATE        : ").append(LocalDateTime.now()).append('\n');
            sb.append("NAVIGATEUR  : ").append(Config.get("browser", "chrome")).append('\n');
            if (driver != null) {
                try {
                    sb.append("URL         : ").append(driver.getCurrentUrl()).append('\n');
                    sb.append("TITRE PAGE  : ").append(driver.getTitle()).append('\n');
                } catch (RuntimeException e) {
                    sb.append("URL         : (navigateur indisponible)\n");
                }
            }
            sb.append("CAPTURE     : ").append(rec.screenshot == null ? "-" : rec.screenshot).append('\n');
            sb.append("\n----- EXCEPTION -----\n");
            if (t != null) {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                sb.append(sw);
            }
            if (!rec.logs.isBlank()) {
                sb.append("\n----- MESSAGES DU TEST -----\n").append(rec.logs).append('\n');
            }
            if (driver != null) {
                try {
                    sb.append("\n----- CONSOLE NAVIGATEUR (derniers messages) -----\n");
                    List<LogEntry> entries = driver.manage().logs().get(LogType.BROWSER).getAll();
                    int from = Math.max(0, entries.size() - 40);
                    for (LogEntry e : entries.subList(from, entries.size())) {
                        sb.append(e.getLevel()).append(" - ").append(e.getMessage()).append('\n');
                    }
                } catch (RuntimeException e) {
                    sb.append("(journal de console non disponible sur ce navigateur)\n");
                }
            }
            Files.writeString(dir.resolve(fileName), sb.toString(), StandardCharsets.UTF_8);
            return "errors/" + fileName;
        } catch (IOException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------ rapport HTML

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void writeHtmlReport(String suiteName) throws IOException {
        List<TestRecord> list = new ArrayList<>(RECORDS);
        list.sort(Comparator.comparing((TestRecord r) -> r.tcId).thenComparing(r -> r.parameters));

        long passed = list.stream().filter(r -> r.status.equals("PASSED")).count();
        long failed = list.stream().filter(r -> r.status.equals("FAILED")).count();
        long skipped = list.stream().filter(r -> r.status.equals("SKIPPED")).count();
        long total = list.size();
        long durationSec = (System.currentTimeMillis() - suiteStart) / 1000;
        String rate = total == 0 ? "0" : String.format("%.1f", passed * 100.0 / total);

        StringBuilder h = new StringBuilder();
        h.append("<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"UTF-8\">")
                .append("<title>Rapport d'execution - Expert Gaming</title><style>")
                .append("body{font-family:Segoe UI,Arial,sans-serif;margin:0;background:#f4f6fa;color:#1f2a44}")
                .append("header{background:#1f2a44;color:#fff;padding:24px 32px}header h1{margin:0 0 6px;font-size:24px}")
                .append("header p{margin:0;color:#c9cfdc;font-size:14px}main{padding:24px 32px}")
                .append(".cards{display:flex;gap:16px;flex-wrap:wrap;margin-bottom:24px}")
                .append(".card{background:#fff;border-radius:8px;padding:16px 24px;min-width:130px;box-shadow:0 1px 3px rgba(0,0,0,.1)}")
                .append(".card b{display:block;font-size:30px}.card span{font-size:13px;color:#5a6275}")
                .append(".p b{color:#1a8f4c}.f b{color:#d33a2c}.s b{color:#c98a00}")
                .append(".filters{margin:0 0 12px}.filters button{border:1px solid #c9cfdc;background:#fff;padding:6px 14px;border-radius:16px;cursor:pointer;margin-right:6px}")
                .append(".filters button.on{background:#1f2a44;color:#fff}")
                .append("table{width:100%;border-collapse:collapse;background:#fff;font-size:13px}")
                .append("th{background:#1f2a44;color:#fff;text-align:left;padding:8px}td{padding:8px;border-bottom:1px solid #e3e7ef;vertical-align:top}")
                .append(".tag{padding:2px 10px;border-radius:10px;color:#fff;font-weight:600;font-size:12px}")
                .append(".PASSED{background:#1a8f4c}.FAILED{background:#d33a2c}.SKIPPED{background:#c98a00}")
                .append("img{max-width:140px;border:1px solid #c9cfdc;border-radius:4px}.msg{color:#8a1f15;font-family:Consolas,monospace;font-size:12px}")
                .append("</style></head><body>");

        h.append("<header><h1>Rapport d'execution des tests automatises</h1><p>")
                .append(esc(suiteName)).append(" &middot; ").append(esc(Config.baseUrl()))
                .append(" &middot; navigateur : ").append(esc(Config.get("browser", "chrome")))
                .append(Config.getBool("headless", false) ? " (headless)" : "")
                .append(" &middot; ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append(" &middot; duree : ").append(durationSec).append(" s</p></header><main>");

        h.append("<div class=\"cards\">")
                .append("<div class=\"card\"><b>").append(total).append("</b><span>Executions</span></div>")
                .append("<div class=\"card p\"><b>").append(passed).append("</b><span>Passed</span></div>")
                .append("<div class=\"card f\"><b>").append(failed).append("</b><span>Failed</span></div>")
                .append("<div class=\"card s\"><b>").append(skipped).append("</b><span>Skipped</span></div>")
                .append("<div class=\"card\"><b>").append(rate).append(" %</b><span>Taux de reussite</span></div></div>");

        h.append("<div class=\"filters\"><button class=\"on\" onclick=\"f('ALL',this)\">Tous</button>")
                .append("<button onclick=\"f('PASSED',this)\">Passed</button>")
                .append("<button onclick=\"f('FAILED',this)\">Failed</button>")
                .append("<button onclick=\"f('SKIPPED',this)\">Skipped</button></div>");

        h.append("<table id=\"t\"><thead><tr><th>ID</th><th>Scenario</th><th>Donnees</th><th>Statut</th><th>Duree</th>")
                .append("<th>Message d'erreur</th><th>Capture</th><th>Trace</th></tr></thead><tbody>");
        for (TestRecord r : list) {
            h.append("<tr data-s=\"").append(r.status).append("\"><td><b>").append(esc(r.tcId)).append("</b></td>")
                    .append("<td>").append(esc(r.description.replaceFirst("^\\S+\\s*-?\\s*", ""))).append("<br><small>")
                    .append(esc(r.className)).append('.').append(esc(r.methodName)).append("</small></td>")
                    .append("<td>").append(esc(r.parameters)).append("</td>")
                    .append("<td><span class=\"tag ").append(r.status).append("\">").append(r.status).append("</span></td>")
                    .append("<td>").append(String.format("%.1f s", r.durationMs / 1000.0)).append("</td>")
                    .append("<td class=\"msg\">").append(esc(r.message)).append("</td><td>");
            if (r.screenshot != null) {
                h.append("<a href=\"").append(esc(r.screenshot)).append("\" target=\"_blank\"><img src=\"")
                        .append(esc(r.screenshot)).append("\" alt=\"capture\"></a>");
            }
            h.append("</td><td>");
            if (r.trace != null) {
                h.append("<a href=\"").append(esc(r.trace)).append("\" target=\"_blank\">trace.txt</a>");
            }
            h.append("</td></tr>");
        }
        h.append("</tbody></table></main><script>")
                .append("function f(s,b){document.querySelectorAll('.filters button').forEach(x=>x.classList.remove('on'));b.classList.add('on');")
                .append("document.querySelectorAll('#t tbody tr').forEach(r=>{r.style.display=(s==='ALL'||r.dataset.s===s)?'':'none'})}")
                .append("</script></body></html>");

        Path out = Paths.get(Config.reportsDir(), "execution-report.html");
        Files.createDirectories(out.getParent());
        Files.writeString(out, h.toString(), StandardCharsets.UTF_8);
        System.out.println("\n>>> Rapport HTML : " + out.toAbsolutePath()
                + "  (Passed=" + passed + ", Failed=" + failed + ", Skipped=" + skipped + ")\n");
    }
}
