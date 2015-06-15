package com.example.administrator.tcpclient;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity implements Runnable,ConfigureFragment.OnFragmentInteractionListener{

    private final int REQ_UPLOAD = 1;

    private final int MSG_TYPE_SEND = 0;
    private final int MSG_TYPE_RECEIVE = 1;
    private final int MSG_TYPE_CONNECT = 2;
    private final int HOST_PORT = 8888;

    private final String KEY_MSG_TYPE = "type";
    private final String KEY_MSG_CONTENT = "content";

    private final String REQUEST_HEAD = "CMD ";
    private final String REQUEST_END = " END";

    //控件对象定义
    private ListView lvSongs;
    private Button btnSendFile;
    private ProgressBar pbSendFile;

    ConfigureFragment fragment;
    DataOutputStream dos;
    ServerSocket mServerSocket = null;
    Socket socket = null;
    ReadThread thread = null;

    private List<HashMap<String,String>> maplist;
    private String dest_ip,dest_port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //Thread myThread = new Thread(MainActivity.this);
        //myThread.start();

        showConfirmPage();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_upload:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try{
                    startActivityForResult(intent,REQ_UPLOAD);
                }catch (Exception ex){
                    Toast.makeText(this,"No file manager available.",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        lvSongs = (ListView)findViewById(R.id.lvSongs);
        btnSendFile = (Button)findViewById(R.id.btnSendFile);
        pbSendFile = (ProgressBar)findViewById(R.id.pbSendFile);
        lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String entry = maplist.get(i).get("title");
                if(entry.charAt(1)=='F'){
                    new SendCmdTask().execute("play "+entry.substring(4));
                }
            }
        });
        btnSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File inFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"zom.wav");
                new SendFileTask().execute(inFile.getAbsolutePath(),inFile.getName());
            }
        });
        pbSendFile.setMax(100);
    }

    @Override
    public void run() {
        try{
            //创建ServerSocket对象，并设置端口为8888
            mServerSocket = new ServerSocket(HOST_PORT);

            //不断循环接收数据信息
            while (true) {
                StringBuilder sb = new StringBuilder();
                //打印开始服务阻塞提示
                Log.i("yj","开始数据接收阻塞等待.....");


                //ServerSocket开启等待接收数据的阻塞，并创建接收对象Socket
                Socket mSocket = mServerSocket.accept();

                //打印接收到数据信息的提示
                Log.i("yj","接收到TCP客户端传递的信息。。。。");

                //创建InputStream对象对Socket接收的数据进行获取
                InputStream mInputStream = mSocket.getInputStream();

                //创建数据输入流，并获取从Socket中提取的数据流的数据
                DataInputStream mDataInputStream = new DataInputStream(new BufferedInputStream(mInputStream));

                //定义字节数组
                byte[] bufferff = new byte[1024 * 4];

                int temp = 0;

                //读取获取过来的数据对象InputStream的数据
                while ((temp = mDataInputStream.read(bufferff)) != -1) {
                    sb.append(new String(bufferff,0,temp));

                }
                //关闭数据流
                mInputStream.close();
                mDataInputStream.close();
                //buffer.close();

                Log.i("yj",sb.toString());

                Message msg = new Message();
                Bundle bundle = msg.getData();
                bundle.putInt(KEY_MSG_TYPE,MSG_TYPE_RECEIVE);
                bundle.putString(KEY_MSG_CONTENT,sb.toString());
                handler.sendMessage(msg);

            }
        }catch (IOException e) {
            e.printStackTrace();
        }finally{
            try{
                if (mServerSocket != null){
                    mServerSocket.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFile(String path){
        File inFile = new File(path);
        long length;
    }

    class SendCmdTask extends AsyncTask<String,Void,Void>{
        String send_data;
        Message msg = new Message();
        Bundle bundle = msg.getData();
        @Override
        protected Void doInBackground(String... strings) {
            try {
                if(socket==null)
                    socket = new Socket(dest_ip, Integer.parseInt(dest_port));
                    //socket = new Socket("192.168.1.117",80);
                OutputStream os = socket.getOutputStream();
                dos = new DataOutputStream(os);
                send_data = REQUEST_HEAD+strings[0]+REQUEST_END;
                dos.write(send_data.getBytes(), 0, send_data.getBytes().length);
                dos.flush();

                bundle.putInt(KEY_MSG_TYPE,MSG_TYPE_SEND);
                bundle.putString(KEY_MSG_CONTENT,"send success");
                handler.sendMessage(msg);

                if(thread==null) {
                    thread = new ReadThread();
                    thread.start();
                }
            }catch(Exception ex){
                ex.printStackTrace();
                bundle.putInt(KEY_MSG_TYPE,MSG_TYPE_SEND);
                bundle.putString(KEY_MSG_CONTENT,ex.toString());
                handler.sendMessage(msg);
            }
            return null;
        }
    }

    class SendFileTask extends AsyncTask<String,Integer,Void>{
        byte[] buffer,head;
        int tmp,size,size_sent;

        Message msg = new Message();
        Bundle bundle = msg.getData();

        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();
            new SendCmdTask().execute("file 0 yo.wav");
        }*/

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pbSendFile.setProgress(size_sent*100/size);
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                if(socket==null)
                    socket = new Socket(dest_ip, Integer.parseInt(dest_port));
                OutputStream os = socket.getOutputStream();
                dos = new DataOutputStream(os);
                InputStream is = new FileInputStream(strings[0]);
                size = is.available();
                size_sent = 0;
                buffer = new byte[1024];

                head = (REQUEST_HEAD+"file "+size+"/"+strings[1]+REQUEST_END).getBytes();
                dos.write(head,0,head.length);
                dos.flush();

                while((tmp=is.read(buffer))!=-1){
                    dos.write(buffer,0,tmp);
                    size_sent += tmp;
                    onProgressUpdate(size_sent);
                    dos.flush();
                    Thread.sleep(100);
                }

                is.close();

                bundle.putInt(KEY_MSG_TYPE,MSG_TYPE_SEND);
                bundle.putString(KEY_MSG_CONTENT,"send success");
                handler.sendMessage(msg);

            }catch(Exception ex){
                ex.printStackTrace();
                bundle.putInt(KEY_MSG_TYPE,MSG_TYPE_SEND);
                bundle.putString(KEY_MSG_CONTENT,ex.toString());
                handler.sendMessage(msg);
            }
            return null;
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.getData().getInt(KEY_MSG_TYPE)){
                case MSG_TYPE_SEND:
                    Toast.makeText(MainActivity.this,
                            msg.getData().getString(KEY_MSG_CONTENT),Toast.LENGTH_SHORT).show();
                    break;
                case MSG_TYPE_RECEIVE:
                    parseReceivedData(msg.getData().getString(KEY_MSG_CONTENT));
                    break;
                case MSG_TYPE_CONNECT:
                    new SendCmdTask().execute("ls");
                    break;
                default:
                    break;
            }
        }
    };

    private void parseReceivedData(String raw_data){
        if(raw_data.startsWith("ls ")){
            fillList(raw_data.substring(3));
        }
        else if(raw_data.startsWith("conn ")){
            new SendCmdTask().execute("ls");
        }
    }

    private void fillList(String data){
        String[] songList = data.split("\n");
        maplist = new ArrayList<>();
        HashMap<String,String> map;
        for(String s : songList){
            map = new HashMap<>();
            map.put("title",s);
            maplist.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this,maplist,R.layout.list_cell,new String[]{"title"},new int[]{R.id.tvTitle});
        lvSongs.setAdapter(adapter);
    }

    private void showConfirmPage(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(fragment==null){
            fragment = ConfigureFragment.newInstance();
            transaction.replace(R.id.fragmentContainer,fragment).commit();
        }
    }

    @Override
    public void onConfirm(String ip, String port) {
        this.dest_ip = ip;
        this.dest_port = port;
        getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).remove(fragment).commit();
        new SendCmdTask().execute("ls");
    }

    public class ReadThread extends Thread{
        @Override
        public void run(){
            byte[] buffer = new byte[1024];
            InputStream inputStream;

            try{
                inputStream = socket.getInputStream();
            }catch (Exception e){
                e.printStackTrace();
                Message msg = new Message();
                Bundle bundle = msg.getData();
                bundle.putInt(KEY_MSG_TYPE,MSG_TYPE_SEND);
                bundle.putString(KEY_MSG_CONTENT,e.toString());
                handler.sendMessage(msg);
                return;
            }

            while (true){
                try {
                    if(inputStream.read(buffer)>0){
                        String s = new String(buffer);
                        //输出s
                        Message msg = new Message();
                        Bundle bundle = msg.getData();
                        bundle.putInt(KEY_MSG_TYPE,MSG_TYPE_RECEIVE);
                        bundle.putString(KEY_MSG_CONTENT,s);
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    Bundle bundle = msg.getData();
                    bundle.putInt(KEY_MSG_TYPE,MSG_TYPE_SEND);
                    bundle.putString(KEY_MSG_CONTENT,e.toString());
                    handler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case REQ_UPLOAD:
                    Uri uri = data.getData();
                    String path = Utils.getPath(this,uri);
                    File file = new File(path);
                    new SendFileTask().execute(file.getAbsolutePath(),file.getName());
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
