https://user-images.githubusercontent.com/8509057/140267217-08b6b6f0-0ff0-4de4-8a46-325e0ffe2837.mp4

|Minecraft ver.|Minecraft Forge ver.|前提MOD|
|:---|:---|:---|
|1.16.3|34.1 or more|[Controllable v0.13](https://www.curseforge.com/minecraft/mc-mods/controllable/files/3335987), [Kotlin for Forge v1.15.1 or more](https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge/files/3452303)|

# JoyFlick
ゲームパッドを用いる、フリック入力ベースの高速な日本語入力手法・JoyFlickをMinecraft内で使えるようにするMODです。

## 操作方法
- +ボタン: ソフトウェアキーボードの展開, 発言

## 追加されるソフトウェアキーボード
いずれのキーボードも[Google CGI API for Japanese Input](https://www.google.co.jp/ime/cgiapi.html) によるIMEを搭載しており、
かな漢字変換を使うことができます。

### JoyFlick
![2021-11-04_15 25 40](https://user-images.githubusercontent.com/8509057/140267837-495761e9-b813-416a-8732-40a768f3d09e.png)

### 50音キーボード
![2021-11-04_15 25 59](https://user-images.githubusercontent.com/8509057/140267902-b9136187-e5aa-42fb-9027-8fad81ab5e47.png)

## 対応コントローラー
- （推奨）Nintendo Switch Pro Controller
- （動作確認）DualSense™

## 参考文献
- [JoyFlick: フリック入力に基づくゲームパッド向けかな文字入力手法](https://www.wiss.org/WISS2020Proceedings/data/17.pdf)
- [JoyFlick: Japanese Text Entry Using Dual Joysticks for Flick Input Users](https://link.springer.com/chapter/10.1007%2F978-3-030-85613-7_8)

# Developers
開発環境は*IntelliJ IDEA*をオススメします。[Minecraft Development](https://plugins.jetbrains.com/plugin/8327-minecraft-development) というイケてるプラグインが便利なので入れておきましょう。
JDKのインストールもお忘れなく。

- Minecraftを起動する: `runClient`
- `accesstransformer.cfg`の変更を反映する: （IntelliJの場合）`Reload All Gradle Projects`ボタンを押す
   - shiftキー2回で呼び出せるメニューで`Reload All Gradle Projects`を検索すると、actionsタブに出てくる
- 配布用の `.jar` ファイルを出力する：`shadowJar`
   - `jar` や `build` ではないので注意
   - 出力された成果物は`build/libs`に入っています

## Mixinを使うとき
メソッドやフィールドを`@Shadow` `@Inject`するときは、 **SRG名を使って** ください
ex. `NewChatGuiMixin.kt`