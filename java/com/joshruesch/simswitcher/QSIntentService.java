package com.joshruesch.simswitcher;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;
import com.joshruesch.simswitcher.R;

@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
public class QSIntentService extends TileService{
    private static final String TAG = QSIntentService.class.getSimpleName();

    private SubscriptionManager mSubscriptionManager;

    private final SubscriptionManager.OnSubscriptionsChangedListener  mListener = new SubscriptionManager.OnSubscriptionsChangedListener(){
        @Override
        public void onSubscriptionsChanged(){
          setCurrentSimDisplayName();
       }
    };

    private void setTileState(CharSequence label, int state){
      Tile tile = getQsTile();
      tile.setState(state);
      tile.setLabel(label);
      tile.updateTile();
    }

    private void setCurrentSimDisplayName(){
      int currentSimID = mSubscriptionManager.getDefaultSubscriptionId();
      if (currentSimID == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
        Log.e(TAG, "No sim found");
        setTileState(getString(R.string.no_sim), Tile.STATE_UNAVAILABLE);
        return;
       }

      SubscriptionInfo current = mSubscriptionManager.getActiveSubscriptionInfo(currentSimID);
      if (current == null) {
        Log.w(TAG, "No sim active");
        setTileState("", Tile.STATE_INACTIVE);
        return;
      }

      // SUCCESS!
      setTileState(current.getDisplayName(), Tile.STATE_ACTIVE);
    }

    @Override
    public void onCreate(){
      mSubscriptionManager = SubscriptionManager.from(this);
    }

    @Override
    public void  onTileAdded() {
      setCurrentSimDisplayName();
    }

    @Override
    public void onStartListening(){
      mSubscriptionManager.addOnSubscriptionsChangedListener(mListener);
    }

    @Override
    public void onStopListening(){
      mSubscriptionManager.removeOnSubscriptionsChangedListener(mListener);
    }

    private void showSimList(){
      Intent intent = new Intent("android.telephony.euicc.action.MANAGE_EMBEDDED_SUBSCRIPTIONS");
      startActivityAndCollapse(intent);
    }

    @Override
    public void onClick() {

        // Check to see if the device is currently locked.
        boolean isCurrentlyLocked = this.isLocked();

        if (!isCurrentlyLocked) {
          showSimList();
        } else {
          unlockAndRun(new Runnable(){
            @Override
            public void run(){
             showSimList();
            }
          });
        }
    }
}

