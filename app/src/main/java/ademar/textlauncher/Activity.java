package ademar.textlauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.CATEGORY_LAUNCHER;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public final class Activity extends android.app.Activity implements
        Comparator<Model>, TextWatcher,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        TextView.OnEditorActionListener {

    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(ACTION_MAIN, null);
        intent.addCategory(CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, 0);
        ArrayList<Model> models = new ArrayList<>();
        long id = 0;
        for (ResolveInfo resolveInfo : availableActivities) {
            models.add(new Model(++id,
                    resolveInfo.loadLabel(packageManager).toString(),
                    resolveInfo.activityInfo.packageName));
        }
        Collections.sort(models, this);

        adapter = new Adapter(models);
        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        EditText filter = findViewById(R.id.filter);
        filter.addTextChangedListener(this);
        filter.setOnEditorActionListener(this);
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
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
        startActivity(getPackageManager().getLaunchIntentForPackage(adapter.getItem(index).packageName));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
        Toast.makeText(this, adapter.getItem(index).packageName, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == IME_ACTION_SEARCH) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                return true;
            }
        }
        return false;
    }

}
