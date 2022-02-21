package com.example.listview.Diary;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.listview.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*RecyclerAdapter: Adapter, necessario per la visualizzazione degli elementi nel diario del viaggiatore*/

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {
    private Context mContext;
    private List<DiaryElement> elements;
    private OnItemClickListener mListener;

    public RecyclerAdapter(Context context, List<DiaryElement> uploads){
        mContext=context;
        elements=uploads;
    }

    @NonNull
    @Override
    public RecyclerAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.row_model, parent, false);
        return new RecyclerViewHolder(v);
    }

    // settaggio dei campi interessati
    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.RecyclerViewHolder holder, int position) {
        DiaryElement currentElement = elements.get(position);
        holder.nameTextView.setText(currentElement.getName());
        holder.descriptionTextView.setText(currentElement.getDescription());
        holder.dateTextView.setText(getDateToday());
        Picasso.with(mContext).load(currentElement.getImageUrl()).placeholder(R.drawable.placeholder).fit().centerCrop().into(holder.diaryImageView);
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public TextView nameTextView, descriptionTextView, dateTextView;
        public ImageView diaryImageView;

        public RecyclerViewHolder(View itemView){
            super((itemView));
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView=itemView.findViewById(R.id.descriptionTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            diaryImageView = itemView.findViewById(R.id.teacherImageView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        // Implementazione del menu onPressed
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(mListener != null){
                int position = getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                    switch (menuItem.getItemId()) {
                        case 1:
                            mListener.onShowItemClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteItemClick(position);
                            return true;
                    }
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

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle(R.string.selectOption);
            MenuItem showItem = contextMenu.add(Menu.NONE, 1, 1, R.string.open);
            MenuItem deleteItem = contextMenu.add(Menu.NONE, 2, 2, R.string.remove);
            showItem.setOnMenuItemClickListener(this);
            deleteItem.setOnMenuItemClickListener(this);
        }
    }

    // Interfaccia che richiama i metodi del menu onPressed
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onShowItemClick(int position);
        void onDeleteItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    private String getDateToday(){
        DateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd");
        Date date=new Date();
        String today= dateFormat.format(date);
        return today;
    }
}
