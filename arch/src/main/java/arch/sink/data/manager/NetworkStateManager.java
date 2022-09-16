package arch.sink.data.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import org.jetbrains.annotations.NotNull;

import arch.sink.R;
import arch.sink.utils.NetworkUtils;
import arch.sink.utils.Utils;


/**
 * @Author SinkDev
 * @Date 2021/7/19-12:49
 * @Email 468766131@qq.com
 */
public class NetworkStateManager implements DefaultLifecycleObserver {

    private final static NetworkStateManager INSTANCE = new NetworkStateManager();
    private NetworkStateReceiver networkStateReceiver;

    private NetworkStateManager() {
    }

    @NonNull
    public static NetworkStateManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void onResume(@NonNull @NotNull LifecycleOwner owner) {
        networkStateReceiver = new NetworkStateReceiver();
        Utils.getApp().
                registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    public void onPause(@NonNull @NotNull LifecycleOwner owner) {
        Utils.getApp().
                unregisterReceiver(networkStateReceiver);
    }

    static class NetworkStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) && !NetworkUtils.isConnected()) {
                Toast.makeText(context, context.getString(R.string.network_not_good), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
