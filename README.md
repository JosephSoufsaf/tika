# GitHub Actions Workflow Modification Design and Implementation Choices

The goal is to have the build fail when a commit introduces a new vulnerability detected by Semgrep without failing for the findings that already exist in Tika.

## Rules and Findings

In the original `tika-core` code, Semgrep reported 8 findings:
- [semgrep-before.txt](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-before/semgrep-before.txt)
- [semgrep-before.json](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-before/semgrep-before.json)

These files were obtained by running `semgrep scan --config r/java.lang.security tika-core --[text/json] --output semgrep-before.[txt/json]` in Powershell locally as documented in Semgrep's CLI [reference page](https://semgrep.dev/docs/cli-reference). To choose the rules, we filtered Semgrep's rule registry ([semgrep.dev/r](https://semgrep.dev/r)) for `java.lang.security`. Here, `java` is the language, `lang` designates rules for the standard language and its library, and `security` is the security category. The search returned 115 rules. Of these, 29 are Pro rules, which can only be run with a Semgrep account, and 4 are not associated with Java code. The workflow therefore runs the remaining 82 rules applicable to Java.

Only new findings, not including the 8 above, should be checked for regression. To do so, we modified the case study's CI workflow that builds and tests `tika-core` (`tache2-tests.yml`, added in Tâche 2) and renamed it [`cas-etude-workflow.yml`](.github/workflows/cas-etude-workflow.yml). As before for Tâche 2, Tika's original workflows are disabled.

The workflow runs on every push, every pull request, and manually. It has two parallel jobs: `semgrep`, which looks for new vulnerabilities, and `build and test tika-core`. If either fails, the run fails. Keeping them separate shows directly why a build failed, and lets the tests run even when a vulnerability is found.

## `semgrep` job in workflow

The job checks out the code with its full history (needed to access the baseline commit), installs Python and Semgrep 1.183.0 (the same version as used locally), then runs the script [`semgrep-scan.sh`](https://github.com/JosephSoufsaf/tika/blob/tache3/.github/scripts/semgrep-scan.sh).

With the `--baseline-commit` option in the [Semgrep CLI Reference](https://semgrep.dev/docs/cli-reference), Semgrep only analyzes the files changed since a reference commit and only reports findings absent from that commit. The existing findings are therefore ignored. The `--error` option leads to exit with code 1 if there are any new findings.

On a push, the baseline is the branch's last commit before the push, so the failure points to the exact commit that introduced the vulnerability. On a pull request, it's the PR's base commit, so all of the PR's commits are checked, not just the last one. On a manual run or the first push of a new branch, GitHub provides no previous commit, so the script uses the point where the branch left `main`.

## Validation

In the original [test run](https://github.com/JosephSoufsaf/tika/actions/runs/42403174876), the only change was to the README, so `semgrep` found nothing new and passed. On the other hand, each of the 7 vulnerabilities was added in its own commit and each made the `semgrep` job fail, reporting exactly the finding that commit introduced. These runs are linked in the individual vulnerability sections below.

The commit for vulnerability 2 is the clearest demonstration of the diff-aware check. `ExternalParser.java` already contained a `command-injection-process-builder` finding on line 193 (part of Tika's baseline), yet the job reported only the new finding on line 303 and ignored the pre-existing one. This confirms the build fails on vulnerabilities introduced by the commit, not on findings that already existed.

# Documentation of Vulnerabilities

We started from Semgrep's [vulnerability overview page](https://docs.semgrep.dev/learn/vulnerabilities/overview) and filtered Semgrep's [rule registry](https://semgrep.dev/r) accordingly. We selected the first four vulnerabilities from those applicable in the overview page and then three others that seemed interesting and that were related to other courses we have taken.

## Vulnerability 1: [Code Injection](https://docs.semgrep.dev/learn/vulnerabilities/code-injection)

For the first one we filtered the [rule registry](https://semgrep.dev/r) by searching for `java.lang.security` and filtered the OWASP for *Injection*. We chose [`script-engine-injection`](https://semgrep.dev/r?q=java.lang.security.audit.script-engine-injection.script-engine-injection).

As for the module, we chose `ConfigDeserializer` because it turns Tika's JSON configuration into settings objects. The configurations are external and therefore a possible entry point for data that can be controlled externally. In `ConfigDeserializer`, we added [`evaluateConfigExpression()`](https://github.com/JosephSoufsaf/tika/blob/fe749a25139bbc1cd32e50fceb73e2252aa7b796/tika-core/src/main/java/org/apache/tika/config/ConfigDeserializer.java#L123-L136) which passes the configuration value to the `eval()` method of `ScriptEngine`, which in turn runs it as a program. Whoever controls the configuration can therefore execute arbitrary code with the Tika process's permissions. 

A local scan of the modified file reported one finding:

```
❯❱ java.lang.security.audit.script-engine-injection.script-engine-injection
      ❰❰ Blocking ❱❱
      Detected potential code injection using ScriptEngine. Ensure user-controlled data cannot enter
      '.eval()', otherwise, this is a code injection vulnerability.
      Details: https://sg.run/gLqn

      133┆ public static Object evaluateConfigExpression(String expression) throws ScriptException {
      134┆     ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
      135┆     return engine.eval(expression);
      136┆ }
```

The commit adding this first vulnerability is found in the following [run](https://github.com/JosephSoufsaf/tika/actions/runs/43116938479).

## Vulnerability 2: [Command Injection](https://docs.semgrep.dev/learn/vulnerabilities/command-injection)

For the second one we again filtered the [rule registry](https://semgrep.dev/r) by searching for `java.lang.security` and filtered the OWASP for *Injection*. We chose [`command-injection-process-builder`](https://semgrep.dev/r?q=java.lang.security.audit.command-injection-process-builder.command-injection-process-builder), which flags a concatenated string passed to a `ProcessBuilder`.

As for the module, we chose `ExternalParser` because its purpose is to run external programs (such as ffmpeg or exiftool) on documents, so running commands is a normal part of its job. The file already contains a safe `ProcessBuilder` use (line 193) that passes the command as a list of separate arguments, which the operating system runs directly without a shell, so a value cannot inject extra commands. Semgrep flags it as well, which is a false positive. On the other hand, we added [`runShellCheck()`](https://github.com/JosephSoufsaf/tika/blob/656acf92e3008eeec96af9a15b033c5d8c836289/tika-core/src/main/java/org/apache/tika/parser/external/ExternalParser.java#L293-L304), which takes the command from the configuration and adds it directly into a `sh -c` shell command. A shell interprets characters such as `;`, `|` and `&&`, so a configuration value like `ffmpeg; rm -rf /data` makes the shell run both `command -v ffmpeg` and `rm -rf /data`, with the Tika process's permissions.

A local scan of the modified file reported two findings, the existing one on line 193 and the new one on line 303:

```
❯❯❱ java.lang.security.audit.command-injection-process-builder.command-injection-process-builder
      ❰❰ Blocking ❱❱
      A formatted or concatenated string was detected as input to a ProcessBuilder call. This is dangerous
      if a variable is controlled by user input and could result in a command injection. Ensure your
      variables are not controlled by users or sufficiently sanitized.
      Details: https://sg.run/gJJe

      193┆ new ProcessBuilder(thisCommandLine),
      ⋮┆----------------------------------------
      303┆ new ProcessBuilder("sh", "-c", "command -v " + userCommand).start();
```

The commit adding this second vulnerability is found in the following [run](https://github.com/JosephSoufsaf/tika/actions/runs/43130652712). In the workflow, the `semgrep` job reports only the new finding on line 303 of `ExternalParser.java`. The finding on line 193 is part of Tika's original baseline, and vulnerability 1 (`script-engine-injection`, added in the previous commit) is part of this commit's baseline, so neither is reported again. This shows that the workflow fails only on vulnerabilities introduced by the commit itself.

## Vulnerability 3: [Insecure Deserialization](https://docs.semgrep.dev/learn/vulnerabilities/insecure-deserialization)

For the third one we filtered the [rule registry](https://semgrep.dev/r) by searching for `java.lang.security` and filtered the OWASP for *Insecure Deserialization*. We chose [`object-deserialization`](https://semgrep.dev/r?q=java.lang.security.audit.object-deserialization.object-deserialization), which flags reading objects with `ObjectInputStream`.

As for the module, we chose `MimeTypesFactory` because it creates the `MimeTypes` registry and `MimeTypes` is `Serializable`, as we knew from working on Tâche 2. A method that loads a previously cached, serialized registry is a realisitic feature to add. In `MimeTypesFactory`, we added [`loadCachedMimeTypes()`](https://github.com/JosephSoufsaf/tika/blob/4a45b2da536acabdded1f35e7fb2a425e09e80a9/tika-core/src/main/java/org/apache/tika/mime/MimeTypesFactory.java#L208-L224), which reads the registry from a stream with `ObjectInputStream.readObject()`. The method `readObject()` rebuilds arbitrary Java objects from the bytes, so if the stream is not trusted, a malicious stream can trigger code execution with the Tika process's permissions.

A local scan of the modified file reported one finding:

```
❯❱ java.lang.security.audit.object-deserialization.object-deserialization
      ❰❰ Blocking ❱❱
      Found object deserialization using ObjectInputStream. Deserializing entire Java objects is dangerous
      because malicious actors can create Java object streams with unintended consequences. Ensure that
      the objects being deserialized are not user-controlled. If this must be done, consider using HMACs
      to sign the data stream to make sure it is not tampered with, or consider only transmitting object
      fields and populating a new object.
      Details: https://sg.run/Ek0A

      222┆ ObjectInputStream ois = new ObjectInputStream(in);
```

The commit adding this third vulnerability is found in the following [run](https://github.com/JosephSoufsaf/tika/actions/runs/43239360394).

## Vulnerability 4: [SQL Injection](https://docs.semgrep.dev/learn/vulnerabilities/sql-injection)

For the fourth one we again filtered the [rule registry](https://semgrep.dev/r) by searching for `java.lang.security` and filtered the OWASP for *Injection*. We chose [`formatted-sql-string`](https://semgrep.dev/r?q=java.lang.security.audit.formatted-sql-string.formatted-sql-string), which flags a concatenated string passed to an SQL execution method.

As for the module, we chose `Metadata` because it holds the key/value data Tika extracts from a document, and that data comes from the document itself so it can be controlled by whoever authors the document. A method that looks up a metadata value in a database is a realistic feature to add. In `Metadata`, we added [`findMetadataValue()`](https://github.com/JosephSoufsaf/tika/blob/19d22ddff82196c908b6af3097e8019f8f52aa65/tika-core/src/main/java/org/apache/tika/metadata/Metadata.java#L803-L819), which builds an SQL query by concatenating the metadata value into the query string instead of using a parameterized query. A document whose metadata contains SQL characters can change the meaning of the query and read or destroy data in the database.

A local scan of the modified file reported two findings on the same line, from two related SQL injection rules:

```
❯❯❱ java.lang.security.audit.formatted-sql-string.formatted-sql-string
      ❰❰ Blocking ❱❱
      Detected a formatted string in a SQL statement. This could lead to SQL injection if variables in the
      SQL statement are not properly sanitized. Use a prepared statements (java.sql.PreparedStatement)
      instead. You can obtain a PreparedStatement using 'connection.prepareStatement'.
      Details: https://sg.run/OPXp

      818┆ statement.executeQuery(sql);

❯❱ java.lang.security.audit.sqli.jdbc-sqli.jdbc-sqli
      ❰❰ Blocking ❱❱
      Detected a formatted string in a SQL statement. This could lead to SQL injection if variables in the
      SQL statement are not properly sanitized. Use a prepared statements (java.sql.PreparedStatement)
      instead. You can obtain a PreparedStatement using 'connection.prepareStatement'.
      Details: https://sg.run/AvkL

      818┆ statement.executeQuery(sql);
```

Both findings describe the same vulnerability (the concatenated SQL string on line 818); `formatted-sql-string` and `jdbc-sqli` are two rules that detect the same pattern. The commit adding this fourth vulnerability is found in the following [run](https://github.com/JosephSoufsaf/tika/actions/runs/43247760533).

## Vulnerability 5: Weak Hash (MD5)

For the fifth one we filtered the [rule registry](https://semgrep.dev/r) by searching for `java.lang.security` and filtered the OWASP for *Cryptographic Failures*. We chose [`use-of-md5`](https://semgrep.dev/r?q=java.lang.security.audit.crypto.use-of-md5.use-of-md5) which flags a call to `MessageDigest.getInstance("MD5")`. This vulnerability is also interesting to us because we studied MD5 and its weaknesses (collision attacks) in our IFT3275 (Sécurité Informatique) class.

As for the module, we chose `InputStreamDigester` because its purpose is to compute a cryptographic digest over a document's content. Choosing a hash algorithm there is a normal part of its job, so selecting MD5 is a realistic mistake. In `InputStreamDigester`, we added [`md5Digest()`](https://github.com/JosephSoufsaf/tika/blob/35bdea7431c946bac9c315942ba658a2bcd805b6/tika-core/src/main/java/org/apache/tika/digest/InputStreamDigester.java#L121-L135), which computes the digest with `MessageDigest.getInstance("MD5")`. Given that MD5 is no longer collision resistant, an attacker can produce two different documents with the same MD5 digest. Thus, a digest used to verify a document’s integrity or to check whether two documents are identical can be bypassed.

A local scan of the modified file reported one finding:

```
❯❱ java.lang.security.audit.crypto.use-of-md5.use-of-md5
      ❰❰ Blocking ❱❱
      Detected MD5 hash algorithm which is considered insecure. MD5 is not collision resistant and is
      therefore not suitable as a cryptographic signature. Use HMAC instead.
      Details: https://sg.run/ryJn

      ▶▶┆ Autofix ▶ "SHA-512"
      132┆ MessageDigest md5Digest = MessageDigest.getInstance("MD5");
```

The commit adding this fifth vulnerability is found in the following [run](https://github.com/JosephSoufsaf/tika/actions/runs/43466654662).

## Vulnerability 6: Disabled Hostname Verification (TLS)

For the sixth one we filtered the [rule registry](https://semgrep.dev/r) by searching for `java.lang.security` and filtered the OWASP for *Identification and Authentication Failures*. We chose [`insecure-hostname-verifier`](https://semgrep.dev/r?q=insecure-hostname-verifier) which flags a `HostnameVerifier` whose `verify()` method accepts any host. This vulnerability is also interesting to us because we studied TLS and certificate verification in our IFT3325 (Téléinformatique) class.

As for the module, we chose `NetworkParser` because it fetches documents from remote servers (it is already the source of the baseline `unencrypted-socket` finding), so TLS connections are a normal part of its job. Skipping the hostname check is a realistic "shortcut" a developer might take to make a certificate error go away. In `NetworkParser`, we added [`AllHostsVerifier`](https://github.com/JosephSoufsaf/tika/blob/f797d07a0e5be58fcbd88390ab251c978f2d9755/tika-core/src/main/java/org/apache/tika/parser/NetworkParser.java#L172-L183), a `HostnameVerifier` whose `verify()` always returns `true`. A TLS certificate is supposed to prove that the server contacted really is the one that was requested. Verifying the hostname is part of that proof. Because this verifier accepts any host, that check is disabled, which lets a man-in-the-middle attacker present their own certificate and impersonate the server.

A local scan of the modified file reported the new finding (line 179) alongside the pre-existing `unencrypted-socket` finding (line 70):

```
❯❱ java.lang.security.audit.crypto.unencrypted-socket.unencrypted-socket
      ❰❰ Blocking ❱❱
      Detected use of a Java socket that is not encrypted. As a result, the traffic could be read by an
      attacker intercepting the network traffic. Use an SSLSocket created by 'SSLSocketFactory' or
      'SSLServerSocketFactory' instead.
      Details: https://sg.run/W8zA

      70┆ try (Socket socket = new Socket(uri.getHost(), uri.getPort())) {

❯❱ java.lang.security.audit.crypto.ssl.insecure-hostname-verifier.insecure-hostname-verifier
      ❰❰ Blocking ❱❱
      Insecure HostnameVerifier implementation detected. This will accept any SSL certificate with any
      hostname, which creates the possibility for man-in-the-middle attacks.
      Details: https://sg.run/5QoD

      179┆ public static class AllHostsVerifier implements HostnameVerifier {
      180┆     public boolean verify(String hostname, SSLSession session) {
      181┆         return true;
      182┆     }
      183┆ }
```

In the workflow, only the `insecure-hostname-verifier` finding on line 179 is reported as new; the `unencrypted-socket` finding is part of Tika's original baseline and is ignored. The commit adding this sixth vulnerability is found in the following [run](https://github.com/JosephSoufsaf/tika/actions/runs/43476113532).

## Vulnerability 7: Overly Permissive File Permissions

For the seventh one we filtered the [rule registry](https://semgrep.dev/r) by searching for `java.lang.security` and filtered the OWASP for *Broken Access Control*. We chose [`overly-permissive-file-permission`](https://semgrep.dev/r?q=overly-permissive-file-permission) which flags giving read, write, and execute permission to all users (`rwxrwxrwx`). This vulnerability is also interesting to us because Unix file permissions and the principle of least privilege were covered in our IFT2245 (Systèmes d'exploitation) class.

As for the module, we chose `TemporaryResources` because its job is to create the temporary files Tika uses while parsing, and those files hold the content of the document being processed. Setting permissions on a temporary file there is a normal part of its job. In `TemporaryResources`, we added [`createSharedTempFile()`](https://github.com/JosephSoufsaf/tika/blob/818ac3695d70f5d8e4215d6a8ae5cc4819b1f6f2/tika-core/src/main/java/org/apache/tika/io/TemporaryResources.java#L209-L223), which creates a temporary file and then sets its permissions to `rwxrwxrwx`. The last three characters grant every other user on the machine read, write, and execute access, so any user can read the document content stored in the file or modify it while Tika is processing it. This breaks the principle of least privilege: a temporary file should only be accessible to the account that created it.

A local scan of the modified file reported one finding:

```
❯❱ java.lang.security.audit.overly-permissive-file-permission.overly-permissive-file-permission
      ❰❰ Blocking ❱❱
      Detected file permissions that are overly permissive (read, write, and execute). It is generally a
      bad practices to set overly permissive file permission such as read+write+exec for all users. If the
      file affected is a configuration, a binary, a script or sensitive data, it can lead to privilege
      escalation or information leakage. Instead, follow the principle of least privilege and give users
      only the  permissions they need.
      Details: https://sg.run/LwzJ

      221┆ Files.setPosixFilePermissions(path,
      222┆     PosixFilePermissions.fromString("rwxrwxrwx"));
```

The commit adding this seventh vulnerability is found in the following [run](https://github.com/JosephSoufsaf/tika/actions/runs/43719281541).

# Before vs. After

Before the additions, Semgrep reported 8 findings on `tika-core` ([semgrep-before.txt](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-before/semgrep-before.txt)). After introducing the 7 vulnerabilities, it reports 16 findings ([semgrep-after.txt](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-after/semgrep-after.txt)): the same 8 findings, plus 8 new ones.

The 8 new findings correspond to the 7 vulnerabilities; vulnerability 4 (SQL Injection) is reported by two rules on the same line (`formatted-sql-string` and `jdbc-sqli`), so it accounts for 2 of the 8.

| # | Vulnerability | New rule | File | CWE | OWASP
|---|---|---|---|---|---|
| 1 | Code Injection | `script-engine-injection` | `config/ConfigDeserializer.java` | CWE-94 | A03 – Injection |
| 2 | Command Injection | `command-injection-process-builder` | `parser/external/ExternalParser.java` | CWE-78 | A03 – Injection |
| 3 | Insecure Deserialization | `object-deserialization` | `mime/MimeTypesFactory.java` | CWE-502 | A08 – Software and Data Integrity Failures |
| 4 | SQL Injection | `formatted-sql-string` + `jdbc-sqli` | `metadata/Metadata.java` | CWE-89 | A03 – Injection |
| 5 | Weak Hash (MD5) | `use-of-md5` | `digest/InputStreamDigester.java` | CWE-328 | A02 – Cryptographic Failures |
| 6 | Disabled Hostname Verification | `insecure-hostname-verifier` | `parser/NetworkParser.java` | CWE-295 | A07 – Identification and Authentication Failures |
| 7 | Overly Permissive File Permissions | `overly-permissive-file-permission` | `io/TemporaryResources.java` | CWE-276 | A01 – Broken Access Control |

The seven vulnerabilities span seven distinct CWEs and five OWASP 2021 categories. Three of them fall under OWASP A03 (Injection), but they are different vulnerability types: Semgrep's own vulnerability overview page lists Code Injection, Command Injection, and SQL Injection as separate categories, each with a different target (a script engine, the operating system, and the database) and a different CWE.

The 8 original findings are never reported as new in any of the commit scans because they exist in both the baseline and the current code. Each vulnerability was added in its own commit, so on each push the workflow reported exactly the findings that commit introduced. A pull request into `main` would instead report all 8 new findings at once, since none of them exist on `main`.

# Humour

## Ce qui a été fait

Quand un test du cas d'étude échoue, le CI affiche un Pepe triste en ASCII art et un lien vers une page web où un meme (Pepe sur la pochette de *Graduation*) s'affiche avec de l'audio. C'est notre variante du rickroll demandé par l'énoncé.

## Comment ça fonctionne

1. Le job `test` de `cas-etude-workflow.yml` build et exécute les tests `MimeType_*_Test`
2. Le job `meme` de `cas-etude-workflow.yml` dépend du job `test` et ne s'exécute que si `test` échoue
    1. needs: [test] + if: always() && needs.test.result == 'failure'
    2. always() est nécessaire, sinon GitHub ignorerait le job dès que test échoue
    3. le build reste rouge: le meme n'efface pas l'erreur
3. Le workflow dans .github/actions/meme/:
   - affiche l'ASCII art (pepe.txt) et le lien dans les logs
   - ajoute une annotation d'erreur avec le lien en haut du run
   - écrit l'art et le lien cliquable dans le résumé du run
4. La page est dans docs/meme/ et publiée avec GitHub Pages:
   https://josephsoufsaf.github.io/tika/meme/meme.html

## Comment l'utiliser

1. Ouvrez l'onglet **Actions** et cliquez sur un run dont le job `test` est rouge, par example [celui-ci](https://github.com/JosephSoufsaf/tika/actions/runs/43759570845)
2. Sur la page **Summary** du run, lisez le sad Pepe et cliquez sur le lien *« Cliquez ici pour la suite »* (les logs de l'étape « Sad Pepe et lien » du job dans [.github/actions/meme/action.yml](https://github.com/JosephSoufsaf/tika/blob/tache3/.github/actions/meme/action.yml) contiennent aussi l'art et le lien)
3. Sur la page avec l'ours, cliquez dessus, puis le meme s'affiche et le son démarre (montez le volume)
4. Sur un run vert, le job dans [.github/actions/meme/action.yml](https://github.com/JosephSoufsaf/tika/blob/tache3/.github/actions/meme/action.yml) est sauté et le meme ne s'affiche pas

Pour essayer vous-même, vous pouvez faire échouer n'importe quel test dans la suite de tests `MimeType_*_Test`.

## Sources : Élément d'humour (site web)

1. Image de Pepe the frog: https://www.amazon.ca/Based-Ascension-Notebook-pepe-notebook/dp/B0FXX719VR
2. Image de l'ours: Art fait par Takashi Murakami pour Kanye West’s Graduation. Image via Gigwise. (www.gigwise.com/photos/63527/9/Daft-Punk-Radiohead-KISS-The-Best-Band-Logos-Ever/)
3. Chanson qui joue dans le background: “Stronger” by Kanye West, from Graduation (2007). Écrit par Kanye West, Mike Dean, Thomas Bangalter, Guy-Manuel de Homem-Christo, et Edwin Birdsong.
4. Ascii art de Pepe the frog: https://emojicombos.com/pepe-ascii-art

# Utilisation IA

L’intelligence artificielle a été utilisée dans ce projet pour traduire en français certaines sections qui avaient été initialement rédigées en anglais.


