package org.openinit.multicloudfilemanager.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.openinit.multicloudfilemanager.R;
import org.openinit.multicloudfilemanager.RecyclerViewAdapters.LogRecyclerViewAdapter;
import org.openinit.multicloudfilemanager.util.SyncLog;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class LogFragment extends Fragment {

    private View fragmentView;

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
        setHasOptionsMenu(false);
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

    private void populateLogs(View v){
        Context c = v.getContext();

        RecyclerView recyclerView =  v.findViewById(R.id.log_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(c));
        recyclerView.setItemAnimator(new LandingAnimator());

        LogRecyclerViewAdapter recyclerViewAdapter = new LogRecyclerViewAdapter(SyncLog.getLog(c));
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}
