package com.techmartis.ebillboard;

/**
 * 全局常量定义
 */
public class Constants {

    /** SharedPreferences 键：自动加载的网址 */
    public static final String PREF_HOME_URL = "pref_home_url";

    /** SharedPreferences 键：自定义 User-Agent */
    public static final String PREF_USER_AGENT = "pref_user_agent";

    /** SharedPreferences 键：UA 预设类型 */
    public static final String PREF_UA_PRESET = "pref_ua_preset";

    /** 默认加载网址 */
    public static final String DEFAULT_URL = "https://www.baidu.com";

    /** UA 预设：系统默认 */
    public static final String UA_PRESET_DEFAULT = "default";

    /** UA 预设：桌面 Chrome */
    public static final String UA_PRESET_DESKTOP = "desktop";

    /** UA 预设：移动端 Chrome */
    public static final String UA_PRESET_MOBILE = "mobile";

    /** UA 预设：自定义 */
    public static final String UA_PRESET_CUSTOM = "custom";

    /** 桌面端 Chrome UA */
    public static final String UA_DESKTOP =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36";

    /** 移动端 Chrome UA */
    public static final String UA_MOBILE =
            "Mozilla/5.0 (Linux; Android 10; Pixel 4) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36";

    /** SharedPreferences 键：设置密码 */
    public static final String PREF_SETTINGS_PASSWORD = "pref_settings_password";

    /** 默认设置密码 */
    public static final String DEFAULT_PASSWORD = "123";
}
