# Classe à tester

La classe choisie pour la génération des tests est `MimeType` (`org.apache.tika.mime.MimeType`, module `tika-core`). Nous avons sélectionné cette classe, car elle présentait un score de mutation de 44 % (35/80), ce qui indique qu'une proportion importante des mutants n'était pas détectée par les tests existants. De plus, sa couverture de lignes était de 74 % (75/102), ce qui laissait une marge d'amélioration intéressante pour renforcer les tests de cette classe. Les chiffres détaillés se trouvent dans la section [Analyse de mutation (PIT)](#analyse-de-mutation-pit).

Ces résultats s'expliquent par le fait que `MimeType` ne possède **aucune classe de test dédiée** : elle n'est testée qu'indirectement, à travers `MimeTypesReaderTest`, `MimeDetectionTest`, `PatternsTest`, `CustomReaderTest` et `ProbabilisticMimeDetectionTest`, qui manipulent des objets `MimeType` en testant la détection de types.

### Méthodes non couvertes ou avec des mutants vivants

D'après le rapport PIT des tests originaux, **45 des 80 mutants** ne sont pas détectés : 41 ne sont jamais exécutés (NO_COVERAGE) et 4 sont exécutés sans qu'aucune assertion ne les détecte (SURVIVED).

| Méthode | Mutants | État avec les tests originaux | Cause |
|---|---|---|---|
| `isValid` | 30 | 30 NO_COVERAGE | Méthode jamais appelée, ni par les tests ni par le reste du paquetage |
| `matchesMagic` | 7 | 7 NO_COVERAGE | Jamais appelée : la détection évalue directement les objets `Magic` |
| `matches` | 2 | 2 NO_COVERAGE | Jamais appelée (simple délégation à `matchesMagic`) |
| `equals` | 4 | 1 NO_COVERAGE | Le cas « objet qui n'est pas un `MimeType` » n'est jamais testé |
| `hashCode` | 1 | 1 NO_COVERAGE | Jamais appelée |
| `hasRootXML` | 2 | 2 SURVIVED | Exécutée par `MimeTypes.add` et `init`, mais aucun test ne vérifie son résultat |
| `hasMagic` | 2 | 1 SURVIVED | Seul le cas `true` est exercé ; le cas `false` n'est jamais vérifié |
| `compareTo` | 1 | 1 SURVIVED | Exécutée lors du tri dans `MimeTypes.init`, mais le résultat n'est jamais vérifié |

Enfin, `MimeType` se prête bien à la génération automatique de tests : c'est une classe de logique pure (validation de noms, comparaisons, accès aux signatures), sans entrées-sorties. Ses méthodes peuvent donc être testées isolément, sans fichiers de test, ce qui convient à un modèle de langage local de petite taille.


# ChatUniTest
ChatUniTest est bel et bien installé dans le pipeline maven du module choisit(tika-core). Donc on voit bien le plug-in et son starter dans le pom.xml

```
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.30.0</version>
  <dependencies>
    <dependency>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-junit5-plugin</artifactId>
      <version>1.2.3</version>
    </dependency>
  </dependencies>
  <configuration>
    <targetClasses>
      <param>org.apache.tika.mime.MimeType</param>
    </targetClasses>
  </configuration>
</plugin>
```
<br><br>


# Les tests générés

Des tests ont bien été générés à l'aide de ChatUniTest. Par défaut, ils ont été enregistrés dans le répertoire tika/tika-core/chatunitest-tests. Nous n'avons pas configuré de chemin de sortie personnalisé à l'aide du paramètre <testOutput></testOutput>.

Afin que Maven et PIT puissent les détecter et les exécuter dans le cadre des tests de mutation, les tests générés ont également été ajoutés au répertoire tika/tika-core/src/test/java/org/apache/tika/mime/.

Les tests générés par ChatUniTest compilent sans aucune correction : la compilation a réussi du premier coup. Les problèmes sont apparus à l'exécution. 15 tests échouaient parce que chatunitest-starter impose un byte-buddy 1.10.20 et un mockito-junit-jupiter 3.8.0, incompatibles avec mockito-core 5.23.0 et Java 24. Ce conflit de versions ne bloque pas la compilation, car l'API de Mockito utilisée par les tests existe bien, et il n'apparaît qu'à la création du premier mock. Une fois ces versions corrigées dans le pom, 4 méthodes de test dans 3 classes demandaient encore une correction de logique, car le modèle avait mal déduit les résultats attendus (compareTo, hasMagic et matchesMagic).

