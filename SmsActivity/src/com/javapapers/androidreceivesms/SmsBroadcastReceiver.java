package com.javapapers.androidreceivesms;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class SmsBroadcastReceiver extends BroadcastReceiver {

	public static final String SMS_BUNDLE = "pdus";
	private static final String SMS_REGEX = ".*driver (.*) \\(\\+*(\\d*).*for(.*)\\..*(KA.*) to pick .*";
	private static final Pattern SMS_PATTERN = Pattern.compile(SMS_REGEX);
	private static final String SMS_REGEX_NEW = ".*Invoice of Rs (\\d*) for (.*) is on its way .*";
	private static final Pattern SMS_PATTERN_NEW = Pattern.compile(SMS_REGEX_NEW);

	@SuppressLint("NewApi")
	public void onReceive(Context context, Intent intent) {
		Bundle intentExtras = intent.getExtras();
		if (intentExtras != null) {
			Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
			String smsMessageStr = "";
			for (int i = 0; i < sms.length; ++i) {
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

				String smsBody = smsMessage.getMessageBody().toString();
				String address = smsMessage.getOriginatingAddress();

				smsMessageStr += "SMS From: " + address + "\n";
				smsMessageStr += smsBody + "\n";

			}
			SmsManager smsmanager = SmsManager.getDefault();
			TelephonyManager tMgr = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			String mPhoneNumber = tMgr.getLine1Number();

			Matcher m = SMS_PATTERN.matcher(smsMessageStr);
			if (m.find()) {
				System.out.println("Found value: " + m.group(0).trim());
				String driverName = m.group(1).trim();
				System.out.println("Driver name: " + driverName);
				String mobileNumber = m.group(2).trim();
				System.out.println("Mobile number : " + mobileNumber);
				String tripNumber = m.group(3).trim().replaceAll("\\s", "");
				System.out.println("Trip id : " + tripNumber);
				String vehileRegNumber = m.group(4).trim().replaceAll("\\s", "");
				System.out.println("Vehile registration number : " + vehileRegNumber);
				Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

				// this will update the UI with message
				SmsActivity inst = SmsActivity.instance();
				inst.updateList(smsMessageStr);
				String webAppLink = "http://knowyourdriver.com/getFeedback?rNumber="
						+ vehileRegNumber + "&dName=" + driverName + "&tNumber=" + tripNumber
						+ "&mNumber=" + mobileNumber + "&source=OLA";
				String smsNew = "Please find feedback for your driver : " + driverName + "("
						+ mobileNumber + ") with vehicle registration number : " + vehileRegNumber
						+ " here - " + webAppLink;
				List<String> messages = smsmanager.divideMessage(smsNew);
				for (String mes : messages) {
					System.out.println("Sending message : " + mes);
					smsmanager.sendTextMessage(mPhoneNumber, null, mes, null, null);
				}
			} else {
				System.out.println("NO MATCH");
				m = SMS_PATTERN_NEW.matcher(smsMessageStr);
				if (m.find()) {
					System.out.println("Found value: " + m.group(0).trim());
					System.out.println("Amount : " + m.group(1).trim());
					String tripNumber = m.group(2).trim().replaceAll("\\s", "");
					System.out.println("Trip id : " + tripNumber);
					Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

					// this will update the UI with message
					SmsActivity inst = SmsActivity.instance();
					inst.updateList(smsMessageStr);
					String webAppLink = "http://knowyourdriver.com/postFeedback?tNumber="
							+ tripNumber + "&source=OLA";
					String smsNew = "Please give honest feedback for your driver for trip : "
							+ tripNumber + " here - " + webAppLink;
					smsmanager.sendTextMessage(mPhoneNumber, null, smsNew, null, null);

				} else {
					System.out.println("NO MATCH Again");
				}
			}

		}
	}
}