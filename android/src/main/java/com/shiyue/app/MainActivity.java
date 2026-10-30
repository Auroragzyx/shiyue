package com.shiyue.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.shiyue.app.model.EventItem;
import com.shiyue.app.util.DataManager;
import com.shiyue.app.util.LunarHelper;
import com.shiyue.app.util.PosterGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private DataManager mDataManager;
    private String mCurrentCategory = "ALL";
    private String mSearchQuery = "";

    // Header views
    private TextView mTvLiveDate;
    private TextView mTvLiveLunar;
    private LinearLayout mContainerCategoryChips;

    // Hero views
    private LinearLayout mCardHero;
    private TextView mTvHeroCategory;
    private TextView mTvHeroBadge;
    private TextView mTvHeroTitle;
    private TextView mTvHeroMeta;
    private TextView mTvHeroDays;
    private TextView mTvHeroLabel;
    private ProgressBar mPbHeroProgress;
    private TextView mTvHeroProgressLabel;
    private TextView mTvHeroPhotosCount;

    // Search and list
    private EditText mEtSearch;
    private TextView mTvEventCount;
    private LinearLayout mContainerEventList;
    private View mFabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mDataManager = DataManager.getInstance(this);

        initViews();
        setupEvents();
        updateLiveDate();
        renderAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderAll();
    }

    private void initViews() {
        mTvLiveDate = findViewById(R.id.tv_live_date);
        mTvLiveLunar = findViewById(R.id.tv_live_lunar);
        mContainerCategoryChips = findViewById(R.id.container_category_chips);

        mCardHero = findViewById(R.id.card_hero);
        mTvHeroCategory = findViewById(R.id.tv_hero_category);
        mTvHeroBadge = findViewById(R.id.tv_hero_badge);
        mTvHeroTitle = findViewById(R.id.tv_hero_title);
        mTvHeroMeta = findViewById(R.id.tv_hero_meta);
        mTvHeroDays = findViewById(R.id.tv_hero_days);
        mTvHeroLabel = findViewById(R.id.tv_hero_label);
        mPbHeroProgress = findViewById(R.id.pb_hero_progress);
        mTvHeroProgressLabel = findViewById(R.id.tv_hero_progress_label);
        mTvHeroPhotosCount = findViewById(R.id.tv_hero_photos_count);

        mEtSearch = findViewById(R.id.et_search);
        mTvEventCount = findViewById(R.id.tv_event_count);
        mContainerEventList = findViewById(R.id.container_event_list);
        mFabAdd = findViewById(R.id.fab_add);
    }

    private void setupEvents() {
        // Palette switcher
        findViewById(R.id.btn_action_palette).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cyclePalette();
            }
        });

        // Theme switcher
        findViewById(R.id.btn_action_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDark = mDataManager.isDarkTheme();
                mDataManager.setDarkTheme(!isDark);
                Toast.makeText(MainActivity.this, isDark ? "已切换至浅色模式" : "已切换至深色模式", Toast.LENGTH_SHORT).show();
                renderAll();
            }
        });

        // Settings / Data manager
        findViewById(R.id.btn_action_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsDialog();
            }
        });

        // Add event FAB
        mFabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditEventDialog(null);
            }
        });

        // Search text watcher
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchQuery = s.toString().trim().toLowerCase();
                renderEventList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cyclePalette() {
        String cur = mDataManager.getPalette();
        String next;
        String name;
        if ("violet".equals(cur)) {
            next = "blue";
            name = "晴空海蓝";
        } else if ("blue".equals(cur)) {
            next = "green";
            name = "旷野森绿";
        } else if ("green".equals(cur)) {
            next = "amber";
            name = "暮光暖杏";
        } else {
            next = "violet";
            name = "灵动香芋紫";
        }
        mDataManager.setPalette(next);
        Toast.makeText(this, "调色盘已切换: " + name, Toast.LENGTH_SHORT).show();
        renderAll();
    }

    private void updateLiveDate() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd E", Locale.CHINESE);
        mTvLiveDate.setText(sdf.format(now));

        LunarHelper.LunarResult lr = LunarHelper.solarToLunar(now);
        mTvLiveLunar.setText(lr.fullStr);
    }

    private void renderAll() {
        renderCategoryChips();
        renderHero();
        renderEventList();
    }

    private void renderCategoryChips() {
        mContainerCategoryChips.removeAllViews();

        List<String> allCats = new ArrayList<>();
        allCats.add("ALL");
        allCats.addAll(mDataManager.getCategories());

        LayoutInflater inflater = LayoutInflater.from(this);
        for (final String cat : allCats) {
            TextView chip = new TextView(this);
            String label = "ALL".equals(cat) ? "全部" : cat;
            boolean isSelected = mCurrentCategory.equals(cat);

            chip.setText(isSelected ? "✓ " + label : label);
            chip.setTextSize(11);
            chip.setPadding(30, 16, 30, 16);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(lp);

            if (isSelected) {
                chip.setBackgroundResource(R.drawable.chip_selected);
                chip.setTextColor(getResources().getColor(R.color.m3_on_primary_dark));
                chip.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                chip.setBackgroundResource(R.drawable.chip_unselected);
                chip.setTextColor(getResources().getColor(R.color.m3_text_secondary_dark));
            }

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentCategory = cat;
                    renderCategoryChips();
                    renderEventList();
                }
            });

            mContainerCategoryChips.addView(chip);
        }

        // Add "+ 管理" button
        TextView manageBtn = new TextView(this);
        manageBtn.setText("+ 管理");
        manageBtn.setTextSize(11);
        manageBtn.setTextColor(getResources().getColor(R.color.m3_primary_dark));
        manageBtn.setPadding(26, 16, 26, 16);
        manageBtn.setBackgroundResource(R.drawable.chip_unselected);
        manageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog();
            }
        });
        mContainerCategoryChips.addView(manageBtn);
    }

    private void renderHero() {
        final EventItem pinned = mDataManager.getPinnedEvent();
        if (pinned == null) {
            mCardHero.setVisibility(View.GONE);
            return;
        }

        mCardHero.setVisibility(View.VISIBLE);
        EventItem.CalculationResult calc = pinned.calculate();

        mTvHeroCategory.setText(getCategoryIcon(pinned.category) + " " + pinned.category);
        mTvHeroBadge.setText(calc.badgeText);
        mTvHeroTitle.setText(pinned.title);
        mTvHeroMeta.setText(calc.subLabel + " · 目标日期 " + calc.targetDateStr + " (" + calc.displayLunarStr + ")");
        mTvHeroDays.setText(String.valueOf(calc.absDays));
        mTvHeroLabel.setText(calc.label);

        if (calc.isSince) {
            int percent = (int) Math.min(100, Math.round(((double) (calc.absDays % 365) / 365.0) * 100));
            mPbHeroProgress.setProgress(percent);
            mPbHeroProgress.setVisibility(View.VISIBLE);
            mTvHeroProgressLabel.setText("年度周期刻度 · " + percent + "% (" + (calc.absDays % 365) + " / 365 天)");
            mTvHeroProgressLabel.setVisibility(View.VISIBLE);
        } else {
            mPbHeroProgress.setVisibility(View.GONE);
            mTvHeroProgressLabel.setVisibility(View.GONE);
        }

        int count = pinned.photos != null ? pinned.photos.size() : 0;
        mTvHeroPhotosCount.setText(count > 0 ? "📸 " + count + " 张现场回忆卡片" : "轻触可补充现场相片与故事");

        mCardHero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailDialog(pinned);
            }
        });
    }

    private void renderEventList() {
        mContainerEventList.removeAllViews();

        List<EventItem> all = mDataManager.getEvents();
        List<EventItem> filtered = new ArrayList<>();

        for (EventItem item : all) {
            boolean matchCat = "ALL".equals(mCurrentCategory) || item.category.equals(mCurrentCategory);
            boolean matchSearch = mSearchQuery.isEmpty() ||
                item.title.toLowerCase().contains(mSearchQuery) ||
                (item.notes != null && item.notes.toLowerCase().contains(mSearchQuery)) ||
                (item.date != null && item.date.contains(mSearchQuery));

            if (matchCat && matchSearch) {
                filtered.add(item);
            }
        }

        mTvEventCount.setText(filtered.size() + " 条记录");

        if (filtered.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("暂无匹配的纪念日\n轻触右下角「+ 添加纪念」记录美好时光");
            emptyTv.setTextSize(13);
            emptyTv.setTextColor(getResources().getColor(R.color.m3_text_secondary_dark));
            emptyTv.setGravity(android.view.Gravity.CENTER);
            emptyTv.setPadding(0, 80, 0, 80);
            mContainerEventList.addView(emptyTv);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (final EventItem item : filtered) {
            View card = inflater.inflate(R.layout.item_event_card, mContainerEventList, false);

            TextView tvIcon = card.findViewById(R.id.tv_item_icon);
            TextView tvTitle = card.findViewById(R.id.tv_item_title);
            TextView tvPinStar = card.findViewById(R.id.tv_item_pin_star);
            TextView tvDate = card.findViewById(R.id.tv_item_date);
            TextView tvBadge = card.findViewById(R.id.tv_item_badge);
            TextView tvPhotosTag = card.findViewById(R.id.tv_item_photos_tag);
            TextView tvDays = card.findViewById(R.id.tv_item_days);
            TextView tvDaysUnit = card.findViewById(R.id.tv_item_days_unit);

            EventItem.CalculationResult calc = item.calculate();

            tvIcon.setText(getCategoryIcon(item.category));
            tvTitle.setText(item.title);
            tvPinStar.setVisibility(item.pinned ? View.VISIBLE : View.GONE);
            tvDate.setText(calc.targetDateStr + " · " + calc.displayLunarStr);
            tvBadge.setText(calc.badgeText);

            int photosCount = item.photos != null ? item.photos.size() : 0;
            if (photosCount > 0) {
                tvPhotosTag.setText("📷 " + photosCount);
                tvPhotosTag.setVisibility(View.VISIBLE);
            } else {
                tvPhotosTag.setVisibility(View.GONE);
            }

            tvDays.setText(String.valueOf(calc.absDays));
            tvDaysUnit.setText(calc.isSince ? "已走过天" : "还剩天");

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetailDialog(item);
                }
            });

            mContainerEventList.addView(card);
        }
    }

    private String getCategoryIcon(String cat) {
        if ("纪念日".equals(cat)) return "❤️";
        if ("生日".equals(cat)) return "🎂";
        if ("学业".equals(cat)) return "🎓";
        if ("生活".equals(cat)) return "🌿";
        if ("节日".equals(cat)) return "🎉";
        if ("工作".equals(cat)) return "💼";
        return "📌";
    }

    // ================= Detail Dialog =================
    private void showDetailDialog(final EventItem event) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_detail_event);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        final EventItem.CalculationResult calc = event.calculate();

        TextView tvTitle = dialog.findViewById(R.id.tv_detail_title);
        TextView tvDays = dialog.findViewById(R.id.tv_detail_days);
        TextView tvLabel = dialog.findViewById(R.id.tv_detail_label);
        TextView tvLunar = dialog.findViewById(R.id.tv_detail_lunar);
        View boxMilestone = dialog.findViewById(R.id.box_detail_milestone);
        TextView tvMilestoneText = dialog.findViewById(R.id.tv_detail_milestone_text);
        TextView tvCatRepeat = dialog.findViewById(R.id.tv_detail_category_repeat);
        TextView tvTarget = dialog.findViewById(R.id.tv_detail_target_date);
        View boxNotes = dialog.findViewById(R.id.box_detail_notes);
        TextView tvNotesContent = dialog.findViewById(R.id.tv_detail_notes_content);

        tvTitle.setText(event.title);
        tvDays.setText(String.valueOf(calc.absDays));
        tvLabel.setText(calc.label + " · " + calc.subLabel);
        tvLunar.setText(calc.displayLunarStr);

        if (calc.milestoneText != null && !calc.milestoneText.isEmpty()) {
            boxMilestone.setVisibility(View.VISIBLE);
            tvMilestoneText.setText(calc.milestoneText);
        } else {
            boxMilestone.setVisibility(View.GONE);
        }

        tvCatRepeat.setText("分类: " + event.category + "  |  周期: " + getRepeatLabel(event.repeat));
        tvTarget.setText("目标日期: " + calc.targetDateStr + " (" + ("lunar".equals(event.calendarType) ? "中国农历" : "公历阳历") + ")");

        if (event.notes != null && !event.notes.isEmpty()) {
            boxNotes.setVisibility(View.VISIBLE);
            tvNotesContent.setText(event.notes);
        } else {
            boxNotes.setVisibility(View.GONE);
        }

        dialog.findViewById(R.id.btn_detail_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Sync to native calendar
        dialog.findViewById(R.id.btn_detail_calendar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncToCalendarNative(event, calc);
            }
        });

        // Generate poster
        dialog.findViewById(R.id.btn_detail_poster).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showPosterDialog(event);
            }
        });

        // Pin button
        final TextView btnPin = dialog.findViewById(R.id.btn_detail_pin);
        btnPin.setText(event.pinned ? "取消置顶" : "⭐ 置顶焦点");
        btnPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataManager.togglePin(event.id);
                dialog.dismiss();
                renderAll();
                Toast.makeText(MainActivity.this, event.pinned ? "已取消置顶" : "已设为置顶焦点并同步桌面小组件", Toast.LENGTH_SHORT).show();
            }
        });

        // Edit button
        dialog.findViewById(R.id.btn_detail_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showEditEventDialog(event);
            }
        });

        // Delete button
        dialog.findViewById(R.id.btn_detail_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataManager.deleteEvent(event.id);
                dialog.dismiss();
                renderAll();
                Toast.makeText(MainActivity.this, "已删除记录", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private String getRepeatLabel(String repeat) {
        if ("yearly".equals(repeat)) return "每年重复";
        if ("monthly".equals(repeat)) return "每月重复";
        if ("weekly".equals(repeat)) return "每周重复";
        return "不重复";
    }

    private void syncToCalendarNative(EventItem event, EventItem.CalculationResult calc) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "时约提醒: " + event.title)
                .putExtra(CalendarContract.Events.DESCRIPTION, (event.notes != null ? event.notes : "") + "\n(" + calc.subLabel + " · " + calc.displayLunarStr + ")")
                .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "唤起系统日历失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ================= Edit Event Dialog =================
    private void showEditEventDialog(final EventItem existing) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_event);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.94),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        final boolean isEdit = existing != null;
        TextView tvSub = dialog.findViewById(R.id.tv_dialog_sub);
        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_title);
        tvSub.setText(isEdit ? "EDIT RECORD" : "NEW RECORD");
        tvTitle.setText(isEdit ? "编辑纪念日" : "添加新纪念日");

        final EditText etTitle = dialog.findViewById(R.id.et_form_title);
        final TextView btnSegSolar = dialog.findViewById(R.id.btn_seg_solar);
        final TextView btnSegLunar = dialog.findViewById(R.id.btn_seg_lunar);
        final View boxSolar = dialog.findViewById(R.id.box_solar_input);
        final View boxLunar = dialog.findViewById(R.id.box_lunar_input);
        final TextView btnPickDate = dialog.findViewById(R.id.btn_pick_date);
        final Spinner spLunarMonth = dialog.findViewById(R.id.spinner_lunar_month);
        final Spinner spLunarDay = dialog.findViewById(R.id.spinner_lunar_day);
        final Spinner spMode = dialog.findViewById(R.id.spinner_mode);
        final Spinner spCategory = dialog.findViewById(R.id.spinner_category);
        final Spinner spRepeat = dialog.findViewById(R.id.spinner_repeat);
        final Switch swPinned = dialog.findViewById(R.id.switch_pinned);
        final EditText etNotes = dialog.findViewById(R.id.et_form_notes);

        // Calendar type state
        final boolean[] isLunar = {isEdit && "lunar".equals(existing.calendarType)};
        final String[] chosenDate = {isEdit ? existing.date : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())};

        btnPickDate.setText("公历日期: " + chosenDate[0] + " (点击更改)");
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] parts = chosenDate[0].split("-");
                int y = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]) - 1;
                int d = Integer.parseInt(parts[2]);
                new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        chosenDate[0] = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        btnPickDate.setText("公历日期: " + chosenDate[0] + " (点击更改)");
                    }
                }, y, m, d).show();
            }
        });

        // Lunar spinners
        String[] lunarMonths = {"正月 (一月)", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月 (十一月)", "腊月 (十二月)"};
        ArrayAdapter<String> adapterLM = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lunarMonths);
        spLunarMonth.setAdapter(adapterLM);

        String[] lunarDays = {"初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"};
        ArrayAdapter<String> adapterLD = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lunarDays);
        spLunarDay.setAdapter(adapterLD);

        // Modes
        String[] modes = {"智能判断 (自动)", "正数累积 (已走过天数)", "倒数计时 (距离还有天数)"};
        ArrayAdapter<String> adapterMode = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, modes);
        spMode.setAdapter(adapterMode);

        // Categories
        List<String> cats = mDataManager.getCategories();
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats);
        spCategory.setAdapter(adapterCat);

        // Repeats
        String[] repeats = {"不重复", "每年重复 (生日/纪念日)", "每月重复 (还款/发薪)", "每周重复"};
        ArrayAdapter<String> adapterRep = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, repeats);
        spRepeat.setAdapter(adapterRep);

        // Segment toggle
        final Runnable updateSeg = new Runnable() {
            @Override
            public void run() {
                if (isLunar[0]) {
                    btnSegLunar.setBackgroundResource(R.drawable.chip_selected);
                    btnSegLunar.setTextColor(getResources().getColor(R.color.m3_on_primary_dark));
                    btnSegSolar.setBackground(null);
                    btnSegSolar.setTextColor(getResources().getColor(R.color.m3_text_secondary_dark));
                    boxSolar.setVisibility(View.GONE);
                    boxLunar.setVisibility(View.VISIBLE);
                } else {
                    btnSegSolar.setBackgroundResource(R.drawable.chip_selected);
                    btnSegSolar.setTextColor(getResources().getColor(R.color.m3_on_primary_dark));
                    btnSegLunar.setBackground(null);
                    btnSegLunar.setTextColor(getResources().getColor(R.color.m3_text_secondary_dark));
                    boxSolar.setVisibility(View.VISIBLE);
                    boxLunar.setVisibility(View.GONE);
                }
            }
        };

        btnSegSolar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLunar[0] = false;
                updateSeg.run();
            }
        });
        btnSegLunar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLunar[0] = true;
                updateSeg.run();
            }
        });

        // Prepopulate if editing
        if (isEdit) {
            etTitle.setText(existing.title);
            if ("since".equals(existing.mode)) spMode.setSelection(1);
            else if ("left".equals(existing.mode)) spMode.setSelection(2);
            else spMode.setSelection(0);

            int catIdx = cats.indexOf(existing.category);
            if (catIdx >= 0) spCategory.setSelection(catIdx);

            if ("yearly".equals(existing.repeat)) spRepeat.setSelection(1);
            else if ("monthly".equals(existing.repeat)) spRepeat.setSelection(2);
            else if ("weekly".equals(existing.repeat)) spRepeat.setSelection(3);
            else spRepeat.setSelection(0);

            swPinned.setChecked(existing.pinned);
            etNotes.setText(existing.notes != null ? existing.notes : "");

            if (existing.lunarMonth >= 1 && existing.lunarMonth <= 12) {
                spLunarMonth.setSelection(existing.lunarMonth - 1);
            }
            if (existing.lunarDay >= 1 && existing.lunarDay <= 30) {
                spLunarDay.setSelection(existing.lunarDay - 1);
            }
        }
        updateSeg.run();

        dialog.findViewById(R.id.btn_dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btn_form_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btn_form_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString().trim();
                if (title.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请输入事件名称", Toast.LENGTH_SHORT).show();
                    return;
                }

                EventItem item = isEdit ? existing : new EventItem();
                item.title = title;
                item.calendarType = isLunar[0] ? "lunar" : "solar";
                item.date = chosenDate[0];
                item.lunarMonth = spLunarMonth.getSelectedItemPosition() + 1;
                item.lunarDay = spLunarDay.getSelectedItemPosition() + 1;

                int modePos = spMode.getSelectedItemPosition();
                item.mode = modePos == 1 ? "since" : (modePos == 2 ? "left" : "auto");

                item.category = (String) spCategory.getSelectedItem();

                int repPos = spRepeat.getSelectedItemPosition();
                item.repeat = repPos == 1 ? "yearly" : (repPos == 2 ? "monthly" : (repPos == 3 ? "weekly" : "none"));

                item.pinned = swPinned.isChecked();
                item.notes = etNotes.getText().toString().trim();

                if (isEdit) {
                    mDataManager.updateEvent(item);
                } else {
                    item.id = "ev-" + System.currentTimeMillis();
                    mDataManager.addEvent(item);
                }

                dialog.dismiss();
                renderAll();
                Toast.makeText(MainActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // ================= Poster Dialog =================
    private void showPosterDialog(final EventItem event) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundResource(R.drawable.dialog_bg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("🖼️ 朋友圈高清海报导出");
        tvTitle.setTextSize(16);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(tvTitle);

        final ImageView ivPoster = new ImageView(this);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().heightPixels * 0.48)
        );
        ivLp.setMargins(0, 24, 0, 24);
        ivPoster.setLayoutParams(ivLp);
        root.addView(ivPoster);

        final boolean[] isM3 = {true};
        final Bitmap[] posterBmp = {null};

        final Runnable updatePreview = new Runnable() {
            @Override
            public void run() {
                posterBmp[0] = PosterGenerator.generatePoster(MainActivity.this, event, isM3[0], mDataManager.getPalette());
                ivPoster.setImageBitmap(posterBmp[0]);
            }
        };
        updatePreview.run();

        // Template toggle button
        TextView btnToggle = new TextView(this);
        btnToggle.setText("切换模板: [Material You 灵动] / [瑞士极简黑白]");
        btnToggle.setTextSize(12);
        btnToggle.setTextColor(getResources().getColor(R.color.m3_primary_dark));
        btnToggle.setGravity(android.view.Gravity.CENTER);
        btnToggle.setPadding(0, 10, 0, 16);
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isM3[0] = !isM3[0];
                updatePreview.run();
            }
        });
        root.addView(btnToggle);

        // Save Button
        TextView btnSave = new TextView(this);
        btnSave.setText("📥 保存 1080×1440 高清海报到相册");
        btnSave.setTextSize(13);
        btnSave.setTextColor(getResources().getColor(R.color.m3_on_primary_dark));
        btnSave.setTypeface(null, android.graphics.Typeface.BOLD);
        btnSave.setGravity(android.view.Gravity.CENTER);
        btnSave.setPadding(0, 24, 0, 24);
        btnSave.setBackgroundResource(R.drawable.btn_primary);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (posterBmp[0] != null) {
                    String uri = PosterGenerator.saveToGallery(MainActivity.this, posterBmp[0], event.title);
                    if (uri != null) {
                        Toast.makeText(MainActivity.this, "海报已成功保存至手机相册！", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, "保存相册失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        root.addView(btnSave);

        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        dialog.show();
    }

    // ================= Category Dialog =================
    private void showCategoryDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 36, 36, 36);
        root.setBackgroundResource(R.drawable.dialog_bg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("🏷️ 分类管理");
        tvTitle.setTextSize(16);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(tvTitle);

        final LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, 16, 0, 16);
        root.addView(listContainer);

        final Runnable refreshList = new Runnable() {
            @Override
            public void run() {
                listContainer.removeAllViews();
                final List<String> cats = mDataManager.getCategories();
                for (int i = 0; i < cats.size(); i++) {
                    final int idx = i;
                    final String cat = cats.get(i);
                    LinearLayout row = new LinearLayout(MainActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(16, 14, 16, 14);
                    row.setBackgroundResource(R.drawable.input_bg);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    lp.setMargins(0, 0, 0, 8);
                    row.setLayoutParams(lp);

                    TextView name = new TextView(MainActivity.this);
                    name.setText(getCategoryIcon(cat) + " " + cat);
                    name.setTextColor(Color.WHITE);
                    name.setTextSize(13);
                    LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                    row.addView(name, nameLp);

                    if (cats.size() > 1) {
                        TextView del = new TextView(MainActivity.this);
                        del.setText("删除");
                        del.setTextColor(0xFFFF8A80);
                        del.setTextSize(12);
                        del.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDataManager.deleteCategory(idx);
                                run();
                                renderAll();
                            }
                        });
                        row.addView(del);
                    }
                    listContainer.addView(row);
                }
            }
        };
        refreshList.run();

        // Add input
        final EditText etNew = new EditText(this);
        etNew.setHint("输入新分类名称 (如: 旅行计划)");
        etNew.setTextColor(Color.WHITE);
        etNew.setHintTextColor(0xFF888888);
        etNew.setTextSize(12);
        etNew.setPadding(20, 20, 20, 20);
        etNew.setBackgroundResource(R.drawable.input_bg);
        root.addView(etNew);

        TextView btnAdd = new TextView(this);
        btnAdd.setText("+ 添加新分类");
        btnAdd.setTextSize(13);
        btnAdd.setTextColor(getResources().getColor(R.color.m3_on_primary_dark));
        btnAdd.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAdd.setGravity(android.view.Gravity.CENTER);
        btnAdd.setPadding(0, 20, 0, 20);
        btnAdd.setBackgroundResource(R.drawable.btn_primary);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, 16, 0, 0);
        btnAdd.setLayoutParams(btnLp);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String val = etNew.getText().toString().trim();
                if (!val.isEmpty()) {
                    mDataManager.addCategory(val);
                    etNew.setText("");
                    refreshList.run();
                    renderAll();
                }
            }
        });
        root.addView(btnAdd);

        dialog.setContentView(root);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        dialog.show();
    }

    // ================= Settings Dialog =================
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙️ 设置与偏好");
        String[] options = {
            "恢复预设演示数据",
            "关于《时约》Native Android"
        };
        builder.setItems(options, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                if (which == 0) {
                    mDataManager.getEvents().clear();
                    mDataManager.getCategories().clear();
                    mDataManager.getCategories().addAll(DataManager.DEFAULT_CATEGORIES);
                    mDataManager.save();
                    renderAll();
                    Toast.makeText(MainActivity.this, "已重置为默认演示数据", Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("关于《时约》")
                        .setMessage("版本: 3.0 (Pure Native Android)\n架构: 100% 纯原生 Native + 2×2 原生桌面微件\n特性: Material 3 规范、离线隐私安全、农历推算、高清海报。")
                        .setPositiveButton("好的", null)
                        .show();
                }
            }
        });
        builder.show();
    }
}
