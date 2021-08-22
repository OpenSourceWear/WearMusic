package cn.wearbbs.music.adapter;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import cn.wearbbs.music.fragment.ConsoleFragment;
import cn.wearbbs.music.fragment.LyricsFragment;
import cn.wearbbs.music.fragment.PlayerFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final Intent intent;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, Intent intent) {
        super(fragmentActivity);
        this.intent = intent;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return PlayerFragment.newInstance(intent);
        } else if(position==1){
            return LyricsFragment.newInstance(intent);
        }
        else{
            return ConsoleFragment.newInstance(intent);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }


}
