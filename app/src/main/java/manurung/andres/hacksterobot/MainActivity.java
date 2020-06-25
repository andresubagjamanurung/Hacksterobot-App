package manurung.andres.hacksterobot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AddKamarFragment.DialogListener {
    String[] ID = {"", "", ""};
    String[] fire_listKamar = {"", "", ""};
    String[] fire_lokasi = {"", "", ""};
    String[] textstrCom = {"", "", ""};
    boolean[] listeningClicked = {false, false, false};     //check if firebase listener clicked (network image)
    boolean[] fragmentAccessedBy = {false, false, false};

    String[] arrlistKamarID1 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};     //N=20
    String[] arrlistKamarID2 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    String[] arrlistKamarID3 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    String[] commandID1 = {"", "", "", "", "", "", "", "", "", "", "0"};     //N=10 !!Array AKHIR selalu bernilai 0, menandakan destinasi akhir robot ke tempat awal
    String[] commandID2 = {"", "", "", "", "", "", "", "", "", "", "0"};
    String[] commandID3 = {"", "", "", "", "", "", "", "", "", "", "0"};

    int[] lenCommandIsi = {0, 0, 0};           //Mengetahui jumlah command ID1 yang berisi/bobot di commandID1
    int[] fire_battRobot = {0, 0, 0};
    boolean[] runOneTime = {false, false, false};
    boolean[] sendState = {true, true, true};
    boolean foundID = false;

    boolean connectedToInternet = false;

    DatabaseReference reff, reff_nonupdateID1, reff_nonupdateID2, reff_nonupdateID3;
    private DatabaseReference reffID1, reffID2, reffID3;
    ValueEventListener valistenerID1, valistenerID2;

    FloatingActionButton fab;

    LinearLayout layoutNoID, layoutUpdateInfo, layoutNewWIFI, layoutSelectDevice;
    Button addID, okID, buttonPairedDevice;
    EditText editID;
    CardView cardAddID, cardBluetoothWIFI, cardListPaired;

    CardView cardController1;
    TextView textID1, textCommandID1, textLokasiID1, textBattID1;
    Button addKamarID1, delKamarID1, buttonNotifyID1;
    ImageButton imgbutCommandID1, imgbutBattID1, imgbutCloseID1;
    ImageView imgviewWheelID1;
    Animation animWheelID1;

    CardView cardController2;
    TextView textID2, textCommandID2, textLokasiID2, textBattID2;
    Button addKamarID2, delKamarID2, buttonNotifyID2;
    ImageButton imgbutCommandID2, imgbutBattID2, imgbutCloseID2;
    ImageView imgviewWheelID2;
    Animation animWheelID2;

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView listPairedDevice;
    private EditText editNewSSID, editNewPassword;

    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status


    public boolean terhubungInternet() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    //Fragmen untuk menambah kamar baru
    @Override
    public void onFinishEditDialog(String inputNewKamar) {
        if (fragmentAccessedBy[0]) {                      //Jika fragmen "new room" diakses oleh ID 1
            if (!inputNewKamar.equals("")) {
                String newListKamar;
                if (fire_listKamar[0].equals("")) {      //Bila listKamar masih kosong
                    newListKamar = inputNewKamar;
                } else {
                    newListKamar = fire_listKamar[0] + "," + inputNewKamar;
                }
                FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[0]).child("listRoom").setValue(newListKamar);
                Toast.makeText(this, "Room has been added", Toast.LENGTH_SHORT).show();
            }
            fragmentAccessedBy[0] = false;
        } else if (fragmentAccessedBy[1]) {             //Jika fragmen "new room" diakses oleh ID 2
            if (!inputNewKamar.equals("")) {
                String newListKamar;
                if (fire_listKamar[1].equals("")) {      //Bila listKamar masih kosong
                    newListKamar = inputNewKamar;
                } else {
                    newListKamar = fire_listKamar[1] + "," + inputNewKamar;
                }
                FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[1]).child("listRoom").setValue(newListKamar);
                Toast.makeText(this, "Room has been added", Toast.LENGTH_SHORT).show();
            }
            fragmentAccessedBy[1] = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addKamarID1 = findViewById(R.id.button_addCommandID1);
        delKamarID1 = findViewById(R.id.button_delCommandID1);
        buttonNotifyID1 = findViewById(R.id.button_notifyID1);
        imgbutCommandID1 = findViewById(R.id.imgButton_commandID1);
        cardBluetoothWIFI = findViewById(R.id.card_new_wifi);
        layoutNewWIFI = findViewById(R.id.layout_new_wifi);
        cardListPaired = findViewById(R.id.card_list_paired_device);
        editNewSSID = findViewById(R.id.edit_new_ssid);
        editNewPassword = findViewById(R.id.edit_new_password);

        layoutUpdateInfo = findViewById(R.id.update_info_layout);
        cardController1 = findViewById(R.id.card_controller1);
        cardController2 = findViewById(R.id.card_controller2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            Drawable background = getResources().getDrawable(R.drawable.main_gradient); //bg_gradient is your gradient.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }

        //Berikan dialog untuk memastikan pengguna telah terhubung dengan koneksi internet
        connectedToInternet = terhubungInternet();
        if (!connectedToInternet) {
            new AlertDialog.Builder(this)
                    .setTitle("Tidak ada koneksi internet")
                    .setMessage("Aplikasi tidak terhubung dengan internet atau tidak mendapat izin untuk mengakses internet")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(R.drawable.caution_internet)
                    .show();
        }//Jika pengguna tidak menekan OK dan tetap masuk ke halaman utama aplikasi, menu_settings telah diatur Enabled=false sehingga pengguna tidak dapat melakukan apapun

        //Toolbar digunakan untuk mengirimkan ssid dan password untuk koneksi WIFI baru pada robot
        Toolbar toolbar = findViewById(R.id.toolbar);
        cardAddID = findViewById(R.id.card_addID);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.idLayout), "Are you sure to change the WIFI connection of the robot ?", Snackbar.LENGTH_LONG)
                        .setAction("OKAY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "Sending new SSID and Password...", Toast.LENGTH_SHORT).show();
                                String ssidpass = editNewSSID.getText().toString() + "\n" + editNewPassword.getText().toString();
                                mConnectedThread.write(ssidpass);
                            }
                        });
                snackbar.show();
            }
        });

        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        listPairedDevice = findViewById(R.id.listview_paired_device);
        listPairedDevice.setAdapter(mBTArrayAdapter); // assign model to view
        listPairedDevice.setOnItemClickListener(mDeviceClickListener);

        //----------------------------------- Bluetooth --------------------------------------------------
        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    String message = "Connected to: " + (String) (msg.obj);
                    if (msg.arg1 == 1) {
                        cardBluetoothWIFI.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "Bluetooth device could not be found", Toast.LENGTH_SHORT).show();
        } else {
            // !!! Fungsi dapat dilakukan disini !!!

        }
        //-----------------------------------------------------------------------------------------------------------------
    }

    //---------------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------- BLuetooth Settings -------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                cardListPaired.setVisibility(View.VISIBLE);
                listPairedDevices();
            } else {
                Toast.makeText(this, "Please give permissions to activate bluetooth connection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void bluetoothTurnStatus() {
        if (cardListPaired.getVisibility() == View.GONE) {   //menyalakan bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(this, "Activating Bluetooth...", Toast.LENGTH_LONG).show();
        } else if (cardListPaired.getVisibility() == View.VISIBLE) {  //mematikan bluetooth
            Toast.makeText(getApplicationContext(), "Deactivating Bluetooth...", Toast.LENGTH_LONG).show();
            mBTAdapter.disable();
            fab.setVisibility(View.GONE);
            cardListPaired.setVisibility(View.GONE);
            cardBluetoothWIFI.setVisibility(View.GONE);
            layoutAfterBluetooth();
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices() {
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        } else {
            Toast.makeText(getApplicationContext(), "Please activate bluetooth first", Toast.LENGTH_SHORT).show();
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if (!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Please activate bluetooth first", Toast.LENGTH_SHORT).show();
                return;
            }
            // Get the device MAC address, which is the last 17 chars in the View
            Toast.makeText(MainActivity.this, "Connecting...", Toast.LENGTH_LONG).show();
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0, info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread() {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
    //------------------------------------------------ Blutooth Settings --------------------------------------------------------
    //---------------------------------------------------------------------------------------------------------------------------

    //Perubahan layout (menutup kontroler dan membuka layout BLEWIFI) saat ingin membuka card BluetoothWIFI
    private void layoutAfterBluetooth() {
        if (!ID[0].equals("")) {
            layoutUpdateInfo.setVisibility(View.VISIBLE);
            cardController1.setVisibility(View.VISIBLE);
        }
        if (!ID[1].equals("")) {
            layoutUpdateInfo.setVisibility(View.VISIBLE);
            cardController2.setVisibility(View.VISIBLE);
        }
        if (!ID[2].equals("")) ; // isi untuk cardController3
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //Menampilkan adder id robot
        if (id == R.id.add_robot && connectedToInternet) {
            if (mBTAdapter.isEnabled()) {
                mBTAdapter.disable();
                fab.setVisibility(View.GONE);
                if (cardListPaired.getVisibility() == View.VISIBLE) {
                    Toast.makeText(getApplicationContext(), "Deactivating Bluetooth...", Toast.LENGTH_LONG).show();
                }
                cardListPaired.setVisibility(View.GONE);
                cardBluetoothWIFI.setVisibility(View.GONE);
            }

            layoutAfterBluetooth();

            if (cardAddID.getVisibility() == View.GONE) {
                cardAddID.setVisibility(View.VISIBLE);
            } else {
                cardAddID.setVisibility(View.GONE);
            }
            return true;
        } else if (id == R.id.bluetooth_password && connectedToInternet) {
            layoutUpdateInfo.setVisibility(View.GONE);
            cardController1.setVisibility(View.GONE);
            cardController2.setVisibility(View.GONE);
            cardAddID.setVisibility(View.GONE);
            bluetoothTurnStatus();
            return true;
        } else {
            //Mencegah pengguna untuk menggunakan aplikasi saat masih tidak terhubung internet
            new AlertDialog.Builder(this)
                    .setTitle("Tidak ada koneksi internet")
                    .setMessage("Aplikasi tidak terhubung dengan internet atau tidak mendapat izin untuk mengakses internet")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(R.drawable.caution_internet)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

    @Override
    protected void onResume() {
        super.onResume();

        layoutUpdateInfo = findViewById(R.id.update_info_layout);
        layoutNoID = findViewById(R.id.layout_noID);
        editID = findViewById(R.id.edit_addID);
        addID = findViewById(R.id.button_addID);
        okID = findViewById(R.id.button_okID);
        cardAddID = findViewById(R.id.card_addID);

        cardController1 = findViewById(R.id.card_controller1);
        addKamarID1 = findViewById(R.id.button_addCommandID1);
        delKamarID1 = findViewById(R.id.button_delCommandID1);
        buttonNotifyID1 = findViewById(R.id.button_notifyID1);
        textID1 = findViewById(R.id.text_ID1);
        textCommandID1 = findViewById(R.id.text_commandID1);
        textLokasiID1 = findViewById(R.id.text_lokasiID1);
        textBattID1 = findViewById(R.id.text_battID1);
        imgbutCommandID1 = findViewById(R.id.imgButton_commandID1);
        imgbutBattID1 = findViewById(R.id.imgButton_battID1);
        imgbutCloseID1 = findViewById(R.id.imgButton_closeID1);
        imgviewWheelID1 = findViewById(R.id.imgview_wheelID1);

        cardController2 = findViewById(R.id.card_controller2);
        addKamarID2 = findViewById(R.id.button_addCommandID2);
        delKamarID2 = findViewById(R.id.button_delCommandID2);
        buttonNotifyID2 = findViewById(R.id.button_notifyID2);
        textID2 = findViewById(R.id.text_ID2);
        textCommandID2 = findViewById(R.id.text_commandID2);
        textLokasiID2 = findViewById(R.id.text_lokasiID2);
        textBattID2 = findViewById(R.id.text_battID2);
        imgbutCommandID2 = findViewById(R.id.imgButton_commandID2);
        imgbutBattID2 = findViewById(R.id.imgButton_battID2);
        imgbutCloseID2 = findViewById(R.id.imgButton_closeID2);
        imgviewWheelID2 = findViewById(R.id.imgview_wheelID2);

        // !!! Note : Aplikasi menganggap robot selalu online (Ini karena masalah pada robot yang memiliki delay selama 300ms saat mengupdate nilai)

        //---------------------------------------------------------------------------------------------------------------------------------------------------------
        //--------------------------------------------- Menambahkan id robot untuk untuk dapat dikontrol ----------------------------------------------------------
        addID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reff_nonupdateID1 = FirebaseDatabase.getInstance().getReference().child("ID_Robot").child("list_ID");
                reff_nonupdateID1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int number_id = Integer.parseInt(dataSnapshot.child("numbers").getValue().toString());
                        for (int i = 1; i <= number_id; i++) {
                            String id_by_number = dataSnapshot.child(Integer.toString(i)).getValue().toString();
                            if (id_by_number.equals(editID.getText().toString())) {
                                layoutUpdateInfo.setVisibility(View.VISIBLE);
                                if (ID[0].equals("")) {
                                    ID[0] = id_by_number;
                                    cardController1.setVisibility(View.VISIBLE);
                                    layoutNoID.setVisibility(View.INVISIBLE);
                                    textID1.setText(ID[0]);
                                    foundID = true;
                                } else if (ID[1].equals("")) {
                                    ID[1] = id_by_number;
                                    cardController2.setVisibility(View.VISIBLE);
                                    layoutNoID.setVisibility(View.INVISIBLE);
                                    textID2.setText(ID[1]);
                                    foundID = true;
                                } else if (ID[2].equals("")) {
                                    ID[2] = id_by_number;
                                } else {
                                    foundID = false;
                                }
                            }
                        }
                        if (!foundID) {
                            Toast.makeText(MainActivity.this, "Robot ID could not be found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Database Error. Please wait for a moment", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
        okID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardAddID.setVisibility(View.GONE);
            }
        });
        //--------------------------------------------------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------------------------------------------------------------------

//1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111
//1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111
        //Mendapatkan data update dari robot secara live
        imgbutBattID1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listeningClicked[0] = true;
                reffID1 = FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[0]);
                valistenerID1 = reffID1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        reffID1 = FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[0]);
                        String old_listKamar = fire_listKamar[0];
                        fire_listKamar[0] = dataSnapshot.child("listRoom").getValue().toString();
                        fire_battRobot[0] = Integer.parseInt(dataSnapshot.child("battRobot").getValue().toString());
                        fire_lokasi[0] = dataSnapshot.child("locNow").getValue().toString();

                        if (!runOneTime[0]) {
                            String idStr = "Robot with ID = " + ID[0] + " is Online";
                            Toast.makeText(MainActivity.this, idStr, Toast.LENGTH_SHORT).show();

                            buttonNotifyID1.setText("Notify !");
                            buttonNotifyID1.setEnabled(true);
                            buttonNotifyID1.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            Drawable topStatus = getResources().getDrawable(R.drawable.online);
                            buttonNotifyID1.setCompoundDrawablesRelativeWithIntrinsicBounds(null, topStatus, null, null);

                            imgbutCommandID1.setBackgroundResource(R.drawable.send_command);
                            imgbutCommandID1.setEnabled(true);
                            addKamarID1.setEnabled(true);
                            delKamarID1.setEnabled(true);
                            addKamarID1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                            delKamarID1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));

                            String str = fire_listKamar[0];     //List kamar yang dapat dilayani robot
                            arrlistKamarID1 = str.split(",", -1);
                            runOneTime[0] = true;
                        }

                        //Bila terjadi perubahan pada list Kamar
                        if (!old_listKamar.equals(fire_listKamar[0])) {
                            String str = fire_listKamar[0];     //List kamar yang dapat dilayani robot
                            arrlistKamarID1 = str.split(",", -1);
                        }

                        textLokasiID1.setText(fire_lokasi[0]);
                        String battpercentage = fire_battRobot[0] + " %";
                        textBattID1.setText(battpercentage);
                        if (fire_battRobot[0] <= 15) {
                            imgbutBattID1.setImageResource(R.drawable.ic_batt_0);
                            textBattID1.setTextColor(getResources().getColor(R.color.red));
                            Toast.makeText(MainActivity.this, "Battery is very low !!!", Toast.LENGTH_LONG).show();
                        } else if (fire_battRobot[0] <= 30) {
                            imgbutBattID1.setImageResource(R.drawable.ic_batt_30);
                            textBattID1.setTextColor(getResources().getColor(R.color.red));
                            Toast.makeText(MainActivity.this, "Battery is very low !!!", Toast.LENGTH_SHORT).show();
                        } else if (fire_battRobot[0] <= 60) {
                            textBattID2.setTextColor(Color.parseColor("#808080"));
                            imgbutBattID1.setImageResource(R.drawable.ic_batt_60);
                        } else if (fire_battRobot[0] <= 80) {
                            textBattID2.setTextColor(Color.parseColor("#808080"));
                            imgbutBattID1.setImageResource(R.drawable.ic_batt_80);
                        } else if (fire_battRobot[0] <= 100) {
                            textBattID2.setTextColor(Color.parseColor("#808080"));
                            imgbutBattID1.setImageResource(R.drawable.ic_batt_100);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Database Error. Please wait for a moment", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });

        //Menambahkan command kamar yang akan dikirimkan ke robot
        addKamarID1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, addKamarID1);
                if (!arrlistKamarID1[0].equals("")) {        //Mencegah agar array kosong tidak dituliskan ke pop up menu
                    for (int i = 0; i < arrlistKamarID1.length; i++) {
                        popupMenu.getMenu().add(0, Menu.FIRST, i, arrlistKamarID1[i]);
                    }
                }
                popupMenu.getMenu().add(0, Menu.FIRST, arrlistKamarID1.length, "New Room");
                popupMenu.getMenu().add(0, Menu.FIRST, arrlistKamarID1.length + 1, "Delete All Rooms");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().toString().equals("New Room")) {          //Apabila "New Room" diklik
                            fragmentAccessedBy[0] = true;                           //Ini menunjukkan bahwa fragmen "new room" diakses dari ID 1
                            AddKamarFragment dialogFragment = new AddKamarFragment();
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                            Bundle bundle = new Bundle();
                            bundle.putBoolean("notAlertDialog", true);
                            dialogFragment.setArguments(bundle);

                            ft = getSupportFragmentManager().beginTransaction();
                            Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);
                            dialogFragment.show(ft, "dialog");
                        } else if (item.getTitle().toString().equals("Delete All Rooms")) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Delete all rooms listed")
                                    .setMessage("Rooms listed will be deleted, are you sure to delete all of rooms ?")
                                    .setPositiveButton("sure", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[0]).child("listRoom").setValue("");
                                            Toast.makeText(MainActivity.this, "All rooms listed have been deleted", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setIcon(R.drawable.caution_internet)
                                    .show();
                        } else if (lenCommandIsi[0] < commandID1.length - 1) {
                            lenCommandIsi[0]++;
                            commandID1[lenCommandIsi[0] - 1] = item.getTitle().toString();
                            if (lenCommandIsi[0] == 1) {
                                textstrCom[0] = commandID1[lenCommandIsi[0] - 1];
                            } else {
                                textstrCom[0] = textstrCom[0] + " -> " + commandID1[lenCommandIsi[0] - 1];
                            }
                            String str = "start -> " + textstrCom[0] + " -> finish";
                            textCommandID1.setText(str);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        //Menghapus command kamar
        delKamarID1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < commandID1.length - 1; i++) {
                    commandID1[i] = "";
                }
                lenCommandIsi[0] = 0;
                textstrCom[0] = "";
                textCommandID1.setText("start");
            }
        });

        //Memberikan command yang telah diisi kepada robot
        imgbutCommandID1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendState[0]) {     //Apakah bersiap mengirim command kepada robot
                    String str = "";
                    str = commandID1[0];
                    for (int i = 1; i < commandID1.length; i++) {   //Mengirim data hingga "0" (komponen terakhir)
                        if (!commandID1[i].equals("")) {
                            str += "," + commandID1[i];
                        }
                    }
                    if (lenCommandIsi[0] == 0) {
                        str = "0";
                    }
                    textstrCom[0] = "";
                    FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[0]).child("commandRoom").setValue(str);
                    imgbutCommandID1.setBackgroundResource(R.drawable.cancel_command);

                    //Animasi wheel---------------------------------------------------------------------------
                    imgviewWheelID1.setVisibility(View.VISIBLE);
                    animWheelID1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_wheel);
                    imgviewWheelID1.startAnimation(animWheelID1);
                    //-----------------------------------------------------------------------------------------
                    sendState[0] = false;
                } else {      //Batalkan pemberian command kepada robot
                    for (int i = 0; i < lenCommandIsi[0]; i++) {
                        commandID1[i] = "";
                    }
                    FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[0]).child("commandRoom").setValue("0");
                    textstrCom[0] = "";
                    lenCommandIsi[0] = 0;
                    textCommandID1.setText("start");
                    imgbutCommandID1.setBackgroundResource(R.drawable.send_command);

                    imgviewWheelID1.setVisibility(View.INVISIBLE);
                    imgviewWheelID1.setAnimation(null);
                    sendState[0] = true;
                }
            }
        });

        buttonNotifyID1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reffID1 = FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[0]);
                reffID1.child("notifyRobot").setValue(true);
                Toast.makeText(MainActivity.this, "Sending notification to robot...", Toast.LENGTH_SHORT).show();
            }
        });

        imgbutCloseID1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ID[0] = "";
                if (listeningClicked[0]) {
                    reffID1.removeEventListener(valistenerID1);         //Menghapus listener ID[0]
                }
                runOneTime[0] = false;
                addKamarID1.setEnabled(false);
                delKamarID1.setEnabled(false);
                buttonNotifyID1.setEnabled(false);
                imgbutCommandID1.setEnabled(false);

                textBattID1.setTextColor(getResources().getColor(R.color.grey));
                buttonNotifyID1.setText(R.string.offline_robot);
                buttonNotifyID1.setTextColor(getResources().getColor(R.color.grey));
                imgbutCommandID1.setBackgroundResource(R.drawable.send_command_grey);
                addKamarID1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                delKamarID1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));

                textBattID1.setText("N/A %");
                Drawable topStatus = getResources().getDrawable(R.drawable.offline);
                buttonNotifyID1.setCompoundDrawablesRelativeWithIntrinsicBounds(null, topStatus, null, null);

                imgbutBattID1.setImageResource(R.drawable.robot_offline);
                textID1.setText(R.string.no_id_robot);
                textLokasiID1.setText(R.string.no_location_robot);

                //Menghapus semua data command kamar
                for (int i = 0; i < commandID1.length - 1; i++) {
                    commandID1[i] = "";
                }
                for (int i = 0; i < arrlistKamarID1.length; i++) {
                    arrlistKamarID1[i] = "";
                }
                lenCommandIsi[0] = 0;
                textstrCom[0] = "";
                textCommandID1.setText("start");
                imgviewWheelID1.setAnimation(null);
                cardController1.setVisibility(View.GONE);
                layoutNoID.setVisibility(View.VISIBLE);
                imgviewWheelID1.setVisibility(View.INVISIBLE);
            }
        });

        //--------------------------------------------------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------------------------------------------------------------------


