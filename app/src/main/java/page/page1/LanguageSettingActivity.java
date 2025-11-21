package page.page1;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class LanguageSettingActivity extends AppCompatActivity {

    private Button btnEnglish, btnChinese;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_setting);

        btnEnglish = findViewById(R.id.btn_english);
        btnChinese = findViewById(R.id.btn_chinese);

        // 切换为英文
        btnEnglish.setOnClickListener(v -> switchLanguage("en"));
        // 切换为中文
        btnChinese.setOnClickListener(v -> switchLanguage("zh"));
    }

    // 切换应用语言
    private void switchLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        config.setLocale(locale);
        resources.updateConfiguration(config, dm);

        // 重启应用生效（返回用户中心页面）
        Intent intent = new Intent(this, MyselfActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}