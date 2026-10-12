package com.example.ui.report;

import android.app.Activity;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.data.model.Site;
import com.example.data.model.Worker;
import com.example.data.model.WorkerMusterSummary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrintHelper {

    public static void printMusterSheet(Activity activity, Site site, String periodLabel, List<WorkerMusterSummary> summaries) {
        String html = generatePrintableHtml(site, periodLabel, summaries);

        WebView webView = new WebView(activity);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
                if (printManager != null) {
                    String jobName = "Muster_Sheet_" + (site != null ? site.getSiteName() : "All_Sites");
                    PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
                    PrintAttributes attributes = new PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(new PrintAttributes.Resolution("id", "print", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build();
                    printManager.print(jobName, printAdapter, attributes);
                }
            }
        });

        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    private static String generatePrintableHtml(Site site, String periodLabel, List<WorkerMusterSummary> summaries) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'/>");
        sb.append("<style>");
        sb.append("@page { size: A4 portrait; margin: 12mm; }");
        sb.append("body { font-family: 'Helvetica Neue', Arial, sans-serif; font-size: 11px; color: #1e293b; margin: 0; padding: 0; }");
        sb.append(".header-box { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 8px; margin-bottom: 12px; }");
        sb.append(".title { font-size: 16px; font-weight: bold; text-transform: uppercase; color: #0f172a; }");
        sb.append(".subtitle { font-size: 12px; color: #475569; margin-top: 3px; }");
        sb.append(".meta-table { width: 100%; margin-bottom: 12px; font-size: 11px; }");
        sb.append(".meta-table td { padding: 3px 0; }");
        sb.append(".muster-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 10px; }");
        sb.append(".muster-table th, .muster-table td { border: 1px solid #94a3b8; padding: 5px 6px; text-align: left; }");
        sb.append(".muster-table th { background-color: #f1f5f9; font-weight: bold; color: #0f172a; }");
        sb.append(".text-right { text-align: right; }");
        sb.append(".text-center { text-align: center; }");
        sb.append(".total-row { font-weight: bold; background-color: #f8fafc; }");
        sb.append(".signatures { margin-top: 35px; width: 100%; }");
        sb.append(".signatures td { width: 33%; text-align: center; padding-top: 40px; border-top: 1px dashed #64748b; font-size: 10px; }");
        sb.append("</style></head><body>");

        // Header
        sb.append("<div class='header-box'>");
        sb.append("<div class='title'>Construction Labour Muster Roll & Wage Sheet</div>");
        sb.append("<div class='subtitle'>Official Site Attendance & Advance Ledger Record</div>");
        sb.append("</div>");

        // Meta Info
        sb.append("<table class='meta-table'><tr>");
        sb.append("<td><strong>Site:</strong> ").append(site != null ? site.getSiteName() : "All Active Sites").append("</td>");
        sb.append("<td class='text-right'><strong>Period:</strong> ").append(periodLabel).append("</td>");
        sb.append("</tr><tr>");
        sb.append("<td><strong>Supervisor:</strong> ").append(site != null && site.getSupervisorName() != null ? site.getSupervisorName() : "In-Charge").append("</td>");
        sb.append("<td class='text-right'><strong>Date Generated:</strong> ").append(new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(new Date())).append("</td>");
        sb.append("</tr></table>");

        // Table
        sb.append("<table class='muster-table'>");
        sb.append("<thead><tr>");
        sb.append("<th class='text-center'>#</th>");
        sb.append("<th>ID</th>");
        sb.append("<th>Worker Name</th>");
        sb.append("<th>Trade</th>");
        sb.append("<th class='text-right'>Daily Rate</th>");
        sb.append("<th class='text-center'>P</th>");
        sb.append("<th class='text-center'>HD</th>");
        sb.append("<th class='text-center'>A</th>");
        sb.append("<th class='text-right'>OT (h)</th>");
        sb.append("<th class='text-right'>Gross (Ks)</th>");
        sb.append("<th class='text-right'>Advance (Ks)</th>");
        sb.append("<th class='text-right'>Net Pay (Ks)</th>");
        sb.append("<th class='text-center'>Signature</th>");
        sb.append("</tr></thead><tbody>");

        int index = 1;
        int sumP = 0;
        int sumHd = 0;
        int sumA = 0;
        double sumOt = 0.0;
        double sumGross = 0.0;
        double sumAdv = 0.0;
        double sumNet = 0.0;

        for (WorkerMusterSummary s : summaries) {
            Worker w = s.getWorker();
            sumP += s.getPresentDays();
            sumHd += s.getHalfDays();
            sumA += s.getAbsentDays();
            sumOt += s.getTotalOtHours();
            sumGross += s.getGrossWage();
            sumAdv += s.getAdvanceDeductions();
            sumNet += s.getNetPayable();

            sb.append("<tr>");
            sb.append("<td class='text-center'>").append(index++).append("</td>");
            sb.append("<td>").append(w.getWorkerCode()).append("</td>");
            sb.append("<td><strong>").append(w.getName()).append("</strong></td>");
            sb.append("<td>").append(w.getWorkerType()).append("</td>");
            sb.append("<td class='text-right'>").append(String.format(Locale.US, "%,.0f Ks", w.getDailyRate())).append("</td>");
            sb.append("<td class='text-center'>").append(s.getPresentDays()).append("</td>");
            sb.append("<td class='text-center'>").append(s.getHalfDays()).append("</td>");
            sb.append("<td class='text-center'>").append(s.getAbsentDays()).append("</td>");
            sb.append("<td class='text-right'>").append(String.format(Locale.US, "%.1f", s.getTotalOtHours())).append("</td>");
            sb.append("<td class='text-right'>").append(String.format(Locale.US, "%,.0f Ks", s.getGrossWage())).append("</td>");
            sb.append("<td class='text-right'>-").append(String.format(Locale.US, "%,.0f Ks", s.getAdvanceDeductions())).append("</td>");
            sb.append("<td class='text-right'><strong>").append(String.format(Locale.US, "%,.0f Ks", s.getNetPayable())).append("</strong></td>");
            sb.append("<td style='height:24px;'></td>");
            sb.append("</tr>");
        }

        // Summary Total Row
        sb.append("<tr class='total-row'>");
        sb.append("<td colspan='5' class='text-right'><strong>TOTALS</strong></td>");
        sb.append("<td class='text-center'>").append(sumP).append("</td>");
        sb.append("<td class='text-center'>").append(sumHd).append("</td>");
        sb.append("<td class='text-center'>").append(sumA).append("</td>");
        sb.append("<td class='text-right'>").append(String.format(Locale.US, "%.1f", sumOt)).append("</td>");
        sb.append("<td class='text-right'>").append(String.format(Locale.US, "%,.0f Ks", sumGross)).append("</td>");
        sb.append("<td class='text-right'>-").append(String.format(Locale.US, "%,.0f Ks", sumAdv)).append("</td>");
        sb.append("<td class='text-right'><strong>").append(String.format(Locale.US, "%,.0f Ks", sumNet)).append("</strong></td>");
        sb.append("<td></td>");
        sb.append("</tr>");

        sb.append("</tbody></table>");

        // Signatures Section
        sb.append("<table class='signatures'><tr>");
        sb.append("<td>Prepared By<br/>(Site Supervisor)</td>");
        sb.append("<td>Checked By<br/>(Accountant)</td>");
        sb.append("<td>Approved By<br/>(Project Manager / Contractor)</td>");
        sb.append("</tr></table>");

        sb.append("</body></html>");
        return sb.toString();
    }
}
