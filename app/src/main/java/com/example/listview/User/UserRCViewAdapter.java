package com.example.listview.User;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.listview.Dealer.Coupon;
import com.example.listview.R;

import java.util.List;

public class UserRCViewAdapter extends RecyclerView.Adapter<UserRCViewAdapter.ViewHolder>
{
    Context context;
    List<Coupon> mainList;
    private OnItemClickListener uListener;

    public UserRCViewAdapter(Context context, List<Coupon> TempList) {
        this.mainList = TempList;
        this.context = context;
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView couponName;
        public TextView couponCode;
        public TextView percentage;
        public TextView dealerName;
        public ImageView icon;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            couponName = (TextView) itemView.findViewById(R.id.showUserNomeCoupon);
            couponCode = (TextView) itemView.findViewById(R.id.showUserCodeCoupon);
            percentage = (TextView) itemView.findViewById(R.id.showUserPercentCoupon);
            dealerName = (TextView) itemView.findViewById(R.id.showDealerName);
            icon = (ImageView) itemView.findViewById(R.id.iconCat);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (uListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    uListener.onItemClick(position);
                }
            }
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyler_view_usercoupon, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Coupon coupon = mainList.get(position);
        holder.couponName.setText(coupon.getNomeCoupon());
        holder.couponCode.setText(coupon.getCodice());
        holder.percentage.setText(String.valueOf(coupon.getPercenutale()));
        holder.dealerName.setText(coupon.getDealerName()); //recuperare il nome del dealer e inserirlo qui dentro

        if(coupon.getCategory().equals("Pizzeria")) {
            holder.icon.setBackgroundResource(R.drawable.ic_pizza);
        } else if(coupon.getCategory().equals("Bed and Breakfast")){
            holder.icon.setBackgroundResource(R.drawable.breakfast);
        } else if(coupon.getCategory().equals("Shore")){
            holder.icon.setBackgroundResource(R.drawable.ic_beach);
        } else if(coupon.getCategory().equals("Dance Club")){
            holder.icon.setBackgroundResource(R.drawable.disco);
        } else if(coupon.getCategory().equals("Church")){
            holder.icon.setBackgroundResource(R.drawable.ic_church);
        } else if(coupon.getCategory().equals("Ice-cream parlour")){
            holder.icon.setBackgroundResource(R.drawable.ic_ice_cream);
        } else if(coupon.getCategory().equals("Restaurant")){
            holder.icon.setBackgroundResource(R.drawable.ic_restaurant);
        } else if(coupon.getCategory().equals("Fun")){
            holder.icon.setBackgroundResource(R.drawable.ic_svago_famiglia);
        } else if(coupon.getCategory().equals("Hotel")){
            holder.icon.setBackgroundResource(R.drawable.hotel);
        } else if(coupon.getCategory().equals("Museum")){
            holder.icon.setBackgroundResource(R.drawable.ic_museum);
        }

    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onClick(int position);
    }

    public void setOnItemClickListener(ListCouponUserFragment listener){
        uListener=listener;
    }

    @Override
    public int getItemCount() {
        return mainList.size();
    }
}
