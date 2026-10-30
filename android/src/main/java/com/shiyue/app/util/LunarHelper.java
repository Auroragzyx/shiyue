package com.shiyue.app.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LunarHelper {

    private static final int[] LUNAR_INFO = {
        0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
        0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
        0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
        0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
        0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
        0x06ca0,0x0b550,0x15355,0x04da0,0x0a5d0,0x14573,0x052d0,0x0a9a8,0x0e950,0x06aa0,
        0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
        0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b5a0,0x195a6,
        0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
        0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
        0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
        0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
        0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
        0x05aa0,0x076a3,0x096d0,0x04bd7,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
        0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0
    };

    private static final String[] GAN = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    private static final String[] ZHI = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    private static final String[] ANIMALS = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    private static final String[] LUNAR_MONTHS = {"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
    private static final String[] LUNAR_DAYS = {
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    public static class LunarResult {
        public int year;
        public int month;
        public int day;
        public boolean isLeap;
        public String str;
        public String fullStr;
    }

    public static int getLunarYearDays(int y) {
        int sum = 348;
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            sum += ((LUNAR_INFO[y - 1900] & i) != 0) ? 1 : 0;
        }
        return sum + getLeapDays(y);
    }

    public static int getLeapDays(int y) {
        if (getLeapMonth(y) != 0) {
            return ((LUNAR_INFO[y - 1900] & 0x10000) != 0) ? 30 : 29;
        }
        return 0;
    }

    public static int getLeapMonth(int y) {
        return LUNAR_INFO[y - 1900] & 0xf;
    }

    public static int getLunarMonthDays(int y, int m) {
        return ((LUNAR_INFO[y - 1900] & (0x10000 >> m)) != 0) ? 30 : 29;
    }

    public static LunarResult solarToLunar(Date solarDate) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(solarDate);
        long timeNow = cal.getTimeInMillis();

        cal.set(1900, Calendar.JANUARY, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long baseTime = cal.getTimeInMillis();

        long offset = (timeNow - baseTime) / 86400000L;
        int y = 1900;
        for (; y < 2100 && offset > 0; y++) {
            int temp = getLunarYearDays(y);
            if (offset < temp) break;
            offset -= temp;
        }

        int leap = getLeapMonth(y);
        boolean isLeap = false;
        int m = 1;
        for (; m <= 12 && offset > 0; m++) {
            if (leap > 0 && m == (leap + 1) && !isLeap) {
                --m;
                isLeap = true;
                int temp = getLeapDays(y);
                if (offset < temp) break;
                offset -= temp;
            } else {
                int temp = getLunarMonthDays(y, m);
                if (offset < temp) break;
                offset -= temp;
            }
            if (isLeap && m == (leap + 1)) isLeap = false;
        }
        int d = (int) offset + 1;

        LunarResult res = new LunarResult();
        res.year = y;
        res.month = m;
        res.day = d;
        res.isLeap = isLeap;

        String monthStr = (m >= 1 && m <= 12) ? LUNAR_MONTHS[m - 1] : String.valueOf(m);
        String dayStr = (d >= 1 && d <= 30) ? LUNAR_DAYS[d - 1] : String.valueOf(d);
        res.str = (isLeap ? "闰" : "") + monthStr + "月" + dayStr;

        String ganZhi = GAN[(y - 4) % 10] + ZHI[(y - 4) % 12];
        String animal = ANIMALS[(y - 4) % 12];
        res.fullStr = ganZhi + animal + "年 " + res.str;

        return res;
    }

    public static Date lunarToSolar(int lunarYear, int lunarMonth, int lunarDay) {
        int offset = 0;
        for (int i = 1900; i < lunarYear; i++) {
            offset += getLunarYearDays(i);
        }
        int leap = getLeapMonth(lunarYear);
        for (int i = 1; i < lunarMonth; i++) {
            offset += getLunarMonthDays(lunarYear, i);
            if (leap == i) {
                offset += getLeapDays(lunarYear);
            }
        }
        offset += (lunarDay - 1);

        Calendar base = Calendar.getInstance();
        base.set(1900, Calendar.JANUARY, 31, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);
        base.add(Calendar.DAY_OF_YEAR, offset);
        return base.getTime();
    }
}
