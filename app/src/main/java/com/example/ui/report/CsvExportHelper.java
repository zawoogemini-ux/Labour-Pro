package com.example.ui.report;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;

import com.example.data.model.Site;
import com.example.data.model.Worker;
import com.example.data.model.WorkerMusterSummary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvExportHelper {

    public static File exportMusterToCsv(Context context, Site site, String periodLabel, List<WorkerMusterSummary> summaries) throws IOException {
        String siteName = site != null ? site.getSiteName().replaceAll("[^a-zA-Z0-9_-]", "_") : "All_Sites";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "Muster_" + siteName + "_" + timeStamp + ".csv";

        File exportDir = new File(context.getCacheDir(), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            // Header
            writer.append("CONSTRUCTION LABOUR MANAGEMENT PRO - MUSTER ROLL REPORT\n");
            writer.append("Site:,").append(site != null ? site.getSiteName() : "All Construction Sites").append("\n");
            writer.append("Period:,").append(periodLabel).append("\n");
            writer.append("Generated On:,").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date())).append("\n\n");

            // Columns
            writer.append("Worker ID,Worker Name,Trade,Gender,Daily Rate (MMK),OT Rate (MMK/hr),Present Days,Half Days,Absent Days,Total OT (Hrs),Gross Wages (MMK),Advances Deducted (MMK),Net Payable (MMK)\n");

            int totalP = 0;
            int totalHd = 0;
            int totalA = 0;
            double totalOt = 0.0;
            double totalGross = 0.0;
            double totalAdv = 0.0;
            double totalNet = 0.0;

            for (WorkerMusterSummary s : summaries) {
                Worker w = s.getWorker();
                totalP += s.getPresentDays();
                totalHd += s.getHalfDays();
                totalA += s.getAbsentDays();
                totalOt += s.getTotalOtHours();
                totalGross += s.getGrossWage();
                totalAdv += s.getAdvanceDeductions();
                totalNet += s.getNetPayable();

                writer.append(escapeCsv(w.getWorkerCode())).append(",")
                        .append(escapeCsv(w.getName())).append(",")
                        .append(escapeCsv(w.getWorkerType())).append(",")
                        .append(escapeCsv(w.getGender())).append(",")
                        .append(String.format(Locale.US, "%.0f", w.getDailyRate())).append(",")
                        .append(String.format(Locale.US, "%.0f", w.getOtRate())).append(",")
                        .append(String.valueOf(s.getPresentDays())).append(",")
                        .append(String.valueOf(s.getHalfDays())).append(",")
                        .append(String.valueOf(s.getAbsentDays())).append(",")
                        .append(String.format(Locale.US, "%.1f", s.getTotalOtHours())).append(",")
                        .append(String.format(Locale.US, "%.0f", s.getGrossWage())).append(",")
                        .append(String.format(Locale.US, "%.0f", s.getAdvanceDeductions())).append(",")
                        .append(String.format(Locale.US, "%.0f", s.getNetPayable())).append("\n");
            }

            // Total row
            writer.append("TOTALS,,,,,")
                    .append(String.valueOf(totalP)).append(",")
                    .append(String.valueOf(totalHd)).append(",")
                    .append(String.valueOf(totalA)).append(",")
                    .append(String.format(Locale.US, "%.1f", totalOt)).append(",")
                    .append(String.format(Locale.US, "%.0f", totalGross)).append(",")
                    .append(String.format(Locale.US, "%.0f", totalAdv)).append(",")
                    .append(String.format(Locale.US, "%.0f", totalNet)).append("\n");
        }

        return file;
    }

    public static void shareCsvFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Construction Labour Muster Roll Report");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Muster Roll CSV"));
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
