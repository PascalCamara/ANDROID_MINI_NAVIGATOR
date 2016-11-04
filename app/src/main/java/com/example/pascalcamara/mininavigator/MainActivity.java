package com.example.pascalcamara.mininavigator;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TextView textResult = (TextView) findViewById(R.id.searchResult);
        // faire disparaitre le layout result

        Button subtmitSearch = (Button) findViewById(R.id.searchSubmit);
        subtmitSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSearchButton();
            }
        });

        Button buttonweb = (Button) findViewById(R.id.buttonWeb);
        buttonweb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInWebAction();
            }
        });

    }

    private void openInWebAction() {
        EditText searchEdit = (EditText) findViewById(R.id.searchBar);
        String searchText = searchEdit.getText().toString();
        if (searchText.length() > 0 ) {
            final String url = searchText.replace("http://", "");
            Uri uri = Uri.parse("http://"+url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    /**
     *  hash function
     *  source : http://www.kospol.gr/204/create-md5-hashes-in-android/
     * @param s string
     * @return string
     */
    public static final String md5(final String s) {
        try {
            // create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void doSearchButton() {
        EditText barSearch = (EditText) findViewById(R.id.searchBar);
        Editable bst = barSearch.getText();
        String bsts = bst.toString();

        // url tapé par l'utilisateur
        final String url = bsts.replace("http://", "");
        Log.d("debug test", url);

        // hash de lurl
        String hashUrl = md5(url);
        Log.d("hash url", hashUrl);

        // verrification si il existe en cache
        File internal = getFilesDir();
        final File f = new File(internal, hashUrl);

        // vérification dernière date de modification
        if (f.exists()) {
            Date lastModif = new Date(f.lastModified());
            //Date lastModif = new Date(116, Calendar.OCTOBER, 27);
            Date now = new Date();

            Long lastModifTime = lastModif.getTime();
            Long nowTime = now.getTime();

            Long dateCompare = (nowTime - lastModifTime) / (1000 * 60 * 60 * 24);

            if (dateCompare >=  7)
                f.delete();
        }

        if (f.exists()) {

            // lecture des données en cache
            String readFileContent = "";
            try {
                FileInputStream fis = new FileInputStream(f);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String s = br.readLine();
                while (s != null) {
                    readFileContent = readFileContent + s;
                    s = br.readLine();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // affichage des données
            TextView searchResult = (TextView) findViewById(R.id.searchResult);
            searchResult.setText(readFileContent);

            /*add scroll
             * http://stackoverflow.com/questions/1748977/making-textview-scrollable-in-android
             */
            searchResult.setMovementMethod(new ScrollingMovementMethod());

        } else {

            // requette http
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    String htmlContent = "";
                    try {
                        URL u = new URL("http://"+ url);
                        HttpURLConnection c = (HttpURLConnection) u.openConnection();
                        InputStream is = c.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String s = br.readLine();
                        while (s != null) {
                            htmlContent = htmlContent + s + "\n";
                            s = br.readLine();
                        }
                        Log.d("html content",htmlContent);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String finalHtmlContent = htmlContent;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //sauvegarde des données en cache
                            try {
                                FileOutputStream is = new FileOutputStream(f);
                                OutputStreamWriter osw = new OutputStreamWriter(is);
                                BufferedWriter bw = new BufferedWriter(osw);
                                bw.write(finalHtmlContent);
                                bw.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // affichage des données
                            TextView searchResult = (TextView) findViewById(R.id.searchResult);
                            searchResult.setText(finalHtmlContent);

                            /*add scroll
                             * http://stackoverflow.com/questions/1748977/making-textview-scrollable-in-android
                             */
                            searchResult.setMovementMethod(new ScrollingMovementMethod());

                        }
                    });
                }
            });
            t.start();
        }

    }

}
