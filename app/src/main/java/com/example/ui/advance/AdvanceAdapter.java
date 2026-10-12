package com.example.ui.advance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.model.Advance;
import com.example.data.model.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdvanceAdapter extends RecyclerView.Adapter<AdvanceAdapter.AdvanceViewHolder> {

    public interface OnAdvanceActionListener {
        void onDeleteAdvance(Advance advance);
    }

    private final Context context;
    private final List<Advance> advances = new ArrayList<>();
    private final Map<Integer, Worker> workerMap;
    private final OnAdvanceActionListener listener;

    public AdvanceAdapter(Context context, Map<Integer, Worker> workerMap, OnAdvanceActionListener listener) {
        this.context = context;
        this.workerMap = workerMap;
        this.listener = listener;
    }

    public void setAdvances(List<Advance> list) {
        advances.clear();
        if (list != null) {
            advances.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdvanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advance_row, parent, false);
        return new AdvanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdvanceViewHolder holder, int position) {
        Advance adv = advances.get(position);
        holder.bind(adv);
    }

    @Override
    public int getItemCount() {
        return advances.size();
    }

    class AdvanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAdvanceWorkerName;
        private final TextView tvAdvanceDate;
        private final TextView tvAdvanceAmount;
        private final TextView tvAdvanceSupervisor;
        private final TextView tvAdvanceRemark;
        private final ImageButton btnDeleteAdvance;

        public AdvanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdvanceWorkerName = itemView.findViewById(R.id.tvAdvanceWorkerName);
            tvAdvanceDate = itemView.findViewById(R.id.tvAdvanceDate);
            tvAdvanceAmount = itemView.findViewById(R.id.tvAdvanceAmount);
            tvAdvanceSupervisor = itemView.findViewById(R.id.tvAdvanceSupervisor);
            tvAdvanceRemark = itemView.findViewById(R.id.tvAdvanceRemark);
            btnDeleteAdvance = itemView.findViewById(R.id.btnDeleteAdvance);
        }

        public void bind(Advance adv) {
            Worker worker = workerMap.get(adv.getWorkerId());
            if (worker != null) {
                tvAdvanceWorkerName.setText(worker.getName() + " (" + worker.getWorkerCode() + ")");
            } else {
                tvAdvanceWorkerName.setText("Worker #" + adv.getWorkerId());
            }

            tvAdvanceDate.setText(adv.getDate());
            tvAdvanceAmount.setText(String.format(Locale.US, "%,.0f Ks", adv.getAmount()));
            tvAdvanceSupervisor.setText("Given By: " + (adv.getSupervisorName() != null ? adv.getSupervisorName() : "Site Supervisor"));

            String remark = adv.getRemark();
            if (remark != null && !remark.trim().isEmpty()) {
                tvAdvanceRemark.setText("• " + remark.trim());
                tvAdvanceRemark.setVisibility(View.VISIBLE);
            } else {
                tvAdvanceRemark.setVisibility(View.GONE);
            }

            btnDeleteAdvance.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteAdvance(adv);
            });
        }
    }
}
