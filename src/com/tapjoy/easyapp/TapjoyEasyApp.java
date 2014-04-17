package com.tapjoy.easyapp;

import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tapjoy.TJError;
import com.tapjoy.TJEvent;
import com.tapjoy.TJEventCallback;
import com.tapjoy.TJEventRequest;
import com.tapjoy.TapjoyAwardPointsNotifier;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyConnectNotifier;
import com.tapjoy.TapjoyConstants;
import com.tapjoy.TapjoyDisplayAdNotifier;
import com.tapjoy.TapjoyEarnedPointsNotifier;
import com.tapjoy.TapjoyFullScreenAdNotifier;
import com.tapjoy.TapjoyLog;
import com.tapjoy.TapjoyNotifier;
import com.tapjoy.TapjoyOffersNotifier;
import com.tapjoy.TapjoySpendPointsNotifier;
import com.tapjoy.TapjoyVideoNotifier;
import com.tapjoy.TapjoyViewNotifier;
import com.tapjoy.TapjoyViewType;


public class TapjoyEasyApp extends Activity implements View.OnClickListener, TapjoyNotifier
{
	TextView pointsTextView;
	TextView tapjoySDKVersionView;

	String displayText = "";
	boolean earnedPoints = false;
	
	// For the display ad.
	View adView;
	RelativeLayout relativeLayout;
	LinearLayout adLinearLayout;
	
	// UI elements
	private Button getPointsButton;
	private Button offers;
	private Button spendPoints;
	private Button awardPoints;
	private Button getDirectPlayVideoAd;
	private Button displayAd;
	private Button sendEvent;
	
	public static final String TAG = "TAPJOY EASY APP";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// OPTIONAL: For custom startup flags.
		Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
		connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");
		
		// If you are not using Tapjoy Managed currency, you would set your own user ID here.
		//	connectFlags.put(TapjoyConnectFlag.USER_ID, "A_UNIQUE_USER_ID");
	   
		// You can also set your event segmentation parameters here.
		//  Hashtable<String, String> segmentationParams = new Hashtable<String, String>();
		//  segmentationParams.put("iap", "true");
		//  connectFlags.put(TapjoyConnectFlag.SEGMENTATION_PARAMS, segmentationParams);
		
		// Connect with the Tapjoy server.  Call this when the application first starts.
		// REPLACE THE APP ID WITH YOUR TAPJOY APP ID.
		String tapjoyAppID = "bba49f11-b87f-4c0f-9632-21aa810dd6f1";
		// REPLACE THE SECRET KEY WITH YOUR SECRET KEY.
		String tapjoySecretKey = "yiQIURFEeKm0zbOggubu";
		
		// NOTE: This is the only step required if you're an advertiser.
		TapjoyConnect.requestTapjoyConnect(getApplicationContext(), tapjoyAppID, tapjoySecretKey, connectFlags, new TapjoyConnectNotifier()
		{
			@Override
			public void connectSuccess() {
				onConnectSuccess();
			}

			@Override
			public void connectFail() {
				onConnectFail();
			}
		});
		
		relativeLayout = (RelativeLayout)findViewById(R.id.RelativeLayout01);
		adLinearLayout = (LinearLayout)findViewById(R.id.AdLinearLayout);
		
		// This button launches the offers page when clicked.
		offers = (Button) findViewById(R.id.OffersButton);
		offers.setOnClickListener(this);
		
		// This button retrieves the virtual currency info from the server.
		getPointsButton = (Button) findViewById(R.id.GetPointsButton);
		getPointsButton.setOnClickListener(this);
		
		// This spends virtual currency for this device.
		spendPoints = (Button) findViewById(R.id.SpendPointsButton);
		spendPoints.setOnClickListener(this);
		
		// This awards virtual currency for this device.
		awardPoints = (Button) findViewById(R.id.AwardPointsButton);
		awardPoints.setOnClickListener(this);
		
