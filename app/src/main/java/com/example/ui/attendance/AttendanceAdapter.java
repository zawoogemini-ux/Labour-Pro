package com.example.ui.attendance;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.model.Attendance;
import com.example.data.model.Worker;
import com.example.data.model.WorkerAttendanceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    public interface OnAttendanceChangeListener {
        void onAttendanceChanged();
    }

    private final Context context;
    private final List<WorkerAttendanceItem> items = new ArrayList<>();
    private final OnAttendanceChangeListener listener;

    private final int colorPresent;
    private final int colorHalfDay;
    private final int colorAbsent;
    private final int colorTextMain;
    private final int colorTransparent;
    private final int colorWhite;

    public AttendanceAdapter(Context context, OnAttendanceChangeListener listener) {
        this.context = context;
        this.listener = listener;
        this.colorPresent = ContextCompat.getColor(context, R.color.status_present);
        this.colorHalfDay = ContextCompat.getColor(context, R.color.status_halfday);
        this.colorAbsent = ContextCompat.getColor(context, R.color.status_absent);
        this.colorTextMain = ContextCompat.getColor(context, R.color.text_main);
        this.colorTransparent = Color.TRANSPARENT;
        this.colorWhite = ContextCompat.getColor(context, R.color.white);
    }

    public void setItems(List<WorkerAttendanceItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<WorkerAttendanceItem> getItems() {
        return items;
    }

    public void markAll(String status) {
        for (WorkerAttendanceItem item : items) {
            Attendance att = item.getAttendance();
            if (att != null) {
                att.setMorningStatus(status);
                att.setEveningStatus(status);
                item.recalculate();
            }
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onAttendanceChanged();
        }
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_row, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        WorkerAttendanceItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class AttendanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWorkerName;
        private final TextView tvWorkerCode;
        private final TextView tvWorkerTrade;
        private final TextView tvWorkerRates;
        private final TextView tvLiveEarned;
        private final TextView tvLiveEarnedSub;

        // Morning buttons
        private final TextView btnMorningP;
        private final TextView btnMorningHd;
        private final TextView btnMorningA;

        // Evening buttons
        private final TextView btnEveningP;
        private final TextView btnEveningHd;
        private final TextView btnEveningA;

        // OT buttons
        private final TextView btnOtMinus;
        private final TextView tvOtValue;
        private final TextView btnOtPlus;

        // Advance & Net note
        private final View layoutAdvanceNote;
        private final TextView tvAdvanceDeductionInfo;
        private final TextView tvNetPayoutInfo;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvWorkerCode = itemView.findViewById(R.id.tvWorkerCode);
            tvWorkerTrade = itemView.findViewById(R.id.tvWorkerTrade);
            tvWorkerRates = itemView.findViewById(R.id.tvWorkerRates);
            tvLiveEarned = itemView.findViewById(R.id.tvLiveEarned);
            tvLiveEarnedSub = itemView.findViewById(R.id.tvLiveEarnedSub);

            btnMorningP = itemView.findViewById(R.id.btnMorningP);
            btnMorningHd = itemView.findViewById(R.id.btnMorningHd);
            btnMorningA = itemView.findViewById(R.id.btnMorningA);

            btnEveningP = itemView.findViewById(R.id.btnEveningP);
            btnEveningHd = itemView.findViewById(R.id.btnEveningHd);
            btnEveningA = itemView.findViewById(R.id.btnEveningA);

            btnOtMinus = itemView.findViewById(R.id.btnOtMinus);
            tvOtValue = itemView.findViewById(R.id.tvOtValue);
            btnOtPlus = itemView.findViewById(R.id.btnOtPlus);

            layoutAdvanceNote = itemView.findViewById(R.id.layoutAdvanceNote);
            tvAdvanceDeductionInfo = itemView.findViewById(R.id.tvAdvanceDeductionInfo);
            tvNetPayoutInfo = itemView.findViewById(R.id.tvNetPayoutInfo);
        }

        public void bind(WorkerAttendanceItem item) {
            Worker worker = item.getWorker();
            Attendance att = item.getAttendance();

            tvWorkerName.setText(worker.getName());
            tvWorkerCode.setText(worker.getWorkerCode());
            tvWorkerTrade.setText(worker.getWorkerType());
            tvWorkerRates.setText(String.format(Locale.US, "Rate: %,.0f Ks/day | OT: %,.0f Ks/h", worker.getDailyRate(), worker.getOtRate()));

            updateShiftButtons(att.getMorningStatus(), btnMorningP, btnMorningHd, btnMorningA);
            updateShiftButtons(att.getEveningStatus(), btnEveningP, btnEveningHd, btnEveningA);
            updateOtDisplay(att.getOtHours());
            updateLiveWageDisplay(item);

            // Morning click listeners
            btnMorningP.setOnClickListener(v -> {
                att.setMorningStatus("P");
                item.recalculate();
                updateShiftButtons("P", btnMorningP, btnMorningHd, btnMorningA);
                updateLiveWageDisplay(item);
                if (listener != null) listener.onAttendanceChanged();
            });

            btnMorningHd.setOnClickListener(v -> {
                att.setMorningStatus("HD");
                item.recalculate();
                updateShiftButtons("HD", btnMorningP, btnMorningHd, btnMorningA);
                updateLiveWageDisplay(item);
                if (listener != null) listener.onAttendanceChanged();
            });

            btnMorningA.setOnClickListener(v -> {
                att.setMorningStatus("A");
                item.recalculate();
                updateShiftButtons("A", btnMorningP, btnMorningHd, btnMorningA);
                updateLiveWageDisplay(item);
                if (listener != null) listener.onAttendanceChanged();
            });

            // Evening click listeners
            btnEveningP.setOnClickListener(v -> {
                att.setEveningStatus("P");
                item.recalculate();
                updateShiftButtons("P", btnEveningP, btnEveningHd, btnEveningA);
                updateLiveWageDisplay(item);
                if (listener != null) listener.onAttendanceChanged();
            });

            btnEveningHd.setOnClickListener(v -> {
                att.setEveningStatus("HD");
                item.recalculate();
                updateShiftButtons("HD", btnEveningP, btnEveningHd, btnEveningA);
                updateLiveWageDisplay(item);
                if (listener != null) listener.onAttendanceChanged();
            });

            btnEveningA.setOnClickListener(v -> {
                att.setEveningStatus("A");
                item.recalculate();
                updateShiftButtons("A", btnEveningP, btnEveningHd, btnEveningA);
                updateLiveWageDisplay(item);
                if (listener != null) listener.onAttendanceChanged();
            });

            // OT Steppers
            btnOtMinus.setOnClickListener(v -> {
                double current = att.getOtHours();
                if (current >= 0.5) {
                    att.setOtHours(current - 0.5);
                    item.recalculate();
                    updateOtDisplay(att.getOtHours());
                    updateLiveWageDisplay(item);
                    if (listener != null) listener.onAttendanceChanged();
                }
            });

            btnOtPlus.setOnClickListener(v -> {
                double current = att.getOtHours();
                if (current < 12.0) {
                    att.setOtHours(current + 0.5);
                    item.recalculate();
                    updateOtDisplay(att.getOtHours());
                    updateLiveWageDisplay(item);
                    if (listener != null) listener.onAttendanceChanged();
                }
            });
        }

        private void updateShiftButtons(String status, TextView btnP, TextView btnHd, TextView btnA) {
            btnP.setBackgroundColor(colorTransparent);
            btnP.setTextColor(colorTextMain);
            btnHd.setBackgroundColor(colorTransparent);
            btnHd.setTextColor(colorTextMain);
            btnA.setBackgroundColor(colorTransparent);
            btnA.setTextColor(colorTextMain);

            if ("P".equalsIgnoreCase(status)) {
                btnP.setBackgroundColor(colorPresent);
                btnP.setTextColor(colorWhite);
            } else if ("HD".equalsIgnoreCase(status)) {
                btnHd.setBackgroundColor(colorHalfDay);
                btnHd.setTextColor(colorWhite);
            } else if ("A".equalsIgnoreCase(status)) {
                btnA.setBackgroundColor(colorAbsent);
                btnA.setTextColor(colorWhite);
            }
        }

        private void updateOtDisplay(double otHours) {
            if (otHours == (long) otHours) {
                tvOtValue.setText(String.format(Locale.US, "%dh", (long) otHours));
            } else {
                tvOtValue.setText(String.format(Locale.US, "%.1fh", otHours));
            }
        }

        private void updateLiveWageDisplay(WorkerAttendanceItem item) {
            Attendance att = item.getAttendance();
            tvLiveEarned.setText(String.format(Locale.US, "%,.0f Ks", att.getTotalEarned()));

            String statusLabel;
            if (att.getDayFraction() == 1.0) {
                statusLabel = "Full Day";
            } else if (att.getDayFraction() == 0.5) {
                statusLabel = "Half Day";
            } else {
                statusLabel = "Absent";
            }

            if (att.getOtHours() > 0) {
                statusLabel += String.format(Locale.US, " + %.1fh OT", att.getOtHours());
            }
            tvLiveEarnedSub.setText(statusLabel);

            double adv = item.getAdvanceDeduction();
            if (adv > 0) {
                layoutAdvanceNote.setVisibility(View.VISIBLE);
                tvAdvanceDeductionInfo.setText(String.format(Locale.US, "Advance: %,.0f Ks", adv));
                double net = item.getNetBalance();
                tvNetPayoutInfo.setText(String.format(Locale.US, "Net: %,.0f Ks", net));
            } else {
                layoutAdvanceNote.setVisibility(View.GONE);
            }
        }
    }
}
