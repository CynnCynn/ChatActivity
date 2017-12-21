package com.example.lenovo.chatactivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private Button sendButton;
    private EditText sendContent;
    private DatagramSocket socket;
    private Lock sendLock = new ReentrantLock();
    private String send_msg;
    private SendThread send;
    private String content;
    private TextView textView;
    private Handler handler;
    int sport=10002;
    private List<Msg> msgList=new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private ImageButton sendPicture;
    private Uri imageUri;
    public static Bitmap mphoto;
    public  String imagePath;

    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    protected static Uri tempUri;
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.send:
                long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date d1=new Date(time);
                String t1=format.format(d1);
                send_msg=sendContent.getText().toString();
                if(!send_msg.equals("")) {
                    sendContent.setText("");
                    Msg msg = new Msg(send_msg, Msg.TYPE_SEND);
                    msgList.add(msg);
                    adapter.notifyItemInserted(msgList.size() - 1);//将ListView定位到最后一行
                    MsgLog msgLog = new MsgLog();
                    msgLog.setType(Msg.TYPE_SEND);
                    msgLog.setContext(send_msg);
                    msgLog.setTime(t1);
                    msgLog.save();
                    if (send == null) {
                        send = new SendThread(socket);
                        new Thread(send).start();
                    } else {
                        synchronized (sendLock) {
                            sendLock.notify();
                        }
                    }
                }
                break;
            case R.id.send_picture:
                showChoosePicDialog();
                break;
            default:
                break;
        }
    }

    class SendThread implements Runnable{
        private DatagramSocket socket;
        SendThread(DatagramSocket socket)
        {
            this.socket=socket;
        }

        @Override
        public void run() {
            while(true){
                SocketAddress socketAddress=new InetSocketAddress("127.0.0.1",10001);
                DatagramPacket p=new DatagramPacket(send_msg.getBytes(),
                        send_msg.getBytes().length,socketAddress);
                try{
                    socket.send(p);
                    synchronized (sendLock){
                        try{
                            sendLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    class ReceiveThread implements Runnable{
        private DatagramSocket socket;

        ReceiveThread(DatagramSocket socket){
            this.socket=socket;
        }

        @Override
        public void run() {
            byte buf[]=new byte[1024];
            try{
                DatagramPacket p=new DatagramPacket(buf,1024);
                while(true){
                    socket.receive(p);
                    Message msg=new Message();
//                    msg.obj=p.getSocketAddress().toString()+" : "+new String(p.getData())+"\n";
                    msg.obj=new String(p.getData())+"\n";
                    handler.sendMessage(msg);
                    for(int i=0;i<1024;i++)
                        buf[i]=0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sendButton=(Button)findViewById(R.id.send);
        sendContent=(EditText)findViewById(R.id.content);
        sendPicture=(ImageButton)findViewById(R.id.send_picture);
        sendButton.setOnClickListener(this);
        sendPicture.setOnClickListener(this);
//        textView=(TextView)findViewById(R.id.text);
        msgRecyclerView =(RecyclerView)findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter =new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        android.support.v7.app.ActionBar actionBar=getSupportActionBar();
        actionBar.hide();


//        验证是不是第一次聊天
        SQLiteDatabase db= LitePal.getDatabase();
        db.close();
        SharedPreferences pref =getSharedPreferences("first",MODE_PRIVATE);
        Boolean first=pref.getBoolean("first",true);
        if(!first) {
            loadMsgLog();
//            SharedPreferences.Editor editor=getSharedPreferences("first",MODE_PRIVATE).edit();
//            editor.putBoolean("first",false);
//            editor.apply();
//            //数据库第一行
//            MsgLog msgLog=new MsgLog();
////            msgLog.setType();
//            msgLog.setContext("content");
//            msgLog.setTime("time");
//            msgLog.save();
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //当程序准备发送此数据包时，
                    // 该方法返回此数据包的目标SocketAddress；
                    // 当程序刚接收到数据包时，该方法返回该数据包发送主机的SocketAddress。
                    SocketAddress socketaddr=new InetSocketAddress("127.0.0.1",sport);
                    socket=new DatagramSocket(socketaddr);
                    ReceiveThread receiveThread=new ReceiveThread(socket);
                    new Thread(receiveThread).start();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
//                content += msg.obj.toString();
//                textView.setText(content);
                String receive = msg.obj.toString();
                String[] receiveArray = receive.split("\n");
                if (!receiveArray[0].equals("")) {
                    Msg receive_msg = new Msg(receiveArray[0], Msg.TYPE_RECEIVED);
                    msgList.add(receive_msg);
                    adapter.notifyItemInserted(msgList.size() - 1);
//                    获取当前时间
                    long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
                    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date d1=new Date(time);
                    String t1=format.format(d1);
//                    保存收到的信息
                    MsgLog msgLog=new MsgLog();
//                    msgLog.setSender("Other");
//                    msgLog.setReceiver("Me");
                    msgLog.setType(Msg.TYPE_RECEIVED);
                    msgLog.setContext(receiveArray[0]);
                    msgLog.setTime(t1);
                    msgLog.save();
                }
            }
        };

    }

    private void loadMsgLog() {

    }

//    protected void showChoosePicDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("发送图片");
//        String[] items = { "选择本地照片", "拍照" };
//        builder.setNegativeButton("取消", null);
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which) {
//                    case CHOOSE_PICTURE: // 选择本地照片
////                        Intent openAlbumIntent = new Intent(
////                                Intent.ACTION_GET_CONTENT);
////                        openAlbumIntent.setType("image/*");
////                        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
//                        Intent intent;
//                        if (Build.VERSION.SDK_INT < 19) {
//                            intent = new Intent(Intent.ACTION_GET_CONTENT);
//                            intent.setType("image/*");
//                        } else {
//                            intent = new Intent(
//                                    Intent.ACTION_PICK,
//                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        }
//                        startActivityForResult(intent, CHOOSE_PICTURE);
//                        break;
//                    case TAKE_PICTURE: // 拍照
//                        Intent openCameraIntent = new Intent(
//                                MediaStore.ACTION_IMAGE_CAPTURE);
//                        tempUri = Uri.fromFile(new File(Environment
//                                .getExternalStorageDirectory(), "image.jpg"));
//                        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
//                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
//                        startActivityForResult(openCameraIntent, TAKE_PICTURE);
//                        break;
//                }
//            }
//        });
//        builder.create().show();
////        sendImageByUri(tempUri);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case TAKE_PICTURE:
//                if (resultCode == RESULT_OK) {
//                    try {
//                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
////                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//                        Msg receive_img=new Msg(bitmap,Msg.TYPE_SEND_IMAGE);
//
//                        msgList.add(receive_img);
//                        adapter.notifyItemInserted(msgList.size() - 1);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
//                break;
//            default:
//                break;
//        }
//    }
protected void showChoosePicDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("设置头像");
    String[] items = { "选择本地照片", "拍照" };
    builder.setNegativeButton("取消", null);
    builder.setItems(items, new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case CHOOSE_PICTURE: // 选择本地照片
                    Intent openAlbumIntent = new Intent(
                            Intent.ACTION_GET_CONTENT);
                    openAlbumIntent.setType("image/*");
                    startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                    break;
                case TAKE_PICTURE: // 拍照
                    Intent openCameraIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    tempUri = Uri.fromFile(new File(Environment
                            .getExternalStorageDirectory(), "image.jpg"));
                    // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                    startActivityForResult(openCameraIntent, TAKE_PICTURE);
                    break;
            }
        }
    });
    builder.create().show();
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回码是可以用的
            switch (requestCode) {
                case TAKE_PICTURE:
                    startPhotoZoom(tempUri); // 对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData()); // 对图片进行裁剪处理
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        setImageToView(data); // 让刚才选择裁剪得到的图片显示在界面上
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Log.i("tag", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param
     */
    protected void setImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            mphoto = extras.getParcelable("data");
            Log.d("set","setImageToView:"+mphoto);
//            mphoto = ImageUtils.toRoundBitmap(mphoto); // 处理后的图片
            Msg receive_img=new Msg(mphoto,Msg.TYPE_SEND_IMAGE);

            msgList.add(receive_img);
            adapter.notifyItemInserted(msgList.size() - 1);
            byte[] tmp=bitmap_to_byte(mphoto);
            send_msg=new String(tmp);
            if (!send_msg.equals("")) {
//                sendContent.setText("");
//                Msg msg = new Msg(send_msg, Msg.TYPE_SEND);
//                msgList.add(msg);
//                adapter.notifyItemInserted(msgList.size() - 1);//将ListView定位到最后一行
//            MsgLog msgLog = new MsgLog();
//            msgLog.setType(Msg.TYPE_SEND);
//            msgLog.setContext(send_msg);
//            msgLog.setTime(t1);
//            msgLog.save();
                if (send == null) {
                    send = new SendThread(socket);
                    new Thread(send).start();
                } else {
                    synchronized (sendLock) {
                        sendLock.notify();
                    }
                }
            }
//            mimage_user.setImageBitmap(mphoto);
//            mheader_img.setImageBitmap(mphoto);
//            userimg_flag=1;
            uploadPic(mphoto);
        }
    }

    private void uploadPic(Bitmap bitmap) {
        // 上传至服务器
        // 把Bitmap转换成file，然后得到file的url，做文件上传操作
        imagePath = ImageUtils.savePhoto(bitmap, Environment
                .getExternalStorageDirectory().getAbsolutePath(), String
                .valueOf(System.currentTimeMillis()));
        Log.e("imagePath", imagePath+"");

        if(imagePath != null){
            Log.d("ImagePath","imagePath:"+imagePath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            // 没有获取到权限，重新请求，或者关闭app
            Toast.makeText(this, "需要存储权限", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendImageByUri(Uri selectedImage) {
        Cursor cursor = getContentResolver().query(selectedImage, null, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            String imagePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;
            if (imagePath == null || imagePath.equals("null")) {
                Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImage(imagePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendImage(file.getAbsolutePath());
        }
    }

    /**
     * 发送图片
     *
     * @param filePath
//     */
    private void sendImage(final String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        Msg receive_img = new Msg(bitmap, Msg.TYPE_SEND_IMAGE);

        msgList.add(receive_img);
        adapter.notifyItemInserted(msgList.size() - 1);
        byte[] tmp=bitmap_to_byte(bitmap);
        send_msg=new String(tmp);
        if (!send_msg.equals("")) {
            sendContent.setText("");
            Msg msg = new Msg(send_msg, Msg.TYPE_SEND);
            msgList.add(msg);
            adapter.notifyItemInserted(msgList.size() - 1);//将ListView定位到最后一行
//            MsgLog msgLog = new MsgLog();
//            msgLog.setType(Msg.TYPE_SEND);
//            msgLog.setContext(send_msg);
//            msgLog.setTime(t1);
//            msgLog.save();
            if (send == null) {
                send = new SendThread(socket);
                new Thread(send).start();
            } else {
                synchronized (sendLock) {
                    sendLock.notify();
                }
            }
        }

//        Log.d("send", "sendImage");
//        Msg message = Msg.createImageMessage(
//                Msg.TYPE_SEND_IMAGE, filePath);
//        adapter.msgList.add(message);
//        DsdMessage message1 = DsdMessage.createTxtMessage(
//                DsdMessage.MESSAGE_TYPE_RECV_TXT, "Receive Successfully!!");
//        adapter.msgList.add(message1);
//		/*
//        * 存储图片数据
//        * */
//        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//        byte[] images=bit_to_img(bitmap);
//        MsgLog chatPhoto =new MsgLog();
//        chatPhoto.setReceiver("Other");
//        chatPhoto.setSender("Me");
//        chatPhoto.setContext(images);
////        chatPhoto.setText(null);
//        chatPhoto.save();
//
//        istView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//        listView.setSelection(listView.getCount() - 1);
    }

    private byte[]bitmap_to_byte(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}
