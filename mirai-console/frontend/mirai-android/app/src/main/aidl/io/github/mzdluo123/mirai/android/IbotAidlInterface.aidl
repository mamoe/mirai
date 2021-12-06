// IbotAidlInterface.aidl
package io.github.mzdluo123.mirai.android;

// Declare any non-default types here with import statements
interface IbotAidlInterface {
    //Console
    String[] getLog();
    void clearLog();
    void sendLog(String log);
    void runCmd(String cmd);
    byte[] getCaptcha();
    String getUrl();
    void submitVerificationResult(String result);
    long getLogonId();

    //Script
    String[] getHostList();
    boolean reloadScript(int index);
    void setScriptConfig(String config);
    void deleteScript(int index);
    int getScriptSize();
    void openScript(int index);
    boolean createScript(String name,int type);
    void enableScript(int index);
    void disableScript(int index);
    String getBotInfo();

}