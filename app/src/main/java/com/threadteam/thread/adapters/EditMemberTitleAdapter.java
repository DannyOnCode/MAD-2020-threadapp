package com.threadteam.thread.adapters;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.threadteam.thread.R;
import com.threadteam.thread.Utils;
import com.threadteam.thread.viewholders.EditMemberTitleViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditMemberTitleAdapter extends RecyclerView.Adapter<EditMemberTitleViewHolder> {

    public List<String> titleData = new ArrayList<>(Collections.nCopies(10, ""));

    @NonNull
    @Override
    public EditMemberTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EditMemberTitleViewHolder viewHolder;

        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.activity_partial_edit_member_title,
                parent,
                false
        );

        viewHolder = new EditMemberTitleViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EditMemberTitleViewHolder holder, final int position) {
        Integer level = Utils.ConvertStageToMinLevel(position);
        String title = Utils.GetDefaultTitleForStage(position);

        holder.EditMemberTitleLvl.setText(level.toString());
        holder.EditMemberTitleEditText.setHint(title);

        if(!titleData.get(position).equals("")) {
            holder.EditMemberTitleEditText.setText(titleData.get(position));
        }

        holder.EditMemberTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleData.set(position, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    public int getItemCount() {
        return 10;
    }

}
