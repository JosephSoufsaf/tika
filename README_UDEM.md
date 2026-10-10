# Classe à tester

La classe choisie pour la génération des tests est MimeType.java. Nous avons sélectionné cette classe, car elle présentait un score de mutation de **44 % (35/80)**, ce qui indique qu'une proportion importante des mutants survivait aux tests existants. De plus, sa couverture de lignes était de **74 % (75/102)**, ce qui laissait une marge d'amélioration intéressante pour renforcer les tests de cette classe.

[Consulter le rapport PIT avant la génération des tests](tika-pit-reports%20-%20Before%20generated%20tests/org.apache.tika.mime/index.html)

<br>
<br>
<br>
<br>
<br>
<br>

# ChatUniTest
ChatUniTest est bel et bien installé dans le pipeline maven du module choisit(tika-core). Donc on voit bien le plug-in et son starter dans le pom.xml

xml
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

<br>
<br>
<br>
<br>
<br>
<br>


# Les tests générés

Des tests ont bien été générés à l'aide de ChatUniTest. Par défaut, ils ont été enregistrés dans le répertoire tika/tika-core/chatunitest-tests. Nous n'avons pas configuré de chemin de sortie personnalisé à l'aide du paramètre <testOutput></testOutput>.

Afin que Maven et PIT puissent les détecter et les exécuter dans le cadre des tests de mutation, les tests générés ont également été ajoutés au répertoire tika/tika-core/src/test/java/org/apache/tika/mime/.

Les tests générés par ChatUniTest compilent sans aucune correction : la compilation a réussi du premier coup. Les problèmes sont apparus à l'exécution. 15 tests échouaient parce que chatunitest-starter impose un byte-buddy 1.10.20 et un mockito-junit-jupiter 3.8.0, incompatibles avec mockito-core 5.23.0 et Java 24. Ce conflit de versions ne bloque pas la compilation, car l'API de Mockito utilisée par les tests existe bien, et il n'apparaît qu'à la création du premier mock. Une fois ces versions corrigées dans le pom, 4 méthodes de test dans 3 classes demandaient encore une correction de logique, car le modèle avait mal déduit les résultats attendus (compareTo, hasMagic et matchesMagic).

<br>
<br>
<br>
<br>
<br>
<br>


# Documentation tests

### Fiche de chaque test généré

#### MimeType_isValid_0_0_Test (8 tests, logique inchangée)

Méthode visée : static boolean isValid(String name).

Les 8 tests générés par cette méthode ont deux défauts en communs : @Mock MediaType inutile et méthode statique appelée via une instance.

