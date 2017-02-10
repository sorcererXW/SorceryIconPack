package com.sorcerer.sorcery.iconpack.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.sorcerer.sorcery.iconpack.net.coolapk.CoolapkClient;
import com.sorcerer.sorcery.iconpack.net.spiders.AppDisplayInfoSpider;
import com.sorcerer.sorcery.iconpack.net.spiders.models.AppDisplayInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * @description:
 * @author: Sorcerer
 * @date: 2017/2/8
 */

public class AppDisplayInfoGetter {
    public static Observable<AppDisplayInfo> getAppDisplayInfo(final String packageName,
                                                               Context context,
                                                               boolean chinese) {
        return Observable.just(new AppDisplayInfo())
                .flatMap(new Function<AppDisplayInfo, ObservableSource<AppDisplayInfo>>() {
                    @Override
                    public ObservableSource<AppDisplayInfo> apply(AppDisplayInfo info)
                            throws Exception {
                        if (PackageUtil.isPackageInstalled(context, packageName)) {
                            try {
                                PackageManager pm =
                                        context.getApplicationContext().getPackageManager();
                                try {
                                    ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
                                    info.setAppName((String) pm.getApplicationLabel(ai));
                                    info.setIcon(pm.getApplicationIcon(ai));
                                } catch (final PackageManager.NameNotFoundException e) {
                                    Timber.e(e);
                                }
                            } catch (Exception e) {
                                Timber.e(e);
                            }
                        }
                        return Observable.just(info);
                    }
                })
                .flatMap(new Function<AppDisplayInfo, ObservableSource<AppDisplayInfo>>() {
                    @Override
                    public ObservableSource<AppDisplayInfo> apply(AppDisplayInfo info)
                            throws Exception {

                        if (chinese && info.needAddition()) {
                            Timber.d("get from coolapk");
                            return CoolapkClient.getInstance().getAppDisplayInfo(packageName)
                                    .map(newInfo -> {
                                        info.add(newInfo);
                                        return info;
                                    });
                        } else {
                            return Observable.just(info);
                        }
                    }
                })
                .flatMap(new Function<AppDisplayInfo, ObservableSource<AppDisplayInfo>>() {
                    @Override
                    public ObservableSource<AppDisplayInfo> apply(AppDisplayInfo info)
                            throws Exception {
                        if (chinese && info.needAddition()) {
                            Timber.d("get from qq");
                            return AppDisplayInfoSpider.getNameFromQQ(packageName)
                                    .map(newInfo -> {
                                        info.add(newInfo);
                                        return info;
                                    });
                        } else {
                            return Observable.just(info);
                        }
                    }
                })
                .flatMap(new Function<AppDisplayInfo, ObservableSource<AppDisplayInfo>>() {
                    @Override
                    public ObservableSource<AppDisplayInfo> apply(AppDisplayInfo info)
                            throws Exception {
                        if (info.needAddition()) {
                            Timber.d("get from play");
                            return AppDisplayInfoSpider.getNameFromPlay(packageName)
                                    .map(newInfo -> {
                                        info.add(newInfo);
                                        return info;
                                    });
                        } else {
                            return Observable.just(info);
                        }
                    }
                })
                .map(info -> {
                    if (info.getAppName() != null) {
                        info.setAppName(formatTitle(info.getAppName()));
                    }
                    return info;
                });
    }

    private static String formatTitle(String title) {
        String[] reg = new String[]{
                "-", ":", "：",
        };
        for (int i = 0; i < reg.length && !isLengthOk(title); i++) {
            String[] split = title.split(reg[i]);
            if (split.length == 2) {
                title = split[0];
            }
        }
        if (!isLengthOk(title)){
            if (title.matches("[^\\(^\\)]*\\([^\\(^\\)]*\\)")) {
                title = title.split("\\(")[0];
            }
        }
        if (!isLengthOk(title)) {
            if (title.matches("[\\u4e00-\\u9fa5]+[A-Za-z]+")) {
                Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
                Matcher matcher = pattern.matcher(title);
                if (matcher.find()) {
                    title = matcher.group();
                }
            } else if (title.matches("[A-Za-z]+[\\u4e00-\\u9fa5]+")) {
                Pattern pattern = Pattern.compile("[A-Za-z]+");
                Matcher matcher = pattern.matcher(title);
                if (matcher.find()) {
                    title = matcher.group();
                }
            }
        }

        return title.trim();
    }

    private static boolean isLengthOk(String s) {
        return s.replaceAll("[^\\x00-\\xff]", "**").getBytes().length < 20;
    }
}
