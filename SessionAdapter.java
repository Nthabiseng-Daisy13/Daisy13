package com.example.timer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<SessionItem> items;

    public SessionAdapter(List<SessionItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType(); // TYPE_HEADER or TYPE_SESSION
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == SessionItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session_timeline, parent, false);
            return new SessionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SessionItem item = items.get(position);

        if (holder instanceof SessionViewHolder && item.getType() == SessionItem.TYPE_SESSION) {
            StudySession session = item.getSession();
            SessionViewHolder sessionHolder = (SessionViewHolder) holder;

            sessionHolder.textDate.setText("Date: " + session.getDate());
            sessionHolder.textDuration.setText("Duration: " + session.getDuration() + " min");
            sessionHolder.textStartTime.setText("Start: " + session.getStartTime());
            sessionHolder.textEndTime.setText("End: " + session.getEndTime());

            // Hide bottom timeline line for last item or before a header


        }
        else if (holder instanceof HeaderViewHolder && item.getType() == SessionItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).headerTitle.setText(item.getHeader());
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textDuration, textStartTime, textEndTime;
        View line;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textViewDate);
            textDuration = itemView.findViewById(R.id.textViewDuration);
            textStartTime = itemView.findViewById(R.id.textStartTime);
            textEndTime = itemView.findViewById(R.id.textEndTime);
            line = itemView.findViewById(R.id.line);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.textViewHeader);
        }
    }
}
