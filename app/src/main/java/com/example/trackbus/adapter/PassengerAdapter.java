package com.example.trackbus.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackbus.R;
import com.example.trackbus.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PassengerAdapter extends RecyclerView.Adapter<PassengerAdapter.PassengerHolder> implements Filterable {
    private List<User>listData;
    List<User> myuserlistfull = new ArrayList<>();

    public PassengerAdapter(List<User> listData) {
        this.listData = listData;
        myuserlistfull = new ArrayList<>(listData);

    }

    @NonNull
    @Override
    public PassengerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.passengerlist,parent,false);
        PassengerHolder passengerHolder = new PassengerAdapter.PassengerHolder(view);
        return passengerHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PassengerHolder holder, int position) {


        User ld=listData.get(position);
        holder.fullname.setText(ld.getFullName());
        holder.email.setText(ld.getEmail());
        holder.profession.setText(ld.getProfession());
        holder.workplace.setText(ld.getWorkplace());
        holder.phone.setText(ld.getPhone());

        if(ld.getPhotoUrl()!=null){

            Picasso.get().load(ld.getPhotoUrl()).into(holder.profpic);
        }else{
            holder.profpic.setImageResource(R.drawable.profile);
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }


    @Override
    public Filter getFilter() {
        return passengerFliter;
    }

    private Filter passengerFliter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<User> filiteredList = new ArrayList<>();

            if(charSequence ==null || charSequence.length()==0){
                filiteredList.addAll(myuserlistfull);
            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (User useritem: myuserlistfull){
                    if(useritem.getWorkplace().toLowerCase().contains(filterPattern)){
                        filiteredList.add(useritem);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filiteredList;
            return  filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            listData.clear();
            listData.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };


    public class PassengerHolder extends RecyclerView.ViewHolder{

        ImageView profpic;

        TextView fullname,email,profession,workplace,phone;

        public PassengerHolder(@NonNull View itemView) {
            super(itemView);
            fullname = itemView.findViewById(R.id.tv_fullname);
            email = itemView.findViewById(R.id.tv_email);
            profession = itemView.findViewById(R.id.tv_description);
            workplace = itemView.findViewById(R.id.tv_workplace);
            phone = itemView.findViewById(R.id.tv_phonenumber);
            profpic = itemView.findViewById(R.id.imgView_proPic);



        }
    }

}