package com.example.ui.report;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.model.Worker;
import com.example.data.model.WorkerMusterSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportMusterAdapter extends RecyclerView.Adapter<ReportMusterAdapter.MusterViewHolder> {

    private final Context context;
    private final List<WorkerMusterSummary> summaries = new ArrayList<>();

    public ReportMusterAdapter(Context context) {
        this.context = context;
    }

    public void setSummaries(List<WorkerMusterSummary> list) {
        summaries.clear();
        if (list != null) {
            summaries.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MusterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_row, parent, false);
        return new MusterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusterViewHolder holder, int position) {
        WorkerMusterSummary item = summaries.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return summaries.size();
    }

    static class MusterViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMusterWorkerName;
        private final TextView tvMusterWorkerCode;
        private final TextView tvMusterWorkerTrade;
        private final TextView tvMusterRate;
        private final TextView tvMusterNetPayable;
        private final TextView tvMusterPresentDays;
        private final TextView tvMusterHdDays;
        private final TextView tvMusterAbsentDays;
        private final TextView tvMusterOtHours;
        private final TextView tvMusterGrossWage;
        private final TextView tvMusterAdvanceDeduct;

        public MusterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMusterWorkerName = itemView.findViewById(R.id.tvMusterWorkerName);
            tvMusterWorkerCode = itemView.findViewById(R.id.tvMusterWorkerCode);
            tvMusterWorkerTrade = itemView.findViewById(R.id.tvMusterWorkerTrade);
            tvMusterRate = itemView.findViewById(R.id.tvMusterRate);
            tvMusterNetPayable = itemView.findViewById(R.id.tvMusterNetPayable);
            tvMusterPresentDays = itemView.findViewById(R.id.tvMusterPresentDays);
            tvMusterHdDays = itemView.findViewById(R.id.tvMusterHdDays);
            tvMusterAbsentDays = itemView.findViewById(R.id.tvMusterAbsentDays);
            tvMusterOtHours = itemView.findViewById(R.id.tvMusterOtHours);
            tvMusterGrossWage = itemView.findViewById(R.id.tvMusterGrossWage);
            tvMusterAdvanceDeduct = itemView.findViewById(R.id.tvMusterAdvanceDeduct);
        }

        public void bind(WorkerMusterSummary item) {
            Worker w = item.getWorker();
            tvMusterWorkerName.setText(w.getName());
            tvMusterWorkerCode.setText(w.getWorkerCode());
            tvMusterWorkerTrade.setText(w.getWorkerType());
            tvMusterRate.setText(String.format(Locale.US, "Rate: %,.0f Ks/day | OT: %,.0f Ks/hr", w.getDailyRate(), w.getOtRate()));

            tvMusterPresentDays.setText(String.format(Locale.US, "Present: %dd", item.getPresentDays()));
            tvMusterHdDays.setText(String.format(Locale.US, "HD: %dd", item.getHalfDays()));
            tvMusterAbsentDays.setText(String.format(Locale.US, "Absent: %dd", item.getAbsentDays()));
            tvMusterOtHours.setText(String.format(Locale.US, "OT: %.1fh", item.getTotalOtHours()));

            tvMusterGrossWage.setText(String.format(Locale.US, "Gross: %,.0f Ks", item.getGrossWage()));
            tvMusterAdvanceDeduct.setText(String.format(Locale.US, "Advance: -%,.0f Ks", item.getAdvanceDeductions()));
            tvMusterNetPayable.setText(String.format(Locale.US, "%,.0f Ks", item.getNetPayable()));
        }
    }
}
