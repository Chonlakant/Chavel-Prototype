package com.google.firebase.quickstart.database.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.chavel.Assymetric.AsymmetricRecyclerView;
import com.google.firebase.quickstart.database.chavel.Assymetric.AsymmetricRecyclerViewAdapter;
import com.google.firebase.quickstart.database.models.Pin;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.ui.activity.PostCommentsActivity;
import com.google.firebase.quickstart.database.ui.activity.PostDetailActivity;
import com.google.firebase.quickstart.database.ui.adapter.ChildAdapter;
import com.google.firebase.quickstart.database.viewholder.FeedViewHolder;

public abstract class PostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";
    public static final String EXTRA_POST_KEY = "post_key";
    private String mPostKey;

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private DatabaseReference mPinReference;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Post, FeedViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public PostListFragment() {}

    PostCommentsActivity.CommentAdapter mCommentAdapter;


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
//        mCommentAdapter.cleanupListener();
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    private RecyclerView mCommentsRecycler;
    private AsymmetricRecyclerView recyclerView;

    ChildAdapter mPinAdapter;

    public String Image1 = "https://wellcome.ac.uk/sites/default/files/styles/news_lead/public/G3520217_SPL_LeanGenes_200606_600x600.jpg?itok=3G_cT3lu";
    public String Image2 = "http://rs1054.pbsrc.com/albums/s499/vadimzbanok/1327.jpg~c200";
    public String Image3 = "http://www.pnas.org/site/misc/images/15-02545.500.jpg";
    public String Image4 = "https://s-media-cache-ak0.pinimg.com/736x/7f/47/e4/7f47e4e3f9f3755fcd6012dfe6a7dc12.jpg";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Post, FeedViewHolder>(Post.class, R.layout.item_feed,
                FeedViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final FeedViewHolder viewHolder, final Post model, final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                mPostKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, mPostKey);
                        startActivity(intent);
                    }
                });

//                // Determine if the current user has liked this post and set UI accordingly
//                if (model.stars.containsKey(getUid())) {
//                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
//                } else {
//                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
//                }

                // Bind UploadPost to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(mPostKey,getContext(),model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        // Run two transactions
                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }
                });


                mPostKey = postRef.getKey();
                if (mPostKey == null) {
                    throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
                }

                // Initialize Database
//                mPostReference = FirebaseDatabase.getInstance().getReference()
//                        .child("posts").child(mPostKey);
                mCommentsReference = mDatabase.child("post-comments").child(mPostKey);

                mCommentsRecycler = viewHolder.comments_recycler;
                mCommentsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

                mCommentAdapter = new PostCommentsActivity.CommentAdapter(getContext(), mCommentsReference);
                mCommentsRecycler.setAdapter(mCommentAdapter);
                mCommentAdapter.notifyDataSetChanged();

                recyclerView = viewHolder.recyclerView;

                model.pins.add(new Pin(Image1));
                model.pins.add(new Pin(Image2));
                model.pins.add(new Pin(Image3));
                model.pins.add(new Pin(Image4));

                mPinAdapter = new ChildAdapter(getContext(),model.pins,4,4);
                recyclerView.setAdapter(new AsymmetricRecyclerViewAdapter<>(getContext(),recyclerView, mPinAdapter));




                //                mCommentsRecycler.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent = new Intent(getActivity(), PostCommentsActivity.class);
//                        intent.putExtra(PostCommentsActivity.EXTRA_POST_KEY, mPostKey);
//                        startActivity(intent);
//                    }
//                });



            }
        };
        mRecycler.setAdapter(mAdapter);

        // Get post key from intent

    }

    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
    // [END post_stars_transaction]

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);



}
