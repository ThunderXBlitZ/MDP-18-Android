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
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_android.Constants;
import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;
import com.example.mdp_android.bluetooth.BluetoothChatService;
import com.example.mdp_android.bluetooth.BluetoothManager;


import static com.example.mdp_android.bluetooth.BluetoothChatService.STATE_NONE;

public class CommFragment extends Fragment implements MainActivity.CallbackFragment {
    private static final String TAG = "CommFragment";
    private EditText msgOut;
    private EditText fn1String, fn2String;
    private Button sendBtn, updateBtn, fn1Btn, fn2Btn;
    private Button cancelBtn, saveBtn;
    private View popupInputDialogView;

    private BluetoothChatService mChatService = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private SharedPreferences mPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        mPreference = getActivity().getApplicationContext().getSharedPreferences("settings", 0);


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
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("settings", 0);
                        SharedPreferences.Editor prefEdit = prefs.edit();
                        prefEdit.putString("s1", fn1String.getText().toString());
                        prefEdit.putString("s2", fn2String.getText().toString());
                        prefEdit.commit();
                        alertDialog.dismiss();
                    }
                });
            }
        });
    }

    public void update(int type, String key, String msg){
        if(key != null && key.equals("MDF")){
            TextView msgIn = getView().findViewById(R.id.msgReceived);
            Log.d("MDF", String.valueOf(msgIn.getText()));
            msgIn.setText(msg+'\n'+msgIn.getText());
        }
    }

    private void setupChat() {
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getView();
                if (view != null) {
                    String message = msgOut.getText().toString();
                    if (BluetoothManager.getInstance().bluetoothAvailable() && BluetoothManager.getInstance().isConnected()) {
                            BluetoothManager.getInstance().sendMessage("MDF", message);
                            Toast.makeText(getActivity(), "Message Sent!", Toast.LENGTH_SHORT).show();
                        } else {
                            BluetoothManager.getInstance().notConnectedMsg();
                        }
                }
            }
        });

        //When user click on the Function 1 Button
        fn1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s1 = mPreference.getString("s1", null);
                if (BluetoothManager.getInstance().bluetoothAvailable() && BluetoothManager.getInstance().isConnected() && s1 != null) {
                    BluetoothManager.getInstance().sendMessage("MDF", s1);
                    Toast.makeText(getActivity(), "F1's String Sent!", Toast.LENGTH_SHORT).show();
                } else {
                    BluetoothManager.getInstance().notConnectedMsg();
                }
            }
        });

        //When user click on the Function 2 Button
        fn2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s2 = mPreference.getString("s2", null);
                if (BluetoothManager.getInstance().bluetoothAvailable() && BluetoothManager.getInstance().isConnected() && s2 != null) {
                    BluetoothManager.getInstance().sendMessage("MDF", s2);
                    Toast.makeText(getActivity(), "F2's String Sent!", Toast.LENGTH_SHORT).show();
                } else {
                    BluetoothManager.getInstance().notConnectedMsg();
                }
            }
        });
    }

    private void initPopupViewControls() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

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
        fn1String.setText(mPreference.getString("s1", null));
        fn2String.setText(mPreference.getString("s2", null));
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}


