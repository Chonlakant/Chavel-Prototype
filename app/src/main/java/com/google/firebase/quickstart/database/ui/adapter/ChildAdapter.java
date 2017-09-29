package com.google.firebase.quickstart.database.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.chavel.Assymetric.AGVRecyclerViewAdapter;
import com.google.firebase.quickstart.database.chavel.Assymetric.AsymmetricItem;
import com.google.firebase.quickstart.database.models.Pin;

import java.util.List;

public class ChildAdapter extends AGVRecyclerViewAdapter<ViewHolder> {
    private final List<Pin> items;
    private int mDisplay = 0;
    private int mTotal = 0;

    private Context context;

    public ChildAdapter(Context context, List<Pin> pinImages, int mDisplay, int mTotal) {
        this.context = context;
      this.items = pinImages;
      this.mDisplay = mDisplay;
        this.mTotal = mTotal;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      Log.d("RecyclerViewActivity", "onCreateView");
      return new ViewHolder(parent, viewType,items);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      Log.d("RecyclerViewActivity", "onBindView position=" + position);
      holder.bind(context,items,position,mDisplay,mTotal);
    }

    @Override
    public int getItemCount() {
      return items.size();
    }



    @Override
    public int getItemViewType(int position) {
      return position % 2 == 0 ? 1 : 0;
    }

    @Override public AsymmetricItem getItem(int position) {
        return (AsymmetricItem) items.get(position);
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
  private final ImageView mImageView;
  private final TextView textView;

  public ViewHolder(ViewGroup parent, int viewType, List<Pin> items) {
    super(LayoutInflater.from(parent.getContext()).inflate(
            R.layout.adapter_item, parent, false));

    mImageView = (ImageView) itemView.findViewById(R.id.mImageView);
    textView = (TextView) itemView.findViewById(R.id.tvCount);

  }


  public void bind(Context context, List<Pin> items, int position, int mDisplay, int mTotal) {
    //ImageLoader.getInstance().displayImage(String.valueOf(item.get(position).getImagePath()), mImageView);
      mImageView.setVisibility(View.VISIBLE);

    textView.setText("+"+(mTotal-mDisplay));
    if(mTotal > mDisplay)
    {
      if(position  == mDisplay-1) {
        textView.setVisibility(View.VISIBLE);
        mImageView.setAlpha(72);
      }
      else {
        textView.setVisibility(View.INVISIBLE);
        mImageView.setAlpha(255);
      }
    }
    else
    {
      mImageView.setAlpha(255);
      textView.setVisibility(View.INVISIBLE);
    }

      Glide.with(context).load(items.get(position).pinImage).into(mImageView);

    // textView.setText(String.valueOf(item.getPosition()));
  }
}
