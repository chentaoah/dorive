/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.web.v1.impl.banner;

import org.springframework.beans.factory.InitializingBean;

public class BannerPrinter implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        System.out.println(" __   __   __          ___ ");
        System.out.println("|  \\ /  \\ |__) | \\  / |__  ");
        System.out.println("|__/ \\__/ |  \\ |  \\/  |___ ");
        System.out.println("                      " + DoriveVersion.getVersion() + "");
    }

    public static void main(String[] args) {
        BannerPrinter bannerPrinter = new BannerPrinter();
        bannerPrinter.afterPropertiesSet();
    }

}
