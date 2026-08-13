package org.openinit.multicloudfilemanager.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.openinit.multicloudfilemanager.R;
import org.openinit.multicloudfilemanager.RecyclerViewAdapters.LogRecyclerViewAdapter;
import org.openinit.multicloudfilemanager.util.SyncLog;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class LogFragment extends Fragment {

    public static final int FILTER_ALL   = -1;
    public static final int FILTER_ERROR = SyncLog.TYPE_ERROR;
    public static final int FILTER_INFO  = SyncLog.TYPE_INFO;

    private View fragmentView;
    private LogRecyclerViewAdapter recyclerViewAdapter;
    private int currentFilter = FILTER_ALL;

    public LogFragment() {
        // Required empty public constructor
    }

    public static LogFragment newInstance() {
        return new LogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FragmentActivity) getContext()).setTitle(R.string.logFragment);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        fragmentView = view;
        populateLogs(fragmentView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateLogs(fragmentView);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_log_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_logs) {
            showClearLogsConfirmation();
            return true;
        } else if (id == R.id.action_filter_logs) {
            View anchor = requireActivity().findViewById(R.id.action_filter_logs);
            showFilterMenu(anchor);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearLogsConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.log_clear_title)
                .setMessage(R.string.log_clear_message)
                .setPositiveButton(R.string.log_clear_confirm, (dialog, which) -> {
                    SyncLog.delete(requireContext());
                    populateLogs(fragmentView);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_log_filter, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.filter_all) {
                currentFilter = FILTER_ALL;
            } else if (id == R.id.filter_errors) {
                currentFilter = FILTER_ERROR;
            } else if (id == R.id.filter_info) {
                currentFilter = FILTER_INFO;
            }
            applyFilter();
            return true;
        });
        popup.show();
    }

    private void applyFilter() {
        if (recyclerViewAdapter == null) return;
        ArrayList<JSONObject> all = SyncLog.getLog(requireContext());
        if (currentFilter == FILTER_ALL) {
            recyclerViewAdapter.setList(all);
        } else {
            ArrayList<JSONObject> filtered = new ArrayList<>();
            for (JSONObject obj : all) {
                try {
                    if (obj.getInt(SyncLog.TYPE) == currentFilter) {
                        filtered.add(obj);
                    }
                } catch (JSONException ignored) {}
            }
            recyclerViewAdapter.setList(filtered);
        }
    }

    private void populateLogs(View v) {
        Context c = v.getContext();

        RecyclerView recyclerView = v.findViewById(R.id.log_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(c));
        recyclerView.setItemAnimator(new LandingAnimator());

        recyclerViewAdapter = new LogRecyclerViewAdapter(SyncLog.getLog(c));
        recyclerView.setAdapter(recyclerViewAdapter);

        // Re-apply active filter after reloading data
        applyFilter();
    }
}
