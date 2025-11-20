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
    private DatabaseHelper dbHelper;
    private Spinner sp;
    private ImageButton imageButton;
    private byte[] image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_m1);
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

        dbHelper = new DatabaseHelper(this);

        String[] ctype = new String[]{getString(R.string.household_goods), getString(R.string.study_stuffs), getString(R.string.electronic_devices), getString(R.string.sport_item)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ctype);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) super.findViewById(R.id.m1_style);
        spinner.setAdapter(adapter);
        sp = (Spinner) findViewById(R.id.m1_style);

        imageButton = (ImageButton) findViewById(R.id.m1_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AddItem.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddItem.this, new
                            String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {

                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);
                }
            }
        });

        Button fabu = (Button) findViewById(R.id.fabu);
        fabu.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                // 在发布按钮点击时获取分类
                String kind = (String) sp.getSelectedItem();

                EditText title = (EditText) findViewById(R.id.m1_title);
                EditText price = (EditText) findViewById(R.id.m1_price);
                EditText phone = (EditText) findViewById(R.id.m1_phone);
                EditText nr = (EditText) findViewById(R.id.m1_nr);

                // 获取输入内容并去除首尾空格
                String titleText = title.getText().toString().trim();
                String priceText = price.getText().toString().trim();
                String phoneText = phone.getText().toString().trim();
                String infoText = nr.getText().toString().trim();

                // 完整的输入验证
                if (titleText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "需要输入商品标题", Toast.LENGTH_SHORT).show();
                    title.requestFocus();
                    return;
                }

                if (priceText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "需要输入商品价格", Toast.LENGTH_SHORT).show();
                    price.requestFocus();
                    return;
                }

                if (phoneText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "需要输入联系方式", Toast.LENGTH_SHORT).show();
                    phone.requestFocus();
                    return;
                }

                if (infoText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "需要输入商品描述", Toast.LENGTH_SHORT).show();
                    nr.requestFocus();
                    return;
                }

                if (image == null) {
                    Toast.makeText(getApplicationContext(), "需要选择商品图片", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 所有验证通过，开始发布流程
                publishItem(kind, titleText, priceText, phoneText, infoText, formatter);
            }
        });

        Button but1 = (Button) findViewById(R.id.but1_m1);
        Button but2 = (Button) findViewById(R.id.but2_m1);

        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddItem.this, main_page.class);
                startActivity(intent);
            }
        });
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddItem.this, MyItems.class);
                startActivity(intent);
            }
        });
    }

    private void publishItem(String kind, String titleText, String priceText,
                             String phoneText, String infoText, SimpleDateFormat formatter) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            Date curDate = new Date(System.currentTimeMillis());
            String time = formatter.format(curDate);

            ContentValues values = new ContentValues();
            values.put("title", titleText);
            values.put("userId", post_userid);
            values.put("kind", kind);
            values.put("time", time);
            values.put("price", priceText);
            values.put("contact", phoneText);
            values.put("info", infoText);
            values.put("image", image);

            long result = db.insert("iteminfo", null, values);
            if (result != -1) {
                Toast.makeText(getApplicationContext(), "发布成功", Toast.LENGTH_SHORT).show();
                // 清空输入框
                clearInputFields();
            } else {
                Toast.makeText(getApplicationContext(), "发布失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AddItem", "发布商品失败", e);
            Toast.makeText(getApplicationContext(), "发布失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private void clearInputFields() {
        EditText title = (EditText) findViewById(R.id.m1_title);
        EditText price = (EditText) findViewById(R.id.m1_price);
        EditText phone = (EditText) findViewById(R.id.m1_phone);
        EditText nr = (EditText) findViewById(R.id.m1_nr);

        title.setText("");
        price.setText("");
        phone.setText("");
        nr.setText("");
        imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
        image = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 获取图片路径
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor c = null;
                try {
                    c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(filePathColumns[0]);
                        String imagePath = c.getString(columnIndex);
                        showImage(imagePath);
                    } else {
                        Toast.makeText(this, "无法获取图片路径", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("AddItem", "图片选择失败", e);
                    Toast.makeText(this, "图片选择失败", Toast.LENGTH_SHORT).show();
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    // 加载图片
    private void showImage(String imagePath) {
        try {
            Bitmap bm = BitmapFactory.decodeFile(imagePath);
            if (bm != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // 压缩图片以避免内存问题
                bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                image = baos.toByteArray();
                imageButton.setImageBitmap(bm);
                Toast.makeText(this, "图片选择成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "图片加载失败，请重新选择", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AddItem", "图片加载失败", e);
            Toast.makeText(this, "图片加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
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