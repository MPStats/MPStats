//package com.mpstats.mpstats;
//
//import android.support.annotation.Nullable;
//
//import com.android.billingclient.api.BillingClient;
//import com.android.billingclient.api.BillingResult;
//import com.android.billingclient.api.Purchase;
//import com.android.billingclient.api.PurchasesUpdatedListener;
//import com.mpstats.mpstats.Data.MPStatsData;
//
//import java.util.List;
//
//public class BillingController implements PurchasesUpdatedListener {
//
//    BillingClient mBillingClient;
//
//    public void Initialize () {
//        if (mBillingClient == null) {
//             mBillingClient = BillingClient
//                     .newBuilder(MPStatsData.getContext())
//                     .setListener(this)
//                     .build();
//
//            Output(mBillingClient.queryPurchases(BillingClient.SkuType.SUBS).getPurchasesList());
//            Output(mBillingClient.queryPurchases(BillingClient.SkuType.INAPP).getPurchasesList());
//        }
//    }
//
//    void Output (List<Purchase> purchases) {
//        if (purchases == null) {
//            return;
//        }
//        for (int i = 0; i < purchases.size(); i++) {
//            Utility.LogError("SKU FROM LIST: " + purchases.get(i).getSku());
//        }
//    }
//
//
//    @Override
//    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
//        mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
//        mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
//        Utility.LogError("ON PURCHASE UPDATED 0: " + purchases);
//        Utility.LogError("ON PURCHASE UPDATED 1: " + purchases.size());
//        Utility.LogError("ON PURCHASE UPDATED 2: " + purchases.get(0).getSku());
//    }
//}