//222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222
//222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222
        //---------------------------------------------------------------------------------------------------------------------------------------------------------
        //--------------------------------------------- Fungsi OnClickListener pada Kontroler ID 2 ----------------------------------------------------------------
        imgbutBattID2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listeningClicked[1] = true;
                reffID2 = FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[1]);
                valistenerID2 = reffID2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        reffID2 = FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[1]);
                        String old_listKamar = fire_listKamar[1];
                        fire_listKamar[1] = dataSnapshot.child("listRoom").getValue().toString();
                        fire_battRobot[1] = Integer.parseInt(dataSnapshot.child("battRobot").getValue().toString());
                        fire_lokasi[1] = dataSnapshot.child("locNow").getValue().toString();

                        if (!runOneTime[1]) {
                            String idStr = "Robot with ID = " + ID[1] + " is Online";
                            Toast.makeText(MainActivity.this, idStr, Toast.LENGTH_SHORT).show();

                            buttonNotifyID2.setText("Notify !");
                            buttonNotifyID2.setEnabled(true);
                            buttonNotifyID2.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            Drawable topStatus = getResources().getDrawable(R.drawable.online);
                            buttonNotifyID2.setCompoundDrawablesRelativeWithIntrinsicBounds(null, topStatus, null, null);

                            imgbutCommandID2.setBackgroundResource(R.drawable.send_command);
                            imgbutCommandID2.setEnabled(true);
                            addKamarID2.setEnabled(true);
                            delKamarID2.setEnabled(true);
                            addKamarID2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                            delKamarID2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));

                            String str = fire_listKamar[1];     //List kamar yang dapat dilayani robot
                            arrlistKamarID2 = str.split(",", -1);
                            runOneTime[1] = true;
                        }

                        //Bila terjadi perubahan pada list Kamar
                        if (!old_listKamar.equals(fire_listKamar[1])) {
                            String str = fire_listKamar[1];     //List kamar yang dapat dilayani robot
                            arrlistKamarID2 = str.split(",", -1);
                        }

                        textLokasiID2.setText(fire_lokasi[1]);
                        String battpercentage = fire_battRobot[1] + " %";
                        textBattID2.setText(battpercentage);
                        if (fire_battRobot[1] <= 15) {
                            imgbutBattID2.setImageResource(R.drawable.ic_batt_0);
                            textBattID2.setTextColor(getResources().getColor(R.color.red));
                            Toast.makeText(MainActivity.this, "Battery is very low !!!", Toast.LENGTH_LONG).show();
                        } else if (fire_battRobot[1] <= 30) {
                            imgbutBattID2.setImageResource(R.drawable.ic_batt_30);
                            textBattID2.setTextColor(getResources().getColor(R.color.red));
                            Toast.makeText(MainActivity.this, "Battery is very low !", Toast.LENGTH_SHORT).show();
                        } else if (fire_battRobot[1] <= 60) {
                            textBattID2.setTextColor(Color.parseColor("#808080"));
                            imgbutBattID2.setImageResource(R.drawable.ic_batt_60);
                        } else if (fire_battRobot[1] <= 80) {
                            textBattID2.setTextColor(Color.parseColor("#808080"));
                            imgbutBattID2.setImageResource(R.drawable.ic_batt_80);
                        } else if (fire_battRobot[1] <= 100) {
                            textBattID2.setTextColor(Color.parseColor("#808080"));
                            imgbutBattID2.setImageResource(R.drawable.ic_batt_100);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        addKamarID2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, addKamarID2);
                if (!arrlistKamarID2[0].equals("")) {        //Mencegah agar array kosong tidak dituliskan ke pop up menu
                    for (int i = 0; i < arrlistKamarID2.length; i++) {
                        popupMenu.getMenu().add(0, Menu.FIRST, i, arrlistKamarID2[i]);
                    }
                }
                popupMenu.getMenu().add(0, Menu.FIRST, arrlistKamarID2.length, "New Room");
                popupMenu.getMenu().add(0, Menu.FIRST, arrlistKamarID2.length + 1, "Delete All Rooms");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().toString().equals("New Room")) {          //Apabila "New Room" diklik
                            fragmentAccessedBy[1] = true;                           //Ini menunjukkan bahwa fragmen "new room" diakses dari ID 2
                            AddKamarFragment dialogFragment = new AddKamarFragment();
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                            Bundle bundle = new Bundle();
                            bundle.putBoolean("notAlertDialog", true);
                            dialogFragment.setArguments(bundle);

                            ft = getSupportFragmentManager().beginTransaction();
                            Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                            if (prev != null) {
                                ft.remove(prev);
                            }
                            ft.addToBackStack(null);
                            dialogFragment.show(ft, "dialog");
                        } else if (item.getTitle().toString().equals("Delete All Rooms")) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Delete all rooms listed")
                                    .setMessage("Rooms listed will be deleted, are you sure to delete all of rooms ?")
                                    .setPositiveButton("sure", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[1]).child("listRoom").setValue("");
                                            Toast.makeText(MainActivity.this, "All rooms listed have been deleted", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .setIcon(R.drawable.caution_internet)
                                    .show();
                        } else if (lenCommandIsi[1] <= commandID2.length - 2) {
                            lenCommandIsi[1]++;
                            commandID2[lenCommandIsi[1] - 1] = item.getTitle().toString();
                            if (lenCommandIsi[1] == 1) {
                                textstrCom[1] = commandID2[lenCommandIsi[1] - 1];
                            } else {
                                textstrCom[1] = textstrCom[1] + " -> " + commandID2[lenCommandIsi[1] - 1];
                            }
                            String str = "start -> " + textstrCom[1] + " -> finish";
                            textCommandID2.setText(str);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        //Menghapus command kamar
        delKamarID2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < commandID2.length - 1; i++) {
                    commandID2[i] = "";
                }
                lenCommandIsi[1] = 0;
                textstrCom[1] = "";
                textCommandID2.setText("start");
            }
        });

        //Memberikan command yang telah diisi kepada robot
        imgbutCommandID2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendState[1]) {    //Kirim command kepada robot
                    String str = "";
                    str = commandID2[0];
                    for (int i = 1; i < commandID2.length; i++) {   //Mengirim data hingga "0" (komponen terakhir)
                        if (!commandID2[i].equals("")) {
                            str += "," + commandID2[i];
                        }
                    }
                    if (lenCommandIsi[1] == 0) {
                        str = "0";
                    }
                    textstrCom[1] = "";
                    FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[1]).child("commandRoom").setValue(str);
                    imgbutCommandID2.setBackgroundResource(R.drawable.cancel_command);

                    //Animasi wheel---------------------------------------------------------------------------
                    imgviewWheelID2.setVisibility(View.VISIBLE);
                    animWheelID2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_wheel);
                    imgviewWheelID2.startAnimation(animWheelID2);
                    //-----------------------------------------------------------------------------------------

                    sendState[1] = false;
                } else {      //Batalkan pemberian command kepada robot
                    for (int i = 0; i < lenCommandIsi[1]; i++) {
                        commandID2[i] = "";
                    }
                    FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[1]).child("commandRoom").setValue("0");
                    textstrCom[1] = "";
                    lenCommandIsi[1] = 0;
                    textCommandID2.setText("start");
                    imgbutCommandID2.setBackgroundResource(R.drawable.send_command);

                    imgviewWheelID2.setVisibility(View.INVISIBLE);
                    imgviewWheelID2.setAnimation(null);
                    sendState[1] = true;
                }
            }
        });

        buttonNotifyID2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reffID2 = FirebaseDatabase.getInstance().getReference().child("ID_Robot").child(ID[1]);
                reffID2.child("notifyRobot").setValue(true);
                Toast.makeText(MainActivity.this, "Sending notification to robot...", Toast.LENGTH_SHORT).show();
            }
        });

        imgbutCloseID2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ID[1] = "";
                if (listeningClicked[1]) {
                    reffID2.removeEventListener(valistenerID2);         //Menghapus listener ID[1]
                }
                runOneTime[1] = false;
                addKamarID2.setEnabled(false);
                delKamarID2.setEnabled(false);
                buttonNotifyID2.setEnabled(false);
                imgbutCommandID2.setEnabled(false);

                textBattID2.setTextColor(getResources().getColor(R.color.grey));
                buttonNotifyID2.setText(R.string.offline_robot);
                buttonNotifyID2.setTextColor(getResources().getColor(R.color.grey));
                imgbutCommandID2.setBackgroundResource(R.drawable.send_command_grey);
                addKamarID2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                delKamarID2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));

                textBattID2.setText("N/A %");
                Drawable topStatus = getResources().getDrawable(R.drawable.offline);
                buttonNotifyID2.setCompoundDrawablesRelativeWithIntrinsicBounds(null, topStatus, null, null);

                imgbutBattID2.setImageResource(R.drawable.robot_offline);
                textID2.setText(R.string.no_id_robot);
                textLokasiID2.setText(R.string.no_location_robot);

                //Menghapus semua data command kamar
                for (int i = 0; i < commandID2.length - 1; i++) {
                    commandID2[i] = "";
                }
                for (int i = 0; i < arrlistKamarID2.length; i++) {
                    arrlistKamarID2[i] = "";
                }
                lenCommandIsi[1] = 0;
                textstrCom[1] = "";
                textCommandID2.setText("start");
                imgviewWheelID2.setAnimation(null);
                cardController2.setVisibility(View.GONE);
                layoutNoID.setVisibility(View.VISIBLE);
                imgviewWheelID2.setVisibility(View.INVISIBLE);
            }
        });


        //--------------------------------------------------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------------------------------------------------------------------

    }

//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
//|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

    @Override
    protected void onStop() {
        super.onStop();
    }
}
