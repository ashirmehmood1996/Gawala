package com.android.example.gawala.Generel.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.gawala.Generel.Models.NotificationModel;
import com.android.example.gawala.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationHolder> {

    private Callback callback;
    private Context context;
    private ArrayList<NotificationModel> notificationModelArrayList;

    public NotificationsAdapter(ArrayList<NotificationModel> notificationModelArrayList, Activity activity) {
        this.notificationModelArrayList = notificationModelArrayList;
        this.context = activity;
        this.callback = (Callback) activity;
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationHolder(LayoutInflater.from(context).inflate(R.layout.li_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        NotificationModel notificationModel = notificationModelArrayList.get(position);
        holder.titleTextView.setText(notificationModel.getNotificationTitle());
        holder.messageTextView.setText(notificationModel.getNotificationMessage());

        long dateModifiedinMilliseconds = Long.parseLong(notificationModel.getTimeStamp());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd, MMM yyyy");
        String modificaionDateInStr = simpleDateFormat.format(dateModifiedinMilliseconds);
        holder.timeStampTextView.setText(modificaionDateInStr);

    }

    @Override
    public int getItemCount() {
        return notificationModelArrayList.size();
    }

    class NotificationHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView, messageTextView, timeStampTextView;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_li_notification_title);
            messageTextView = itemView.findViewById(R.id.tv_li_notification_message);
            timeStampTextView = itemView.findViewById(R.id.tv_li_notification_time_stamp);
        }
    }

    public interface Callback {
        void onNotificationItemClicked(int pos);
    }
}
