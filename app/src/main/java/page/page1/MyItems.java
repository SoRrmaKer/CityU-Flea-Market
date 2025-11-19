package page.page1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static page.page1.LoginMainActivity.post_userid;

public class MyItems extends AppCompatActivity implements View.OnClickListener{
    String TABLENAME = "iteminfo";
    byte[] imagedata;
    Bitmap imagebm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseHelper database = new DatabaseHelper(this);
        final SQLiteDatabase db = database.getWritableDatabase();
        ListView listView = (ListView)findViewById(R.id.show_fabu);
        Map<String, Object> item;
        final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        // 关键修复：只查询当前登录用户的商品
        String currentUserId = LoginMainActivity.post_userid;

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "请先登录", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MyItems.this, LoginMainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 只查询当前用户的商品，按时间倒序排列
        Cursor cursor = db.query(
                TABLENAME,
                null,
                "userId=?",  // 只查询当前用户的商品
                new String[]{currentUserId},
                null, null,
                "id DESC"  // 按ID倒序排列，最新的在前面
        );

        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                item = new HashMap<String, Object>();
                item.put("id",cursor.getInt(0));
                item.put("userid",cursor.getString(1));
                item.put("title",cursor.getString(2));
                item.put("kind",cursor.getString(3));
                item.put("info",cursor.getString(4));
                item.put("price",cursor.getString(5));
                imagedata = cursor.getBlob(6);
                imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                item.put("image",imagebm);
                cursor.moveToNext();
                data.add(item);
            }
        } else {
            Toast.makeText(getApplicationContext(), "您还没有发布过商品", Toast.LENGTH_SHORT).show();
        }
        cursor.close();

        // 使用SimpleAdapter布局listview
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, data, R.layout.activity_my_fabu, new String[] { "image", "title", "kind", "info", "price" },
                new int[] { R.id.item_image, R.id.title, R.id.kind, R.id.info, R.id.price });
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView && data instanceof Bitmap){
                    ImageView iv = (ImageView)view;
                    iv.setImageBitmap( (Bitmap)data );
                    return true;
                }else{
                    return false;
                }
            }
        });
        listView.setAdapter(simpleAdapter);

        Button button1=(Button)findViewById(R.id.but1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MyItems.this,main_page.class);
                startActivity(intent);
            }
        });

        Button button2=(Button)findViewById(R.id.but2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MyItems.this,MyItems.class);
                startActivity(intent);
            }
        });

        // 添加列表项点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyItems.this, item_info.class);
                intent.putExtra("id", data.get(position).get("id").toString());
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String delId = data.get(position).get("id").toString();
                String itemUserId = data.get(position).get("userid").toString();

                // 验证当前用户是否有权限删除这个商品
                if (!itemUserId.equals(currentUserId)) {
                    Toast.makeText(getApplicationContext(), "您只能删除自己发布的商品", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if(db.delete(TABLENAME,"id=?",new String[]{delId}) > 0) {
                    Toast.makeText(getApplicationContext(), "删除成功，请刷新", Toast.LENGTH_SHORT).show();
                    // 刷新页面
                    recreate();
                    return true;
                }
                else {
                    Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
        }
    }
}