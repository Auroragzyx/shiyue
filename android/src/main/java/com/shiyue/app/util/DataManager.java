package com.shiyue.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.shiyue.app.ShiYueWidgetProvider;
import com.shiyue.app.model.EventItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataManager {

    private static final String PREF_NAME = "ShiYueNativePrefs";
    private static final String KEY_EVENTS = "KEY_EVENTS_V2";
    private static final String KEY_CATEGORIES = "KEY_CATEGORIES";
    private static final String KEY_THEME = "KEY_THEME"; // "dark", "light"
    private static final String KEY_PALETTE = "KEY_PALETTE"; // "violet", "blue", "green", "amber"

    public static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
        "纪念日", "生日", "学业", "生活", "节日", "工作"
    );

    private static DataManager sInstance;
    private final Context mContext;
    private final SharedPreferences mPrefs;

    private final List<EventItem> mEvents = new ArrayList<>();
    private final List<String> mCategories = new ArrayList<>();

    private DataManager(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        load();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataManager(context);
        }
        return sInstance;
    }

    private void load() {
        mCategories.clear();
        String catJson = mPrefs.getString(KEY_CATEGORIES, null);
        if (catJson != null) {
            try {
                JSONArray arr = new JSONArray(catJson);
                for (int i = 0; i < arr.length(); i++) {
                    mCategories.add(arr.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mCategories.isEmpty()) {
            mCategories.addAll(DEFAULT_CATEGORIES);
        }

        mEvents.clear();
        String evJson = mPrefs.getString(KEY_EVENTS, null);
        if (evJson != null) {
            try {
                JSONArray arr = new JSONArray(evJson);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    EventItem item = EventItem.fromJsonObject(obj);
                    if (item != null) {
                        mEvents.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Initialize default sample data if empty
        if (mEvents.isEmpty()) {
            initDefaultSamples();
            save();
        }
    }

    private void initDefaultSamples() {
        EventItem ev1 = new EventItem();
        ev1.id = "ev-1";
        ev1.title = "我们在一起";
        ev1.date = "2025-05-28";
        ev1.category = "纪念日";
        ev1.mode = "since";
        ev1.repeat = "yearly";
        ev1.calendarType = "solar";
        ev1.pinned = true;
        ev1.notes = "在海边看日落许下的约定，愿岁岁年年。";

        EventItem ev2 = new EventItem();
        ev2.id = "ev-2";
        ev2.title = "研究生入学考试 (初试)";
        ev2.date = "2026-12-26";
        ev2.category = "学业";
        ev2.mode = "left";
        ev2.repeat = "none";
        ev2.calendarType = "solar";
        ev2.pinned = false;
        ev2.notes = "沉潜蓄力，终将上岸！";

        EventItem ev3 = new EventItem();
        ev3.id = "ev-3";
        ev3.title = "妈妈的生日 (农历)";
        ev3.date = "2026-11-15";
        ev3.category = "生日";
        ev3.mode = "left";
        ev3.repeat = "yearly";
        ev3.calendarType = "lunar";
        ev3.lunarMonth = 10;
        ev3.lunarDay = 6;
        ev3.pinned = false;
        ev3.notes = "十月初六，提前订好鲜花和生日礼物。";

        mEvents.add(ev1);
        mEvents.add(ev2);
        mEvents.add(ev3);
    }

    public synchronized void save() {
        try {
            JSONArray catArr = new JSONArray();
            for (String c : mCategories) {
                catArr.put(c);
            }
            JSONArray evArr = new JSONArray();
            for (EventItem ev : mEvents) {
                evArr.put(ev.toJsonObject());
            }

            mPrefs.edit()
                .putString(KEY_CATEGORIES, catArr.toString())
                .putString(KEY_EVENTS, evArr.toString())
                .apply();

            syncWidget();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncWidget() {
        EventItem pinned = getPinnedEvent();
        if (pinned != null) {
            EventItem.CalculationResult calc = pinned.calculate();
            SharedPreferences widgetPrefs = mContext.getSharedPreferences("ShiYueWidgetPrefs", Context.MODE_PRIVATE);
            widgetPrefs.edit()
                .putString("title", pinned.title)
                .putString("days", String.valueOf(calc.absDays))
                .putString("label", calc.label)
                .putString("subLabel", "时约 // " + pinned.category)
                .putString("target", calc.subLabel + " · " + calc.targetDateStr)
                .apply();
        }
        ShiYueWidgetProvider.updateAllWidgets(mContext);
    }

    public EventItem getPinnedEvent() {
        for (EventItem e : mEvents) {
            if (e.pinned) return e;
        }
        return mEvents.isEmpty() ? null : mEvents.get(0);
    }

    public List<EventItem> getEvents() {
        return mEvents;
    }

    public List<String> getCategories() {
        return mCategories;
    }

    public void addEvent(EventItem item) {
        if (item.pinned) {
            for (EventItem e : mEvents) e.pinned = false;
        }
        mEvents.add(0, item);
        save();
    }

    public void updateEvent(EventItem item) {
        if (item.pinned) {
            for (EventItem e : mEvents) {
                if (!e.id.equals(item.id)) e.pinned = false;
            }
        }
        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).id.equals(item.id)) {
                mEvents.set(i, item);
                break;
            }
        }
        save();
    }

    public void deleteEvent(String id) {
        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).id.equals(id)) {
                mEvents.remove(i);
                break;
            }
        }
        save();
    }

    public void togglePin(String id) {
        for (EventItem e : mEvents) {
            if (e.id.equals(id)) {
                boolean target = !e.pinned;
                if (target) {
                    for (EventItem item : mEvents) item.pinned = false;
                }
                e.pinned = target;
                break;
            }
        }
        save();
    }

    public void addCategory(String cat) {
        if (!mCategories.contains(cat)) {
            mCategories.add(cat);
            save();
        }
    }

    public void deleteCategory(int idx) {
        if (idx >= 0 && idx < mCategories.size() && mCategories.size() > 1) {
            String removed = mCategories.remove(idx);
            for (EventItem e : mEvents) {
                if (removed.equals(e.category)) {
                    e.category = mCategories.get(0);
                }
            }
            save();
        }
    }

    public boolean isDarkTheme() {
        return !"light".equals(mPrefs.getString(KEY_THEME, "dark"));
    }

    public void setDarkTheme(boolean dark) {
        mPrefs.edit().putString(KEY_THEME, dark ? "dark" : "light").apply();
    }

    public String getPalette() {
        return mPrefs.getString(KEY_PALETTE, "violet");
    }

    public void setPalette(String palette) {
        mPrefs.edit().putString(KEY_PALETTE, palette).apply();
    }
}
