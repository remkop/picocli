// This file is in UTF-8 encoding and contains Japanese characters.

package picocli.examples.cjk;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "日本語対応デモ", mixinStandardHelpOptions = true, usageHelpWidth = 60,
        description = {"123456789012345678901234567890123456789012345678901234567890",
                "１２３４５６７８９０１２３４５６７８９０１２３４５６７８９０",
                "@|red 漢字|@、@|green ひらがな|@、@|blue カタカナ|@などはローマ字よりも幅が長い。Picocliはそういう文字に@|bold,blink ２倍の幅|@を与えます。"})
public class JapaneseDemo implements Runnable {
    @Override public void run() { }

    public static void main(String[] args) {
        new CommandLine(new JapaneseDemo()).execute(args);
    }
}