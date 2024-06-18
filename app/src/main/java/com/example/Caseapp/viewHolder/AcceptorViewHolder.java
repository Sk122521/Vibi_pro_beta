package com.example.Caseapp.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.Caseapp.R;
import com.example.Caseapp.model.Acceptor;

public class AcceptorViewHolder extends RecyclerView.ViewHolder {
    TextView acceptorPhoneTextView;
    public AcceptorViewHolder( View itemView) {
        super(itemView);
        acceptorPhoneTextView = itemView.findViewById(R.id.acceptorPhone);
    }

    public void bind(Acceptor acceptor) {
        acceptorPhoneTextView.setText(acceptor.getAcceptorName()+" with Id "+"'"+acceptor.getAcceptorId() +"'"+" and Phone number "+acceptor.getAcceptorPhone()+ " has accepted your case");
    }
}

