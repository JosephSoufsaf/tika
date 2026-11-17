# Choix de conception et d'implémentation de la modification du workflow GitHub Actions

L'objectif est de faire échouer le build lorsqu'un commit introduit une nouvelle vulnérabilité détectée par Semgrep, sans échouer à cause des alertes qui existent déjà dans Tika.

## Règles et alertes

Dans le code original de `tika-core`, Semgrep a signalé 8 alertes :
- [semgrep-before.txt](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-before/semgrep-before.txt)
- [semgrep-before.json](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-before/semgrep-before.json)

Ces fichiers ont été obtenus en exécutant `semgrep scan --config r/java.lang.security tika-core --[text/json] --output semgrep-before.[txt/json]` localement dans PowerShell, comme documenté dans la [page de référence de la CLI](https://semgrep.dev/docs/cli-reference) de Semgrep. Pour choisir les règles, nous avons filtré le registre de règles de Semgrep ([semgrep.dev/r](https://semgrep.dev/r)) sur `java.lang.security`. Ici, `java` est le langage, `lang` désigne les règles pour le langage standard et sa bibliothèque, et `security` est la catégorie de sécurité. La recherche a retourné 115 règles. Parmi celles-ci, 29 sont des règles Pro, qui ne peuvent être exécutées qu'avec un compte Semgrep, et 4 ne concernent pas du code Java. Le workflow exécute donc les 82 règles restantes applicables à Java.

Seules les nouvelles alertes, excluant les 8 ci-dessus, doivent être vérifiées pour détecter une régression. Pour ce faire, nous avons modifié le workflow de CI du cas d'étude qui build et teste `tika-core` (`tache2-tests.yml`, ajouté à la Tâche 2) et l'avons renommé [`cas-etude-workflow.yml`](.github/workflows/cas-etude-workflow.yml). Comme pour la Tâche 2, les workflows originaux de Tika sont désactivés.

Le workflow s'exécute à chaque push, à chaque pull request, et manuellement. Il comporte deux jobs parallèles : `semgrep`, qui cherche les nouvelles vulnérabilités, et `build and test tika-core`. Si l'un des deux échoue, l'exécution échoue. Les garder séparés montre directement pourquoi un build a échoué, et permet aux tests de s'exécuter même lorsqu'une vulnérabilité est détectée.

## Le job `semgrep` dans le workflow

Le job récupère le code avec tout son historique (nécessaire pour accéder au commit de référence), installe Python et Semgrep 1.183.0 (la même version que celle utilisée localement), puis exécute le script [`semgrep-scan.sh`](https://github.com/JosephSoufsaf/tika/blob/tache3/.github/scripts/semgrep-scan.sh).

Avec l'option `--baseline-commit` de la [référence de la CLI de Semgrep](https://semgrep.dev/docs/cli-reference), Semgrep n'analyse que les fichiers modifiés depuis un commit de référence et ne signale que les alertes absentes de ce commit. Les alertes existantes sont donc ignorées. L'option `--error` fait sortir avec le code 1 s'il y a de nouvelles alertes.

Lors d'un push, le commit de référence est le dernier commit de la branche avant le push, de sorte que l'échec pointe vers le commit exact qui a introduit la vulnérabilité. Lors d'une pull request, il s'agit du commit de base de la PR, de sorte que tous les commits de la PR sont vérifiés, et pas seulement le dernier. Lors d'une exécution manuelle ou du premier push d'une nouvelle branche, GitHub ne fournit aucun commit précédent, donc le script utilise le point où la branche a quitté `main`.

## Validation

Dans l'[exécution de test](https://github.com/JosephSoufsaf/tika/actions/runs/42403174876) initiale, le seul changement portait sur le README, donc `semgrep` n'a rien trouvé de nouveau et a réussi. À l'inverse, chacune des 7 vulnérabilités a été ajoutée dans son propre commit, et chacune a fait échouer le job `semgrep`, en signalant exactement l'alerte que ce commit introduisait. Ces exécutions sont liées dans les sections individuelles des vulnérabilités ci-dessous.

Le commit de la vulnérabilité 2 est la démonstration la plus claire de l'analyse différentielle. `ExternalParser.java` contenait déjà une alerte `command-injection-process-builder` à la ligne 193 (faisant partie de la base de référence de Tika), pourtant le job n'a signalé que la nouvelle alerte à la ligne 303 et a ignoré celle qui préexistait. Cela confirme que le build échoue sur les vulnérabilités introduites par le commit, et non sur les alertes qui existaient déjà.

# Documentation des vulnérabilités

Nous sommes partis de la [page de présentation des vulnérabilités](https://docs.semgrep.dev/learn/vulnerabilities/overview) de Semgrep et avons filtré le [registre de règles](https://semgrep.dev/r) de Semgrep en conséquence. Nous avons sélectionné les quatre premières vulnérabilités parmi celles applicables de la page de présentation, puis trois autres qui nous semblaient intéressantes et qui étaient liées à d'autres cours que nous avons suivis.

## Vulnérabilité 1 : [Injection de code](https://docs.semgrep.dev/learn/vulnerabilities/code-injection)

Pour la première, nous avons filtré le [registre de règles](https://semgrep.dev/r) en cherchant `java.lang.security` et en filtrant l'OWASP sur *Injection*. Nous avons choisi [`script-engine-injection`](https://semgrep.dev/r?q=java.lang.security.audit.script-engine-injection.script-engine-injection).

Pour ce qui est du module, nous avons choisi `ConfigDeserializer` parce qu'il transforme la configuration JSON de Tika en objets de paramètres. Les configurations sont externes et constituent donc un point d'entrée possible pour des données contrôlables de l'extérieur. Dans `ConfigDeserializer`, nous avons ajouté [`evaluateConfigExpression()`](https://github.com/JosephSoufsaf/tika/blob/fe749a25139bbc1cd32e50fceb73e2252aa7b796/tika-core/src/main/java/org/apache/tika/config/ConfigDeserializer.java#L123-L136), qui transmet la valeur de configuration à la méthode `eval()` de `ScriptEngine`, qui l'exécute alors comme un programme. Quiconque contrôle la configuration peut donc exécuter du code arbitraire avec les permissions du processus Tika.

Une analyse locale du fichier modifié a signalé une alerte :

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

Le commit qui ajoute cette première vulnérabilité se trouve dans l'[exécution](https://github.com/JosephSoufsaf/tika/actions/runs/43116938479) suivante.

## Vulnérabilité 2 : [Injection de commande](https://docs.semgrep.dev/learn/vulnerabilities/command-injection)

Pour la deuxième, nous avons de nouveau filtré le [registre de règles](https://semgrep.dev/r) en cherchant `java.lang.security` et en filtrant l'OWASP sur *Injection*. Nous avons choisi [`command-injection-process-builder`](https://semgrep.dev/r?q=java.lang.security.audit.command-injection-process-builder.command-injection-process-builder), qui signale une chaîne concaténée passée à un `ProcessBuilder`.

Pour ce qui est du module, nous avons choisi `ExternalParser` parce que son rôle est d'exécuter des programmes externes (comme ffmpeg ou exiftool) sur des documents, donc exécuter des commandes fait partie de son fonctionnement normal. Le fichier contient déjà une utilisation sûre de `ProcessBuilder` (ligne 193) qui passe la commande sous forme de liste d'arguments séparés, que le système d'exploitation exécute directement sans shell, de sorte qu'une valeur ne peut pas injecter de commandes supplémentaires. Semgrep la signale quand même, ce qui est un faux positif. En revanche, nous avons ajouté [`runShellCheck()`](https://github.com/JosephSoufsaf/tika/blob/656acf92e3008eeec96af9a15b033c5d8c836289/tika-core/src/main/java/org/apache/tika/parser/external/ExternalParser.java#L293-L304), qui prend la commande depuis la configuration et l'ajoute directement dans une commande shell `sh -c`. Un shell interprète des caractères comme `;`, `|` et `&&`, donc une valeur de configuration comme `ffmpeg; rm -rf /data` fait exécuter au shell à la fois `command -v ffmpeg` et `rm -rf /data`, avec les permissions du processus Tika.

Une analyse locale du fichier modifié a signalé deux alertes, celle existante à la ligne 193 et la nouvelle à la ligne 303 :

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
Le commit qui ajoute cette deuxième vulnérabilité se trouve dans l'[exécution](https://github.com/JosephSoufsaf/tika/actions/runs/43130652712) suivante. Dans le workflow, le job `semgrep` ne signale que la nouvelle alerte à la ligne 303 de `ExternalParser.java`. L'alerte à la ligne 193 fait partie de la base de référence originale de Tika, et la vulnérabilité 1 (`script-engine-injection`, ajoutée dans le commit précédent) fait partie de la base de référence de ce commit, donc aucune des deux n'est signalée de nouveau. Cela montre que le workflow n'échoue que sur les vulnérabilités introduites par le commit lui-même.

## Vulnérabilité 3 : [Désérialisation non sécurisée](https://docs.semgrep.dev/learn/vulnerabilities/insecure-deserialization)

Pour la troisième, nous avons filtré le [registre de règles](https://semgrep.dev/r) en cherchant `java.lang.security` et en filtrant l'OWASP sur *Insecure Deserialization*. Nous avons choisi [`object-deserialization`](https://semgrep.dev/r?q=java.lang.security.audit.object-deserialization.object-deserialization), qui signale la lecture d'objets avec `ObjectInputStream`.

Pour ce qui est du module, nous avons choisi `MimeTypesFactory` parce qu'il crée le registre `MimeTypes` et que `MimeTypes` est `Serializable`, comme nous le savions en ayant travaillé sur la Tâche 2. Une méthode qui charge un registre sérialisé mis en cache auparavant est une fonctionnalité réaliste à ajouter. Dans `MimeTypesFactory`, nous avons ajouté [`loadCachedMimeTypes()`](https://github.com/JosephSoufsaf/tika/blob/4a45b2da536acabdded1f35e7fb2a425e09e80a9/tika-core/src/main/java/org/apache/tika/mime/MimeTypesFactory.java#L208-L224), qui lit le registre depuis un flux avec `ObjectInputStream.readObject()`. La méthode `readObject()` reconstruit des objets Java arbitraires à partir des octets, donc si le flux n'est pas de confiance, un flux malveillant peut déclencher l'exécution de code avec les permissions du processus Tika.

Une analyse locale du fichier modifié a signalé une alerte :

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

Le commit qui ajoute cette troisième vulnérabilité se trouve dans l'[exécution](https://github.com/JosephSoufsaf/tika/actions/runs/43239360394) suivante.

## Vulnérabilité 4 : [Injection SQL](https://docs.semgrep.dev/learn/vulnerabilities/sql-injection)

Pour la quatrième, nous avons de nouveau filtré le [registre de règles](https://semgrep.dev/r) en cherchant `java.lang.security` et en filtrant l'OWASP sur *Injection*. Nous avons choisi [`formatted-sql-string`](https://semgrep.dev/r?q=java.lang.security.audit.formatted-sql-string.formatted-sql-string), qui signale une chaîne concaténée passée à une méthode d'exécution SQL.

Pour ce qui est du module, nous avons choisi `Metadata` parce qu'il contient les données clé/valeur que Tika extrait d'un document, et ces données proviennent du document lui-même, donc elles peuvent être contrôlées par celui qui rédige le document. Une méthode qui recherche une valeur de métadonnée dans une base de données est une fonctionnalité réaliste à ajouter. Dans `Metadata`, nous avons ajouté [`findMetadataValue()`](https://github.com/JosephSoufsaf/tika/blob/19d22ddff82196c908b6af3097e8019f8f52aa65/tika-core/src/main/java/org/apache/tika/metadata/Metadata.java#L803-L819), qui construit une requête SQL en concaténant la valeur de métadonnée dans la chaîne de requête au lieu d'utiliser une requête paramétrée. Un document dont les métadonnées contiennent des caractères SQL peut changer le sens de la requête et lire ou détruire des données dans la base de données.

Une analyse locale du fichier modifié a signalé deux alertes sur la même ligne, provenant de deux règles d'injection SQL liées :

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

Les deux alertes décrivent la même vulnérabilité (la chaîne SQL concaténée à la ligne 818) ; `formatted-sql-string` et `jdbc-sqli` sont deux règles qui détectent le même motif. Le commit qui ajoute cette quatrième vulnérabilité se trouve dans l'[exécution](https://github.com/JosephSoufsaf/tika/actions/runs/43247760533) suivante.

## Vulnérabilité 5 : Hachage faible (MD5)

Pour la cinquième, nous avons filtré le [registre de règles](https://semgrep.dev/r) en cherchant `java.lang.security` et en filtrant l'OWASP sur *Cryptographic Failures*. Nous avons choisi [`use-of-md5`](https://semgrep.dev/r?q=java.lang.security.audit.crypto.use-of-md5.use-of-md5), qui signale un appel à `MessageDigest.getInstance("MD5")`. Cette vulnérabilité nous intéresse aussi parce que nous avons étudié MD5 et ses faiblesses (attaques par collision) dans notre cours IFT3275 (Sécurité informatique).

Pour ce qui est du module, nous avons choisi `InputStreamDigester` parce que son rôle est de calculer une empreinte cryptographique sur le contenu d'un document. Choisir un algorithme de hachage y fait partie de son fonctionnement normal, donc sélectionner MD5 est une erreur réaliste. Dans `InputStreamDigester`, nous avons ajouté [`md5Digest()`](https://github.com/JosephSoufsaf/tika/blob/35bdea7431c946bac9c315942ba658a2bcd805b6/tika-core/src/main/java/org/apache/tika/digest/InputStreamDigester.java#L121-L135), qui calcule l'empreinte avec `MessageDigest.getInstance("MD5")`. Étant donné que MD5 n'est plus résistant aux collisions, un attaquant peut produire deux documents différents ayant la même empreinte MD5. Ainsi, une empreinte utilisée pour vérifier l'intégrité d'un document ou pour déterminer si deux documents sont identiques peut être contournée.

Une analyse locale du fichier modifié a signalé une alerte :

```
❯❱ java.lang.security.audit.crypto.use-of-md5.use-of-md5
      ❰❰ Blocking ❱❱
      Detected MD5 hash algorithm which is considered insecure. MD5 is not collision resistant and is
      therefore not suitable as a cryptographic signature. Use HMAC instead.
      Details: https://sg.run/ryJn

      ▶▶┆ Autofix ▶ "SHA-512"
      132┆ MessageDigest md5Digest = MessageDigest.getInstance("MD5");
```

Le commit qui ajoute cette cinquième vulnérabilité se trouve dans l'[exécution](https://github.com/JosephSoufsaf/tika/actions/runs/43466654662) suivante.

## Vulnérabilité 6 : Vérification du nom d'hôte désactivée (TLS)

Pour la sixième, nous avons filtré le [registre de règles](https://semgrep.dev/r) en cherchant `java.lang.security` et en filtrant l'OWASP sur *Identification and Authentication Failures*. Nous avons choisi [`insecure-hostname-verifier`](https://semgrep.dev/r?q=insecure-hostname-verifier), qui signale un `HostnameVerifier` dont la méthode `verify()` accepte n'importe quel hôte. Cette vulnérabilité nous intéresse aussi parce que nous avons étudié TLS et la vérification des certificats dans notre cours IFT3325 (Téléinformatique).

Pour ce qui est du module, nous avons choisi `NetworkParser` parce qu'il récupère des documents depuis des serveurs distants (il est déjà la source de l'alerte `unencrypted-socket` de la base de référence), donc les connexions TLS font partie de son fonctionnement normal. Contourner la vérification du nom d'hôte est un « raccourci » réaliste qu'un développeur pourrait prendre pour faire disparaître une erreur de certificat. Dans `NetworkParser`, nous avons ajouté [`AllHostsVerifier`](https://github.com/JosephSoufsaf/tika/blob/f797d07a0e5be58fcbd88390ab251c978f2d9755/tika-core/src/main/java/org/apache/tika/parser/NetworkParser.java#L172-L183), un `HostnameVerifier` dont la méthode `verify()` retourne toujours `true`. Un certificat TLS est censé prouver que le serveur contacté est réellement celui qui a été demandé. Vérifier le nom d'hôte fait partie de cette preuve. Comme ce vérificateur accepte n'importe quel hôte, cette vérification est désactivée, ce qui permet à un attaquant de type « man-in-the-middle » de présenter son propre certificat et d'usurper l'identité du serveur.

Une analyse locale du fichier modifié a signalé la nouvelle alerte (ligne 179) aux côtés de l'alerte `unencrypted-socket` préexistante (ligne 70) :

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

Dans le workflow, seule l'alerte `insecure-hostname-verifier` à la ligne 179 est signalée comme nouvelle ; l'alerte `unencrypted-socket` fait partie de la base de référence originale de Tika et est ignorée. Le commit qui ajoute cette sixième vulnérabilité se trouve dans l'[exécution](https://github.com/JosephSoufsaf/tika/actions/runs/43476113532) suivante.

## Vulnérabilité 7 : Permissions de fichier trop permissives

Pour la septième, nous avons filtré le [registre de règles](https://semgrep.dev/r) en cherchant `java.lang.security` et en filtrant l'OWASP sur *Broken Access Control*. Nous avons choisi [`overly-permissive-file-permission`](https://semgrep.dev/r?q=overly-permissive-file-permission), qui signale l'octroi des permissions de lecture, d'écriture et d'exécution à tous les utilisateurs (`rwxrwxrwx`). Cette vulnérabilité nous intéresse aussi parce que les permissions de fichier Unix et le principe du moindre privilège ont été abordés dans notre cours IFT2245 (Systèmes d'exploitation).

Pour ce qui est du module, nous avons choisi `TemporaryResources` parce que son rôle est de créer les fichiers temporaires que Tika utilise pendant l'analyse, et ces fichiers contiennent le contenu du document en cours de traitement. Définir les permissions d'un fichier temporaire y fait partie de son fonctionnement normal. Dans `TemporaryResources`, nous avons ajouté [`createSharedTempFile()`](https://github.com/JosephSoufsaf/tika/blob/818ac3695d70f5d8e4215d6a8ae5cc4819b1f6f2/tika-core/src/main/java/org/apache/tika/io/TemporaryResources.java#L209-L223), qui crée un fichier temporaire puis définit ses permissions à `rwxrwxrwx`. Les trois derniers caractères accordent à tout autre utilisateur de la machine un accès en lecture, écriture et exécution, de sorte que n'importe quel utilisateur peut lire le contenu du document stocké dans le fichier ou le modifier pendant que Tika le traite. Cela enfreint le principe du moindre privilège : un fichier temporaire ne devrait être accessible qu'au compte qui l'a créé.

Une analyse locale du fichier modifié a signalé une alerte :

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

Le commit qui ajoute cette septième vulnérabilité se trouve dans l'[exécution](https://github.com/JosephSoufsaf/tika/actions/runs/43719281541) suivante.

# Avant vs. Après

Avant les ajouts, Semgrep signalait 8 alertes sur `tika-core` ([semgrep-before.txt](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-before/semgrep-before.txt)). Après l'introduction des 7 vulnérabilités, il en signale 16 ([semgrep-after.txt](https://github.com/JosephSoufsaf/tika/blob/tache3/semgrep-after/semgrep-after.txt)) : les 8 mêmes alertes, plus 8 nouvelles.

Les 8 nouvelles alertes correspondent aux 7 vulnérabilités ; la vulnérabilité 4 (Injection SQL) est signalée par deux règles sur la même ligne (`formatted-sql-string` et `jdbc-sqli`), elle compte donc pour 2 des 8.

| # | Vulnérabilité | Nouvelle règle | Fichier | CWE | OWASP |
|---|---|---|---|---|---|
| 1 | Injection de code | `script-engine-injection` | `config/ConfigDeserializer.java` | CWE-94 | A03 – Injection |
| 2 | Injection de commande | `command-injection-process-builder` | `parser/external/ExternalParser.java` | CWE-78 | A03 – Injection |
| 3 | Désérialisation non sécurisée | `object-deserialization` | `mime/MimeTypesFactory.java` | CWE-502 | A08 – Software and Data Integrity Failures |
| 4 | Injection SQL | `formatted-sql-string` + `jdbc-sqli` | `metadata/Metadata.java` | CWE-89 | A03 – Injection |
| 5 | Hachage faible (MD5) | `use-of-md5` | `digest/InputStreamDigester.java` | CWE-328 | A02 – Cryptographic Failures |
| 6 | Vérification du nom d'hôte désactivée | `insecure-hostname-verifier` | `parser/NetworkParser.java` | CWE-295 | A07 – Identification and Authentication Failures |
| 7 | Permissions de fichier trop permissives | `overly-permissive-file-permission` | `io/TemporaryResources.java` | CWE-276 | A01 – Broken Access Control |

Les sept vulnérabilités couvrent sept CWE distincts et cinq catégories OWASP 2021. Trois d'entre elles relèvent de l'OWASP A03 (Injection), mais ce sont des types de vulnérabilités différents : la page de présentation des vulnérabilités de Semgrep elle-même répertorie l'injection de code, l'injection de commande et l'injection SQL comme des catégories distinctes, chacune ayant une cible différente (un moteur de script, le système d'exploitation et la base de données) et un CWE différent.

Semgrep attribue à chaque règle une sévérité : parmi les 8 alertes, 2 sont de sévérité ERROR (`formatted-sql-string` et `command-injection-process-builder`) et 6 sont de sévérité WARNING. Cette distinction n'affecte pas notre vérification, car l'option `--error` fait échouer le build dès qu'il y a une seule alerte, quelle que soit sa sévérité.

Les 8 alertes originales ne sont jamais signalées comme nouvelles dans aucune des analyses de commit, car elles existent à la fois dans la base de référence et dans le code actuel. Chaque vulnérabilité a été ajoutée dans son propre commit, donc à chaque push le workflow signalait exactement les alertes que ce commit introduisait. Après avoir ajouté toutes les vulnérabilités une par une à l'aide de commits, nous avons fait une pull request vers `main` pour lister ensemble toutes les vulnérabilités ajoutées dans une seule [exécution](https://github.com/JosephSoufsaf/tika/actions/runs/43985817535).

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

Claude a été utilisé pour énumérer plusieurs des vulnérabilités applicables ainsi que pour résumer les différents modules de `tika-core` dans le but de trouver les meilleurs emplacements pour les vulnérabilités. Parmi celles-ci, nous avons choisi celles qui avaient le plus de sens pour nous.

Il a aussi écrit le script [`.github/scripts/semgrep-scan.sh`](https://github.com/JosephSoufsaf/tika/blob/tache3/.github/scripts/semgrep-scan.sh) à partir de mes consignes précisant ce que je voulais.

On l'a également utilisé dans ce projet pour traduire en français certaines sections qui avaient été initialement rédigées en anglais.
