package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewMemberViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    private ConstraintLayout BaseViewMemberConstraintLayout;
    private CardView ViewMemberCardView;
    private ConstraintLayout ViewMemberConstraintLayout;
    public CircleImageView MemberProfileImageView;
    public TextView MemberName;
    public TextView MemberTitle;
    public TextView MemberLevel;
    public ProgressBar MemberProgressBar;
    public TextView MemberExp;

    public ViewMemberViewHolder(@NonNull View itemView) {
        super(itemView);

        BaseViewMemberConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.baseViewMemberConstraintLayout);
        ViewMemberCardView = (CardView) itemView.findViewById(R.id.viewMemberCardView);
        ViewMemberConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.viewMemberConstraintLayout);
        MemberProfileImageView = (CircleImageView) itemView.findViewById(R.id.memberProfileImageView);
        MemberName = (TextView) itemView.findViewById(R.id.memberName);
        MemberTitle = (TextView) itemView.findViewById(R.id.memberTitle);
        MemberLevel = (TextView) itemView.findViewById(R.id.memberLevel);
        MemberProgressBar = (ProgressBar) itemView.findViewById(R.id.memberProgressBar);
        MemberExp = (TextView) itemView.findViewById(R.id.memberExp);
    }
}
