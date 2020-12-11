package ademar.textlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.ACTION_PACKAGE_ADDED;
import static android.content.Intent.ACTION_PACKAGE_REMOVED;
import static android.content.Intent.ACTION_PACKAGE_REPLACED;
import static android.content.Intent.CATEGORY_LAUNCHER;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public final class Activity extends android.app.Activity implements
        Comparator<Model>,
        TextWatcher,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        TextView.OnEditorActionListener,
        View.OnClickListener {

    private final Adapter adapter = new Adapter();

    private EditText filter;
    private ImageView clear;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        update();

        ListView list = findViewById(R.id.list);
        filter = findViewById(R.id.filter);
        clear = findViewById(R.id.clear_filter);

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        filter.addTextChangedListener(this);
        filter.setOnEditorActionListener(this);
        clear.setOnClickListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PACKAGE_ADDED);
        intentFilter.addAction(ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                update();
            }
        };

        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public int compare(Model lhs, Model rhs) {
        return lhs.label.compareToIgnoreCase(rhs.label);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        adapter.filter(editable.toString());
        if (clear != null) {
            clear.setVisibility(editable.length() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        clearSearch();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
        hideKeyboard();
        try {
            startActivity(getPackageManager().getLaunchIntentForPackage(adapter.getItem(index).packageName));
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
        hideKeyboard();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", adapter.getItem(index).packageName, null));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, adapter.getItem(index).packageName, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        return actionId == IME_ACTION_SEARCH && hideKeyboard();
    }

    private boolean hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && filter != null) {
            inputMethodManager.hideSoftInputFromWindow(filter.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private void clearSearch() {
        if (filter != null) {
            filter.setText("");
        }
        hideKeyboard();
    }

    private void update() {
        clearSearch();

        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(ACTION_MAIN, null);
        intent.addCategory(CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, 0);
        ArrayList<Model> models = new ArrayList<>();
        long id = 0;
        for (ResolveInfo resolveInfo : availableActivities) {
            if ("ademar.textlauncher".equalsIgnoreCase(resolveInfo.activityInfo.packageName)) continue;
            models.add(new Model(
                    ++id,
                    resolveInfo.loadLabel(packageManager).toString(),
                    resolveInfo.activityInfo.packageName
            ));
        }
        Collections.sort(models, this);
        adapter.update(models);
    }

}
