package com.jbs.swipe;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.example.android.trivialdrivesample.util.IabException;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabHelper.OnConsumeFinishedListener;
import com.example.android.trivialdrivesample.util.IabHelper.OnIabPurchaseFinishedListener;
import com.example.android.trivialdrivesample.util.IabHelper.OnIabSetupFinishedListener;
import com.example.android.trivialdrivesample.util.IabHelper.QueryInventoryFinishedListener;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.jbs.swipe.shop.BillingAPI;
import com.jbs.swipe.shop.BillingCallback;

public class GooglePlayBilling extends BillingAPI {
	
	private IabHelper helper;
	private AndroidApplication app;
	
	public GooglePlayBilling(AndroidApplication app, String publicKey) {
		this.app = app;
		helper = new IabHelper(app, publicKey);
		helper.startSetup(new OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				System.out.println("In-App-Billing Setup Success : " + result.isSuccess());
				System.out.println("Message : " + result.getMessage());
			}
		});
	}
	
	@Override
	public void destroy() {
		helper.dispose();
	}
	
	@Override
	public void requestPurchase(String sku, final BillingCallback billingCallback) {
		System.out.println("Requesting purchase of : " + sku);
		helper.launchPurchaseFlow(app, sku, IabHelper.ITEM_TYPE_INAPP, 666, new OnIabPurchaseFinishedListener() {
			@Override
			public void onIabPurchaseFinished(IabResult result, Purchase info) {
				System.out.println("InAppBilling Purchase Finished! Message : " + result.getMessage());
				if (result.isSuccess())
					redeemPendingPurchasesOf(info.getSku(), new OnConsumeFinishedListener() {
						@Override
						public void onConsumeFinished(Purchase purchase, IabResult result) {
							if (result.isSuccess()) {
								System.out.println("Successfully consumed Purchase of : " + purchase.getSku());
								// Let the rest of the game know that the purchase was a complete success.
								billingCallback.callback(true);
							} else if (result.isFailure()) {
								System.out.println("Failed to consume Purchase of : " + purchase.getSku());
								billingCallback.callback(false);
							}
						}
					});
				if (result.isFailure())
					// Let the rest of the game know that the purchase was a failure.
					billingCallback.callback(false);
			}
		}, "");
	}
	
	public void redeemPendingPurchasesOf(String sku, OnConsumeFinishedListener listener) {
		Inventory inventory = inventoryOf(sku);
		if (inventory.hasPurchase(sku))
			helper.consumeAsync(inventory.getPurchase(sku), listener);
	}
	
	public Inventory inventoryOf(String... skus) {
		ArrayList<String> list = new ArrayList<String>();
		for (String sku : skus)
			list.add(sku);
		
		Inventory inventory = null;
		try {
			inventory = helper.queryInventory(true, list);
		} catch (IabException e) {
			e.printStackTrace();
		}
		return inventory;
	}
	
	public IabHelper helper() {
		return this.helper;
	}
}