| Test | Intention | Données | Oracle | Verdict |
|------|-----------|---------|--------|---------|
| ...withNullName_throwsIllegalArgumentException | isValid(null) lève une exception | null | Le code lève IllegalArgumentException | Bon (type d'exception, pas le message) |
| ...withEmptyName_returnsFalse | Un nom vide est invalide | "" | Pas de /, donc le nom ne respecte pas la grammaire : false | Bon |
| ...withNameContainingInvalidCharacters_returnsFalse | Un caractère interdit invalide le nom | "text/plain; charset=UTF-8" | ; est special : false | Moyen : donnée réaliste (en-tête Content-Type), mais le ; arrête la boucle, donc l'espace et le = qui suivent ne sont jamais testés |
| ...withNameContainingValidCharactersAndSlash_returnsTrue | Un nom normal est valide | "text/plain" | Deux tokens non vides séparés par un / : true | Bon (le plus utile : tue la plupart des mutants de isValid) |
| ...withNameStartingWithSlash_returnsFalse | Slash initial interdit | "/text/plain" | Premier token vide : false | Bon (teste la condition i == 0) |
| ...withNameEndingWithSlash_returnsFalse | Slash final interdit | "text/plain/" | Second token vide : false | **Faible** : la donnée contient **deux** slashes, donc rejetée par le drapeau slash et non par la position finale. Le nom du test promet plus que ce qu'il vérifie. |
| ...withNameContainingConsecutiveSlashes_returnsFalse | Deux slashes interdits | "text/plain//" | Un seul slash autorisé : false | Bon |
| ...withNameContainingSlashAtStartAndEnd_returnsFalse | Slashes aux deux extrémités | "/text/plain/" | false | Faible : redondant avec le test du slash initial |

**Données absentes (cause des mutants survivants de la section « Documentation mutants ») :** espace, caractère DEL (127), un seul slash final.

#### MimeType_hasMagic_17_0_Test (2 tests bruts, 3 tests après réécriture)

Méthode visée : hasMagic(), qui retourne magics != null.

| Test (version corrigée) | Intention | Données | Oracle | Verdict |
|---|---|---|---|---|
| testHasMagicWhenNoMagicAdded | Pas de magic au départ | new MimeType(OCTET_STREAM) | Le champ magics est null à la construction : false | Bon |
| testHasMagicAfterAddingMagic | addMagic active hasMagic | un Magic simulé (mock) | addMagic crée la liste : true | Bon |
| testHasMagicIgnoresNullMagic | addMagic(null) est ignoré | null | addMagic retourne sans effet si magic == null : false | Bon |

Version brute (**Faux**) : when(mimeType.getMagics()).thenReturn(...) sur un objet réel, avec @Mock MediaType en paramètre du constructeur. Le LLM a traité MimeType comme un mock et a mal compris le contrat (hasMagic ne regarde pas getMagics()).

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

### Critique qualitative des oracles

Répartition des 24 tests générés :

| Catégorie | Tests | Exemples |
|---|---|---|
| Oracle pertinent et spécifique | 12 | les 8 tests de isValid (dont 2 de portée limitée, voir section « Documentation tests ») ; 3 tests de matchesMagic ; equals |
| Oracle faux (version brute) | 4 | compareTo ; 2 tests hasMagic ; matchesMagic_ExceptionInMagic |
| Vérification triviale ou recopiée du code | 6 | tests de matches_19_0 (valeurs par défaut) |
| Oracle fragile ou tautologique | 2 | hashCode (couplé à l'implémentation), toString (assertion sur un stub) |

Constats : 
- **Forces :** les tests de isValid et de matchesMagic sont bien construits et spécifiques.
- **Faiblesses :** le LLM infère les oracles du code (getMinLength == 0) ou les imagine (compareTo == 1) au lieu de les tirer d'une spécification ; il ne cible pas les valeurs limites ; il nomme des tests d'après une méthode qu'il n'appelle pas ; il utilise mal Mockito (when sur un objet réel, réflexion inutile).
- **Comparaison avec les tests originaux :** [À COMPLÉTER : relire 2 ou 3 tests originaux, ex. MimeDetectionTest, MimeTypesReaderTest]. Les données du rapport montrent que ces tests tuent surtout les mutants des accesseurs et du chargement du registre MIME (voir section « Documentation mutants »), avec des fichiers réels : c'est un style d'intégration, plus spécifique, mais plus lent.

<br>
<br>
<br>
<br>
<br>
<br>

# Tests supplémentaires

Fichier : tika-core/src/test/java/org/apache/tika/mime/MimeType_survivors_Test.java (7 tests). Chaque test a la même documentation en Javadoc dans le code. Les 7 mutants non tués de la section « Documentation mutants » sont tous tués au run D. Le rapport attribue 18 mutants à cette classe, car il crédite le premier test tueur : certains mutants déjà tués ont été re-crédités ici.

### testIsValid_spaceInsideNameIsRejected
- **Mutant ciblé :** isValid ligne 121, ch <= ' ' devient ch < ' ' (SURVIVED). Tué : 1 mutant.
- **Intention :** un espace (code 32) est interdit dans un nom de type MIME.
- **Données :** "text/pl ain". Structure valide (un seul slash, ni en première ni en dernière position) ; le seul défaut est l'espace. Le mutant l'accepterait et retournerait true.
- **Oracle :** la Javadoc de isValid : token := 1*<US-ASCII sauf SPACE, CTLs, tspecials>. L'espace est exclu : false.

### testIsValid_delCharacterIsRejected
- **Mutant ciblé :** ligne 121, ch >= 127 devient ch > 127 (SURVIVED). Tué : 2 mutants (celui-ci et le retour forcé à true de la ligne 124).
- **Intention :** le caractère DEL (127), dernier caractère de contrôle US-ASCII, est refusé.
- **Données :** "text/pl" + (char) 127 + "ain". 127 est exactement la valeur limite ; seul le mutant la laisse passer.
- **Oracle :** la Javadoc exclut les CTLs, qui valent 0 à 31 et 127 en US-ASCII : false.

### testIsValid_singleTrailingSlashIsRejected
- **Mutant ciblé :** ligne 126, i + 1 == name.length() devient i - 1 == name.length() (SURVIVED). Tué : 3 mutants (lignes 126 et 127).
- **Intention :** un nom qui finit par un slash unique est invalide (second token vide).
- **Données :** "text/". Un seul slash, en dernière position. Les tests existants ont deux slashes et sont rejetés par le drapeau slash, pas par la position.
- **Oracle :** grammaire name := token "/" token avec token := 1*<caractère> : au moins un caractère après le slash : false.

### testHasRootXML_falseWhenNoneRegistered
- **Mutant ciblé :** ligne 268, hasRootXML retour true et condition négée (SURVIVED). Tué : 2 mutants.
- **Intention :** un type neuf n'a aucun rootXML.
- **Données :** instance neuve, sans appel à addRootXML (l'état initial distingue true de false).
- **Oracle :** le champ rootXML est null par défaut et addRootXML est le seul moyen de l'initialiser : false.

### testHasRootXML_trueAfterAddRootXML
- **Mutant ciblé :** ligne 268, condition négée (SURVIVED). Tué : 1 mutant (ligne 250, condition de création de la liste dans addRootXML).
- **Intention :** après l'ajout d'un rootXML, hasRootXML() retourne true.
- **Données :** namespace http://www.w3.org/1999/xhtml, nom local html (valeurs réalistes ; le constructeur de RootXML exige qu'au moins l'un des deux soit non vide).
- **Oracle :** « Add some rootXML info to this mime-type » : un ajout réussi implique que le type possède du rootXML : true.

### testMatches_trueWhenAMagicMatches
- **Mutant ciblé :** ligne 308, matches retour false (NO_COVERAGE). Tué : 3 mutants (lignes 298, 301 et 308).
- **Intention :** matches(data) retourne true si une signature magique reconnaît les données.
- **Données :** un Magic simulé dont eval(...) retourne true, ce qui impose le résultat sans dépendre d'un vrai fichier.
- **Oracle :** matches est vrai si et seulement si une signature correspond ; ici elle correspond : true.

### testMatches_falseWhenNoMagicMatches
- **Mutant ciblé :** ligne 308, matches retour true (NO_COVERAGE). Tué : 6 mutants (lignes 283, 298 x2, 300, 304 et 308).
- **Intention :** matches(data) retourne false si aucune signature ne correspond.
- **Données :** un Magic simulé dont eval(...) retourne false.
- **Oracle :** même contrat ; aucune signature ne correspond : false.

**Vérification :** run D, MimeType : 80 mutants, 80 tués.