<br><br>


# Documentation tests

### Fiche de chaque test généré

#### MimeType_isValid_0_0_Test (8 tests, logique inchangée)

Méthode visée : static boolean isValid(String name).

Les 8 tests générés par cette méthode ont deux défauts en communs : @Mock MediaType inutile et méthode statique appelée via une instance.

| Test | Intention | Données | Oracle | Verdict |
|------|-----------|---------|--------|---------|
| testIsValid_withNullName_throwsIllegalArgumentException | isValid(null) lève une exception | null | Le code lève IllegalArgumentException | Bon (type d'exception, pas le message) |
| testIsValid_withEmptyName_returnsFalse | Un nom vide est invalide | "" | Pas de /, donc le nom ne respecte pas la grammaire : false | Bon |
| testIsValid_withNameContainingInvalidCharacters_returnsFalse | Un caractère interdit invalide le nom | "text/plain; charset=UTF-8" | ; est special : false | Moyen : donnée réaliste (en-tête Content-Type), mais le ; arrête la boucle, donc l'espace et le = qui suivent ne sont jamais testés |
| testIsValid_withNameContainingValidCharactersAndSlash_returnsTrue | Un nom normal est valide | "text/plain" | Deux tokens non vides séparés par un / : true | Bon (le plus utile : tue la plupart des mutants de isValid) |
| testIsValid_withNameStartingWithSlash_returnsFalse | Slash initial interdit | "/text/plain" | Premier token vide : false | Bon (teste la condition i == 0) |
| testIsValid_withNameEndingWithSlash_returnsFalse | Slash final interdit | "text/plain/" | Second token vide : false | **Faible** : la donnée contient **deux** slashes, donc rejetée par le drapeau slash et non par la position finale. Le nom du test promet plus que ce qu'il vérifie. |
| testIsValid_withNameContainingConsecutiveSlashes_returnsFalse | Deux slashes interdits | "text/plain//" | Un seul slash autorisé : false | Bon |
| testIsValid_withNameContainingSlashAtStartAndEnd_returnsFalse | Slashes aux deux extrémités | "/text/plain/" | false | Faible : redondant avec le test du slash initial |


#### MimeType_hasMagic_17_0_Test (2 tests bruts, 3 tests après réécriture)

Méthode visée : hasMagic(), qui retourne magics != null.

| Test (version corrigée) | Intention | Données | Oracle | Verdict |
|---|---|---|---|---|
| testHasMagicWhenNoMagicAdded | Pas de magic au départ | new MimeType(OCTET_STREAM) | Le champ magics est null à la construction : false | Bon |
| testHasMagicAfterAddingMagic | addMagic active hasMagic | un Magic simulé (mock) | addMagic crée la liste : true | Bon |
| testHasMagicIgnoresNullMagic | addMagic(null) est ignoré | null | addMagic retourne sans effet si magic == null : false | Bon |


#### MimeType_matchesMagic_18_1_Test (4 tests)

Méthode visée : matchesMagic(byte[] data). Mise en place : deux Magic simulés injectés par réflexion dans le champ privé magics, avec des printStackTrace qui avalent les erreurs. Défaut : addMagic est accessible depuis le même package, la réflexion était inutile.

| Test | Intention | Données | Oracle | Verdict |
|---|---|---|---|---|
| testMatchesMagic_NoMagics | Sans magic, aucune correspondance | magics = null | La boucle est protégée par magics != null : false | Bon |
| testMatchesMagic_MagicDoesNotMatch | Aucune signature ne correspond | deux mocks renvoyant false | Aucun eval vrai : false | Bon |
| testMatchesMagic_MagicMatches | Une signature correspond | mock 1 false, mock 2 true | Parcours jusqu'au 2e élément : true | Bon (vérifie que la boucle ne s'arrête pas trop tôt) |
| testMatchesMagic_ExceptionInMagic | Comportement si eval lève une exception | mock 1 lève RuntimeException | Brut : true (**Faux**). Le code n'a pas de try/catch : l'exception se propage. Corrigé : assertThrows. | Bon après correction |

