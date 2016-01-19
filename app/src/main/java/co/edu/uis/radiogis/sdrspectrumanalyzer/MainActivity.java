package co.edu.uis.radiogis.sdrspectrumanalyzer;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {


    public Handler mHandler;
    private Socket socket;
    private static final int SERVERPORT = 9999;
    //TODO: ingresar la ip desde la interfaz de usuario
    private static final String SERVER_IP = "192.168.0.51";
    TabHost TbH;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Resources res = getResources();
        TbH = (TabHost) findViewById(R.id.tabHost); //llamamos al Tabhost
        TbH.setup();                                                         //lo activamos

        TabHost.TabSpec tab1 = TbH.newTabSpec("tab1");  //aspectos de cada Tab (pestaña)
        TabHost.TabSpec tab2 = TbH.newTabSpec("tab2");

        tab1.setIndicator("Control");    //qué queremos que aparezca en las pestañas
        tab1.setContent(R.id.control); //definimos el id de cada Tab (pestaña)

        tab2.setIndicator("Grafica");
        tab2.setContent(R.id.grafica);

        TbH.addTab(tab1); //añadimos los tabs ya programados
        TbH.addTab(tab2);
        TbH.setCurrentTab(0);

        Spinner ventana = (Spinner) findViewById(R.id.ventana);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ventana_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ventana.setAdapter(adapter);
        Spinner base = (Spinner) findViewById(R.id.base);
        ArrayAdapter<CharSequence> adapterb = ArrayAdapter.createFromResource(this, R.array.base_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        base.setAdapter(adapterb);
        Spinner escala = (Spinner) findViewById(R.id.escala);
        ArrayAdapter<CharSequence> adaptere = ArrayAdapter.createFromResource(this, R.array.escala_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        escala.setAdapter(adaptere);
        new Thread(new Udpr()).start();
        //se inicia tarea en segundo plano, esta controla la conexion de el socket
        new Thread(new ClientThread()).start();

       //TODO: poner la grafica en su lugar

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.cambiarip:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void fr(View view) {
        EditText et = (EditText) findViewById(R.id.fr);
        String str = "{\"fc\":"+et.getText().toString()+"}";
        Message msg = Message.obtain();
        msg.obj =  str;
        mHandler.sendMessage(msg);

    }

    public void span(View view) {

        EditText et = (EditText) findViewById(R.id.span);
        String str = "{\"ab\":"+et.getText().toString()+"}";
        Message msg1 = Message.obtain();
        msg1.obj =  str;
        mHandler.sendMessage(msg1);

    }

    public void ganancia(View view) {

        EditText et = (EditText) findViewById(R.id.ganancia);
        String str = "{\"gan\":"+et.getText().toString()+"}";
        Message msg2 = Message.obtain();
        msg2.obj =  str;
        mHandler.sendMessage(msg2);
    }

    public void ventana(View view) {

        Spinner et = (Spinner) findViewById(R.id.ventana);
        String str = "{\"ventana\":\""+et.getSelectedItem().toString()+"\"}";
        Message msg3 = Message.obtain();
        msg3.obj =  str;
        mHandler.sendMessage(msg3);

    }

    public void base(View view) {

        Spinner et = (Spinner) findViewById(R.id.base);
        String str = "{\"base\":\""+et.getSelectedItem().toString()+"\"}";
        Message msg4 = Message.obtain();
        msg4.obj =  str;
        mHandler.sendMessage(msg4);

    }

    public void escala(View view) {

        Spinner et = (Spinner) findViewById(R.id.escala);
        //TODO: implementar escala en el server(Jesús)
        //String str = "{\"escala\":\""+et.getSelectedItem().toString()+"\"}";
        //Message msg5 = Message.obtain();
        //msg5.obj =  str;
        //mHandler.sendMessage(msg5);

    }



    public void inicia(View view) {
        String start="1";
        Message msg5 = Message.obtain();
        msg5.obj =  start;
        mHandler.sendMessage(msg5);

    }




    // esta clase es la tarea en segundo plano encargada de la conexion y envio de mensajes
    class ClientThread implements Runnable {
        @Override
        public void run() {
            // el loop encargado de recibir los mensajes y acutar acorde a estos
            Looper.prepare();
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // Act on the message
                    InetAddress serverAddr = null;
                    try {
                        serverAddr = InetAddress.getByName(SERVER_IP);
                        socket = new Socket(serverAddr, SERVERPORT);
                        String str = (String)msg.obj;
                        if(str=="1"){
                            JSONObject obj = new JSONObject();
                            obj.put("start",Boolean.TRUE);
                            obj.put("n",8);
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(obj);
                        }else{
                            Log.d("run", "conexion acertada" + str);
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(str);
                        }
                        socket.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
            Looper.loop();

        }

    }

    class Udpr implements  Runnable{
        @Override
        public void run() {
            try {
                DatagramSocket clientsocket=new DatagramSocket(9999);
                byte[] receivedata = new byte[1472];
                while(true)
                {
                    DatagramPacket recv_packet = new DatagramPacket(receivedata, receivedata.length);
                    Log.d("UDP", "S: Receiving...");
                    clientsocket.receive(recv_packet);
                    String rec_str = new String(recv_packet.getData());
                    Log.d(" Received String ",rec_str);
                    InetAddress ipaddress = recv_packet.getAddress();
                    int port = recv_packet.getPort();
                    Log.d("IPAddress : ",ipaddress.toString());
                    Log.d(" Port : ",Integer.toString(port));
                }
            } catch (Exception e) {
                Log.e("UDP", "S: Error", e);
            }
        }
    }
}
