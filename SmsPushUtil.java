package sendSms;


import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;



public class SmsPushUtil {
	
	static Properties properties = new Properties();
	private static final Logger logger = Logger.getLogger(SmsPushUtil.class.getName());
	static {
        try {
            FileHandler fh = new FileHandler("SmsPushUtil.log", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	
	 // Database credentials and URL (replace with your actual credentials)

    private static final String DB_URL = properties.getProperty("db_url");
    private static final String USER = properties.getProperty("user");
    private static final String PASS = properties.getProperty("pass");

    // Constant parameters
    private static final String SRC_CHANNEL = properties.getProperty("src_channel");
    private static final String USERNAME = properties.getProperty("username");
    private static final String PASSWORD = properties.getProperty("password");
    private static final String MESSAGE_CONTENT = properties.getProperty("message_content");
    private static final String KEY = properties.getProperty("key_content");
    
    private static final boolean SMS_INTEGRATION = Boolean.parseBoolean(properties.getProperty("sms_integration"));
   
    
//  public void fetchAndSendMessage() {
//    System.out.println("Inside fetch and send message ===" + DB_URL);
//    logger.info("Inside fetch and send message ===" + DB_URL);
//
//    if (SMS_INTEGRATION) {
//        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
//            String sql = "SELECT cp.mobile_number, cp.ACCOUNT_ID, adr.TRANSACTION_AMOUNT, adr.TRANSACTION_DATE_TIME FROM ENTPROD.CUSTOMER_PROFILE cp, ENTPROD.ALERT_DENORMALIZATION_RT adr WHERE cp.ACCOUNT_ID = adr.ACCOUNT_ID";
//
//            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
//                ResultSet rs = pstmt.executeQuery();
//                while (rs.next()) {
//                    String mobileNo = rs.getString("mobile_number");
//                    String ACNO = rs.getString("ACCOUNT_ID");
//                    String AMOUNT = rs.getString("TRANSACTION_AMOUNT");
//                    String TXNDATETIME = rs.getString("TRANSACTION_DATE_TIME");
//                    String lastFourDigits = ACNO.length() > 4 ? ACNO.substring(ACNO.length() - 4) : ACNO;
//                    String sendMsg = "Dear Customer, Funds transfer was observed in your A/C ending with " + lastFourDigits + " for amount of Rs" + AMOUNT + " on " + TXNDATETIME + ". Not You? Dial 18005721916 - KVB";
//                    System.out.println("Sending message to: " + mobileNo);
//
//                    if (mobileNo != null) {
//                        sendMessage(mobileNo, sendMsg, KEY);
//                    }
//
//                    logger.info("Query for sending message" + sql);
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//}

    
    public static void fetchAndSendMessage(Long ref_No) {
    	
    	ArrayList<String> dataList = new ArrayList<String>();
    	System.out.println("Inside fetch and send message ==="+DB_URL);
    	logger.info("Inside fetch and send message ==="+DB_URL);
        // Connect to the database and fetch mobile numbers
    	if (SMS_INTEGRATION) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT cp.mobile_number, cp.ACCOUNT_ID , adr.TRANSACTION_AMOUNT, adr.TRANSACTION_DATE_TIME FROM ENTPROD.CUSTOMER_PROFILE cp, ENTPROD.ALERT_DENORMALIZATION_RT adr WHERE cp.ACCOUNT_ID  = adr.ACCOUNT_ID AND adr.ALERT_ID = " +ref_No+ ""; // Adjust SQL query based on your schema
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                	dataList.add(rs.getString("mobile_number"));
                	dataList.add(rs.getString("ACCOUNT_ID"));
                	dataList.add(rs.getString("TRANSACTION_AMOUNT"));
                	dataList.add(rs.getString("TRANSACTION_DATE_TIME"));
                    String lastFourDigits = dataList.get(1).length() > 4 ? dataList.get(1).substring(dataList.get(1).length() - 4) : dataList.get(1);
                    String sendMsg = "Dear Customer, Funds transfer was observed in your A/C ending with " + lastFourDigits+ "for amount of Rs" +dataList.get(2)+ "on" +dataList.get(3)+ ". Not You? Dial 18005721916 - KVB";
                    System.out.println("Sending message to: " + dataList.get(0));
                    if (dataList.get(0)!= null)
                    sendMessage(dataList.get(0),sendMsg, KEY);
                    logger.info("Query for sending message"+sql);
                    //sendMessage(mobileNo, KEY);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	}
		
    }
    
    

    public static void sendMessage(String mobileNo, String sendMsg, String key) {
//    public void sendMessage(String mobileNo, String key) {
    	System.out.println("Inside send Message "+ mobileNo );
    	logger.info("Inside send Message "+ mobileNo);    	 try (CloseableHttpClient client = HttpClients.custom()
    	            .setDefaultRequestConfig(RequestConfig.custom()
    	                    .setConnectTimeout(10 * 1000)
    	                    .setSocketTimeout(10 * 1000)
    	                    .build())
    	            .build()) {
            String url = "https://www.kvbbank.net/bpmsUAT/rest/CueRest/invokeESBService/SMSPush";
            System.out.println("URL===="+ url);
            
         // Create a JSON object for MobileNo and Message
            JSONObject mobileMessageJson = new JSONObject();
            mobileMessageJson.put("MobileNo", mobileNo);
            mobileMessageJson.put("Message", sendMsg);
            

            // Encrypt this JSON string
            String encryptStr = encryptMsg(mobileMessageJson.toString(), key);
//          String encryptStr = encryptMsg(sendMsg, key);
            
            // Create the full JSON payload with encrypted request
            JSONObject msg = new JSONObject();
            msg.put("Src_Channel", SRC_CHANNEL);
            msg.put("UserName", USERNAME);
            msg.put("Password", PASSWORD);
            msg.put("encryptReq", encryptStr);
            
            JSONObject inputVariables = new JSONObject();
            inputVariables.put("in_msg", msg);
            
            StringEntity entity = new StringEntity(inputVariables.toString());
            HttpPost post = new HttpPost(url);
            post.setEntity(entity);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");

            HttpResponse response = client.execute(post);

         // Read the response
            String responseString = EntityUtils.toString(response.getEntity());
            System.out.println("Response: " + responseString);

            // Additional logic to check if the response indicates a successful operation
            JSONObject responseObject = new JSONObject(responseString);
            if (responseObject.optString("bpms_error_code").equals("00")) {
            		String msg_response = responseObject.optString("encryptRes");
            		String plainMsg = decryptMsg(msg_response,KEY);
                System.out.println("Test SMS sent successfully"+plainMsg);
                logger.info("SMS sent successfully==>"+plainMsg);
                
            } else {
                System.out.println("Failed to send SMS. Error: " + responseObject.optString("ErrorMessage"));
            }
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private static String encryptMsg(String messageContent, String key) throws Exception {
    	 String encryptedMessage = AESEncryption.encryptAES256ECB(messageContent, key);
		return encryptedMessage;
		// TODO Auto-generated method stub
		
	}
    
    private static String decryptMsg(String messageContent, String key) throws Exception {
   	 String decryptedMessage = AESEncryption.decryptAES256ECB(messageContent, key);
		return decryptedMessage;
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
    	System.out.println("Inside Main method======>");
    	//SmsPushUtil client = new SmsPushUtil();
       // client.fetchAndSendMessage(ref_No);
        System.out.println("Main method Executed Succesfully.......");
    }
}
