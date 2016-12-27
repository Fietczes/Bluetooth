package pl.fietczes.bluetooth;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;




import java.io.IOException;//obsluga wyjatkow
import java.io.InputStream;// strumien wejsciowy
import java.io.OutputStream;//strumien wyjsciowy
import java.lang.reflect.Method;//nie wiem
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;//UUID kanał komunikacji

import layout.fragment1;
import layout.fragnemt2;
import pl.fietczes.bluetooth.R;
import android.R.layout;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;//clasa obrazujaca parametry orzadzenia bluetooth
import android.bluetooth.BluetoothDevice;//urzadzenie zewnetrzne bluetooth
import android.bluetooth.BluetoothSocket;//kanal bluetooth
import android.content.Intent;//Intencja czyli jak dla mnie zobrazowananie
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;//nasłuchiwacz użycia elementutu
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "bluetooth2";
    int i;
    float t1;
    Button btnOn, btnOff, wyslij, kury, gesi, Ustaw_gesi,Lista,Polancz;
    TextView txtArduino;
    Handler h;
    EditText tekst_do_wyslania;
    Spinner spinner,spinner2;//Lista rozwijana

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler !!!!!!!!!!!!!!!!!! To trzeba sprawdzic co oznacza
    private BluetoothAdapter btAdapter = null;//Utworzenie  reprezentacji bluetooth na urzadzenu
    private BluetoothSocket btSocket = null;//utworzenie kanalu odpowiadajacego za nadawanie i odbieranie
    private StringBuilder sb = new StringBuilder();

    ListAdapter adapterek_listy;
    ArrayAdapter<String> adapter;


    private ConnectedThread mConnectedThread;//To chyba jest klasa która rozszerza główny wontek


    // SPP UUID servicec
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private String address = "98:D3:33:80:9F:09";//to jest MAC mojego urzadzenia
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * Called when the activity is first created.
     */

    /*Deklaracje potrzebne do ciaglego przepytywania*/
    private int mInterval = 10000; // 10 seconds by default, can be changed later
    private Handler mHandler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnOn = (Button) findViewById(R.id.btOn);                  // button LED ON
        btnOff = (Button) findViewById(R.id.btOff);                // button LED OFF
        kury = (Button) findViewById(R.id.Kury_button);                  // button LED ON
        gesi = (Button) findViewById(R.id.Gesi_button);
        Ustaw_gesi = (Button) findViewById(R.id.ustawienie1);
        txtArduino = (TextView) findViewById(R.id.textView);      // for display the received data from the Arduino
        wyslij = (Button) findViewById(R.id.wyslij);
        Lista = (Button) findViewById(R.id.ListaUrzadzen);
        Polancz = (Button) findViewById(R.id.Connect);
        tekst_do_wyslania = (EditText) findViewById(R.id.editText);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);//Nie uzywana część kodu
        final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);


        mHandler = new Handler();


        h = new Handler() {
            public void handleMessage(Message msg) {//Retrieve the a Handler implementation that will receive this message. The object must implement Handler.handleMessage(). Each Handler has its own name-space for message codes, so you do not need to worry about yours conflicting with other handlers.
                //Wyżej implementacja funkcji ktora jest zwracana jako
                switch (msg.what) {//tutaj w parametze wywołujemy Definiowany przez użytkownika kod komunikatu dieki czemu mozna ustalic czymjest wiadmoas. Czyli zwraca wartsc dzieki ktorej wykona sie nasz kod kiedy jest jakas wiadomosc
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            Log.e(TAG, Integer.toString(sb.indexOf("#")));
                            if(sb.indexOf("#")!=(-1))
                            {i=sb.indexOf("#");
                                      // and clear
                            if(sb.charAt(i) == '#') {

                                Log.e(TAG, "Znaleziono dana do zpisanaia");
                                String zmienna = new String();
                                i++;
                                while (sb.charAt(i) != '~')//Utworzenie stringa z nazwą zmiennej do zapisu
                                {
                                    zmienna += sb.charAt(i++);
                                    Log.e(TAG, zmienna);
                                }
                                i++;
                                String cyfra = new String();//spytac dziubka czy muszę usować
                                while (sb.charAt(i) != '#') {
                                    cyfra += sb.charAt(i++);
                                    Log.e(TAG,cyfra);
                                }
                                float f = Float.parseFloat(cyfra);
                                if (zmienna.equals("t1")==true)//Porównananie w celu przypisania do odpowiedniego stringa
                                {Log.e(TAG,String.valueOf(f));
                                    t1 = f;}

                                sb.delete(0, i - 1);

                            }}
                            sb.delete(0, sb.length());
                            txtArduino.setText("Data from Arduino: " + sbprint);
                            Log.e(TAG, sbprint);// update TextView
                           // btnOff.setEnabled(true);//Nieuzywany
                           // btnOn.setEnabled(true);//daje brak reakcji na klniecia
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");


                        break;

                }
            }

            ;
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        Lista.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try {
                    Log.e(TAG, "Funkcja Szukania Urzadzen odpalona");
                    BluetoothAdapter btInterface;//Tworzymy nasze urzadzenie bluetooth (tzn obiekt posidajacy wszystkie parametry takiego urzadzenia)
                    List<String> s = new ArrayList<String>();//Tworzymy nową listę Stringów


                    btInterface = BluetoothAdapter.getDefaultAdapter();//Pobieramy liste sparowanych urzadzen z urzadzenia do naszego obiektu
                    Iterable<BluetoothDevice> pairedDevices = btInterface.getBondedDevices();//An iterator over a collection. Iterator takes the place of Enumeration in the Java Collections Framework. Iterators differ from enumerations in two ways:

                    //Iterators allow the caller to remove elements from the underlying collection during the iteration with well-defined semantics.
                            //Method names have been improved.
                    Iterator<BluetoothDevice> it = pairedDevices.iterator();


                    for (BluetoothDevice bt : pairedDevices) {//for dal wszystkich urzadzen
                        s.add(bt.getName());//dla wszystkich
                        Log.e(TAG, bt.getName());//Wyswietlenie z nazwy wszystkich urzadzen
                    }

                    String[] myStringArray = {"a", "b", "c"};
                    String[] stringArray = s.toArray(new String[s.size()]);
                    adapter = new ArrayAdapter<String>(MainActivity.this, layout.simple_list_item_1, s);//Musiałem dać MainActivity.this bo
                    //ListView lv = (ListView)findViewById(R.id.list);
                    //lv.setAdapter(adapter);

                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    spinner2.setAdapter(adapter);



                } catch (Exception e) {
                    Log.e(TAG, "bład w szukaniu urzadzen" + e.getMessage());
                }
            }
        });

        Polancz.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startRepeatingTask();//Na samym koncu metody on create 24.12.2016
                {
                    btAdapter.startDiscovery();//Nie koniecznie jest potrzebne otwarcie przeszukiwania
                    BluetoothDevice devise = null;
                    try {//zamykamy łacze żeby nie używało za dużo
                        btSocket.close();
                    } catch (IOException e2) {
                        errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
                    }

                    String nazwa_do_polanczenia = spinner2.getSelectedItem().toString();

                    try {
                        Log.e(TAG, "Funkcja Szukania Urzadzen odpalona");
                        BluetoothAdapter btInterface;//Tworzymy nasze urzadzenie bluetooth (tzn obiekt posidajacy wszystkie parametry takiego urzadzenia)
                        List<String> s = new ArrayList<String>();//Tworzymy nową listę Stringów


                        btInterface = BluetoothAdapter.getDefaultAdapter();//Pobieramy liste sparowanych urzadzen z urzadzenia do naszego obiektu
                        Iterable<BluetoothDevice> pairedDevices = btInterface.getBondedDevices();//An iterator over a collection. Iterator takes the place of Enumeration in the Java Collections Framework. Iterators differ from enumerations in two ways:

                        //Iterators allow the caller to remove elements from the underlying collection during the iteration with well-defined semantics.
                        //Method names have been improved.
                        Iterator<BluetoothDevice> it = pairedDevices.iterator();

                        Log.e(TAG, nazwa_do_polanczenia);
                        for (BluetoothDevice bt : pairedDevices) {//for dal wszystkich urzadzen
                            if(bt.getName().equalsIgnoreCase(nazwa_do_polanczenia))//dla wszystkich
                            {btAdapter = BluetoothAdapter.getDefaultAdapter();
                            address = bt.getAddress();//zeby lanczylo z automatu
                            devise= bt;
                                Log.e(TAG,"Urzadzenie znalezione"+devise.getAddress());
                                Log.e(TAG, "Adres urzadzenia" +devise.getName());}
                            Log.e(TAG,bt.getAddress());
                            Log.e(TAG, bt.getName());//Wyswietlenie z nazwy wszystkich urzadzen

                        }

                    } catch (Exception e) {
                        Log.e(TAG, "bład w szukaniu urzadzen" + e.getMessage());
                    }

                    try {
                        btSocket = createBluetoothSocket(devise);
                    } catch (IOException e) {
                        errorExit("Fatal Error", "In onResume() and socket create failed: nie utworylo socketa" + e.getMessage() + ".");
                    }

                    // Discovery is resource intensive.  Make sure it isn't going on
                    // when you attempt to connect and pass your message.
                    btAdapter.cancelDiscovery();//Kończymy przeszukiwanie urządzeń chociaz chyba nie szukaliśmy bo wydaje mi sie że użyliśmy adresu wklepanego z reki

                    // Establish the connection.  This will block until it connects.
                    Log.d(TAG, "...Connecting...");
                    try {// łaczenie  w wątku
                        btSocket.connect();
                        Log.d(TAG, "....Connection ok...");
                    } catch (IOException e) {
                        try {
                            btSocket.close();
                            Log.d(TAG, "Nie znalazlo urzadzenia");
                        } catch (IOException e2) {
                            errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                        }
                    }

                    // Create a data stream so we can talk to server.
                    Log.d(TAG, "...Create Socket...");

                    mConnectedThread = new ConnectedThread(btSocket);//utworzenie objektu klasy rozszerzającego wątek główny
                    mConnectedThread.start();//uruchomienie tego wątku (chyba jest to metoda run w kalsie którą przedstawia ten wontek)



                }
            }
        });

        Ustaw_gesi.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                txtArduino.setText("zadzialal");
                String string = ("t4");
                string.concat("\n");
                mConnectedThread.write(string);

            }
        });

        kury.setOnClickListener(new OnClickListener() {


            public void onClick(View v) {
                Fragment fragment;
                fragment = new fragment1();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.Fragment_glowny, fragment);
                ft.commit();

            }
        });

        gesi.setOnClickListener(new OnClickListener() {


            public void onClick(View v) {
                Log.d(TAG, "Powinien zadzialac");

                Fragment fragment;
                fragment = new fragnemt2();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.Fragment_glowny, fragment);
                ft.commit();
                txtArduino.setText(String.valueOf(t1));

            }
        });

        wyslij.setOnClickListener(new OnClickListener() {


            public void onClick(View v) {
                String string = ("t1" + tekst_do_wyslania.getText().toString());
                string.concat("\n");
                mConnectedThread.write(string);
            }
        });

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String string = ("t2" + tekst_do_wyslania.getText().toString());
                string.concat("\n");
                mConnectedThread.write(string);    // Send "1" via Bluetooth
                //Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String string = ("t3" + tekst_do_wyslania.getText().toString());
                string.concat("\n");
                mConnectedThread.write(string);     // Send "0" via Bluetooth
                //Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                Log.e(TAG, "Proba wyslania r");
                String string = ("r");//Wywołujemy z tego wontku kolejny odpowiadający za wysłanie zapytania
                string.concat("\n");
                mConnectedThread.write(string); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {//Sorki rozszerzenie wątka reakcji na błędy
        if (Build.VERSION.SDK_INT >= 10) {// jeśli wersja jest wieksza od 10
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {//co ma sie dziać po powrocie do aktwnosci
        super.onResume();//wywołanie metody matki

        Log.d(TAG, "...onResume - try connect...");//wyswietlenie komunikatu w Android Studio

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);//Tworzymy model naszego urządzenia oddając do niego adress (być może musimy wpisać nasz adress

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP. Trzeba sprawdzić co to za UI używamy

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();//Kończymy przeszukiwanie urządzeń chociaz chyba nie szukaliśmy bo wydaje mi sie że użyliśmy adresu wklepanego z reki

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {// łaczenie  w wątku
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
                Log.d(TAG, "Nie znalazlo urzadzenia");
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(btSocket);//utworzenie objektu klasy rozszerzającego wątek główny
        mConnectedThread.start();//uruchomienie tego wątku (chyba jest to metoda run w kalsie którą przedstawia ten wontek)
    }

    @Override
    public void onPause() {//gdy zapauzowany
        super.onPause();//ocywiście konstruktor domyslny

        Log.d(TAG, "...In onPause()...");

        try {//zamykamy łacze żeby nie używało za dużo
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//Wyświetlenie informacji dla użytkownika o bluetooth
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://pl.fietczes.bluetooth/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://pl.fietczes.bluetooth/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    class ConnectedThread extends Thread {//nasza kalsa z watkiem
        private final InputStream mmInStream;//strumien który możemy tylko modyfikowac jesli przekazujemy referencje
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();//Tak bo chcemy zeby orginalane zostały nie modyfikowalne bo one sa cały czas używane
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"

                    //Tutaj Wysłanie do UI (UserInterface) Wyciągnięcie z wątku i wysłanie za pomoca metody sendToTarget(); dzięki temu że wcześniej ustawiliśmy msg.what - zmienini sie na 1
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }



        public void FragmentChanger(View view) { //Nieaktywan cześć kodu chyba
            Fragment fragment;

            if (view == findViewById(R.id.Kury_button)) {
                fragment = new fragment1();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.Fragment_glowny, fragment);
                ft.commit();


            }
            if (view == findViewById(R.id.Gesi_button)) {
                fragment = new fragnemt2();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.Fragment_glowny, fragment);
                ft.commit();
            }
        }
        private void Connect()//Metoda odpowiedzialana za połaczenie
        { btAdapter.startDiscovery();//Nie koniecznie jest potrzebne otwarcie przeszukiwania
            try {//zamykamy łacze żeby nie używało za dużo
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
            }

            String nazwa_do_polanczenia = spinner2.getSelectedItem().toString();
            BluetoothDevice devise = null;
            try {
                Log.e(TAG, "Funkcja Szukania Urzadzen odpalona");
                BluetoothAdapter btInterface;//Tworzymy nasze urzadzenie bluetooth (tzn obiekt posidajacy wszystkie parametry takiego urzadzenia)
                List<String> s = new ArrayList<String>();//Tworzymy nową listę Stringów


                btInterface = BluetoothAdapter.getDefaultAdapter();//Pobieramy liste sparowanych urzadzen z urzadzenia do naszego obiektu
                Iterable<BluetoothDevice> pairedDevices = btInterface.getBondedDevices();//An iterator over a collection. Iterator takes the place of Enumeration in the Java Collections Framework. Iterators differ from enumerations in two ways:

                //Iterators allow the caller to remove elements from the underlying collection during the iteration with well-defined semantics.
                //Method names have been improved.
                Iterator<BluetoothDevice> it = pairedDevices.iterator();


                for (BluetoothDevice bt : pairedDevices) {//for dal wszystkich urzadzen
                    if(bt.getName().equalsIgnoreCase(nazwa_do_polanczenia));//dla wszystkich
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    address = bt.getAddress();
                    devise= bt;
                    Log.e(TAG,bt.getAddress());
                    Log.e(TAG, bt.getName());//Wyswietlenie z nazwy wszystkich urzadzen
                }

            } catch (Exception e) {
                Log.e(TAG, "bład w szukaniu urzadzen" + e.getMessage());
            }


            try
            {
                btSocket = devise.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            }
            catch (Exception e)
            {
                Log.d(TAG, "Nie utworzono polanczenia");
            }

            // Create a data stream so we can talk to server.
            Log.d(TAG, "...Create Socket...");

            mConnectedThread = new ConnectedThread(btSocket);//utworzenie objektu klasy rozszerzającego wątek główny
            mConnectedThread.start();//uruchomienie tego wątku (chyba jest to metoda run w kalsie którą przedstawia ten wontek)

        }
        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
}

