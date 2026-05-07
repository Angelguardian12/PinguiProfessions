package bely.pinguiprofessions.managers;

import bely.pinguiprofessions.PinguiProfessions;

import java.util.ArrayList;
import java.util.List;

public class ReportManager {

    private final PinguiProfessions plugin;
    private final List<Report> activeReports = new ArrayList<>();

    public ReportManager(PinguiProfessions plugin) {
        this.plugin = plugin;
    }

    public void addReport(String reporter, String target, String reason) {
        activeReports.add(new Report(reporter, target, reason, System.currentTimeMillis()));
    }

    public List<Report> getActiveReports() {
        return activeReports;
    }

    public void removeReport(Report report) {
        activeReports.remove(report);
    }

    public static class Report {
        public final String reporter;
        public final String target;
        public final String reason;
        public final long timestamp;

        public Report(String reporter, String target, String reason, long timestamp) {
            this.reporter = reporter;
            this.target = target;
            this.reason = reason;
            this.timestamp = timestamp;
        }
    }
}
