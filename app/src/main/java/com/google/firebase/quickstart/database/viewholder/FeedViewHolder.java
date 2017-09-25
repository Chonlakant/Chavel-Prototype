package com.google.firebase.quickstart.database.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.quickstart.database.ui.activity.PostCommentsActivity;
import com.google.firebase.quickstart.database.ui.activity.PostDetailActivity;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Post;

/**
 * Created by korrio on 9/21/2017 AD.
 */

public class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView header_title;

    public ImageView btn_expand_toggle;
    public ImageView img_comments;
    public ImageView img_like;
    public ImageView img_share;
    public ImageView header_photo;
    public RecyclerView comments_recycler;

    public ImageView img_star;
    public TextView header_sub_title;
    public TextView star_count;

    public TextView username;
    public TextView header_meta_title;

    private Context context;
    private String postKey;

    public FeedViewHolder(View itemView) {
        super(itemView);
        header_title = (TextView) itemView.findViewById(R.id.header_title);
        header_meta_title = (TextView) itemView.findViewById(R.id.header_meta_title);
        header_photo = (ImageView) itemView.findViewById(R.id.header_photo);

        header_sub_title = (TextView) itemView.findViewById(R.id.header_subtitle);

        img_share = (ImageView) itemView.findViewById(R.id.img_share);
        img_comments = (ImageView) itemView.findViewById(R.id.img_comment);
        img_star = (ImageView) itemView.findViewById(R.id.img_star);

        comments_recycler = (RecyclerView) itemView.findViewById(R.id.recycler_comments);
        btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
        star_count = (TextView) itemView.findViewById(R.id.star_count);

        username = (TextView) itemView.findViewById(R.id.username);

    }

    public void bindToPost(String postKey,Context context, Post post, View.OnClickListener starClickListener) {
        this.context = context;
        this.postKey = postKey;

        String unixTimeStr = post.unixTime + "";

        header_title.setText(post.title);
        header_meta_title.setText("TH:"+post.postId);
        header_sub_title.setText(post.body);
        star_count.setText(String.valueOf(post.starCount));
        username.setText(post.author);
       // header_meta_title.setText(post.lat + "," + post.lng);

        img_share.setOnClickListener(this);
        img_comments.setOnClickListener(this);
        img_star.setOnClickListener(starClickListener);

        header_photo.setOnClickListener(this);
        header_title.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        Toast.makeText(context,"id:" + v.getId(),Toast.LENGTH_SHORT).show();
        Intent intent;
        switch (v.getId()) {
            case R.id.recycler_comments:
            case R.id.img_comment:
                intent = new Intent(context, PostCommentsActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                context.startActivity(intent);
                break;

            case R.id.header_title:
            case R.id.header_photo:
                intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                context.startActivity(intent);
        }

        //        switch (v.getId()) {
//            case R.id.img_like:
//                if (mOnItemClickLikeListener != null) {
//                    mOnItemClickLikeListener.onItemLikeClick(v, getPosition());
//                }
//                break;
//
//            case R.id.img_comments:
//                if (mOnItemClickCommentListener != null) {
//                    mOnItemClickCommentListener.onItemCommentClick(v, getPosition());
//                }
//                break;
//
//            case R.id.img_sh:
//                if (mOnItemClickShListener != null) {
//                    mOnItemClickShListener.onItemShClick(v, getPosition());
//                }
//                break;
//            case R.id.header_photo:
//                if (mOnItemClickPhotoListener != null) {
//                    mOnItemClickPhotoListener.onItemPhotoClick(v, getPosition());
//                }
//                break;
//
//            case R.id.header_title:
//                if (mOnItemClickRouteTitleListener != null) {
//                    mOnItemClickRouteTitleListener.onItemRouteTitleClick(v, getPosition());
//                }
//                break;
//
//        }
    }
}
