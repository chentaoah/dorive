package com.gitee.dorive.autoconfigure.starter;

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
