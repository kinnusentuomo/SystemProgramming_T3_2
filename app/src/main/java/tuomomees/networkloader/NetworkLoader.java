package tuomomees.networkloader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.SimpleFormatter;

public class NetworkLoader extends AppCompatActivity {

    PrinterThread printer;
    Button buttonGetData;
    EditText writeAddress;
    ListView listView;

    ArrayList<String> listItems=new ArrayList<>();
    ArrayAdapter<String> adapter;

    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_loader);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initializeWidgets();

        printer = new PrinterThread();
        printer.stopThread();
    }

    private void initializeWidgets()
    {
        buttonGetData = (Button) findViewById(R.id.buttonGetData);
        writeAddress = (EditText) findViewById(R.id.WebAddress);
        listView = (ListView) findViewById(R.id.textList);

        adapter=new ArrayAdapter<String>(NetworkLoader.this,
                //android.R.layout.simple_list_item_1,
                R.layout.custom_textview,
                listItems);
        listView.setAdapter(adapter);
    }

    public void onButtonGetDataClicked(View view)
    {

        String httpAddress = writeAddress.getText().toString();
        addTextToListView("HTTP address set: " + httpAddress);

        if(printer.isThreadRunning())
        {
            addTextToListView("Thread is already running, can't start another.");
        }

        else
        {
            printer = new PrinterThread();
            printer.setAddress(httpAddress);
            printer.resumeThread();

            Activity activity = this;
            printer.initializeActivity(activity);

            printer.initializeListArray(listItems);

            addTextToListView("Starting thread.");
            printer.start();
        }
    }

    @SuppressLint("LongLogTag")
    public void addTextToListView(String str)
    {
        //counter++;
        //listItems.add(counter + ": " + str + "\n");

        if(str != null)
        {
            listItems.add(getDate() + ": " + str + "\n");
            adapter.notifyDataSetChanged();
        }
        else
        {
            listItems.add(getDate() + "Failed to add data.");
            adapter.notifyDataSetChanged();
        }

        Log.d("Adding to list", str);
    }

    public String getDate()
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.HOUR, 3);

        @SuppressLint("DefaultLocale") String formattedTime = String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
                );

        return formattedTime;
    }


/*
    class PrinterThread extends Thread implements Runnable{

        private volatile boolean running = true;
        private String threadHttpAddress = null;
        private String allData = null;

        public void run()
        {
            try {
                while (running)
                {
                    loadStuff();
                    updateUiThread();
                    running = false;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        void setAddress(String address)
        {
            threadHttpAddress = address;
        }

        void stopThread()
        {
            running = false;
        }

        void resumeThread()
        {
            running = true;
        }

        boolean isThreadRunning()
        {
            return running;
        }

        public String getData()
        {
            return allData;
        }

        private void loadStuff() {
            HttpURLConnection urlConnection = null;
            boolean working = true;

            try{
                URL url = new URL(threadHttpAddress);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                allData = fromStream(in);
                in.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally{
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
            }
        }

        @NonNull
        private String fromStream(InputStream in) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }
            return out.toString();
        }

        private void updateUiThread() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if(allData == null)
                    {
                        addTextToListView("Can't get data, address may be wrong.");
                    }

                    else
                    {
                        addTextToListView(allData);
                        addTextToListView("All data added, thread finished.");
                    }
                }
            });
        }

    }
*/
}
