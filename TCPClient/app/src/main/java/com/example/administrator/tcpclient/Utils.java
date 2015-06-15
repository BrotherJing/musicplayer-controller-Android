package com.example.administrator.tcpclient;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Administrator on 2015/6/14 0014.
 */
public class Utils {

    public static String getPath(Context context, Uri data){
        if("content".equalsIgnoreCase(data.getScheme())){
            String[] projections = {"_data"};
            Cursor cursor = null;
            try{
                cursor = context.getContentResolver().query(data,projections,null,null,null);
                int index = cursor.getColumnIndexOrThrow(projections[0]);
                if(cursor.moveToFirst()){
                    return cursor.getString(index);
                }
            }catch(Exception ex){
                return null;
            }
        }else if("file".equalsIgnoreCase(data.getScheme())){
            return data.getPath();
        }
        return null;
    }

}
