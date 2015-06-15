package com.example.administrator.tcpclient;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ConfigureFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private EditText etip,etport;
    private Button btnConfirm;

    public static ConfigureFragment newInstance() {
        ConfigureFragment fragment = new ConfigureFragment();
        return fragment;
    }

    public ConfigureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_configure, container, false);
        etip = (EditText)mainView.findViewById(R.id.etIP);
        etport = (EditText)mainView.findViewById(R.id.etPort);
        btnConfirm = (Button)mainView.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener!=null){
                    mListener.onConfirm(etip.getText().toString(),etport.getText().toString());
                }
            }
        });
        return mainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onConfirm(String ip,String port);
    }

}
