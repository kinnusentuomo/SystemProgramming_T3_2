package tuomomees.networkloader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


/**
 * Luokan on luonut tuomo päivämäärällä 18.9.2017.
 */

class PrinterThread extends Thread implements Runnable{

    private volatile boolean running = true;
    private String threadHttpAddress = null;
    private String allData = null;

    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;

    private Activity networkLoaderActivity;

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

    void initializeActivity(Activity c)
    {
        networkLoaderActivity = c;
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

    void initializeListArray(ArrayList array)
    {
        listItems = array;
    }

    private String getDate()
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.HOUR, 3);

        @SuppressLint("DefaultLocale") String formattedTime = String.format("%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        );

        return formattedTime;
    }

    private void addTextToListView(String str)
    {
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

    private void updateUiThread() {

        networkLoaderActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean runThis = true;
                while (runThis)
                {
                    //Initializing LisView
                    ListView text;
                    text = (ListView) networkLoaderActivity.findViewById(R.id.textList);

                    adapter=new ArrayAdapter<>(networkLoaderActivity,
                            R.layout.custom_textview,
                            listItems);
                    text.setAdapter(adapter);
                    //END

                    if(allData != null)
                    {
                        addTextToListView(allData);
                    }
                    else
                    {
                        addTextToListView("allData = null");
                    }

                    runThis = false;
                }
            }
        });
    }

}