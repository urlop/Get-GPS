package com.example.ruby.getgps.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.models.WorkSource;
import com.example.ruby.getgps.ui.adapters.WorkSourcesAdapter;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.UIHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class WorkSourcesActivity extends AppCompatActivity {

    protected
    @Bind(R.id.rv_work)
    RecyclerView rv_work;
    protected
    @Bind(R.id.bt_next)
    Button bt_next;
    protected
    @Bind(R.id.v_progress)
    View v_progress;

    private EditText et_name;

    private GridLayoutManager lLayout;
    private Dialog dialog;
    private final List<WorkSource> list = new ArrayList<>();
    private String[] colors;
    private boolean editProfile = false;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_sources);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            editProfile = getIntent().getExtras().getBoolean(Constants.EDIT_PROFILE_EXTRA);
            bt_next.setText(this.getResources().getString(R.string.worksource_save));
            user = (User) getIntent().getSerializableExtra(Constants.EDIT_PROFILE_USER_EXTRA);
        }
        setupView();
        setupList();
    }

    private void setupView() {
        UIHelper.setupAlternativeStatusBar(this);

        lLayout = new GridLayoutManager(WorkSourcesActivity.this, 3);
        rv_work.setHasFixedSize(true);
        rv_work.setLayoutManager(lLayout);

        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(v);
            }
        });

        //DIALOG
        dialog = new Dialog(this);
        dialog.setTitle("Create Custom");
        dialog.setContentView(R.layout.dialog_work_sources_add);
        dialog.setCancelable(true);

        et_name = (EditText) dialog.findViewById(R.id.et_name);
        et_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.add || id == EditorInfo.IME_NULL) {
                    addWorkSource();
                    return true;
                }
                return false;
            }
        });

        Button bt_ws_add = (Button) dialog.findViewById(R.id.bt_ws_add);
        bt_ws_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWorkSource();
            }
        });
    }

    private void setupList() {
        String[] names = getResources().getStringArray(R.array.work_sources_names);
        TypedArray drawables = getResources().obtainTypedArray(R.array.work_sources_icons);
        colors = getResources().getStringArray(R.array.work_sources_colors);

        for (int i = 0; i < names.length; i++) {
            list.add(new WorkSource(names[i], drawables.getDrawable(i), colors[i]));
        }
        boolean included = false;
        if (editProfile) {
            for (String incomeSources : user.getIncomeSources()) {
                for (String name : names) {
                    if (incomeSources.equals(name)) {
                        included = true;
                    }
                }
                if (!included) {
                    WorkSource workSource = new WorkSource(incomeSources, null, colors[new Random().nextInt(10)], true);
                    workSource.setSelected(true);
                    list.add(workSource);
                }
                included = false;
            }
        }
        WorkSourcesAdapter adapter;
        if (!editProfile) {
            adapter = new WorkSourcesAdapter(list, this);
        } else {
            adapter = new WorkSourcesAdapter(list, this, user.getIncomeSources());
        }
        rv_work.setAdapter(adapter);
    }

    @OnClick(R.id.fab)
    public void btnFab(View view) {
        dialog.show();
    }

    private void addWorkSource() {
        dialog.cancel();
        String name = et_name.getText().toString();
        if (!name.trim().isEmpty() && name.matches("[a-zA-Z ]+")) {
            list.add(new WorkSource(et_name.getText().toString(), null, colors[new Random().nextInt(10)], true));
            Toast.makeText(this, R.string.ws_added, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.ws_error_name, Toast.LENGTH_SHORT).show();
        }

        et_name.setText("");
    }

    private void sendData(View button) {
        button.setEnabled(false);
        UIHelper.showProgress(this, v_progress, null, true);

        List<String> selectedWorkSources = new ArrayList<>();
        for (WorkSource ws : list) {
            if (ws.isSelected()) {
                selectedWorkSources.add(ws.getName());
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("income_sources", selectedWorkSources);
        Call<User> call = RequestManager.getDefault(this).updateCurrentUser(map);
        call.enqueue(new CustomCallback<User>(this, call) {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    UIHelper.showProgress(WorkSourcesActivity.this, v_progress, null, false);

                    Toast.makeText(WorkSourcesActivity.this, R.string.ws_ok_send, Toast.LENGTH_SHORT).show();
                    if (!editProfile) {
                        Intent intent = new Intent(WorkSourcesActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        User user = response.body();
                        Intent intent = new Intent();
                        intent.putExtra(Constants.EDIT_PROFILE_USER_EXTRA, user);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } else {
                    Toast.makeText(WorkSourcesActivity.this, R.string.ws_error_send, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
