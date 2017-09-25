package com.google.firebase.quickstart.database.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Post;

import org.w3c.dom.Text;

/**
 * Created by korrio on 9/21/2017 AD.
 */

public class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView header_title;

    public ImageView btn_expand_toggle;
    public ImageView img_comments;
    public ImageView img_like;
    public ImageView img_sh;
    public ImageView photo;
    public RecyclerView comments_recycler;

    public ImageView img_star;

    public TextView txt_msg;

    public TextView star_count;

    public FeedViewHolder(View itemView) {
        super(itemView);
        header_title = (TextView) itemView.findViewById(R.id.header_title);
        //img_comments = (ImageView) itemView.findViewById(R.id.img_comments);
        photo = (ImageView) itemView.findViewById(R.id.post_author_photo);
        img_like = (ImageView) itemView.findViewById(R.id.img_like);
        img_sh = (ImageView) itemView.findViewById(R.id.img_sh);
        btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
        comments_recycler = (RecyclerView) itemView.findViewById(R.id.recycler_comments);

        img_star = (ImageView) itemView.findViewById(R.id.img_star);

        star_count = (TextView) itemView.findViewById(R.id.star_count);
        txt_msg = (TextView) itemView.findViewById(R.id.txt_msg);

        img_like.setOnClickListener(this);
//        img_comments.setOnClickListener(this);
        img_sh.setOnClickListener(this);
        photo.setOnClickListener(this);
        header_title.setOnClickListener(this);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        header_title.setText(post.title);
        //authorView.setText(post.author);
        star_count.setText(String.valueOf(post.starCount));
        txt_msg.setText(post.body);

        img_star.setOnClickListener(starClickListener);
    }

    @Override
    public void onClick(View v) {
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
//            case R.id.photo:
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
