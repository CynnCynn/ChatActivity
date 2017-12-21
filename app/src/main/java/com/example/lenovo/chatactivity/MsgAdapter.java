package com.example.lenovo.chatactivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Lenovo on 2017/11/11.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Msg> mMsgList;
    final int SAVE_PICTURE=0;
    private static final String SAVE_PIC_PATH= Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ?
            Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
    private static final String SAVE_REAL_PATH = SAVE_PIC_PATH+ "/chat/savePic";//保存的确切位置
    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        ImageView leftImg;
        ImageView rightImg;
        public ViewHolder(View view){
            super(view);
            leftLayout=(LinearLayout)view.findViewById(R.id.left_layout);
            rightLayout=(LinearLayout)view.findViewById(R.id.right_layout);
            leftMsg=(TextView)view.findViewById(R.id.left_msg);
            rightMsg=(TextView)view.findViewById(R.id.right_msg);
            leftImg=(ImageView)view.findViewById(R.id.left_img);
            rightImg=(ImageView)view.findViewById(R.id.right_img);
        }

    }
    public MsgAdapter(List<Msg> msgList){
        mMsgList=msgList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Msg msg=mMsgList.get(position);
        if(msg.getType()==Msg.TYPE_RECEIVED){
            //如果收到的消息，则显示左边的消息布局，将右边的消息布局隐藏
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        }else if (msg.getType()==Msg.TYPE_SEND){
            //如果发出消息，则显示左边的消息布局，将右边的消息布局隐藏
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(msg.getContent());
        }
        else if(msg.getType()==Msg.TYPE_SEND_IMAGE){
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightImg.setVisibility(View.VISIBLE);
            holder.rightMsg.setVisibility(View.GONE);
            holder.rightImg.setImageBitmap(msg.getPicture());
//            holder.rightMsg.setText(msg.getContent());
        }
        else if(msg.getType()==Msg.TYPE_RECEIVED_IMAGE){
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftImg.setVisibility(View.VISIBLE);
            holder.leftMsg.setVisibility(View.GONE);
            holder.leftImg.setImageBitmap(msg.getPicture());
//            holder.rightMsg.setText(msg.getContent());
        }

        holder.rightImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("保存图片");
//                String[] items = { "保存图片"};
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bitmap image = ((BitmapDrawable)holder.rightImg.getDrawable()).getBitmap();
//                        File file;
                        long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
                        SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
                        Date d1=new Date(time);
                        String t1=format.format(d1);
                        try {
                            saveFile(image,"image"+t1+".jpg",SAVE_REAL_PATH);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                        Uri uri = Uri.fromFile(file);
//                        intent.setData(uri);
//                        view.getContext().sendBroadcast(intent);
                       Log.d("Save","Saved");
                    }
                });
                builder.setNegativeButton("取消", null);
//                builder.setItems(items, new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        switch (which) {
//                            case SAVE_PICTURE: // 选择本地照片
////                                Intent openAlbumIntent = new Intent(
////                                        Intent.ACTION_GET_CONTENT);
////                                openAlbumIntent.setType("image/*");
////                                startActivityForResult(openAlbumIntent, SAVE_PICTURE);
//                                break;
//
//                        }
//                    }
//                });
                builder.create().show();
                Toast.makeText(view.getContext(),"Picture saved in"+SAVE_REAL_PATH,Toast.LENGTH_SHORT).show();
            }
        });
    }


//            Msg.MESSAGE_TYPE_SEND_IMAGE, filePath);

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

    public static void saveFile(Bitmap bm, String fileName, String path) throws IOException {
        String subForder = SAVE_REAL_PATH + path;
        File foder = new File(subForder);
        if (!foder.exists()) {
            foder.mkdirs();
        }
        File myCaptureFile = new File(subForder, fileName);
        if (!myCaptureFile.exists()) {
            myCaptureFile.createNewFile();
        }
//        www.2cto.com
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
    }
}