		// This button displays a direct play video when clicked.
		getDirectPlayVideoAd = (Button) findViewById(R.id.GetDirectPlayVideoAd);
		getDirectPlayVideoAd.setOnClickListener(this);
		
		// This button displays the Display ad when clicked.
		displayAd = (Button) findViewById(R.id.DisplayAd);
		displayAd.setOnClickListener(this);
		
		// Events
		sendEvent = (Button) findViewById(R.id.SendEventButton);
		sendEvent.setOnClickListener(this);
		
		pointsTextView = (TextView)findViewById(R.id.PointsTextView);
		
		tapjoySDKVersionView = (TextView)findViewById(R.id.TapjoySDKVersionView);
		tapjoySDKVersionView.setText("SDK version: " + TapjoyConstants.TJC_LIBRARY_VERSION_NUMBER);
	}

	public void onConnectSuccess()
	{
		// NOTE:  The get/spend/awardTapPoints methods will only work if your virtual currency
		// is managed by Tapjoy.
		//
		// For NON-MANAGED virtual currency, TapjoyConnect.getTapjoyConnectInsance().setUserID(...)
		// must be called after requestTapjoyConnect.

		// Get notifications whenever Tapjoy currency is earned.
		TapjoyConnect.getTapjoyConnectInstance().setEarnedPointsNotifier(new TapjoyEarnedPointsNotifier()
		{
			@Override
			public void earnedTapPoints(int amount)
			{
				earnedPoints = true;
				updateTextInUI("You've just earned " + amount + " Tap Points!");
				showPopupMessage("You've just earned " + amount + " Tap Points!");
			}
		});
		
		// Get notifications when Tapjoy views open or close.
		TapjoyConnect.getTapjoyConnectInstance().setTapjoyViewNotifier(new TapjoyViewNotifier()
		{
			@Override
			public void viewWillOpen(int viewType)
			{
				TapjoyLog.i(TAG, getViewName(viewType) + " is about to open");
			}
			
			@Override
			public void viewWillClose(int viewType)
			{
				TapjoyLog.i(TAG, getViewName(viewType) + " is about to close");
			}
			
			@Override
			public void viewDidOpen(int viewType)
			{
				TapjoyLog.i(TAG, getViewName(viewType) + " did open");
			}
			
			@Override
			public void viewDidClose(int viewType)
			{
				TapjoyLog.i(TAG, getViewName(viewType) + " did close");
				
				// Best Practice: We recommend calling getTapPoints as often as possible so the userÕs balance is always up-to-date.
				TapjoyConnect.getTapjoyConnectInstance().getTapPoints(TapjoyEasyApp.this);
			}
		});
		
		// Get notifications on video start, complete and error
		TapjoyConnect.getTapjoyConnectInstance().setVideoNotifier(new TapjoyVideoNotifier() {

			@Override
			public void videoStart() {
				Log.i(TAG, "video has started");
			}

			@Override
			public void videoError(int statusCode) {
				Log.i(TAG, "there was an error with the video: " + statusCode);
			}

			@Override
			public void videoComplete() {
				Log.i(TAG, "video has completed");
				
				// Best Practice: We recommend calling getTapPoints as often as possible so the userÕs balance is always up-to-date.
				TapjoyConnect.getTapjoyConnectInstance().getTapPoints(TapjoyEasyApp.this);
			}
			
		});
	}
	
	public void onConnectFail()
	{
		Log.e(TAG, "Tapjoy connect call failed.");
		updateTextInUI("Tapjoy connect failed!");
	}
	
	public void onClick(View v)
	{
		if (v instanceof Button) 
		{
			final Button button = ((Button) v);
			int id = button.getId();
			
			switch (id)
			{
				//--------------------------------------------------------------------------------
				// GET TAP POINTS
				//--------------------------------------------------------------------------------
				case R.id.GetPointsButton:
					// Disable button
					button.setEnabled(false);
					// Retrieve the virtual currency amount from the server.
					TapjoyConnect.getTapjoyConnectInstance().getTapPoints(TapjoyEasyApp.this);
					break;
					
				//--------------------------------------------------------------------------------
				// SPEND TAP POINTS
				//--------------------------------------------------------------------------------
				case R.id.SpendPointsButton:
					// Disable button
					button.setEnabled(false);
					// Spend virtual currency.
					TapjoyConnect.getTapjoyConnectInstance().spendTapPoints(25, new TapjoySpendPointsNotifier()
					{
						@Override
						public void getSpendPointsResponseFailed(String error)
						{
							updateTextInUI("spendTapPoints error: " + error);
							reenableButtonInUI(button);
						}
						
						@Override
						public void getSpendPointsResponse(String currencyName, int pointTotal)
						{
							updateTextInUI(currencyName + ": " + pointTotal);
							reenableButtonInUI(button);
						}
					});
					break;
					
				//--------------------------------------------------------------------------------
				// AWARD TAP POINTS
				//--------------------------------------------------------------------------------
				case R.id.AwardPointsButton:
					// Disable button
					button.setEnabled(false);
					// Award virtual currency.
					TapjoyConnect.getTapjoyConnectInstance().awardTapPoints(10, new TapjoyAwardPointsNotifier()
					{
						@Override
						public void getAwardPointsResponseFailed(String error)
						{
							updateTextInUI("awardTapPoints error: " + error);
							reenableButtonInUI(button);
						}
						
						@Override
						public void getAwardPointsResponse(String currencyName, int pointTotal)
						{
							updateTextInUI(currencyName + ": " + pointTotal);
							reenableButtonInUI(button);
						}
					});
					break;
					
				//--------------------------------------------------------------------------------
				// SHOW OFFERS
				//--------------------------------------------------------------------------------
				case R.id.OffersButton:
					// Disable button
					button.setEnabled(false);
					// Show the Offers web view from where users can download the latest offers for virtual currency.
					TapjoyConnect.getTapjoyConnectInstance().showOffers(new TapjoyOffersNotifier()
					{
						@Override
						public void getOffersResponse()
						{
							updateTextInUI("showOffers succeeded");
							reenableButtonInUI(button);
						}
						
						@Override
						public void getOffersResponseFailed(String error)
						{
							updateTextInUI("showOffers error: " + error);
							reenableButtonInUI(button);
						}
					});
					break;
					
				//--------------------------------------------------------------------------------
				// DIRECT PLAY VIDEO AD
				//--------------------------------------------------------------------------------
				case R.id.GetDirectPlayVideoAd:
					// Disable button
					button.setEnabled(false);
					// Shows a direct play video
					// The getFullScreenAd call has been deprecated and should only be used for getting direct play videos. 
					// To show a full screen ad please use the Events framework -- see EVENTS section below
					TapjoyConnect.getTapjoyConnectInstance().getFullScreenAd(new TapjoyFullScreenAdNotifier()
					{
						@Override
						public void getFullScreenAdResponseFailed(int error)
						{
							updateTextInUI("getFullScreenAd error: " + error);
							reenableButtonInUI(button);
						}
						
						@Override
						public void getFullScreenAdResponse()
						{
							updateTextInUI("getFullScreenAd success");
							TapjoyConnect.getTapjoyConnectInstance().showFullScreenAd();
							reenableButtonInUI(button);
						}
					});
					break;
                    
				// --------------------------------------------------------------------------------
				// BANNER/DISPLAY ADS
				// --------------------------------------------------------------------------------
				case R.id.DisplayAd:
					// Disable button
					button.setEnabled(false);
					// Show the display/banner ad.
					TapjoyConnect.getTapjoyConnectInstance().enableDisplayAdAutoRefresh(true);
					TapjoyConnect.getTapjoyConnectInstance().getDisplayAd(TapjoyEasyApp.this, new TapjoyDisplayAdNotifier()
					{
						@Override
						public void getDisplayAdResponseFailed(String error)
						{
							updateTextInUI("getDisplayAd error: " + error);
							reenableButtonInUI(button);
						}
						
						@Override
						public void getDisplayAdResponse(View view)
						{
							// Using screen width, but substitute for the any width.
							int desired_width = adLinearLayout.getMeasuredWidth();
							
							// Scale the display ad to fit incase the width is smaller than the display ad width.
							adView = scaleDisplayAd(view, desired_width);
							
							updateDisplayAdInUI(adView);
							updateTextInUI("getDisplayAd success");
							reenableButtonInUI(button);
						}
					});
					break;
					
				// --------------------------------------------------------------------------------
				// Events
				// --------------------------------------------------------------------------------
				case R.id.SendEventButton:
					button.setEnabled(false);
					
					TJEvent evt = new TJEvent(this, "test_unit", new TJEventCallback()
					{
						@Override
						public void sendEventCompleted(TJEvent event, boolean contentAvailable) {
							reenableButtonInUI(button);
							updateTextInUI("Tapjoy send event completed, contentAvailable: " + contentAvailable);
							if (contentAvailable) {
								// If enableAutoPresent is set to false for the event, we need to present the event's content ourselves
								event.showContent();
							}
						}

						@Override
						public void sendEventFail(TJEvent event, TJError error) {
							reenableButtonInUI(button);
							updateTextInUI("Tapjoy send event failed with error: " + error.message);
							
						}

						@Override
						public void contentDidShow(TJEvent event) {
							TapjoyLog.i(TAG, "Tapjoy event content did show");
							
						}

						@Override
						public void contentDidDisappear(TJEvent event) {
							TapjoyLog.i(TAG, "Tapjoy event content did disappear");
							
							// Best Practice: We recommend calling getTapPoints as often as possible so the userÕs balance is always up-to-date.
							TapjoyConnect.getTapjoyConnectInstance().getTapPoints(TapjoyEasyApp.this);
						}

						@Override
						public void didRequestAction(TJEvent event, TJEventRequest request) {
							// Dismiss the event content
						    Intent intent = new Intent(getApplicationContext(),TapjoyEasyApp.class);
	                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
	                        startActivity(intent);
	                        
							String message = "Type: " + request.type + ", Identifier: " + request.identifier + ", Quantity: " + request.quantity;
							AlertDialog dialog = new AlertDialog.Builder(TapjoyEasyApp.this).setTitle("Got Action Callback").setMessage(message).
							setPositiveButton("Okay", new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton)
								{
									dialog.dismiss();
								}
							}).create();
							dialog.show();
							
							/*
							// Your app should perform an action based on the value of the request.type property
							switch(request.type){
								case TJEventRequest.TYPE_IN_APP_PURCHASE:
									// Your app should initiate an in-app purchase of the product identified by request.identifier
									break;
								case TJEventRequest.TYPE_VIRTUAL_GOOD:
									// Your app should award the user the item specified by request.identifier with the amount specified by request.quantity
									break;
								case TJEventRequest.TYPE_CURRENCY:
									// The user has been awarded the currency specified with request.identifier, with the amount specified by request.quantity
									break;
								case TJEventRequest.TYPE_NAVIGATION:
									// Your app should attempt to navigate to the location specified by request.identifier
									break;
							}
							*/
							
							// Your app must call either callback.completed() or callback.cancelled() to complete the lifecycle of the request
						    request.callback.completed();
						}
					});
					
					// By default, ad content will be shown automatically on a successful send. For finer control of when content should be shown, call:
					evt.enableAutoPresent(false);
					
					evt.send();
					updateTextInUI("Sending event...");
					break;
			}
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		// Re-enable auto-refresh when we regain focus.
		TapjoyConnect.getTapjoyConnectInstance().enableDisplayAdAutoRefresh(true);

		// Inform the SDK that the app has resumed.
		TapjoyConnect.getTapjoyConnectInstance().appResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		// Disable banner ad auto-refresh when the screen loses focus.
		TapjoyConnect.getTapjoyConnectInstance().enableDisplayAdAutoRefresh(false);
		
		// Inform the SDK that the app has paused.
		TapjoyConnect.getTapjoyConnectInstance().appPause();
	}
	
	//================================================================================
	// TapjoyNotifier Methods
	//================================================================================	
	@Override
	public void getUpdatePointsFailed(String error)
	{
		updateTextInUI("getTapPoints error: " + error);
		reenableButtonInUI(getPointsButton);
	}
	
	@Override
	public void getUpdatePoints(String currencyName, int pointTotal)
	{
		Log.i(TAG, "currencyName: " + currencyName);
		Log.i(TAG, "pointTotal: " + pointTotal);
		
		if (earnedPoints)
		{
			updateTextInUI(displayText + "\n" + currencyName + ": " + pointTotal);
			earnedPoints = false;
		}
		else
		{
			updateTextInUI(currencyName + ": " + pointTotal);
		}
		reenableButtonInUI(getPointsButton);
	}

	//================================================================================
	// Helper Methods
	//================================================================================
	/**
	 * Update the text view in the UI.
	 * @param text							Text to update the text view with.
	 */
	private void updateTextInUI(final String text)
	{
		displayText = text;
		
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				if (pointsTextView != null)
					pointsTextView.setText(text);
			}
		});
	}
	
	/**
	 * Add the banner ad to our UI.
	 * @param view							Banner ad view.
	 */
	private void updateDisplayAdInUI(final View view)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				// Remove all subviews of our ad layout.
				adLinearLayout.removeAllViews();
				
				// Add the ad to our layout.
				adLinearLayout.addView(view);
			}
		});
	}
	
	/**
	 * REenable a button in the UI.
	 * @param button							Button to reenable.
	 */
	private void reenableButtonInUI(final Button button)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				button.setEnabled(true);
			}
		});
	}
	
	/**
	 * Scales a display ad view to fit within a specified width. Returns a resized (smaller) view if the display ad
	 * is larger than the width. This method does not modify the view if the banner is smaller than the width (does not resize larger).
	 * @param adView                                                Display Ad view to resize.
	 * @param targetWidth                                   Width of the parent view for the display ad.
	 * @return                                                              Resized display ad view.
	 */
	private static View scaleDisplayAd(View adView, int targetWidth)
	{
		int adWidth = adView.getLayoutParams().width;
		int adHeight = adView.getLayoutParams().height;

		// Scale if the ad view is too big for the parent view.
		if (adWidth > targetWidth)
		{
			int scale;
			int width = targetWidth;
			Double val = Double.valueOf(width) / Double.valueOf(adWidth);
			val = val * 100d;
			scale = val.intValue();

			((android.webkit.WebView) (adView)).getSettings().setSupportZoom(true);
			((android.webkit.WebView) (adView)).setPadding(0, 0, 0, 0);
			((android.webkit.WebView) (adView)).setVerticalScrollBarEnabled(false);
			((android.webkit.WebView) (adView)).setHorizontalScrollBarEnabled(false);
			((android.webkit.WebView) (adView)).setInitialScale(scale);

			// Resize banner to desired width and keep aspect ratio.
			LayoutParams layout = new LayoutParams(targetWidth, (targetWidth*adHeight)/adWidth);
			adView.setLayoutParams(layout);
		}

		return adView;
	}
	
	/**
	 * Helper method to get the name of each view type.
	 * @param type							Tapjoy view type from the view notification callbacks.
	 * @return								Name of the view.
	 */
	public String getViewName(int type)
	{
		String name = "";
		switch (type)
		{
			case TapjoyViewType.FULLSCREEN_AD:
				name = "fullscreen ad";
				break;
			case TapjoyViewType.OFFER_WALL_AD:
				name = "offer wall ad";
				break;
			case TapjoyViewType.VIDEO_AD:
				name = "video ad";
				break;
			case TapjoyViewType.EVENT:
				name = "event";
				break;
			default:
				name = "undefined type: " + type;
				break;
		}
		
		return name;
	}
	
	private void showPopupMessage(final String text)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast toast = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show(); 
			}
		});
	}
}
