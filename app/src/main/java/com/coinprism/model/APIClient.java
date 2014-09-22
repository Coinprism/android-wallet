package com.coinprism.model;

import android.util.JsonToken;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class APIClient
{
    private final String baseUrl;
    private final HashMap<String, AssetDefinition> cache = new HashMap<String, AssetDefinition>();

    public APIClient(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public AssetDefinition getAssetDefinition(String address)
    {
        return cache.get(address);
    }

    public Collection<AssetDefinition> getAllAssetDefinitions()
    {
        return cache.values();
    }

    private AssetDefinition fetchAssetDefinition(String address) throws IOException
    {
        AssetDefinition definition = cache.get(address);
        if (definition == null)
        {
            String httpResponse = executeHttp(new HttpGet(this.baseUrl + "/v1/assets/" + address));

            try
            {
                JSONObject jObject = new JSONObject(httpResponse);

                definition = new AssetDefinition(
                    jObject.getString("asset_address"),
                    jObject.getString("name"),
                    jObject.getString("name_short"),
                    jObject.getInt("divisibility")
                );
            }
            catch (JSONException ex)
            {
                definition = new AssetDefinition(address);
            }

            cache.put(address, definition);
        }

        return definition;
    }

    public AddressBalance getAddressBalance(String address) throws IOException, JSONException
    {
        String json = executeHttp(new HttpGet(this.baseUrl + "/v1/addresses/" + address));

        JSONObject jObject = new JSONObject(json);
        JSONArray assets = jObject.getJSONArray("assets");
        Long bitcoinBalance = jObject.getLong("unconfirmed_balance") + jObject.getLong("balance");

        ArrayList<AssetBalance> assetBalances = new ArrayList<AssetBalance>();

        for (int i = 0; i < assets.length(); i++)
        {
            JSONObject assetObject = (JSONObject) assets.get(i);

            String assetAddress = assetObject.getString("address");

            BigInteger quantity = new BigInteger(assetObject.getString("balance"))
                    .add(new BigInteger(assetObject.getString("unconfirmed_balance")));

            assetBalances.add(new AssetBalance(fetchAssetDefinition(assetAddress), quantity));
        }

        return new AddressBalance(bitcoinBalance, assetBalances);
//        Calendar c = Calendar.getInstance();
//        int seconds = c.get(Calendar.SECOND);
//        AssetBalance[] assetBalances = new AssetBalance[]
//                {
//                        new AssetBalance(new AssetDefinition("abcdef", "Test Coin" + seconds, "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test1 Coin", "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test2 Coin" + seconds, "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test2 Coin", "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test3 Coin" + seconds, "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test4 Coin", "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test5 Coin" + seconds, "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test Coin", "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test1 Coin" + seconds, "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test2 Coin", "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test2 Coin", "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test3 Coin" + seconds, "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test4 Coin", "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test5 Coin" + seconds, "MACO", 2), new BigInteger("15000")),
//                        new AssetBalance(new AssetDefinition("abcdef", "Test6 Coin", "MACO", 2), new BigInteger("15000"))
//                };
//
//        AddressBalance addressBalance = new AddressBalance(156580L, assetBalances);
//        return addressBalance;
    }

    private static String executeHttp(HttpUriRequest url) throws IOException
    {
        HttpClient httpclient;
        UncheckedSSLSocketFactory sslFactory;
        try
        {
            sslFactory = new UncheckedSSLSocketFactory(null);
            sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }
        catch (Exception ex)
        {
            throw new IOException(ex.getMessage(), ex);
        }

        // Enable HTTP parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        // Register the HTTP and HTTPS Protocols. For HTTPS, register our custom SSL Factory object.
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", sslFactory, 443));

        // Create a new connection manager using the newly created registry and then create a new HTTP client
        // using this connection manager
        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

        //HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
//        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.;
//
        DefaultHttpClient client = new DefaultHttpClient();
//
//        SchemeRegistry registry = new SchemeRegistry();
//        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
//        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
//        registry.register(new Scheme("https", socketFactory, 443));
//        SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);

        httpclient = new DefaultHttpClient(ccm, client.getParams());

        HttpResponse response;

        response = httpclient.execute(url);
        StatusLine statusLine = response.getStatusLine();
        String responseString = null;
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

        return responseString;
    }
}
