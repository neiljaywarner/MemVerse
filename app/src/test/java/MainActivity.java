import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.spiritflightapps.memverse.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

// this is calls Web page when register button pressed!


    public void open_uri(View view) {
        Uri webpage = Uri.parse("https://www.memverse.com/users/sign_up");
        Intent intent = new Intent(Intent.ACTION_VIEW,webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

// this is calls Web page when sign in  button pressed!


        public void open_sing_in(View view){
            Uri webpage2 = Uri.parse("https://www.memverse.com/users/sign_in");
            Intent intent2 = new Intent(Intent.ACTION_VIEW,webpage2);
            if (intent2.resolveActivity(getPackageManager()) != null) {
                startActivity(intent2);
            }
        }
    }

