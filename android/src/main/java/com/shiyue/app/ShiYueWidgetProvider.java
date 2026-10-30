package com.shiyue.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class ShiYueWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("ShiYueWidgetPrefs", Context.MODE_PRIVATE);
        String title = prefs.getString("title", "我们在一起");
        String days = prefs.getString("days", "520");
        String label = prefs.getString("label", "DAYS SINCE");
        String subLabel = prefs.getString("subLabel", "时约 // 恋爱纪念");
        String target = prefs.getString("target", "点击进入《时约》");

        String pkg = context.getPackageName();
        int layoutId = context.getResources().getIdentifier("widget_shiyue", "layout", pkg);
        if (layoutId == 0) return;

        RemoteViews views = new RemoteViews(pkg, layoutId);

        int titleId = context.getResources().getIdentifier("widget_title", "id", pkg);
        int daysId = context.getResources().getIdentifier("widget_days", "id", pkg);
        int labelId = context.getResources().getIdentifier("widget_label", "id", pkg);
        int subId = context.getResources().getIdentifier("widget_sub", "id", pkg);
        int targetId = context.getResources().getIdentifier("widget_target", "id", pkg);
        int containerId = context.getResources().getIdentifier("widget_container", "id", pkg);

        if (titleId != 0) views.setTextViewText(titleId, title);
        if (daysId != 0) views.setTextViewText(daysId, days);
        if (labelId != 0) views.setTextViewText(labelId, label);
        if (subId != 0) views.setTextViewText(subId, subLabel);
        if (targetId != 0) views.setTextViewText(targetId, target);

        // Click widget to launch MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (containerId != 0) {
            views.setOnClickPendingIntent(containerId, pendingIntent);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllWidgets(Context context) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName name = new ComponentName(context, ShiYueWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(name);
            if (ids != null && ids.length > 0) {
                for (int id : ids) {
                    updateAppWidget(context, manager, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
