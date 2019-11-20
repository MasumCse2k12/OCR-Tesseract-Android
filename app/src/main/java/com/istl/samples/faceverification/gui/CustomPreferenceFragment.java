package com.istl.samples.faceverification.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.imperialsoupgmail.faceverification.R;
import com.neurotec.face.verification.client.NFaceVerificationClient;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomPreferenceFragment extends PreferenceFragment {

    // ===========================================================
    // Private static fields
    // ===========================================================

    private static final String TAG = "CustomPreferenceFragment";

    // ===========================================================
    // Private fields
    // ===========================================================

    private ListView mListView;

    private List<String> mItems;

    // ===========================================================
    // Private constructor
    // ===========================================================

    public CustomPreferenceFragment() {
    }

    // ===========================================================
    // Public methods
    // ===========================================================



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.custom_pref_layout, container, false);
        final List<String> livenessActions = Arrays.asList(getResources().getStringArray(R.array.arr_liveness_actions));
        mItems = new ArrayList<>();
        initList();

        mListView = (ListView) view.findViewById(R.id.action_list);
        mListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mItems));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {
                final int addPosition = mItems.size() - 1;
                if (position == addPosition) {
//                    ListFragment.newInstance(livenessActions, true, null).show(getFragmentManager(), TAG);
                }
            }
        });

        final Button clearButton = (Button) view.findViewById(R.id.brn_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearItems();
            }
        });
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NFaceVerificationClient.getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SettingsFragment.KEY_PREF_LIVENESS_CUSTOM_ACTIONS, getActionsString());
        editor.commit();
    }

    public void addItem(final String item) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mItems.add(mItems.size() - 1, item);
                ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    public void clearItems() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mItems.clear();
                mItems.add(getResources().getString(R.string.msg_add_action));
                ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private String getActionsString() {
        if (mItems.size() <= 1) {
            return SettingsFragment.PREF_LIVENESS_CUSTOM_ACTIONS_DEFAULT_VALUE;
        } else {
            StringBuilder builder = new StringBuilder();
            // last mItems element is 'Add'
            for (int i = 0; i < mItems.size() - 1; i++) {
                builder.append(mItems.get(i));
                builder.append(',');
            }
            builder.deleteCharAt(builder.length() - 1); // remove trailing ','
            return builder.toString();
        }
    }

    private void initList() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NFaceVerificationClient.getContext());
        String actionsString = sharedPreferences.getString(SettingsFragment.KEY_PREF_LIVENESS_CUSTOM_ACTIONS, SettingsFragment.PREF_LIVENESS_CUSTOM_ACTIONS_DEFAULT_VALUE);
        if (!actionsString.equals(SettingsFragment.PREF_LIVENESS_CUSTOM_ACTIONS_DEFAULT_VALUE)) {
            String[] actions = actionsString.split(",");
            for (String a : actions) {
                mItems.add(a);
            }
        }
        mItems.add(getResources().getString(R.string.msg_add_action));
    }
}
