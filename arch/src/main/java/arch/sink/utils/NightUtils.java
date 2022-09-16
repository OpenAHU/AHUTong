package arch.sink.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

public class NightUtils {
    /**
     * 获取黑夜模式是否开启
     *
     * @param context 上下文
     * @return 结果
     */
    public static boolean isNightMode(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                ((context.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
    }
}
