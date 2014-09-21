package com.coinprism.model;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;

import javax.net.ssl.HostnameVerifier;


public class BalanceLoader extends AsyncTask<String, Integer, AddressBalance>
{
    private final String baseUrl;
    private AddressBalance addressBalance;
    private WalletState parent;

    public BalanceLoader(String baseUrl, WalletState parent)
    {
        this.baseUrl = baseUrl;
        this.parent = parent;
    }

    @Override
    protected AddressBalance doInBackground(String... addresses)
    {
//        UncheckedSSLSocketFactory sslFactory = new UncheckedSSLSocketFactory(null);
//        sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//
//        // Enable HTTP parameters
//        HttpParams params = new BasicHttpParams();
//        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//
//        // Register the HTTP and HTTPS Protocols. For HTTPS, register our custom SSL Factory object.
//        SchemeRegistry registry = new SchemeRegistry();
//        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        registry.register(new Scheme("https", sslFactory, 443));
//
//        // Create a new connection manager using the newly created registry and then create a new HTTP client
//        // using this connection manager
//        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        DefaultHttpClient client = new DefaultHttpClient();

        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        registry.register(new Scheme("https", socketFactory, 443));
        SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);

        HttpClient httpclient = new DefaultHttpClient(mgr, client.getParams());
        HttpResponse response;
        String responseString = null;

        try
        {
            response = httpclient.execute(
                    new HttpGet(this.baseUrl + "/v1/addresses/" + addresses[0]));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            }
            else
            {
                // Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

            return parseJson(responseString);
        }
        catch (ClientProtocolException e)
        {
            this.notifyError();
        }
        catch (IOException e)
        {
            this.notifyError();
        }
        catch (JSONException e)
        {
            this.notifyError();
        }

        return null;
    }

    @Override
    protected void onPostExecute(AddressBalance result)
    {
        super.onPostExecute(result);
        //Do anything with response..

        this.parent.updateData(result);
    }

    private AddressBalance parseJson(String json) throws JSONException
    {
        if (json != null)
        {
            JSONObject jObject = new JSONObject(json);
            Calendar c = Calendar.getInstance();
            int seconds = c.get(Calendar.SECOND);
            AssetBalance[] assetBalances = new AssetBalance[]
                    {
                            new AssetBalance(new AssetDefinition("abcdef", "Test Coin" + seconds, "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test1 Coin", "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test2 Coin"+ seconds, "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test2 Coin", "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test3 Coin"+ seconds, "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test4 Coin", "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test5 Coin"+ seconds, "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test Coin", "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test1 Coin"+ seconds, "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test2 Coin", "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test2 Coin", "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test3 Coin"+ seconds, "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test4 Coin", "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test5 Coin"+ seconds, "MACO", 2), new BigInteger("15000")),
                            new AssetBalance(new AssetDefinition("abcdef", "Test6 Coin", "MACO", 2), new BigInteger("15000"))
                    };

            addressBalance = new AddressBalance(156580L, assetBalances);
            return addressBalance;
        }
        else
        {
            return null;
        }
    }

    private void notifyError()
    {

    }

    public AddressBalance getAddressBalance()
    {
        return this.addressBalance;
    }
}
