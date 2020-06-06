package com.threadteam.thread;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class ViewServerStatusCardViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    public TextView serverName;
    public TextView serverTitle;
    public TextView serverLevel;
    private CardView serverRedirectionCard;

    public ViewServerStatusCardViewHolder(@NonNull View itemView) {
        super(itemView);

        serverName = itemView.findViewById(R.id.serverNameDisplay);
        serverTitle = itemView.findViewById(R.id.serverTitleDisplay);
        serverLevel = itemView.findViewById(R.id.levelDisplay);
        serverRedirectionCard = itemView.findViewById(R.id.serverRedirect);
    }

}
