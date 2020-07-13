package com.threadteam.thread.viewholders;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;

public class EditMemberTitleViewHolder extends RecyclerView.ViewHolder {

    // VIEW OBJECTS
    private ConstraintLayout BaseEditMemTitleConstraintLayout;
    private CardView EditMemberTitleLvlCardView;
    public TextView EditMemberTitleLvl;
    public EditText EditMemberTitleEditText;
    private View EditMemberLine;

    public EditMemberTitleViewHolder(@NonNull View itemView) {
        super(itemView);

        BaseEditMemTitleConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.baseEditMemTitleConstraintLayout);
        EditMemberTitleLvlCardView = (CardView) itemView.findViewById(R.id.editMemberTitleLvlCardView);
        EditMemberTitleLvl = (TextView) itemView.findViewById(R.id.editMemberTitleLvl);
        EditMemberTitleEditText = (EditText) itemView.findViewById(R.id.editMemberTitleEditText);
        EditMemberLine = itemView.findViewById(R.id.editMemberLine);
    }
}
