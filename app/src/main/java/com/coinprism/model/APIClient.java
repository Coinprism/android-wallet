package com.coinprism.model;

import android.util.JsonReader;
import android.util.JsonToken;

import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
                    jObject.getInt("divisibility"),
                    jObject.getString("icon_url"));
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
    }

    public List<SingleAssetTransaction> getTransactions(String address)
        throws IOException, JSONException, ParseException
    {
        String json = executeHttp(new HttpGet(
            this.baseUrl + "/v1/addresses/" + address + "/transactions"));

        JSONArray transactions = new JSONArray(json);

        ArrayList<SingleAssetTransaction> assetBalances = new ArrayList<SingleAssetTransaction>();

        for (int i = 0; i < transactions.length(); i++)
        {
            JSONObject transactionObject = (JSONObject) transactions.get(i);
            String transactionId = transactionObject.getString("hash");
            Date date = parseDate(
                transactionObject.getString("block_time"));

            HashMap<String, BigInteger> quantities = new HashMap<String, BigInteger>();
            BigInteger satoshiDelta = BigInteger.ZERO;

            JSONArray inputs = transactionObject.getJSONArray("inputs");
            for (int j = 0; j < inputs.length(); j++)
            {
                JSONObject input = (JSONObject) inputs.get(j);
                if (isAddress(input, address))
                {
                    if (!input.isNull("asset_address"))
                        addQuantity(quantities,
                            input.getString("asset_address"),
                            new BigInteger(input.getString("asset_quantity")).negate());

                    satoshiDelta = satoshiDelta.subtract(
                        BigInteger.valueOf(input.getLong("value")));
                }
            }

            JSONArray outputs = transactionObject.getJSONArray("outputs");
            for (int j = 0; j < outputs.length(); j++)
            {
                JSONObject output = (JSONObject) outputs.get(j);
                if (isAddress(output, address))
                {
                    if (!output.isNull("asset_address"))
                        addQuantity(quantities,
                            output.getString("asset_address"),
                            new BigInteger(output.getString("asset_quantity")));

                    satoshiDelta = satoshiDelta.add(
                        BigInteger.valueOf(output.getLong("value")));
                }
            }

            if (!satoshiDelta.equals(BigInteger.ZERO))
                assetBalances.add(
                    new SingleAssetTransaction(transactionId, date, null, satoshiDelta));

            for (String key : quantities.keySet())
            {
                assetBalances.add(new SingleAssetTransaction(
                    transactionId,
                    date,
                    getAssetDefinition(key),
                    quantities.get(key)));
            }
        }

        return assetBalances;
    }

    public Transaction buildAssetTransaction(String fromAddress, String toAddress, String amount,
        String assetAddress)
    {
        return null;
    }

    public Transaction buildBitcoinTransaction(String fromAddress, String toAddress, String amount)
        throws JSONException, IOException
    {
        try
        {
            JSONObject toObject = new JSONObject();
            toObject.put("address", toAddress);
            toObject.put("amount", amount);
            JSONArray array = new JSONArray();
            array.put(toObject);
            JSONObject postData = new JSONObject();
            postData.put("fees", 10000);
            postData.put("from", fromAddress);
            postData.put("to", array);

            HttpPost post = new HttpPost(this.baseUrl + "/v1/sendbitcoin?format=raw");
            post.setEntity(new StringEntity(postData.toString()));
            post.addHeader("Content-Type", "application/json");
            String result = executeHttp(post);
            JSONObject jsonResponse = new JSONObject(result);

            byte[] data = hexStringToByteArray(jsonResponse.getString("raw"));
            Transaction transaction = new Transaction(
                WalletState.getState().getConfiguration().getNetworkParameters(),
                data);
            transaction.ensureParsed();

            return transaction;
        }
        catch (UnsupportedEncodingException ex)
        {
            return null;
        }
    }

    private static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public String broadcastTransaction(Transaction transaction) throws IOException, JSONException
    {
        String serializedTransaction =
            Utils.bytesToHexString(transaction.bitcoinSerialize());

        try
        {
            HttpPost post = new HttpPost(this.baseUrl + "/v1/sendrawtransaction");
            post.setEntity(new StringEntity("\"" + serializedTransaction + "\""));
            post.addHeader("Content-Type", "application/json");
            String result = executeHttp(post);

            return result.substring(1, result.length() - 2);
        }
        catch (UnsupportedEncodingException ex)
        {
            return null;
        }
    }

    private void addQuantity(HashMap<String, BigInteger> map, String assetAddress,
        BigInteger quantity)
    {
        if (!map.containsKey(assetAddress))
            map.put(assetAddress, quantity);
        else
            map.put(assetAddress, quantity.add(map.get(assetAddress)));
    }

    private static Date parseDate(String input) throws java.text.ParseException
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        return df.parse(input.substring(0, 21));
    }

    private boolean isAddress(JSONObject member, String localAddress) throws JSONException
    {
        JSONArray addresses = member.getJSONArray("addresses");

        return addresses.length() == 1 && addresses.getString(0).equals(localAddress);
    }

    private static String executeHttp(HttpRequestBase url) throws IOException
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
