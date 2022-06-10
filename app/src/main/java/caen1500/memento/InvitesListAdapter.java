package caen1500.memento;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InvitesListAdapter extends RecyclerView.Adapter<InvitesListAdapter.ViewHolder> {

   private final List<String> invites;
   private final LayoutInflater layoutInflater;
   private ItemClickListener itemClickListener;

   InvitesListAdapter(Context context, List<String> data) {
      this.layoutInflater = LayoutInflater.from(context);
      this.invites = data;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = layoutInflater.inflate(R.layout.recycler_view, parent, false);
      return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(ViewHolder holder, int position) {
      String animal = invites.get(position);
      holder.textView.setText(animal);
   }

   @Override
   public int getItemCount() {
      return invites.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      private final TextView textView;

      public ViewHolder(View itemView) {
         super(itemView);
         textView = itemView.findViewById(R.id.tvInvites);
         itemView.setOnClickListener(this);
      }

      @Override
      public void onClick(View view) {
         if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
      }
   }

   public void setClickListener(ItemClickListener itemClickListener) {
      this.itemClickListener = itemClickListener;
   }

   public interface ItemClickListener {
      void onItemClick(View view, int position);
   }
}