Les données (new byte[0]) n'ont pas d'importance ici : le résultat est dicté par les mocks, donc le test vérifie la logique de parcours, pas la détection réelle.

#### MimeType_matches_19_0_Test (6 tests : aucun n'appelle matches())

ChatUniTest a nommé la classe d'après matches, mais le LLM a testé d'autres méthodes. Tous les tests créent new MediaType("application", "octet-stream") et un MimeType.

| Test | Ce qu'il vérifie vraiment | Oracle | Verdict |
|---|---|---|---|
| testConstructor | getType() retourne le type passé au constructeur | Le constructeur stocke le type | Faible (trivial) |
| testGetMinLength | getMinLength() == 0 | Le champ vaut 0 (final int minLength = 0) | Faible : oracle recopié du code |
| testSetInterpreted | setInterpreted(true) puis isInterpreted() vrai | Un setter suivi de son getter | Moyen (tue le mutant « return false » d'isInterpreted) |
| testGetLinks | La liste des liens est vide au départ | Javadoc : « will never be null » | Moyen |
| testGetAcronym | L'acronyme vaut "" au départ | Valeur initiale du code | Faible |
| testGetUniformTypeIdentifier | L'UTI vaut "" au départ | Valeur initiale du code | Faible |

Conséquence : avant les tests manuels, matches() n'était couverte par aucun test (mutants NO_COVERAGE, voir section « Documentation mutants »).

#### Autres classes

| Test | Intention | Données | Oracle | Verdict |
|---|---|---|---|---|
| MimeType_compareTo_22_0_Test.testCompareTo | Relation d'ordre entre deux types | application/octet-stream et text/plain | Contrat Comparable : 0 pour soi-même, signe selon l'ordre lexicographique (application < text) | Brut **Faux** (1 et -1 exacts, signes inversés), corrigé en signes |
| MimeType_equals_23_0_Test.testEqualsObject | Contrat d'equals | image/jpeg identique, text/plain, null, new Object() | Égalité sur le type seul ; null et autre classe : false | Bon pour la couverture des branches ; Faible en diagnostic (4 assertions dans un test) ; ne vérifie ni symétrie ni lien avec hashCode |
| MimeType_hashCode_24_0_Test.testHashCode | hashCode() cohérent avec le type | application/octet-stream | Attendu : "application/octet-stream".hashCode() | **Faible** : l'oracle suppose un détail d'implémentation de MediaType. Le contrat serait « objets égaux, même hashCode ». |
| MimeType_toString_25_0_Test.testToString | toString() retourne le nom du type | MediaType simulé, toString() stubbé à "application/pdf" | Délégation à type.toString() | **Faible (tautologique)** : le test affirme ce qu'il a lui-même stubbé |

---

# Comparaison qualitative des oracles : IA vs écrits à la main

**Projet :** *tika-core*, classe *org.apache.tika.mime.MimeType*

**Corpus comparés**

- **IA** : 8 classes de test générées par ChatUniTest (*chatunitest-tests/*), versions brutes.
- **Manuel** : *MimeType_survivors_Test* (7 tests écrits pour tuer des mutants PIT survivants).
- **Contexte** : les tests Tika amont (*MimeTypesReaderTest*, *MimeDetectionTest*, etc.) servent de référence mais ne sont pas comparés ici.

**Méthode :** lecture du code des tests, du code source de *MimeType* et du rapport PIT. Les tests n'ont pas été exécutés ; les verdicts sur les tests IA viennent de la lecture.

---

## 1. Oracles générés par l'IA

| Test | Ce qu'il vérifie | Verdict |
|---|---|---|
| *isValid* (8 tests) | Chaîne vide, paramètres *; charset*, slash au début, à la fin ou doublé, *text/plain* valide | Pertinent et spécifique à la grammaire, mais entrées à défauts multiples et un seul cas valide |
| *equals* | Même type, type différent, *null*, autre classe | Pertinent (contrat d'égalité) |
| *compareTo* | *assertEquals(1, …)* et *assertEquals(-1, …)* | **Faux** : *"application/octet-stream".compareTo("text/plain")* vaut -19 (*'a' - 't'*), ni la valeur ni le signe ne sont bons |
| *hashCode* | Égal à *"application/octet-stream".hashCode()* | Recopie l'implémentation (*MediaType.hashCode* fait exactement cela) |
| *toString* | Stub un mock pour renvoyer *"application/pdf"*, puis l'assert | Tautologique : teste la configuration du mock |
| *hasMagic* | *when(mimeType.getMagics())* sur un objet réel | Inexécutable (Mockito exige un mock) ; sémantique supposée contraire au code |
| *matchesMagic* | Mocks, réflexion sur un champ privé, « exception ⇒ *true* » | Comportement inventé : le code propage l'exception |
| *matches_19_0* | *getMinLength()==0*, *getAcronym()==""*, *getLinks()* vide… | Trivial et mal ciblé : n'appelle jamais *matches()* |

**Corrections humaines dans *src/test*** : seuls *compareTo* (*<0* / *>0*), *hasMagic* (API réelle *addMagic*) et *matchesMagic* (*assertThrows*) ont été réparés. Les autres, y compris *toString* et *matches_19_0*, sont restés tels quels.

---

## 2. Oracles écrits à la main (*MimeType_survivors_Test*)

| Test | Mutant visé | Oracle |
|---|---|---|
| *isValid("text/pl ain")* | *<=* devient *<* (ligne 121) | Javadoc : l'espace est exclu des tokens |
| *isValid("text/pl" + (char) 127 + "ain")* | *>=* devient *>* (ligne 121) | Javadoc : DEL (127) est un caractère de contrôle |
| *isValid("text/")* | *i + 1* devient *i - 1* (ligne 126) | Grammaire *token "/" token* : il faut au moins un caractère après le slash |
| *hasRootXML()* sur objet neuf | Retour *true* / condition niée | *rootXML* est *null* par défaut, donc *false* |
| *hasRootXML()* après *addRootXML* | Condition niée | Un ajout réussi implique *true* |
| *matches(data)* avec un magic qui retourne *true* | Retour *false* (non couvert) | *matches* est vrai ssi une signature correspond |
| *matches(data)* avec un magic qui retourne *false* | Retour *true* (non couvert) | Aucune signature ne correspond, donc *false* |

<br>

## 3. Comparaison point par point

| Critère | IA (ChatUniTest) | Manuel (*survivors*) |
|---|---|---|
| Source de l'oracle | Signature, nom de méthode ou code : il recopie (*hashCode*, toString) ou devine (*compareTo*, exception) | Javadoc, citée dans chaque test |
| Qualité des oracles | Sur 24 tests : environ 12 pertinents, 4 faux ou inexécutables, 8 triviaux ou tautologiques | 7 sur 7 pertinents |
| Spécificité des données | Entrées à défauts multiples (*"text/plain//"*, *"/text/plain/"*) | Une entrée par règle |
| Valeurs limites | Cas évidents seulement | Limites exactes (32, 127, dernier caractère) |
| Traçabilité | Aucune | Mutant visé, intention, données, justification |
| Étendue | Large (8 méthodes) | Étroite (*isValid*, *hasRootXML*, *matches*) |
| Robustesse | Réflexion sur champ privé, stubs inutiles | Un mock simple par test |

### Cas commun : *isValid*

L'IA écrit 8 tests raisonnables, mais 6 mutants survivent. Les 3 tests manuels visent exactement ces mutants (espace seul, DEL à 127, slash final unique). Les tests IA rejetaient *"text/plain/"* et *"text/plain//"* grâce au drapeau « deux slashes » et non grâce au test de position, donc ils ne détectaient pas le mutant sur *i + 1 == length*.

<br>

## 4. Limites de la comparaison

- **Pas à armes égales** : les tests manuels ont été écrits après avoir vu les mutants survivants, alors que l'IA a travaillé sans ce retour.
- **PIT ne note qu'un tueur par mutant** : les chiffres ci-dessus ne sont pas un crédit exclusif et dépendent de l'ordre d'exécution.
- **Les tests manuels ne sont pas à l'abri de la tautologie** : pour *matches*, le résultat est imposé par le mock, donc on teste surtout la délégation.
- **Peu de tests manuels** (7) : cela ne montre pas qu'un humain couvrirait mieux seul, mais qu'un humain guidé par PIT comble ce que l'IA laisse passer.
- **Les corrections de *src/test* sont aussi du travail humain** : *compareTo*, *hasMagic* et *matchesMagic* sont, dans la pratique, des oracles réparés à la main.
- **Lecture statique** : les tests n'ont pas été exécutés, donc « faux » et « inexécutable » reposent sur l'analyse du code.

# Analyse de mutation (PIT)

PIT est intégré au pipeline Maven du module `tika-core` (`tika-core/pom.xml`), avec le plugin JUnit 5 requis par les tests de Tika :

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.30.0</version>
  <dependencies>
    <dependency>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-junit5-plugin</artifactId>
      <version>1.2.3</version>
    </dependency>
  </dependencies>
  <configuration>
    <targetClasses>
      <param>org.apache.tika.mime.MimeType</param>
      <param>org.apache.tika.mime.ProbabilisticMimeDetectionSelector</param>
    </targetClasses>
  </configuration>
</plugin>
```

**Commande :**
```
mvn -pl tika-core org.pitest:pitest-maven:mutationCoverage
```

**Remarque sur `ProbabilisticMimeDetectionSelector` :** cette classe figurait dans la configuration initiale de PIT, lorsque nous comparions des candidats. Nous ne l'avons finalement pas retenue : aucun test n'a été généré ni écrit pour elle. Elle apparaît dans les trois rapports PIT, mais ses résultats restent identiques (63/122 mutants tués) et ne sont pas discutés. **Tous les scores ci-dessous concernent uniquement `MimeType`**, et non les totaux du paquetage.

**Protocole :** la configuration de PIT est restée la même pour les trois exécutions. Seul le contenu de `src/test/java/org/apache/tika/mime/` a changé :

1. **Tests originaux** : PIT est exécuté avant l'ajout de tout nouveau test.
2. **+ tests générés** : les tests de ChatUniTest (corrigés) sont copiés depuis `chatunitest-tests/`, puis PIT est relancé.
3. **+ tests manuels** : `MimeType_survivors_Test` est ajouté, puis PIT est relancé.

### Résultats pour `MimeType`

| Exécution | Couverture de lignes | Score de mutation | Force des tests |
|---|---|---|---|
| Tests originaux | 74 % (75/102) | **44 % (35/80)** | 90 % (35/39) |
| + tests générés (ChatUniTest) | 94 % (96/102) | **91 % (73/80)** | 94 % (73/78) |
| + tests manuels | 95 % (97/102) | **100 % (80/80)** | 100 % (80/80) |

*La force des tests (test strength) ne compte que les mutants couverts par au moins un test. L'écart entre score de mutation et force des tests correspond aux mutants jamais exécutés (NO_COVERAGE) : 41 avant l'ajout de tests, 2 après les tests générés, 0 à la fin.*

Rapports : [avant](pit-reports-pre-additions/index.html) · [après tests générés](pit-reports-post-AI-additions/index.html) · [après tests manuels](pit-reports-post-manual-additions/index.html)

# Documentation des mutants détectés par les tests générés

En comparant les rapports PIT avant et après l'ajout des tests générés (page `MimeType.java`), **38 mutants passent à l'état KILLED** : 35 → 73 mutants tués sur 80. Pour chaque mutant, PIT n'enregistre que le **premier** test qui le tue ; un autre test pourrait aussi le détecter.

| Méthode | Avant | Après tests générés | Nouveaux mutants tués |
|---|---|---|---|
| `isValid` | 0/30 (tous NO_COVERAGE) | 27/30 | 27 |
| `matchesMagic` | 0/7 (tous NO_COVERAGE) | 7/7 | 7 |
| `hasMagic` | 1/2 | 2/2 | 1 |
| `compareTo` | 0/1 (SURVIVED) | 1/1 | 1 |
| `equals` | 3/4 | 4/4 | 1 |
| `hashCode` | 0/1 (NO_COVERAGE) | 1/1 | 1 |
| **Total** | | | **38** |

## `isValid` : 27 mutants tués

Avant les tests générés, `isValid` n'était appelée par aucun test (ni par le reste du paquetage) : ses 30 mutants étaient NO_COVERAGE.

- **16 conditions niées de la ligne 121** (`ch == '('` devient `ch != '('`, etc.), plus la condition niée `ch == '/'` (ligne 125), les 3 conditions niées de la ligne 126 et le retour remplacé par `false` (ligne 132), soit **21 mutants**. Tous sont tués par `testIsValid_withNameContainingValidCharactersAndSlash_returnsTrue` (`"text/plain"`). C'est le seul test où le résultat attendu est `true` : nier une comparaison de caractère fait rejeter une lettre ordinaire, et le mutant retourne `false` au lieu de `true`.
- **Test `name == null` nié (ligne 114) et les 2 mutants de la boucle `i < name.length()` (ligne 119)** : tués par `testIsValid_withEmptyName_returnsFalse` (`""`). Avec le test nié, `""` (non nul) lève une exception ; avec `i <= length` ou la condition niée, la boucle lit `charAt(0)` sur une chaîne vide, ce qui lève `StringIndexOutOfBoundsException`.
- **Retour `true` à la ligne 132** : tué par le même test ; `""` ne contient pas de slash et doit donner `false`.
- **Retour `true` à la ligne 124** (caractère interdit) : tué par `testIsValid_withNameContainingInvalidCharacters_returnsFalse` ; le `;` de `"text/plain; charset=UTF-8"` doit provoquer `false`.
- **Retour `true` à la ligne 127** (slash mal placé) : tué par `testIsValid_withNameStartingWithSlash_returnsFalse` (`"/text/plain"`).

**3 mutants survivent** : les deux changements de borne de la ligne 121 (`ch <= ' '` et `ch >= 127`), car aucune entrée ne contient exactement un espace ou le caractère 127 comme seul défaut, et `i + 1` → `i - 1` (ligne 126), car les entrées terminées par un slash en contiennent deux et sont rejetées par le drapeau `slash` avant le test de position (évaluation court-circuitée du `||`). Ils sont traités dans la section [Tests supplémentaires](#tests-supplémentaires-écrits-à-la-main).

## `matchesMagic` : 7 mutants tués

Cette méthode n'était appelée par aucun test existant : la détection évalue directement les objets `Magic`. Les tests générés injectent deux `Magic` simulés.

- **Condition `magics != null` niée (ligne 298)** : tuée par `testMatchesMagic_NoMagics`. Avec `magics == null`, le mutant entre dans la boucle et appelle `magics.size()` : `NullPointerException`.
- **Condition `i < magics.size()` niée et incrément `i++` → `i--` (ligne 298)** : tués par `testMatchesMagic_MagicMatches`. Le premier mutant sort de la boucle sans rien évaluer et retourne `false` au lieu de `true` ; le second passe à l'indice -1 après le premier `Magic` : `IndexOutOfBoundsException`.
- **Borne `<` → `<=` (ligne 298)** : tuée par `testMatchesMagic_MagicDoesNotMatch`. Aucun `Magic` ne correspond, la boucle va jusqu'à l'indice 2 d'une liste de taille 2 : `IndexOutOfBoundsException`.
- **Retour `true` → `false` (ligne 301)** : tué par `testMatchesMagic_MagicMatches`, qui attend `true` quand le second `Magic` correspond.
- **Retour `false` → `true` (ligne 304)** : tué par `testMatchesMagic_NoMagics`, qui attend `false` sans aucun `Magic`.
- **Condition `magic.eval(data)` niée (ligne 300)** : attribuée à `testMatchesMagic_MagicMatches`, mais **pas grâce à son assertion**. Avec ce mutant, la méthode retourne `true` dès le premier `Magic` (qui retourne `false`), donc l'assertion `assertTrue` passe quand même. Le test échoue parce que le stub de `magic2` n'est jamais utilisé, ce que le mode strict de `MockitoExtension` signale comme une erreur. Le mutant serait aussi tué légitimement par `testMatchesMagic_MagicDoesNotMatch`, qui attend `false`.

## Autres méthodes : 4 mutants tués

- **`hasMagic`, retour remplacé par `true`** (SURVIVED avant) : tué par `testHasMagicIgnoresNullMagic`. Les tests originaux n'utilisaient que des types possédant des signatures ; ce test vérifie le cas `false` après `addMagic(null)`.
- **`compareTo`, retour remplacé par `0`** (SURVIVED avant) : tué par `testCompareTo`, qui exige `< 0` et `> 0` pour deux types différents.
- **`equals`, retour `false` → `true` pour un objet qui n'est pas un `MimeType`** (NO_COVERAGE avant) : tué par `testEqualsObject`, qui vérifie `equals(null)` et `equals(new Object())`.
- **`hashCode`, retour remplacé par `0`** (NO_COVERAGE avant) : tué par `testHashCode`. Même si son oracle recopie l'implémentation (voir la critique des tests), il suffit à détecter ce mutant.

## Tests générés sans effet sur le score

Les 6 tests de `MimeType_matches_19_0_Test` et `MimeType_toString_25_0_Test` ne tuent **aucun nouveau mutant** : les mutants de `getType`, `isInterpreted`, `getLinks`, `getAcronym`, `getUniformTypeIdentifier` et `toString` étaient déjà tués par les tests originaux (`MimeTypesReaderTest`, `MimeDetectionTest`), et `getMinLength` ne produit aucun mutant. Cela confirme les verdicts « trivial » et « tautologique » de la critique.

## Mutants restants

Après les tests générés, 7 mutants ne sont toujours pas détectés : les 3 de `isValid` décrits plus haut, les 2 de `hasRootXML` (SURVIVED : son résultat n'est vérifié directement par aucun test) et les 2 de `matches` (NO_COVERAGE : aucun test généré n'appelle `matches()`). Ils sont tués par les tests manuels décrits dans la section [Tests supplémentaires](#tests-supplémentaires-écrits-à-la-main).

# Tests supplémentaires (écrits à la main)

Après l'ajout des tests générés, 7 mutants de `MimeType` n'étaient toujours pas détectés : 3 dans `isValid` et 2 dans `hasRootXML` (SURVIVED), ainsi que 2 dans `matches` (NO_COVERAGE). Nous avons écrit un test par mutant, dans `tika-core/src/test/java/org/apache/tika/mime/MimeType_survivors_Test.java`. Chaque test utilise une entrée dont le **seul** défaut est celui que le mutant ignore : ainsi, seul le mutant change le résultat. Les oracles proviennent de la Javadoc, pas du code.

### 1. `testIsValid_spaceInsideNameIsRejected`
- **Mutant visé :** `isValid`, ligne 121 : `ch <= ' '` devient `ch < ' '` (SURVIVED).
- **Intention :** un espace est interdit dans un nom de type MIME, y compris dans le sous-type.
- **Données :** `"text/pl ain"`. La structure est valide (un seul slash, ni au début ni à la fin) ; le seul défaut est l'espace (code 32), qui est exactement la valeur limite de la condition. Le mutant accepterait l'espace et retournerait `true`.
- **Oracle :** la Javadoc définit `token := 1*<any (US-ASCII) CHAR except SPACE, CTLs, or tspecials>`. L'espace est exclu, donc `false`.

### 2. `testIsValid_delCharacterIsRejected`
- **Mutant visé :** `isValid`, ligne 121 : `ch >= 127` devient `ch > 127` (SURVIVED).
- **Intention :** le caractère DEL (127), dernier caractère de contrôle US-ASCII, est refusé.
- **Données :** `"text/pl" + (char) 127 + "ain"`. 127 est la valeur limite : `127 > 127` est faux, donc seul le mutant laisse passer ce caractère.
- **Oracle :** la Javadoc exclut les CTLs (caractères de contrôle), qui sont en US-ASCII les codes 0 à 31 et 127. Donc `false`.

### 3. `testIsValid_singleTrailingSlashIsRejected`
- **Mutant visé :** `isValid`, ligne 126 : `i + 1 == name.length()` devient `i - 1 == name.length()` (SURVIVED).
- **Intention :** un nom qui se termine par un slash unique est invalide (second token vide).
- **Données :** `"text/"`, un seul slash, en dernière position. Les tests générés (`"text/plain/"`, `"text/plain//"`) contiennent deux slashes : ils sont rejetés par le drapeau `slash` avant que la position ne soit vérifiée (évaluation court-circuitée du `||`), d'où la survie du mutant.
- **Oracle :** la grammaire `name := token "/" token`, avec un token d'au moins un caractère, impose au moins un caractère après le slash. Donc `false`.

### 4. `testHasRootXML_falseWhenNoneRegistered`
- **Mutants visés :** `hasRootXML`, ligne 268 : retour remplacé par `true`, et condition niée (SURVIVED).
- **Intention :** un type fraîchement créé n'a aucune description rootXML.
- **Données :** une instance neuve, sans appel à `addRootXML`. L'état initial est le cas qui distingue `true` de `false`.
- **Oracle :** le champ `rootXML` vaut `null` par défaut, et `addRootXML` est le seul moyen de l'initialiser. Donc `false`.

### 5. `testHasRootXML_trueAfterAddRootXML`
- **Mutant visé :** `hasRootXML`, ligne 268 : condition niée (SURVIVED).
- **Intention :** après un ajout, `hasRootXML()` retourne `true`.
- **Données :** l'espace de noms XHTML et le nom local `"html"`, des valeurs réalistes. Le constructeur de `RootXML` exige qu'au moins l'un des deux soit non vide.
- **Oracle :** selon la Javadoc de `addRootXML` (« Add some rootXML info to this mime-type »), un ajout réussi implique que le type possède du rootXML. Donc `true`.

### 6. `testMatches_trueWhenAMagicMatches`
- **Mutant visé :** `matches`, ligne 308 : retour remplacé par `false` (NO_COVERAGE).
- **Intention :** `matches(data)` retourne `true` quand une signature magique reconnaît les données.
- **Données :** un `Magic` simulé (Mockito) dont `eval(...)` retourne `true`, ajouté avec `addMagic`. Le résultat est ainsi contrôlé par le test, sans dépendre d'un vrai fichier.
- **Oracle :** `matches` est vrai si et seulement si une signature correspond. Ici, la signature simulée correspond, donc `true`.

### 7. `testMatches_falseWhenNoMagicMatches`
- **Mutant visé :** `matches`, ligne 308 : retour remplacé par `true` (NO_COVERAGE).
- **Intention :** `matches(data)` retourne `false` quand aucune signature ne correspond.
- **Données :** un `Magic` simulé dont `eval(...)` retourne `false`.
- **Oracle :** même contrat ; aucune signature ne correspond, donc `false`.

**Résultat :** avec ces 7 tests, le score de mutation de `MimeType` passe de 91 % (73/80) à **100 % (80/80)**.

**Limite :** pour `matches`, le résultat est imposé par le mock ; ces deux tests vérifient surtout que `matches` délègue correctement à `matchesMagic`, et non la détection réelle par signature.

# Exécution dans GitHub Actions

Le workflow [`.github/workflows/tache2-tests.yml`](.github/workflows/tache2-tests.yml) s'exécute automatiquement à chaque push et à chaque pull request, sur toutes les branches qui le contiennent. Il peut aussi être lancé manuellement (« Run workflow »). Il effectue les étapes suivantes sur une machine Ubuntu :

1. installation de Java 24 (Temurin), la version utilisée localement ;
2. compilation de `tika-core` et de ses dépendances (`mvn -pl tika-core -am install -DskipTests`), qui applique aussi les vérifications de style de Tika (checkstyle) ;
3. exécution de tous les nouveaux tests, générés et manuels, grâce au motif `MimeType_*_Test` :
```
   mvn -pl tika-core test -Dtest="MimeType_*_Test" -Dsurefire.failIfNoSpecifiedTests=false
```

**Résultat :** les **32 nouveaux tests** (25 tests générés par ChatUniTest après correction, et 7 tests écrits à la main) s'exécutent avec succès : 0 échec, 0 erreur. [Voir l'exécution réussie](https://github.com/JosephSoufsaf/tika/actions).

**Corrections nécessaires pour que le build passe hors de la machine d'origine :**

- **groupId de `chatunitest-starter`** : la dépendance était déclarée avec `io.github.zju-aces-ise` (minuscules), alors que l'artefact est publié sur Maven Central sous `io.github.ZJU-ACES-ISE`. Le build fonctionnait localement sous Windows, dont le système de fichiers ignore la casse et où l'artefact était déjà en cache, mais échouait sur une machine neuve et sous Linux (GitHub Actions). Le groupId a été corrigé dans `tika-core/pom.xml`.
- **Imports `*` dans les fichiers de test** : les 8 classes générées par ChatUniTest, ainsi que `MimeType_survivors_Test`, utilisaient des imports génériques (`import static org.junit.jupiter.api.Assertions.*;`, `import org.mockito.*;`, etc.). La configuration checkstyle de Tika, qui s'applique aussi aux tests et fait échouer le build, interdit cette forme (règle `AvoidStarImport`, 33 erreurs). Les imports ont été remplacés par des imports explicites, sans modifier le code des tests.
