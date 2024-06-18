package com.example.Caseapp.viewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Caseapp.R;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView imageView;

    public TextView title;
    public Button activateBtn;
    public Button notification_btn;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_topic);
        title = itemView.findViewById(R.id.name_topic);
        activateBtn = itemView.findViewById(R.id.activate_btn);
    }

}