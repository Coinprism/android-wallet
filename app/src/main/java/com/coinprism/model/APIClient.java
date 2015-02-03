/*
 * Copyright (c) 2014 Flavien Charlon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.coinprism.model;

import org.bitcoinj.core.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Provides functions for accessing the Coinprism API.
 */
public class APIClient
{
    private final String baseUrl;
    private final HashMap<String, AssetDefinition> cache = new HashMap<String, AssetDefinition>();
    private final static String userAgent = "Coinprism Android";

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

    private AssetDefinition fetchAssetDefinition(String address) throws IOException, APIException
    {
        AssetDefinition definition = cache.get(address);
        if (definition == null)
        {
            String httpResponse = executeHttpGet(this.baseUrl + "/v1/assets/" + address);

            try
            {
                JSONObject jObject = new JSONObject(httpResponse);

                definition = new AssetDefinition(
                    jObject.getString("asset_id"),
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

    /**
     * Gets the balance of an address from the Coinprism API.
     *
     * @param address the address for which to query the balance
     * @return the balance of the address
     */
    public AddressBalance getAddressBalance(String address) throws IOException, JSONException, APIException
    {
        String json = executeHttpGet(this.baseUrl + "/v1/addresses/" + address);

        JSONObject jObject = new JSONObject(json);
        JSONArray assets = jObject.getJSONArray("assets");
        Long bitcoinBalance = jObject.getLong("unconfirmed_balance") + jObject.getLong("balance");

        ArrayList<AssetBalance> assetBalances = new ArrayList<AssetBalance>();

        for (int i = 0; i < assets.length(); i++)
        {
            JSONObject assetObject = (JSONObject) assets.get(i);

            String assetId = assetObject.getString("id");

            BigInteger quantity = new BigInteger(assetObject.getString("balance"))
                    .add(new BigInteger(assetObject.getString("unconfirmed_balance")));

            assetBalances.add(new AssetBalance(fetchAssetDefinition(assetId), quantity));
        }

        return new AddressBalance(bitcoinBalance, assetBalances);
    }

    /**
     * Gets the list of recent transactions for a given address.
     *
     * @param address the address for which to query the recent transactions
     * @return a list of transactions
     */
    public List<SingleAssetTransaction> getTransactions(String address)
        throws IOException, JSONException, ParseException, APIException
    {
        String json = executeHttpGet(
            this.baseUrl + "/v1/addresses/" + address + "/transactions");

        JSONArray transactions = new JSONArray(json);

        ArrayList<SingleAssetTransaction> assetBalances = new ArrayList<SingleAssetTransaction>();

        for (int i = 0; i < transactions.length(); i++)
        {
            JSONObject transactionObject = (JSONObject) transactions.get(i);
            String transactionId = transactionObject.getString("hash");

            Date date = null;
            if (!transactionObject.isNull("block_time"))
                date = parseDate(transactionObject.getString("block_time"));

            HashMap<String, BigInteger> quantities = new HashMap<String, BigInteger>();
            BigInteger satoshiDelta = BigInteger.ZERO;

            JSONArray inputs = transactionObject.getJSONArray("inputs");
            for (int j = 0; j < inputs.length(); j++)
            {
                JSONObject input = (JSONObject) inputs.get(j);
                if (isAddress(input, address))
                {
                    if (!input.isNull("asset_id"))
                        addQuantity(quantities,
                            input.getString("asset_id"),
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
                    if (!output.isNull("asset_id"))
                        addQuantity(quantities,
                            output.getString("asset_id"),
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

    /**
     * Calls the API to create a valid unsigned transaction.
     *
     * @param fromAddress the address from which to send the funds or assets
     * @param toAddress the address where to send the funds or assets
     * @param amount the amount to send, in asset units or satoshis
     * @param assetId the asset ID of the asset to send, or null for bitcoins
     * @param fees the fees to pay, in satoshis
     * @return the unsigned transaction for the requested operation
     */
    public Transaction buildTransaction(String fromAddress, String toAddress, String amount, String assetId, long fees)
        throws JSONException, IOException, APIException
    {
        try
        {
            JSONObject toObject = new JSONObject();
            toObject.put("address", toAddress);
            toObject.put("amount", amount);
            if (assetId != null)
                toObject.put("asset_id", assetId);

            JSONArray array = new JSONArray();
            array.put(toObject);
            JSONObject postData = new JSONObject();
            postData.put("fees", fees);
            postData.put("from", fromAddress);
            postData.put("to", array);

            String result;
            if (assetId != null)
                result = executeHttpPost(this.baseUrl + "/v1/sendasset?format=raw", postData.toString());
            else
                result = executeHttpPost(this.baseUrl + "/v1/sendbitcoin?format=raw", postData.toString());

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

    private static String byteArrayToHexString(byte[] bytes)
    {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Broadcasts a transaction to the Bitcoin network.
     *
     * @param transaction the transaction to broadcast
     * @return the transaction hash of the transaction
     */
    public String broadcastTransaction(Transaction transaction) throws IOException, JSONException, APIException
    {
        String serializedTransaction = byteArrayToHexString(transaction.bitcoinSerialize());

        try
        {
            String result = executeHttpPost(
                this.baseUrl + "/v1/sendrawtransaction", "\"" + serializedTransaction + "\"");

            return result.substring(1, result.length() - 1);
        }
        catch (UnsupportedEncodingException ex)
        {
            return null;
        }
    }

    private void addQuantity(HashMap<String, BigInteger> map, String assetId,
        BigInteger quantity)
    {
        if (!map.containsKey(assetId))
            map.put(assetId, quantity);
        else
            map.put(assetId, quantity.add(map.get(assetId)));
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

    protected String executeHttpGet(String url) throws IOException, APIException
    {
        URL target = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) target.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", userAgent);

        return getHttpResponse(connection);
    }

    protected String executeHttpPost(String url, String body) throws IOException, APIException
    {
        URL target = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) target.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Content-Type", "application/json");

        OutputStream output = null;
        try
        {
            output = connection.getOutputStream();
            output.write(body.getBytes("UTF-8"));
        }
        finally
        {
            if (output != null)
                output.close();
        }

        return getHttpResponse(connection);
    }

    private static String getHttpResponse(HttpsURLConnection connection) throws IOException, APIException
    {
        int responseCode = connection.getResponseCode();

        if (responseCode < 400)
        {
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());

            return readStream(inputStream);
        }
        else
        {
            InputStream inputStream = new BufferedInputStream(connection.getErrorStream());
            String response = readStream(inputStream);
            try
            {
                JSONObject error = new JSONObject(response);
                String errorCode = error.getString("ErrorCode");
                String subCode = error.optString("SubCode");

                throw new APIException(errorCode, subCode);
            }
            catch (JSONException exception)
            {
                throw new IOException(exception.getMessage());
            }
        }
    }

    private static String readStream(InputStream in)
    {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try
        {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null)
            {
                response.append(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
