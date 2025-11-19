package page.page1;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutMainActivity extends AppCompatActivity {
    private Button cometo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_main);

        cometo = (Button) findViewById(R.id.cometo);

        // 添加点击事件监听器
        cometo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接跳转到主页
                Intent intent = new Intent(AboutMainActivity.this, main_page.class);
                startActivity(intent);

                // 可选：结束当前Activity
                // finish();
            }
        });
    }
}