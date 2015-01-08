package com.example.prabir.grinchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by prabir on 1/7/15.
 */
public class FriendsFragment extends ListFragment {
    public static final String TAG = FriendsFragment.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null) {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            getListView().getContext(),
                            android.R.layout.simple_list_item_1,
                            usernames);
                    getListView().setAdapter(adapter);
                } else {
                    // failure to get friends
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.signupErrorTitle)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        AdapterView.OnItemLongClickListener longClick = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                DialogInterface.OnClickListener onClickRemove = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFriendsRelation.remove(mFriends.get(position));
                        mCurrentUser.saveInBackground();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setNeutralButton(R.string.remove_friend, onClickRemove);
                builder.create().show();
                return false;
            }
        };
        getListView().setOnItemLongClickListener(longClick);



    }


}