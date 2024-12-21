package com.example.casper.Experiment2024.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.casper.Experiment2024.R;
import com.example.casper.Experiment2024.model.CheckIn;
import java.util.List;

public class CheckInAdapter extends RecyclerView.Adapter<CheckInAdapter.CheckInViewHolder> {

    private List<CheckIn> checkInList;

    public static class CheckInViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDescription;
        public CheckInViewHolder(View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }

    public CheckInAdapter(List<CheckIn> checkInList) {
        this.checkInList = checkInList;
    }

    @NonNull
    @Override
    public CheckInViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_check_in, parent, false);
        return new CheckInViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckInViewHolder holder, int position) {
        CheckIn currentItem = checkInList.get(position);
        holder.tvDescription.setText(currentItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return checkInList.size();
    }

    // 更新数据集的方法
    public void setCheckInList(List<CheckIn> checkInList) {
        this.checkInList = checkInList;
        notifyDataSetChanged();
    }
}