/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.mime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

public class MimeType_survivors_Test {

    private MimeType mimeType;

    @BeforeEach
    public void setUp() {
        mimeType = new MimeType(MediaType.OCTET_STREAM);
    }

    /**
     * Nom : testIsValid_spaceInsideNameIsRejected
     * Mutant ciblé : MimeType.isValid, ligne 121, « changed conditional
     *   boundary » (ch <= ' ' devient ch < ' ') – SURVIVED.
     * Intention : vérifier qu'un espace (code 32) est interdit dans un nom
     *   de type MIME, y compris dans la partie « sous-type ».
     * Données : "text/pl ain". Structure valide (un seul slash, jamais en
     *   première ou dernière position) ; le SEUL défaut est l'espace. Le
     *   mutant (< au lieu de <=) accepterait l'espace et retournerait true.
     *   Une chaîne avec d'autres défauts masquerait la différence.
     * Oracle : la Javadoc de isValid définit token := 1*<any US-ASCII CHAR
     *   except SPACE, CTLs, or tspecials>. L'espace est explicitement exclu,
     *   donc le résultat attendu est false, indépendamment du code.
     */
    @Test
    public void testIsValid_spaceInsideNameIsRejected() {
        assertFalse(MimeType.isValid("text/pl ain"));
    }

    /**
     * Nom : testIsValid_delCharacterIsRejected
     * Mutant ciblé : MimeType.isValid, ligne 121, « changed conditional
     *   boundary » (ch >= 127 devient ch > 127) – SURVIVED.
     * Intention : vérifier que le caractère DEL (127), dernier caractère de
     *   contrôle US-ASCII, est refusé.
     * Données : "text/pl" + (char) 127 + "ain". 127 est exactement la valeur
     *   limite : seul le mutant la laisse passer (127 > 127 est faux).
     * Oracle : la Javadoc exclut les CTLs ; en US-ASCII, les CTLs sont 0 à 31
     *   et 127. Résultat attendu : false.
     */
    @Test
    public void testIsValid_delCharacterIsRejected() {
        assertFalse(MimeType.isValid("text/pl" + (char) 127 + "ain"));
    }

    /**
     * Nom : testIsValid_singleTrailingSlashIsRejected
     * Mutant ciblé : MimeType.isValid, ligne 126, « addition remplacée par
     *   soustraction » (i + 1 == length devient i - 1 == length) – SURVIVED.
     * Intention : vérifier qu'un nom qui se termine par un slash unique est
     *   invalide (deuxième token vide).
     * Données : "text/". Un seul slash, en dernière position. Les tests
     *   existants ("text/plain/", "text/plain//") contiennent deux slashes,
     *   donc rejetés par le drapeau « slash » et non par ce test de position.
     * Oracle : la grammaire de la Javadoc, name := token "/" token, avec
     *   token := 1*<caractère>, impose au moins un caractère après le
     *   slash. Résultat attendu : false.
     */
    @Test
    public void testIsValid_singleTrailingSlashIsRejected() {
        assertFalse(MimeType.isValid("text/"));
    }

    /**
     * Nom : testHasRootXML_falseWhenNoneRegistered
     * Mutant ciblé : hasRootXML, ligne 268, « return true » et « negated
     *   conditional » – SURVIVED.
     * Intention : un type fraîchement créé n'a aucun rootXML.
     * Données : instance neuve sans appel à addRootXML (l'état initial
     *   est le cas qui distingue true de false).
     * Oracle : le champ rootXML est null par défaut et addRootXML est le
     *   seul moyen de l'initialiser (code et Javadoc de addRootXML) ;
     *   attendu : false.
     */
    @Test
    public void testHasRootXML_falseWhenNoneRegistered() {
        assertFalse(mimeType.hasRootXML());
    }

    /**
     * Nom : testHasRootXML_trueAfterAddRootXML
     * Mutant ciblé : hasRootXML, ligne 268, « negated conditional » – SURVIVED.
     * Intention : après l'ajout d'une description rootXML, hasRootXML()
     *   retourne true.
     * Données : namespace XHTML et nom local "html", valeurs réalistes
     *   (le constructeur de RootXML exige l'un des deux non vide).
     * Oracle : « Add some rootXML info to this mime-type » : un ajout
     *   réussi implique que le type possède du rootXML ; attendu : true.
     */
    @Test
    public void testHasRootXML_trueAfterAddRootXML() {
        mimeType.addRootXML("http://www.w3.org/1999/xhtml", "html");
        assertTrue(mimeType.hasRootXML());
    }

    /**
     * Nom : testMatches_trueWhenAMagicMatches
     * Mutant ciblé : matches, ligne 308, « return false » – NO_COVERAGE.
     * Intention : matches(data) retourne true quand une signature magique
     *   reconnaît les données.
     * Données : un Magic simulé dont eval(...) retourne true. Le résultat
     *   est ainsi imposé par le test, sans dépendre d'un vrai fichier.
     * Oracle : matches est le point d'entrée de la détection par magic :
     *   il est vrai si et seulement si une signature correspond ; ici la
     *   signature (simulée) correspond, donc true.
     */
    @Test
    public void testMatches_trueWhenAMagicMatches() {
        Magic magic = mock(Magic.class);
        when(magic.eval(any(byte[].class))).thenReturn(true);
        mimeType.addMagic(magic);
        assertTrue(mimeType.matches(new byte[] {1}));
    }

    /**
     * Nom : testMatches_falseWhenNoMagicMatches
     * Mutant ciblé : matches, ligne 308, « return true » – NO_COVERAGE.
     * Intention : matches(data) retourne false quand aucune signature ne
     *   correspond.
     * Données : un Magic simulé dont eval(...) retourne false.
     * Oracle : même contrat que ci-dessus ; aucune signature ne correspond,
     *   donc false.
     */
    @Test
    public void testMatches_falseWhenNoMagicMatches() {
        Magic magic = mock(Magic.class);
        when(magic.eval(any(byte[].class))).thenReturn(false);
        mimeType.addMagic(magic);
        assertFalse(mimeType.matches(new byte[] {1}));
    }
}
