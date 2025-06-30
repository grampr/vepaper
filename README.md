# README (English)

## Vepaper – run and control standalone Paper servers from Velocity  
Version 1.12.4 - requires Velocity 3.4+ & Java 17+

---

### 1. What is Vepaper?
Vepaper is a Velocity proxy plug-in that starts one or more Paper servers **as local
sub-processes** and lets you control them without leaving the proxy.

* Zero configuration by default – drop the jar, start Velocity, play.  
* Automatic Paper jar download (latest build).  
* Automatically generates `server.properties` and accepts Mojang EULA.  
* Paper console output is relayed to the Velocity console  
  (can be disabled with a command).  
* Send any Paper console command directly from Velocity.  
* No changes are ever made to `velocity.toml`, `paper-global.yml` or **secrets**.

---

### 2. Installation
1. Build from source (`mvn clean package`, JDK 17) or download the release jar.  
2. Put `vepaper-*.jar` into the `plugins/` folder of your Velocity proxy.  
3. (Re-)Start Velocity.  
   * The folder `plugins/vepaper/` and a file `config.properties`
     are created automatically.  
   * Default settings:  
     • 1 Paper instance  
     • port 25563  
     • 4096 MB RAM
4. Edit `config.properties` if necessary and restart Velocity.

---

### 3. Configuration (`plugins/vepaper/config.properties`)
```
instances     = 1      # number of Paper servers to launch
startingPort  = 25563  # first port; each additional instance uses +1
maxMemoryMb   = 4096   # RAM per instance
```

---

### 4. Commands (proxy console / in-game OP)

| Command                      | Description                                   |
|------------------------------|-----------------------------------------------|
| `/paper <index|port> <cmd>` | Send any console command to a Paper instance.<br>index = 0-based order, port = TCP port. |
| `/paperlog on` / `off`      | Enable / disable relaying Paper log lines to the Velocity console (default **on**). |

Examples  
```
/paper 0 say Hello from Velocity!
/paper 25563 stop
/paperlog off
```

---

### 5. Files generated per instance
```
servers/
 └─ paper-25563/
     ├─ paper.jar         (auto-download)
     ├─ eula.txt          (always eula=true)
     ├─ server.properties (generated with server-port etc.)
     └─ latest.log        (Paper console output)
```

The plug-in **does not** touch any other configuration files; feel free to edit
`paper-global.yml`, `spigot.yml`, resource packs, etc. manually.

---

### 6. Uninstallation
Stop Velocity, delete the `vepaper` folder and the plug-in jar, then remove the
generated `servers/` directories if you no longer need the Paper instances.

---

### 7. License
MIT License – see repository.



---

# README（日本語）

## Vepaper – Velocity から Paper サーバーを起動・操作するプラグイン  
バージョン 1.12.4 - Velocity 3.4+ / Java 17 以上が必要

---

### 1. 概要
Vepaper は Velocity プロキシ上で Paper サーバーを **サブプロセスとして自動起動**
し、コンソール操作を Velocity から直接行えるようにするプラグインです。

* Jar を入れて起動するだけで動作（デフォルト設定済み）。  
* Paper 最新ビルドを自動ダウンロード。  
* `server.properties` を自動生成し `eula=true` を設定。  
* Paper のログを Velocity コンソールへ転送  
 （コマンドで ON/OFF 切替可能）。  
* 任意の Paper コンソールコマンドを Velocity から送信可能。  
* `velocity.toml` や `paper-global.yml`、シークレットは **一切書き換えません**。

---

### 2. 導入手順
1. ソースからビルド（`mvn clean package`、JDK 17）  
   もしくは Release Jar を取得。  
2. Jar を Velocity の `plugins/` フォルダに配置。  
3. Velocity を起動。  
   * `plugins/vepaper/` と `config.properties` が自動生成されます。  
   * デフォルト設定  
     ・Paper インスタンス 1 個  
     ・ポート 25563  
     ・メモリ 4096 MB
4. 設定を変更する場合は `config.properties` を編集後、Velocity を再起動。

---

### 3. 設定ファイル（`plugins/vepaper/config.properties`）
```
instances     = 1      # 起動する Paper の数
startingPort  = 25563  # 先頭のポート番号（以降 +1 ずつ）
maxMemoryMb   = 4096   # インスタンスごとのメモリ (MB)
```

---

### 4. コマンド（プロキシコンソール／OP プレイヤー）
| コマンド                         | 説明 |
|----------------------------------|------|
| `/paper <index|port> <cmd>`      | 指定 Paper へコンソールコマンド送信。<br>index は 0 から、port は TCP ポート番号。 |
| `/paperlog on` / `off`           | Paper のログ転送を有効／無効（デフォルト on）。 |

使用例  
```
/paper 0 say Velocity からこんにちは
/paper 25563 stop
/paperlog off
```

---

### 5. 生成されるファイル
```
servers/
 └─ paper-25563/
     ├─ paper.jar         （自動 DL）
     ├─ eula.txt          （常に eula=true）
     ├─ server.properties （ポート等を自動設定）
     └─ latest.log        （Paper ログ）
```
`paper-global.yml` や `spigot.yml` などその他の設定ファイルは
プラグインが変更しません。必要に応じて手動で編集してください。

---

### 6. アンインストール
Velocity を停止し、`plugins/vepaper/` フォルダとプラグイン jar を削除。
不要であれば `servers/` 内の Paper ディレクトリも削除してください。

---

### 7. ライセンス
MIT License（レポジトリ参照）。
