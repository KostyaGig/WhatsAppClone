package ru.kostya.whatsapp.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


import ru.kostya.whatsapp.R;
import ru.kostya.whatsapp.views.contact.ContactActivity;
import ru.kostya.whatsapp.views.profile.UserProfileActivity;
import ru.kostya.whatsapp.views.settings.SettingsActivity;
import ru.kostya.whatsapp.fragment.CallsFragment;
import ru.kostya.whatsapp.fragment.ChatsFragment;
import ru.kostya.whatsapp.fragment.StatusFragment;
import ru.kostya.whatsapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    //Походу благодаря этой штуке нам не нужно находить наши элементы по id,а как в котлине напрямую ,но перед названием id нужно написать binding.название Id
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //не знаю для чего это,но в gradle (module app) нужно добавить обязательную строчку,там есть коммент,чтобы было легче найти
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setUpWithViewPager(binding.mainViewPager);
        binding.tabLayout.setupWithViewPager(binding.mainViewPager);

        setSupportActionBar(binding.toolbar);

        binding.fabAction.setOnClickListener(this);
        binding.mainViewPager.addOnPageChangeListener(this);
    }

    private void setUpWithViewPager(ViewPager viewPager) {
        MainActivity.SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ChatsFragment(), "Chats");
        adapter.addFragment(new StatusFragment(), "Status");
        adapter.addFragment(new CallsFragment(), "Calls");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.fab_action:
                Toast.makeText(MainActivity.this, "Fab was clicked", Toast.LENGTH_LONG).show();
                break;
        }
    }

    //Слушатель ViewPager
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        changeFabICon(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //Add this code
    private static class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_search:
                Toast.makeText(MainActivity.this, "Action Search", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_new_group:
                Toast.makeText(MainActivity.this, "Action New Group", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_new_broadcast:
                Toast.makeText(MainActivity.this, "Action New Broadcast", Toast.LENGTH_LONG).show();

                break;
            case R.id.action_wa_web:
                Toast.makeText(MainActivity.this, "Action Web", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_starred_message:
                Toast.makeText(MainActivity.this, "Action Starred Message", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_settings:
                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settings);
                break;
        }
        return true;
    }

    private void changeFabICon(final int position) {
        //С помощью этого метода мы красивой анимацией будем изменять иконку нашей fab в зависимости от номера фрагмента ViewPager
        //Если будет выбран 0,то это чат устанавливаем иконку для fab chat
        //Отслеживаем позицию с помощью onPagerChangeListener И передаем позицию в этот метод
        //Принимаем позицию и в зависимости от позиции устанавливаем ту или иную иконку
        binding.fabAction.hide();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (position) {
                    case 0:
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_chat));
                            //Если это кнопка чат,то мы ей прописываем листенер именно в case,таким образом мы можем одной кнопку прописать свою логику,что мы хотим делать по нажатию на кнопке,если например активен view pager ЧАТ
                        //Если View pager - чат,то

                        binding.fabAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent contactIntent = new Intent(MainActivity.this, ContactActivity.class);
                                startActivity(contactIntent);
                            }
                        });

                        break;
                    case 1:
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_camera));
                        break;
                    case 2:
                        binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_call));
                        break;
                }
                binding.fabAction.show();
            }
        }, 400);

    }

}