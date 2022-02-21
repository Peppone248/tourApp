package com.example.listview.Dealer;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.listview.R;

import java.util.List;

/*RcViewAdapter: necessario per la gestione e visualizzazione dei coupon nella lista del Delaer, agisce con ListCouponFragment */

public class RcViewAdapter extends RecyclerView.Adapter<RcViewAdapter.ViewHolder> {
    Context context;
    List<Coupon> cp;
    private OnItemClickListener mListener;

    public RcViewAdapter(Context context, List<Coupon> TempList) {
        this.cp = TempList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_coupon, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Set dei dati interessati
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Coupon coupon = cp.get(position);
        holder.NameCpTextView.setText(coupon.getNomeCoupon());
        holder.CodeCpTextView.setText(coupon.getCodice());
        holder.descCpTextView.setText(coupon.getDescription());
        holder.PercentDiscountTextView.setText(String.valueOf(coupon.getPercenutale())+ "%");
    }

    @Override
    public int getItemCount() {
        return cp.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        public TextView NameCpTextView, PercentDiscountTextView, CodeCpTextView, descCpTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            NameCpTextView = (TextView) itemView.findViewById(R.id.ShowCpName);
            CodeCpTextView = (TextView) itemView.findViewById(R.id.ShowCpCode);
            descCpTextView = (TextView) itemView.findViewById(R.id.ShowDescCp);
            PercentDiscountTextView = (TextView) itemView.findViewById(R.id.ShowPercentCoupon);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        // Richiamo il metodo per l'eliminazione dell'item
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(mListener != null){
                int position = getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                    mListener.onDeleteItemClick(position);
                    notifyDataSetChanged();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }

        // Creazione del menu onPressed
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle(R.string.selectOption);
            MenuItem deleteItem = contextMenu.add(Menu.NONE, 1, 1, R.string.remove);
            deleteItem.setOnMenuItemClickListener(this);
        }

    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteItemClick(int position);
    }
    public void setOnItemClickListener(ListCouponFragment listener) {
        mListener = listener;
    }
}
