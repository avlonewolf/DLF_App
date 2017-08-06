package backbenchersbeta.dlf;

/**
 * Created by Ankit Vimal on 05-04-2016.
 */
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder>{

    private List<Shop> countries;
    private int rowLayout;
    private Context mContext;

    public ShopAdapter(List<Shop> countries, int rowLayout, Context context) {
        this.countries = countries;
        this.rowLayout = rowLayout;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Shop country = countries.get(i);
        viewHolder.Name.setBackground(country.name);
          }

    @Override
    public int getItemCount() {
        return countries == null ? 0 : countries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView Name;

        public ViewHolder(View itemView) {
            super(itemView);
            Name = (ImageView)itemView.findViewById(R.id.Name);

        }

    }
}

