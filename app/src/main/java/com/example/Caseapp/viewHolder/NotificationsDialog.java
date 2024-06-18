package com.example.Caseapp.viewHolder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Caseapp.R;
import com.example.Caseapp.model.Acceptor;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class NotificationsDialog extends Dialog {
    private Context context;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<Acceptor, AcceptorViewHolder> nAdapter;

    public NotificationsDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.notification_dilog);

        Query startAt = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("notifications").orderByChild("status").equalTo("unseen");
        FirebaseRecyclerOptions<Acceptor> options =
                new FirebaseRecyclerOptions.Builder<Acceptor>()
                        .setQuery(startAt, Acceptor.class)
                        .build();

        nAdapter = new FirebaseRecyclerAdapter<Acceptor, AcceptorViewHolder>(options) {
            @NonNull
            @Override
            public AcceptorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.acceptor_item, parent, false);
                return new AcceptorViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AcceptorViewHolder holder, int position, @NonNull Acceptor model) {
                holder.bind(model);
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(nAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        nAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        nAdapter.stopListening();
    }
}

