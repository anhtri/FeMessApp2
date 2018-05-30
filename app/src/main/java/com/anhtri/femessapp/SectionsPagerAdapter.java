package com.anhtri.femessapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.anhtri.fragment.ChatsFragment;
import com.anhtri.fragment.FriendsFragment;
import com.anhtri.fragment.RequestFragment;

class SectionsPagerAdapter extends FragmentPagerAdapter {
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 2:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            default:
                return null;

        }
    }

    @Override
    public int getCount() {

        //We have 3 tabs, so we should return 3;
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chats";
            case 1:
                return "Friends";
            case 2:
                return "Requests";
            default:
                return null;

        }
    }
}
