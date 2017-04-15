package com.kupangstudio.shoufangbao.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kupangstudio.shoufangbao.R;
import com.kupangstudio.shoufangbao.model.BuildLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by long1 on 16/3/2.
 * Copyright 16/3/2 android_xiaobai.
 * 楼盘详情户型adapter
 */
public class BuildDetailLayoutAdapter extends RecyclerView.Adapter<BuildDetailLayoutAdapter.MyViewHolder> {

    private Context context;
    private List<BuildLayout> layouts;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public BuildDetailLayoutAdapter(Context context, List<BuildLayout> layouts) {
        this.context = context;
        this.layouts = layouts;
        initImageLoader();
    }

    private void initImageLoader() {
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_build_detail_layout, parent
                , false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        BuildLayout layout = layouts.get(position);
        imageLoader.displayImage(layout.getUrl(), holder.imageView, options);
        holder.tvLayout.setText(layout.getTitle());
        holder.tvPrice.setText(layout.getPrice());
        holder.tvSale.setText(layout.getDiscount());
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, position);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return layouts.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView tvLayout;
        TextView tvPrice;
        TextView tvSale;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            tvLayout = (TextView) itemView.findViewById(R.id.tv_layout);
            tvPrice = (TextView) itemView.findViewById(R.id.tv_price);
            tvSale = (TextView) itemView.findViewById(R.id.tv_sale);
        }
    }
}
