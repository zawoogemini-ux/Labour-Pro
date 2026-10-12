package com.example.ui.workermaster;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.model.Site;
import com.example.data.model.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

    public interface OnWorkerActionListener {
        void onEditWorker(Worker worker);
        void onDeleteWorker(Worker worker);
    }

    private final Context context;
    private final List<Worker> workerList = new ArrayList<>();
    private final List<Worker> allWorkers = new ArrayList<>();
    private final Map<Integer, String> siteNameMap;
    private final OnWorkerActionListener listener;

    public WorkerAdapter(Context context, Map<Integer, String> siteNameMap, OnWorkerActionListener listener) {
        this.context = context;
        this.siteNameMap = siteNameMap;
        this.listener = listener;
    }

    public void setWorkers(List<Worker> list) {
        allWorkers.clear();
        if (list != null) {
            allWorkers.addAll(list);
        }
        applyFilter("", "All", -1);
    }

    public void applyFilter(String query, String tradeFilter) {
        applyFilter(query, tradeFilter, -1);
    }

    public void applyFilter(String query, String tradeFilter, int siteId) {
        workerList.clear();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.US);
        boolean filterAllTrade = tradeFilter == null || "All".equalsIgnoreCase(tradeFilter);
        boolean filterAllSite = siteId <= 0;

        for (Worker w : allWorkers) {
            boolean matchesSite = filterAllSite || w.getAssignedSiteId() == siteId;
            boolean matchesTrade = filterAllTrade || (w.getWorkerType() != null && w.getWorkerType().equalsIgnoreCase(tradeFilter));
            boolean matchesQuery = q.isEmpty()
                    || (w.getName() != null && w.getName().toLowerCase(Locale.US).contains(q))
                    || (w.getWorkerCode() != null && w.getWorkerCode().toLowerCase(Locale.US).contains(q))
                    || (w.getWorkerType() != null && w.getWorkerType().toLowerCase(Locale.US).contains(q));

            if (matchesSite && matchesTrade && matchesQuery) {
                workerList.add(w);
            }
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return workerList.size();
    }

    public int getFilteredMaleCount() {
        int count = 0;
        for (Worker w : workerList) {
            if ("Male".equalsIgnoreCase(w.getGender())) count++;
        }
        return count;
    }

    public int getFilteredFemaleCount() {
        int count = 0;
        for (Worker w : workerList) {
            if ("Female".equalsIgnoreCase(w.getGender())) count++;
        }
        return count;
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_worker_row, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        Worker worker = workerList.get(position);
        holder.bind(worker);
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    class WorkerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWorkerInitials;
        private final TextView tvItemWorkerName;
        private final TextView tvItemWorkerCode;
        private final TextView tvItemWorkerTrade;
        private final TextView tvItemWorkerGender;
        private final TextView tvItemAssignedSite;
        private final TextView tvItemDailyRate;
        private final TextView tvItemOtRate;
        private final ImageButton btnCallWorker;
        private final ImageButton btnEditWorker;
        private final ImageButton btnDeleteWorker;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkerInitials = itemView.findViewById(R.id.tvWorkerInitials);
            tvItemWorkerName = itemView.findViewById(R.id.tvItemWorkerName);
            tvItemWorkerCode = itemView.findViewById(R.id.tvItemWorkerCode);
            tvItemWorkerTrade = itemView.findViewById(R.id.tvItemWorkerTrade);
            tvItemWorkerGender = itemView.findViewById(R.id.tvItemWorkerGender);
            tvItemAssignedSite = itemView.findViewById(R.id.tvItemAssignedSite);
            tvItemDailyRate = itemView.findViewById(R.id.tvItemDailyRate);
            tvItemOtRate = itemView.findViewById(R.id.tvItemOtRate);
            btnCallWorker = itemView.findViewById(R.id.btnCallWorker);
            btnEditWorker = itemView.findViewById(R.id.btnEditWorker);
            btnDeleteWorker = itemView.findViewById(R.id.btnDeleteWorker);
        }

        public void bind(Worker worker) {
            tvItemWorkerName.setText(worker.getName());
            tvItemWorkerCode.setText(worker.getWorkerCode());
            tvItemWorkerTrade.setText(worker.getWorkerType());

            if ("Female".equalsIgnoreCase(worker.getGender())) {
                tvItemWorkerGender.setText("မ (Female)");
                tvItemWorkerGender.setTextColor(0xFFBE185D);
            } else {
                tvItemWorkerGender.setText("ကျား (Male)");
                tvItemWorkerGender.setTextColor(0xFF1D4ED8);
            }

            String initials = getInitials(worker.getName());
            tvWorkerInitials.setText(initials);

            String siteName = siteNameMap.get(worker.getAssignedSiteId());
            if (siteName != null && !siteName.isEmpty()) {
                tvItemAssignedSite.setText("• " + siteName);
                tvItemAssignedSite.setVisibility(View.VISIBLE);
            } else {
                tvItemAssignedSite.setVisibility(View.GONE);
            }

            tvItemDailyRate.setText(String.format(Locale.US, "Daily: %,.0f Ks", worker.getDailyRate()));
            tvItemOtRate.setText(String.format(Locale.US, "OT: %,.0f Ks/hr", worker.getOtRate()));

            if (worker.getPhoneNumber() != null && !worker.getPhoneNumber().trim().isEmpty()) {
                btnCallWorker.setVisibility(View.VISIBLE);
                btnCallWorker.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + worker.getPhoneNumber()));
                    context.startActivity(intent);
                });
            } else {
                btnCallWorker.setVisibility(View.GONE);
            }

            btnEditWorker.setOnClickListener(v -> {
                if (listener != null) listener.onEditWorker(worker);
            });

            btnDeleteWorker.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteWorker(worker);
            });
        }

        private String getInitials(String name) {
            if (name == null || name.trim().isEmpty()) return "W";
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.US);
            } else {
                return ("" + parts[0].charAt(0)).toUpperCase(Locale.US);
            }
        }
    }
}
