package com.sorcerer.sorcery.iconpack.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.sorcerer.sorcery.iconpack.App;
import com.sorcerer.sorcery.iconpack.SorceryPrefs;

import javax.inject.Inject;

/**
 * @description:
 * @author: Sorcerer
 * @date: 2017/2/24
 */

public abstract class BaseFragment extends Fragment {
    @Inject
    protected SorceryPrefs mPrefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getContext().getApplicationContext()).getAppComponent().inject(this);
    }
}