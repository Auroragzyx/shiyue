package com.shiyue.app.model;

import com.shiyue.app.util.LunarHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventItem {
    public String id;
    public String title;
    public String date; // YYYY-MM-DD
    public String category;
    public String mode; // "auto", "since", "left"
    public String repeat; // "none", "yearly", "monthly", "weekly"
    public String calendarType; // "solar", "lunar"
    public int lunarMonth = 1;
    public int lunarDay = 1;
    public boolean pinned = false;
    public String notes;
    public List<PhotoItem> photos = new ArrayList<>();

    public static class CalculationResult {
        public long diffDays;
        public long absDays;
        public boolean isSince;
        public String label;
        public String subLabel;
        public String badgeText;
        public String targetDateStr;
        public String displayLunarStr;
        public String milestoneText;
    }

    public CalculationResult calculate() {
        CalculationResult res = new CalculationResult();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

        if ("lunar".equals(calendarType) && lunarMonth > 0 && lunarDay > 0) {
            int curYear = today.get(Calendar.YEAR);
            Date solarThisYear = LunarHelper.lunarToSolar(curYear, lunarMonth, lunarDay);
            Calendar cThisYear = Calendar.getInstance();
            cThisYear.setTime(solarThisYear);
            cThisYear.set(Calendar.HOUR_OF_DAY, 0);
            cThisYear.set(Calendar.MINUTE, 0);
            cThisYear.set(Calendar.SECOND, 0);
            cThisYear.set(Calendar.MILLISECOND, 0);

            if (cThisYear.before(today) && !"since".equals(mode)) {
                Date nextYearSolar = LunarHelper.lunarToSolar(curYear + 1, lunarMonth, lunarDay);
                target.setTime(nextYearSolar);
            } else {
                target.setTime(solarThisYear);
            }
            res.displayLunarStr = "农历 " + lunarMonth + "月" + lunarDay + "日";
        } else {
            if (date != null && date.contains("-")) {
                String[] parts = date.split("-");
                if (parts.length == 3) {
                    int y = Integer.parseInt(parts[0]);
                    int m = Integer.parseInt(parts[1]) - 1;
                    int d = Integer.parseInt(parts[2]);
                    target.set(y, m, d);

                    if ("yearly".equals(repeat)) {
                        Calendar nextY = Calendar.getInstance();
                        nextY.set(today.get(Calendar.YEAR), m, d, 0, 0, 0);
                        nextY.set(Calendar.MILLISECOND, 0);
                        if (nextY.before(today) && !"since".equals(mode)) {
                            nextY.add(Calendar.YEAR, 1);
                        }
                        if (!"since".equals(mode)) target = nextY;
                    } else if ("monthly".equals(repeat)) {
                        Calendar nextM = Calendar.getInstance();
                        nextM.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), d, 0, 0, 0);
                        nextM.set(Calendar.MILLISECOND, 0);
                        if (nextM.before(today) && !"since".equals(mode)) {
                            nextM.add(Calendar.MONTH, 1);
                        }
                        if (!"since".equals(mode)) target = nextM;
                    }
                }
            }
            LunarHelper.LunarResult lr = LunarHelper.solarToLunar(target.getTime());
            res.displayLunarStr = "农历 " + lr.str;
        }

        long diffMillis = target.getTimeInMillis() - today.getTimeInMillis();
        long diffDays = Math.round((double) diffMillis / 86400000.0);
        res.diffDays = diffDays;
        res.absDays = Math.abs(diffDays);

        boolean isSince = "since".equals(mode) || ("auto".equals(mode) && diffDays < 0);
        res.isSince = isSince;
        res.label = isSince ? "DAYS SINCE" : "DAYS LEFT";
        res.subLabel = isSince ? "正数日 · 已走过" : "倒数日 · 距离还有";

        if (diffDays == 0) {
            res.badgeText = "今天 TODAY";
        } else if (diffDays == 1) {
            res.badgeText = "明天 TOMORROW";
        } else if (isSince) {
            res.badgeText = "已过去 " + res.absDays + " 天";
        } else {
            res.badgeText = "还有 " + res.absDays + " 天";
        }

        res.targetDateStr = sdf.format(target.getTime());

        // Milestones
        if (isSince) {
            long[] milestones = {100, 200, 300, 500, 1000, 2000, 5000, 10000};
            for (long ms : milestones) {
                if (ms > res.absDays) {
                    res.milestoneText = "🎯 " + ms + " 天整纪念日 · 还有 " + (ms - res.absDays) + " 天";
                    break;
                }
            }
        }

        return res;
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("title", title);
            obj.put("date", date);
            obj.put("category", category);
            obj.put("mode", mode);
            obj.put("repeat", repeat);
            obj.put("calendarType", calendarType);
            obj.put("lunarMonth", lunarMonth);
            obj.put("lunarDay", lunarDay);
            obj.put("pinned", pinned);
            obj.put("notes", notes != null ? notes : "");

            JSONArray photosArr = new JSONArray();
            if (photos != null) {
                for (PhotoItem p : photos) {
                    photosArr.put(p.toJsonObject());
                }
            }
            obj.put("photos", photosArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static EventItem fromJsonObject(JSONObject obj) {
        if (obj == null) return null;
        EventItem item = new EventItem();
        item.id = obj.optString("id", "ev-" + System.currentTimeMillis());
        item.title = obj.optString("title", "");
        item.date = obj.optString("date", "2026-01-01");
        item.category = obj.optString("category", "纪念日");
        item.mode = obj.optString("mode", "auto");
        item.repeat = obj.optString("repeat", "none");
        item.calendarType = obj.optString("calendarType", "solar");
        item.lunarMonth = obj.optInt("lunarMonth", 1);
        item.lunarDay = obj.optInt("lunarDay", 1);
        item.pinned = obj.optBoolean("pinned", false);
        item.notes = obj.optString("notes", "");

        JSONArray photosArr = obj.optJSONArray("photos");
        if (photosArr != null) {
            for (int i = 0; i < photosArr.length(); i++) {
                JSONObject pObj = photosArr.optJSONObject(i);
                if (pObj != null) {
                    item.photos.add(PhotoItem.fromJsonObject(pObj));
                }
            }
        }
        return item;
    }
}
