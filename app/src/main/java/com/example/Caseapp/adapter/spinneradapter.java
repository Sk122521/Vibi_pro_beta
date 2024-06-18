package com.example.Caseapp.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.Caseapp.R;
import com.example.Caseapp.utils.OperatedXcode2;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class spinneradapter extends ArrayAdapter<OperatedXcode2>
{

    LayoutInflater layoutInflater;

    public spinneradapter(@NonNull Context context, int resource,@NonNull List<OperatedXcode2> users)
    {
        super(context, resource, users);
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View rowView = layoutInflater.inflate(R.layout.spinner_item, null,true);
        OperatedXcode2 user = getItem(position);
        TextView textView = (TextView)rowView.findViewById(R.id.xcodetext);
        CircleImageView imageView = (CircleImageView) rowView.findViewById(R.id.xcodeimage);
        textView.setText(user.getCode());
        imageView.setImageResource(setImage(user.getImage()));;
        return rowView;
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if(convertView == null)
            convertView = layoutInflater.inflate(R.layout.spinner_item, parent,false);

        OperatedXcode2 user = getItem(position);
        TextView textView = (TextView)convertView.findViewById(R.id.xcodetext);
        CircleImageView imageView = (CircleImageView) convertView.findViewById(R.id.xcodeimage);
        textView.setText(user.getCode());
        imageView.setImageResource(setImage(user.getImage()));
        return convertView;
    }

    public int setImage(int image){
        switch (image)
        {
            case 0:
                return R.mipmap.xcode2_0;
            case 1:
                return R.mipmap.xcode2_1;
            case 2:
                return R.mipmap.xcode2_2;
            case 3:
                return R.mipmap.xcode2_3;
            case 4:
                return R.mipmap.xcode2_4;
        }
        return R.mipmap.xcode2_0;
    }

}

