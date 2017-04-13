package com.example.u0151051.exchange_rate_immediate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Spinner sp;
    Button btn;
    AlertDialog alertDialog;
    int index = 0;
    // 宣告一字串放網址
    String string_url = "http://rate.bot.com.tw/xrt?Lang=zh-TW";
    List<String> buy_cash;// 現金買進
    List<String> sell_cash;// 現金買出
    List<String> buy_curren_rate;// 即期買入
    List<String> sell_current_rate;// 即期賣出
    static String urlData = null;

    //載入畫面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            findid();

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    void findid() {
        sp = (Spinner) findViewById(R.id.spinner);
        sp.setOnItemSelectedListener(onItemSelectedListener);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(c);
        buy_cash = new ArrayList<String>();// 現金買進
        sell_cash = new ArrayList<String>();// 現金買出
        buy_curren_rate = new ArrayList<String>();// 即期買入
        sell_current_rate = new ArrayList<String>();// 即期賣出
        //Android4.0在網路的部份多了一個新的Exception，叫做android.os.NetworkOnMainThreadException
        //意思就是說：網路的活動跑在主要執行緒上了，很貼心的告訴你，這樣子你的APP可能會因為網路的活動等待回應太久，而被系統強制關閉
        //改用要用多執行緒的方式來執行
        new Thread(new Runnable() {
            @Override
            public void run() {
                urlData = GetURLData();
            }
        }).setPriority(Thread.MAX_PRIORITY);
        Parser(urlData);
        alertDialog = getAlertDialog("選擇匯率為");
    }

    View.OnClickListener c = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //按下button顯示對話框
            alertDialog.show();
        }
    };
    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            index = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    //讀網頁
    public String GetURLData() {
        String urlData = null;
        String decodedString;
        try {
            //建立連線物件
            HttpURLConnection hc = null;
            // 產生一個URL類別物件,利用它的有字串的建構子用來定址
            URL url = new URL(string_url);
            //連線
            // URL有一個openConnection()用來建立連線再把連線結果傳回HttpURLConnection
            hc = (HttpURLConnection) url.openConnection();
            hc.setDoInput(true);
            hc.setDoOutput(true);
            hc.connect();
            //用 BufferedReader讀回來(先用getInputStream()串流)
            BufferedReader in = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            while ((decodedString = in.readLine()) != null) {
                urlData += decodedString;
            }
            in.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            Log.e("Error", e.toString());
        }
        return urlData;
    }

    // 傳進來的網頁字串用indexOf比對我們要的資料的位置在哪,再利用substring得到我們要的字串資料
    public void Parser(String urlData) {
        try {
            String temp = null;
            int start1 = 0;
            int end = 0;
            int count = 0;
            do {
                // 1.現金買入
                // indexOf中的html碼要到該網頁去擷取這段html,知道我們要的頭和尾
                start1 = urlData.indexOf("<td data-table=\"本行現金買入\" class=\"rate-content-cash text-right print_hide\">",
                        end + 1);
                end = urlData.indexOf("</td>", start1 + 1);
                temp = urlData.substring(start1 + 72, end);
                //如果無資料
                if (!temp.equals("-")) {
                    buy_cash.add(temp);
                } else {
                    buy_cash.add("無資料");
                }
                // 2.現金賣出
                start1 = urlData.indexOf("<td data-table=\"本行現金賣出\" class=\"rate-content-cash text-right print_hide\">",
                        end + 1);
                end = urlData.indexOf("</td>", start1 + 1);
                temp = urlData.substring(start1 + 72, end);
                if (!temp.equals("-")) {
                    sell_cash.add(temp);
                } else {
                    sell_cash.add("無資料");
                }
                // 3.即期買入
                start1 = urlData.indexOf(
                        "<td data-table=\"本行即期買入\" class=\"rate-content-sight text-right print_hide\" data-hide=\"phone\">",
                        end + 1);
                end = urlData.indexOf("</td>", start1 + 1);
                temp = urlData.substring(start1 + 91, end);
                if (!temp.equals("-")) {
                    buy_curren_rate.add(temp);
                } else {
                    buy_curren_rate.add("無資料");
                }
                //4. 即期賣出
                start1 = urlData.indexOf(
                        "<td data-table=\"本行即期賣出\" class=\"rate-content-sight text-right print_hide\" data-hide=\"phone\">",
                        end + 1);
                end = urlData.indexOf("</td>", start1 + 1);
                temp = urlData.substring(start1 + 91, end);
                if (!temp.equals("-")) {
                    sell_current_rate.add(temp);
                } else {
                    sell_current_rate.add("無資料");
                }
                count++;
            } while (count < 19);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //寫一個方法設定基本對話框
    // AlertDialog單一對話方塊最多只能包含 3 個動作按鈕
    AlertDialog getAlertDialog(String title) {
        //產生一個Builder物件(要選android app那個)
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        //設定Dialog的標題(標題可有可無)
        builder.setTitle(title);
        //設定Dialog的內容(一定要有)
        builder.setMessage("現金匯率:\n買入=" + buy_cash.get(index) + "\n賣出=" + sell_cash.get(index) + "\n\n即期匯率:\n買入=" + buy_curren_rate.get(index) + "\n賣出=" + sell_current_rate.get(index));
        //設定Positive按鈕資料:AlertDialog.Builder setPositiveButton (CharSequence text,DialogInterface.OnClickListener listener)
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "您按下OK按鈕", Toast.LENGTH_SHORT).show();
            }
        });
        //利用Builder的create()方法建立AlertDialog
        return builder.create();
    }


}
