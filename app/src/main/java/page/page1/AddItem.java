package page.page1;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static page.page1.LoginMainActivity.post_userid;

public class AddItem extends AppCompatActivity {
    private static final byte REQUEST_SYSTEM_PIC = 10;
    private  DatabaseHelper dbHelper;
    private Spinner  sp;
    private ImageButton imageButton;
    private byte[] image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_m1);
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");

        dbHelper = new DatabaseHelper(this);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] ctype = new String[]{"生活用品", "学习用品", "电子产品", "体育用品"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ctype);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) super.findViewById(R.id.m1_style);
        spinner.setAdapter(adapter);
        sp = (Spinner) findViewById(R.id.m1_style);

        imageButton=(ImageButton)findViewById(R.id.m1_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AddItem.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddItem.this, new
                            String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    //打开系统相册
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);
                }
            }
        });

        Button fabu=(Button)findViewById(R.id.fabu);
        fabu.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                // 在发布按钮点击时获取分类
                String kind = (String) sp.getSelectedItem();

                EditText title=(EditText)findViewById(R.id.m1_title);
                EditText price=(EditText)findViewById(R.id.m1_price);
                EditText phone=(EditText)findViewById(R.id.m1_phone);
                EditText nr=(EditText)findViewById(R.id.m1_nr);

                // 获取输入内容
                String titleText = title.getText().toString().trim();
                String priceText = price.getText().toString().trim();
                String phoneText = phone.getText().toString().trim();
                String infoText = nr.getText().toString().trim();

                // 输入验证
                if (titleText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "需要输入商品标题", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (priceText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "需要输入商品价格", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phoneText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "需要输入联系方式", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (infoText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "需要输入商品描述", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (image == null) {
                    Toast.makeText(getApplicationContext(), "需要选择商品图片", Toast.LENGTH_SHORT).show();
                    return;
                }

                Date curDate = new Date(System.currentTimeMillis());
                String time = formatter.format(curDate);

                ContentValues values=new ContentValues();
                values.put("title", titleText);
                values.put("userId", post_userid);
                values.put("kind", kind);
                values.put("time", time);
                values.put("price", priceText);
                values.put("contact", phoneText);
                values.put("info", infoText);
                values.put("image", image);

                long result = db.insert("iteminfo",null,values);
                if (result != -1) {
                    Toast.makeText(getApplicationContext(), "发布成功", Toast.LENGTH_SHORT).show();
                    // 清空输入框
                    title.setText("");
                    price.setText("");
                    phone.setText("");
                    nr.setText("");
                    imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
                    image = null;
                } else {
                    Toast.makeText(getApplicationContext(), "发布失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button but1 = (Button)findViewById(R.id.but1_m1);
        Button but2 = (Button)findViewById(R.id.but2_m1);
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(AddItem.this,MyItems.class);
                startActivity(intent);
            }
        });
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(AddItem.this,main_page.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            if (c != null && c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String imagePath = c.getString(columnIndex);
                showImage(imagePath);
                c.close();
            }
        }
    }

    //加载图片
    private void showImage(String imaePath) {
        try {
            Bitmap bm = BitmapFactory.decodeFile(imaePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            image = baos.toByteArray();
            imageButton.setImageBitmap(bm);
        } catch (Exception e) {
            Toast.makeText(this, "图片加载失败，请重新选择", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限已授予，可以打开相册
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(this, "需要相册权限才能选择图片", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}