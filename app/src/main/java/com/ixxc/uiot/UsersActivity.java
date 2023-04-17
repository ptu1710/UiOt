package com.ixxc.uiot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ixxc.myuit.R;
import com.ixxc.uiot.API.APIManager;
import com.ixxc.uiot.Adapter.UserAdapter;
import com.ixxc.uiot.Interface.UsersListener;
import com.ixxc.uiot.Model.User;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {
    Toolbar toolbar;
    Menu actionbarMenu;
    ActionBar actionBar;
    TextView tv_forbidden;
    RecyclerView rv_users;

    List<User> userList;

    List<String> userNameList;
    UserAdapter userAdapter;

    Handler handler = new Handler(message -> {
        Bundle bundle = message.getData();
        int query = bundle.getInt("QUERY_USER");
        int delete = bundle.getInt("DELETE_USER");

        if (query == 403) {
            tv_forbidden.setVisibility(View.VISIBLE);
        } else if (query == 200) {
            showUsers();
        } else if (query == 0) {
            Toast.makeText(this, "Delete successful! " + delete, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Something went wrong here
            Toast.makeText(this, "Request failed with unknown error!", Toast.LENGTH_SHORT).show();
        }

        return false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        InitVars();
        InitViews();

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Users");
        actionBar.setDisplayHomeAsUpEnabled(true);

        new Thread(() -> {
            String queryString = "{\"realmPredicate\": {\"name\": \"master\"}}";
            JsonParser jsonParser = new JsonParser();
            JsonObject query = (JsonObject)jsonParser.parse(queryString);

            int code = APIManager.queryUsers(query);

            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt("QUERY_USER", code);
            message.setData(bundle);
            handler.sendMessage(message);
        }).start();
    }

    private void InitVars() {
        userNameList = new ArrayList<>();
    }

    private void InitViews() {
        tv_forbidden = findViewById(R.id.tv_forbidden);
        rv_users = findViewById(R.id.rv_users);
        toolbar = findViewById(R.id.action_bar);
    }

    private void showUsers() {
        userList = User.getUsersList();
        for (User user : userList) userNameList.add(user.getDisplayName());

        userAdapter = new UserAdapter(userNameList, new UsersListener() {
            final MenuItem item = actionbarMenu.findItem(R.id.delete);
            final MenuItem item1 = actionbarMenu.findItem(R.id.cancel);
            @Override
            public void onItemClicked(View v, int pos) {
                item.setVisible(false);
                item1.setVisible(false);

                Intent intent = new Intent(UsersActivity.this, UserInfoActivity.class);
                intent.putExtra("USER_ID", userList.get(pos).id);
                startActivity(intent);
            }

            @Override
            public void onItemLongClicked(View v, int pos) {
                item.setIconTintList(UsersActivity.this.getColorStateList(R.color.white));
                item1.setIconTintList(UsersActivity.this.getColorStateList(R.color.white));
                item.setVisible(true);
                item1.setVisible(true);
            }
        }, this);

        rv_users.setLayoutManager(new LinearLayoutManager(this));
        rv_users.setAdapter(userAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        actionbarMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.cancel) {
            final MenuItem item1 = actionbarMenu.findItem(R.id.delete);
            item1.setVisible(false);
            item.setVisible(false);

            userAdapter.notifyItemChanged(userAdapter.checkedPos);
            userAdapter.checkedPos = -1;
        } else if (item.getItemId() == R.id.delete) {
            new Thread(() -> {
                Log.d(GlobalVars.LOG_TAG, "onOptionsItemSelected: " + userList.get(userAdapter.checkedPos).id);
                int code = APIManager.deleteUser(userList.get(userAdapter.checkedPos).id);

                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("DELETE_USER", code);
                message.setData(bundle);
                handler.sendMessage(message);
            }).start();
        }

        return super.onOptionsItemSelected(item);
    }
}