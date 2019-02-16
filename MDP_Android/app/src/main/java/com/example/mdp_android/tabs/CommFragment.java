package com.example.mdp_android.tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;
import com.example.mdp_android.bluetooth.BluetoothChatService;
import com.example.mdp_android.bluetooth.BluetoothManager;


import static com.example.mdp_android.bluetooth.BluetoothChatService.STATE_NONE;

public class CommFragment extends Fragment {
    private static final String TAG = "CommFragment";
    private EditText msgOut;
    private EditText fn1String, fn2String;
    private Button sendBtn, updateBtn, fn1Btn, fn2Btn;
    private Button cancelBtn, saveBtn;
    private View  popupInputDialogView;

    private BluetoothChatService mChatService = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private SharedPreferences mPreference;
    private SharedPreferences.Editor mEditor;


    private static final int REQUEST_ENABLE_BT = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.activity_communication, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Variables
        msgOut = view.findViewById(R.id.editText);
        sendBtn = view.findViewById(R.id.sendMsgBtn);
        updateBtn = view.findViewById(R.id.msgUpdateBtn);
        fn1Btn = view.findViewById(R.id.fn1Btn);
        fn2Btn = view.findViewById(R.id.fn2Btn);
        popupInputDialogView = getLayoutInflater().inflate(R.layout.activity_communication, null);

        //Preference
        mPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPreference = getActivity().getApplicationContext().getSharedPreferences("settings",0);


        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }
            });
        }

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create a AlertDialog Builder.
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                // alertDialogBuilder.setTitle("Test");
                alertDialogBuilder.setCancelable(true);

                // Init popup dialog view and it's ui controls.
                initPopupViewControls();

                // Set the inflated layout view object to the AlertDialog builder.
                alertDialogBuilder.setView(popupInputDialogView);

                // Create AlertDialog and show.
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                // When user click the cancel button in the popup dialog.
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                // When user click the save button in the popup dialog.
                saveBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        SharedPref(fn1String.getText().toString(), fn2String.getText().toString());
                        alertDialog.dismiss();
                    }
                });
            }
        });
    }


    public void update(String type, String msg) {
        switch ("type") {
            case "Bluetooth":
                break;
            case "1": // Constants.MESSAGE_STATE_CHANGE
                break;
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        //When user clicks on Send Button
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getView();

                if (null != view) {
                    String message = msgOut.getText().toString();
                    BluetoothManager.getInstance().sendMessage(message);
                    //Toast message to notify user message has been sent
                    Toast.makeText(getActivity(), "Message Sent!" , Toast.LENGTH_SHORT).show();
                }
            }
        });

        //When user click on the Function 1 Button
        fn1Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                    BluetoothManager.getInstance().sendMessage(mPreference.getString("s1", null));
                    //Toast message to notify user message has been sent
                    Toast.makeText(getActivity(), "F1's String Sent!", Toast.LENGTH_SHORT).show();

            }
        });

        //When user click on the Function 2 Button
        fn2Btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                BluetoothManager.getInstance().sendMessage(mPreference.getString("s2",null));
                //Toast message to notify user message has been sent
                Toast.makeText(getActivity(), "F2's String Sent!" , Toast.LENGTH_SHORT).show();

            }
        });



    }

    private void initMainActivityControls()
    {
        if(updateBtn == null)
        {
            updateBtn = getActivity().findViewById(R.id.msgUpdateBtn);
        }


    }

    private void initPopupViewControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        // Inflate the popup dialog from a layout xml file.
        popupInputDialogView = layoutInflater.inflate(R.layout.activity_popupdialog, null);


        // Get user input Edittext and button
        fn1String = popupInputDialogView.findViewById(R.id.fn1Edit);
        fn2String = popupInputDialogView.findViewById(R.id.fn2Edit);
        saveBtn = popupInputDialogView.findViewById(R.id.saveBtn);
        cancelBtn = popupInputDialogView.findViewById(R.id.cancelBtn);

        fn1String.setEnabled(true);
        fn2String.setEnabled(true);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);

        // Display values from sharedPreference
        fn1String.setText(mPreference.getString("s1",null));
        fn2String.setText(mPreference.getString("s2",null));

    }


    public void SharedPref(String Fn1,String Fn2){
        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("settings", 0);
        SharedPreferences.Editor prefEdit = prefs.edit();
        prefEdit.putString("s1", Fn1);
        prefEdit.putString("s2", Fn2);
        prefEdit.commit();
    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}


